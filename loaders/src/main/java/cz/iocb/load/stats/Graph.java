package cz.iocb.load.stats;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinDataTypes.xsdLongType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Condition;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.ConstantLiteralMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedIriMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedLiteralMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.classes.LangStringConstantTagClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.ResultTag;
import cz.iocb.sparql.engine.mapping.classes.SimpleLiteralClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.parser.model.expression.Literal;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.UsedVariable;
import cz.iocb.sparql.engine.translator.imcode.SqlIntercode;;



public class Graph
{
    public static class HashString extends SimpleLiteralClass
    {
        public HashString(String name)
        {
            super(name + "-hash", ResultTag.LONG, "bigint", new IRI("http://localhost/" + name));
        }
    }

    private final HashString hashString = new HashString("string");
    private final HashString hashEnString = new HashString("tagstring");


    private final Dataset litDataset;
    private final Dataset iriDataset;
    private final Map<Resource, Dataset> iriPredicates;
    private final Map<Resource, Dataset> litPredicates;
    private final Map<Resource, Map<Resource, Dataset>> datatypePredicates;
    private final Map<Set<Resource>, Dataset> classes;


    public Graph()
    {
        this.iriDataset = new Dataset();
        this.litDataset = new Dataset();
        this.iriPredicates = new HashMap<Resource, Dataset>();
        this.litPredicates = new HashMap<Resource, Dataset>();
        this.datatypePredicates = new HashMap<Resource, Map<Resource, Dataset>>();
        this.classes = new HashMap<Set<Resource>, Dataset>();
    }


    void add(Request request, QuadMapping map, Map<IRI, Resource> datatypes, List<Short> excluded) throws SQLException
    {
        if(map.getSubject() instanceof ConstantBlankNodeMapping
                || map.getSubject() instanceof ParametrisedBlankNodeMapping
                || map.getObject() instanceof ConstantBlankNodeMapping
                || map.getObject() instanceof ParametrisedBlankNodeMapping)
            throw new UnsupportedOperationException();


        ConstantIriMapping predicateMapping = (ConstantIriMapping) map.getPredicate();

        if(!(predicateMapping.getResourceClass() instanceof OntologyResource))
        {
            System.err.println("skip " + predicateMapping.getIRI().getValue());
            return;
        }

        Resource predicate = new Resource(predicateMapping.getColumns());

        if(map.getObject() instanceof ConstantLiteralMapping || map.getObject() instanceof ParametrisedLiteralMapping)
        {
            LiteralClass rc = ((LiteralClass) map.getObject().getResourceClass(null));
            Resource datatype = datatypes.get(rc.getTypeIri());

            if(datatype == null)
                throw new RuntimeException("unexpected datatype " + rc.getTypeIri().toString());

            if(rc.getGeneralClass() == xsdString && rc.getName().equals("string-others"))
                map = hashObject(request, map, hashString);
            else if(rc.getGeneralClass() == rdfLangString && rc == LangStringConstantTagClass.get("en"))
                map = hashObject(request, map, hashEnString);

            addMapping(datatypePredicates, predicate, datatype, map);
            addMapping(litPredicates, predicate, map);
            litDataset.add(map);
        }
        else if(map.getObject() instanceof ConstantIriMapping || map.getObject() instanceof ParametrisedIriMapping)
        {
            addMapping(iriPredicates, predicate, map);
            iriDataset.add(map);
        }

        if(predicateMapping.getIRI().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        {
            QuadMapping fmap = addClassFilter(request, map, excluded);
            Set<Resource> set = getClasses(request, fmap);

            if(!set.isEmpty())
            {
                Set<Resource> key = new HashSet<Resource>(set);
                Dataset dataset = new Dataset(fmap);

                Iterator<Entry<Set<Resource>, Dataset>> it = classes.entrySet().iterator();

                while(it.hasNext())
                {
                    Entry<Set<Resource>, Dataset> entry = it.next();

                    Set<Resource> intersection = new HashSet<Resource>(entry.getKey());
                    intersection.retainAll(set);

                    if(!intersection.isEmpty())
                    {
                        key.addAll(entry.getKey());
                        dataset.add(entry.getValue());
                        it.remove();
                    }
                }

                classes.put(key, dataset);
            }
        }
    }


    private QuadMapping hashObject(Request request, QuadMapping map, HashString hashClass) throws SQLException
    {
        if(map instanceof SingleTableQuadMapping m)
        {
            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), m.getSubject(), m.getPredicate(),
                    hashObject(request, m.getTable(), m.getObject(), hashClass), m.getConditions());
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraph(),
                    m.getSubjectTableIdx(), m.getSubject(), m.getPredicateTableIdx(), m.getPredicate(),
                    m.getObjectTableIdx(),
                    hashObject(request, m.getTables().get(m.getObjectTableIdx()), m.getObject(), hashClass),
                    m.getConditions());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private NodeMapping hashObject(Request request, Table table, NodeMapping map, HashString hashClass)
            throws SQLException
    {
        Column col = map.getColumns(request).get(0);

        if(map instanceof ConstantLiteralMapping)
        {
            try(ResultSet r = request.getStatement().executeQuery("select (hashtextextended(" + col + ",0)::int8)"))
            {
                r.next();
                long value = r.getLong(1);

                Literal literal = new Literal(Long.toString(value), xsdLongType, hashClass.getTypeIri());

                return new ConstantLiteralMapping(hashClass, literal);
            }
        }
        else if(map instanceof ParametrisedLiteralMapping)
        {
            boolean canBeNull = request.getConfiguration().getDatabaseSchema().isNullableColumn(table, col);

            List<Column> cols = List
                    .of((Column) new ExpressionColumn("(hashtextextended(" + col + ",0)::int8)", canBeNull));
            return new ParametrisedLiteralMapping(hashClass, cols);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private static QuadMapping addClassFilter(Request request, QuadMapping map, List<Short> excluded)
    {
        ConstantIriMapping predicateMapping = (ConstantIriMapping) map.getPredicate();

        if(!predicateMapping.getIRI().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
            return map;

        NodeMapping objectMapping = map.getObject();

        ResourceClass resClass = objectMapping.getResourceClass(request);

        if(!(resClass instanceof OntologyResource))
            return map;

        List<Column> cols = objectMapping.getColumns(request);

        Condition condition = new Condition();

        for(short s : excluded)
        {
            Column x = new ConstantColumn(s, "smallint");
            Column y = cols.get(0);

            if(!(y instanceof ConstantColumn) || x.equals(y))
                condition.addAreNotEqual(cols.get(0), new ConstantColumn(s, "smallint"));
        }

        if(map instanceof SingleTableQuadMapping m)
        {
            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), m.getSubject(), m.getPredicate(),
                    m.getObject(), Conditions.and(m.getConditions(), new Conditions(condition)));
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            List<Conditions> conditions = new ArrayList<Conditions>();

            for(int i = 0; i < m.getConditions().size(); i++)
            {
                if(i != m.getObjectTableIdx())
                    conditions.add(m.getConditions().get(i));
                else
                    conditions.add(Conditions.and(m.getConditions().get(i), new Conditions(condition)));
            }

            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraph(),
                    m.getSubjectTableIdx(), m.getSubject(), m.getPredicateTableIdx(), m.getPredicate(),
                    m.getObjectTableIdx(), m.getObject(), conditions);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private static Set<Resource> getClasses(Request request, QuadMapping map) throws SQLException
    {
        SqlIntercode ic = (new Dataset(map)).translate(request, null, "O");

        UsedVariable ovar = ic.getVariables().get("O");

        if(ovar == null)
            return Set.of();

        List<Column> o = ovar.getMapping();
        String sql = "SELECT DISTINCT " + o.get(0) + ", " + o.get(1) + " FROM (" + ic.translate(request) + ") as t";

        Set<Resource> result = new HashSet<Resource>();

        try(Connection connection = request.getConfiguration().getConnectionPool().getConnection())
        {
            try(Statement statement = connection.createStatement())
            {
                try(ResultSet rs = statement.executeQuery(sql))
                {
                    while(rs.next())
                        result.add(new Resource(rs.getShort(1), rs.getInt(2)));
                }
            }
        }

        return result;
    }


    private static void addMapping(Map<Resource, Dataset> map, Resource iri, QuadMapping mapping)
    {
        Dataset set = map.get(iri);

        if(set == null)
        {
            set = new Dataset();
            map.put(iri, set);
        }

        set.add(mapping);
    }


    private static void addMapping(Map<Resource, Map<Resource, Dataset>> map, Resource major, Resource minor,
            QuadMapping mapping)
    {
        Map<Resource, Dataset> m = map.get(major);

        if(m == null)
        {
            m = new HashMap<Resource, Dataset>();
            map.put(major, m);
        }

        addMapping(m, minor, mapping);
    }


    protected Dataset getIriDataset()
    {
        return iriDataset;
    }


    protected Dataset getLitDataset()
    {
        return litDataset;
    }


    protected Map<Resource, Dataset> getIriPredicates()
    {
        return iriPredicates;
    }


    protected Map<Resource, Dataset> getLitPredicates()
    {
        return litPredicates;
    }


    protected Map<Resource, Map<Resource, Dataset>> getDatatypePredicates()
    {
        return datatypePredicates;
    }


    protected Map<Set<Resource>, Dataset> getClasses()
    {
        return classes;
    }
}
