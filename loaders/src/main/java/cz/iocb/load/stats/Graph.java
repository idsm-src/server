package cz.iocb.load.stats;

import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT8;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdLong;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import static cz.iocb.sparql.engine.mapping.classes.CodeHelper.constant;
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
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyUnitResource;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Condition;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.ExpressionColumn;
import cz.iocb.sparql.engine.database.SourceTable;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.imcode.SqlIntercode;
import cz.iocb.sparql.engine.mapping.ConstantBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.ConstantLiteralMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedBlankNodeMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedIriMapping;
import cz.iocb.sparql.engine.mapping.ParametrisedLiteralMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.LangStringWithTagClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.SubsetLiteralClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.rdf.Literal;
import cz.iocb.sparql.engine.rdf.TypedLiteral;
import cz.iocb.sparql.engine.rdf.Variable;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.VariableBinding;



public class Graph
{
    public static class HashString extends SubsetLiteralClass
    {
        public HashString(String name)
        {
            super(name + "-hash", xsdLong);
        }
    }

    private static final Variable varO = new Variable("O");

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
        this.iriPredicates = new HashMap<>();
        this.litPredicates = new HashMap<>();
        this.datatypePredicates = new HashMap<>();
        this.classes = new HashMap<>();
    }


    void add(Request request, QuadMapping map, Map<Iri, Resource> datatypes, List<Short> excluded) throws SQLException
    {
        if(map.getSubject() instanceof ConstantBlankNodeMapping
                || map.getSubject() instanceof ParametrisedBlankNodeMapping
                || map.getObject() instanceof ConstantBlankNodeMapping
                || map.getObject() instanceof ParametrisedBlankNodeMapping)
            throw new UnsupportedOperationException();

        map = generalize(map);


        ConstantIriMapping predicateMapping = (ConstantIriMapping) map.getPredicate();

        if(!(predicateMapping.getResourceClass() instanceof OntologyResource))
        {
            System.err.println("skip " + predicateMapping.getIri().getValue());
            return;
        }

        Resource predicate = new Resource(predicateMapping.getColumns());

        if(map.getObject() instanceof ConstantLiteralMapping || map.getObject() instanceof ParametrisedLiteralMapping)
        {
            LiteralClass rc = ((LiteralClass) map.getObject().getResourceClass(null));
            Resource datatype = datatypes.get(rc.getTypeIri());

            if(datatype == null)
                throw new RuntimeException("unexpected datatype " + rc.getTypeIri().toString());

            if(rc.getResultResourceClass() == xsdString && rc.getResourceName().equals("string-others"))
                map = hashObject(request, map, hashString);
            else if(rc.getResultResourceClass() == rdfLangString && rc == LangStringWithTagClass.get("en"))
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

        if(predicateMapping.getIri().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
        {
            QuadMapping fmap = addClassFilter(request, map, excluded);
            Set<Resource> set = getClasses(request, fmap);

            if(!set.isEmpty())
            {
                Set<Resource> key = new HashSet<>(set);
                Dataset dataset = new Dataset(fmap);

                Iterator<Entry<Set<Resource>, Dataset>> it = classes.entrySet().iterator();

                while(it.hasNext())
                {
                    Entry<Set<Resource>, Dataset> entry = it.next();

                    Set<Resource> intersection = new HashSet<>(entry.getKey());
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


    private static QuadMapping generalize(QuadMapping map)
    {
        TermMapping subject = generalize(map.getSubject());
        TermMapping predicate = generalize(map.getPredicate());
        TermMapping object = generalize(map.getObject());

        if(subject == map.getSubject() && predicate == map.getPredicate() && object == map.getObject())
        {
            return map;
        }
        else if(map instanceof SingleTableQuadMapping m)
        {
            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), subject, predicate, object, m.getConditions(),
                    m.isDistinct());
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraphTableIdx(), m.getGraph(),
                    m.getSubjectTableIdx(), subject, m.getPredicateTableIdx(), predicate, m.getObjectTableIdx(), object,
                    m.getConditions(), m.getDistinct());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private static TermMapping generalize(TermMapping map)
    {
        if(map instanceof ConstantIriMapping m && m.getResourceClass() instanceof OntologyUnitResource c)
            return new ConstantIriMapping(m.getIri(), c.getResource(),
                    c.toGeneralClass(c.getResource(), m.getColumns(), false));

        if(map instanceof ParametrisedIriMapping m && m.getResourceClass() instanceof OntologyUnitResource c)
            return new ParametrisedIriMapping(c.getResource(),
                    c.toGeneralClass(c.getResource(), m.getColumns(), false));

        return map;
    }


    private QuadMapping hashObject(Request request, QuadMapping map, HashString hashClass) throws SQLException
    {
        if(map instanceof SingleTableQuadMapping m)
        {
            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), m.getSubject(), m.getPredicate(),
                    hashObject(request, m.getTable(), m.getObject(), hashClass), m.getConditions(), m.isDistinct());
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraphTableIdx(), m.getGraph(),
                    m.getSubjectTableIdx(), m.getSubject(), m.getPredicateTableIdx(), m.getPredicate(),
                    m.getObjectTableIdx(),
                    hashObject(request, m.getTables().get(m.getObjectTableIdx()), m.getObject(), hashClass),
                    m.getConditions(), m.getDistinct());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private TermMapping hashObject(Request request, SourceTable table, TermMapping map, HashString hashClass)
            throws SQLException
    {
        Column col = map.getColumns(request).get(0);

        if(map instanceof ConstantLiteralMapping)
        {
            try(ResultSet r = request.getStatement().executeQuery("select (hashtextextended(" + col + ",0)::int8)"))
            {
                r.next();
                long value = r.getLong(1);

                Literal literal = new TypedLiteral(Long.toString(value), hashClass.getTypeIri());

                return new ConstantLiteralMapping(hashClass, literal);
            }
        }
        else if(map instanceof ParametrisedLiteralMapping)
        {
            boolean canBeNull = request.getConfiguration().getDatabaseSchema().isNullableColumn(table, col);

            List<Column> cols = List
                    .of((Column) new ExpressionColumn("(hashtextextended(" + col + ",0)::int8)", INT8, canBeNull));
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

        if(!predicateMapping.getIri().getValue().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
            return map;

        TermMapping objectMapping = map.getObject();

        ResourceClass resClass = objectMapping.getResourceClass(request);

        if(!(resClass instanceof OntologyResource))
            return map;

        List<Column> cols = objectMapping.getColumns(request);

        Condition condition = new Condition();

        for(short s : excluded)
        {
            Column x = constant(s, INT2);
            Column y = cols.get(0);

            if(!(y instanceof ValueColumn) || x.equals(y))
                condition.addAreNotEqual(cols.get(0), constant(s, INT2));
        }

        if(map instanceof SingleTableQuadMapping m)
        {
            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), m.getSubject(), m.getPredicate(),
                    m.getObject(), Conditions.and(m.getConditions(), new Conditions(condition)), m.isDistinct());
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            List<Conditions> conditions = new ArrayList<>();

            for(int i = 0; i < m.getConditions().size(); i++)
            {
                if(i != m.getObjectTableIdx())
                    conditions.add(m.getConditions().get(i));
                else
                    conditions.add(Conditions.and(m.getConditions().get(i), new Conditions(condition)));
            }

            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraphTableIdx(), m.getGraph(),
                    m.getSubjectTableIdx(), m.getSubject(), m.getPredicateTableIdx(), m.getPredicate(),
                    m.getObjectTableIdx(), m.getObject(), conditions, m.getDistinct());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    private static Set<Resource> getClasses(Request request, QuadMapping map) throws SQLException
    {
        SqlIntercode ic = (new Dataset(map)).translate(request, null, varO);

        VariableBinding ovar = ic.getVariableBindings().get(varO);

        if(ovar == null)
            return Set.of();

        ResourceClass resourceClass = getResourceClass(ovar);
        List<Column> o = ovar.getMapping(resourceClass);

        if(!(resourceClass instanceof OntologyResource))
        {
            System.err.println("skip " + resourceClass.getResourceName() + " class");
            return Set.of();
        }

        String sql = "SELECT DISTINCT " + o.get(0) + ", " + o.get(1) + " FROM (" + ic.translate(request) + ") as t";

        Set<Resource> result = new HashSet<>();

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


    static ResourceClass getResourceClass(VariableBinding binding)
    {
        Set<ResourceClass> classes = binding.getClasses();

        if(classes.size() != 1)
            throw new IllegalArgumentException();

        return classes.iterator().next();
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
            m = new HashMap<>();
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
