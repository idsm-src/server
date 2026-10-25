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
import cz.iocb.sparql.engine.mapping.ParametrisedTermMapping;
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



/**
 * Quad mappings of one graph of the endpoint sorted into the datasets the VoID statistics count: the triples with IRI
 * and with literal objects, the triples of each property (and each datatype of its literal objects), and the type
 * triples of each group of classes assigned by the same mappings.
 */
public class Graph
{
    /**
     * Literal class of the {@code xsd:long} hashes standing for strings, so that the distinct strings are counted by
     * their hashes.
     */
    public static class HashString extends SubsetLiteralClass
    {
        /**
         * Creates the hash class of a string class.
         *
         * @param name the name of the hashed string class
         */
        public HashString(String name)
        {
            super(name + "-hash", xsdLong);
        }
    }

    /**
     * Variable bound to the objects of the type triples.
     */
    private static final Variable varO = new Variable("O");

    /**
     * Hash class of the strings of the class {@code string-others}, which are all the strings but the molfiles.
     */
    private final HashString hashString = new HashString("string");

    /**
     * Hash class of the English language-tagged strings.
     */
    private final HashString hashEnString = new HashString("tagstring");


    /**
     * Mappings of the triples with literal objects.
     */
    private final Dataset litDataset;

    /**
     * Mappings of the triples with IRI objects.
     */
    private final Dataset iriDataset;

    /**
     * Mappings of the triples with IRI objects by their property.
     */
    private final Map<Resource, Dataset> iriPredicates;

    /**
     * Mappings of the triples with literal objects by their property.
     */
    private final Map<Resource, Dataset> litPredicates;

    /**
     * Mappings of the triples with literal objects by their property and the datatype of the objects.
     */
    private final Map<Resource, Map<Resource, Dataset>> datatypePredicates;

    /**
     * Mappings of the type triples by the classes they may assign; mappings sharing a class are merged into one entry.
     */
    private final Map<Set<Resource>, Dataset> classes;


    /**
     * Creates the graph without any mapping.
     */
    public Graph()
    {
        this.iriDataset = new Dataset();
        this.litDataset = new Dataset();
        this.iriPredicates = new HashMap<>();
        this.litPredicates = new HashMap<>();
        this.datatypePredicates = new HashMap<>();
        this.classes = new HashMap<>();
    }


    /**
     * Adds a quad mapping of the graph to the datasets it belongs to; a mapping whose property is not an ontology
     * resource is skipped, and the string objects of the class {@code string-others} and the English language-tagged
     * string objects are replaced by their hashes.
     *
     * @param request the current request
     * @param map the quad mapping
     * @param datatypes the resources of the datatypes by their IRIs
     * @param excluded the units excluded from the statistics
     * @throws SQLException if a database error occurs
     * @throws UnsupportedOperationException if the mapping maps blank nodes
     */
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

        checkUnit(predicate, predicateMapping.getIri(), excluded);

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


    /**
     * Refuses a resource of an excluded unit: the mapping of the statistics excludes the units from all its resources,
     * so the properties and the datatypes of the statistics must belong to none of them (the classes are skipped).
     *
     * @param resource the resource
     * @param iri the IRI of the resource
     * @param excluded the excluded units
     * @throws IllegalStateException if the resource belongs to an excluded unit
     */
    static void checkUnit(Resource resource, Iri iri, List<Short> excluded)
    {
        if(excluded.contains(resource.unit()))
            throw new IllegalStateException(
                    iri.getValue() + " belongs to an ontology unit excluded from the statistics");
    }


    /**
     * Replaces the term mappings of ontology unit classes in a quad mapping by mappings of the whole ontology class.
     *
     * @param map the quad mapping
     * @return the generalized quad mapping, or the mapping itself when nothing changes
     * @throws IllegalArgumentException if the mapping is of an unknown kind
     */
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


    /**
     * Replaces a term mapping of an ontology unit class (or of a union of them) by a mapping of the whole ontology
     * class, so that all the resources are counted in the same columns.
     *
     * @param map the term mapping
     * @return the generalized term mapping, or the mapping itself when it is of another class
     */
    private static TermMapping generalize(TermMapping map)
    {
        if(map instanceof ConstantIriMapping m && m.getResourceClass() instanceof OntologyUnitResource c)
            return new ConstantIriMapping(m.getIri(), c.getResource(),
                    c.toGeneralClass(c.getResource(), m.getColumns(), false));

        if(map instanceof ParametrisedIriMapping m && m.getResourceClass() instanceof OntologyUnitResource c)
            return new ParametrisedIriMapping(c.getResource(),
                    c.toGeneralClass(c.getResource(), m.getColumns(), false));

        // a union of unit classes (or their complement) is stored in the columns of the whole class
        if(map instanceof ParametrisedTermMapping m
                && m.getResourceClass(null).getEffectiveClass() instanceof OntologyResource c)
            return new ParametrisedIriMapping(c, m.getColumns(null));

        return map;
    }


    /**
     * An object mapping replaced by the hash of its value, together with the condition the object table needs for it.
     *
     * @param mapping the hashed object mapping
     * @param condition the condition of the object table, empty when none is needed
     */
    private record HashedObject(TermMapping mapping, Condition condition)
    {
    }


    /**
     * Replaces the object mapping of a quad mapping by the hashes of its strings.
     *
     * @param request the current request
     * @param map the quad mapping
     * @param hashClass the hash class of the strings
     * @return the quad mapping with the hashed objects
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the mapping is of an unknown kind
     */
    private QuadMapping hashObject(Request request, QuadMapping map, HashString hashClass) throws SQLException
    {
        if(map instanceof SingleTableQuadMapping m)
        {
            HashedObject hashed = hashObject(request, m.getTable(), m.getObject(), hashClass);

            return new SingleTableQuadMapping(m.getTable(), m.getGraph(), m.getSubject(), m.getPredicate(),
                    hashed.mapping(), Conditions.and(m.getConditions(), hashed.condition()), m.isDistinct());
        }
        else if(map instanceof JoinTableQuadMapping m)
        {
            int idx = m.getObjectTableIdx();
            HashedObject hashed = hashObject(request, m.getTables().get(idx), m.getObject(), hashClass);

            List<Conditions> conditions = new ArrayList<>(m.getConditions());
            conditions.set(idx, Conditions.and(conditions.get(idx), hashed.condition()));

            return new JoinTableQuadMapping(m.getTables(), m.getJoinColumnsPairs(), m.getGraphTableIdx(), m.getGraph(),
                    m.getSubjectTableIdx(), m.getSubject(), m.getPredicateTableIdx(), m.getPredicate(), idx,
                    hashed.mapping(), conditions, m.getDistinct());
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    /**
     * Replaces a literal object mapping by the hashes of its strings: a constant is hashed at once, a column is
     * replaced by an expression hashing it.
     *
     * @param request the current request
     * @param table the table of the object mapping
     * @param map the literal object mapping
     * @param hashClass the hash class of the strings
     * @return the hashed object mapping with the condition it needs
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the mapping is not a literal mapping
     */
    private HashedObject hashObject(Request request, SourceTable table, TermMapping map, HashString hashClass)
            throws SQLException
    {
        Column col = map.getColumns(request).get(0);
        Condition condition = new Condition();

        if(map instanceof ConstantLiteralMapping)
        {
            try(ResultSet r = request.getStatement().executeQuery("select (hashtextextended(" + col + ",0)::int8)"))
            {
                r.next();
                long value = r.getLong(1);

                Literal literal = new TypedLiteral(Long.toString(value), hashClass.getTypeIri());

                return new HashedObject(new ConstantLiteralMapping(hashClass, literal), condition);
            }
        }
        else if(map instanceof ParametrisedLiteralMapping)
        {
            // a nullable column is tested for null as the column itself and not as its hash: the hash function is
            // strict, so the rows admitted by the condition have a non-null hash, and the string is hashed once per row
            // instead of once more for the test
            if(request.getConfiguration().getDatabaseSchema().isNullableColumn(table, col))
                condition.addIsNotNull(col);

            List<Column> cols = List
                    .of((Column) new ExpressionColumn("(hashtextextended(" + col + ",0)::int8)", INT8, false));
            return new HashedObject(new ParametrisedLiteralMapping(hashClass, cols), condition);
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }


    /**
     * Restricts a type quad mapping whose objects are ontology resources to the classes of the units not excluded from
     * the statistics.
     *
     * @param request the current request
     * @param map the quad mapping
     * @param excluded the units excluded from the statistics
     * @return the restricted quad mapping, or the mapping itself when it maps no ontology classes
     * @throws IllegalArgumentException if the mapping is of an unknown kind
     */
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


    /**
     * Reads from the database the classes a type quad mapping assigns; classes that are not ontology resources are
     * skipped.
     *
     * @param request the current request
     * @param map the type quad mapping
     * @return the classes, empty when there are none or they are skipped
     * @throws SQLException if a database error occurs
     */
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
            if(map.getObject() instanceof ConstantIriMapping object)
                System.err.println("skip class " + object.getIri().getValue());
            else
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


    /**
     * Returns the only resource class of a variable binding.
     *
     * @param binding the variable binding
     * @return the resource class
     * @throws IllegalArgumentException if the binding has more than one class
     */
    static ResourceClass getResourceClass(VariableBinding binding)
    {
        Set<ResourceClass> classes = binding.getClasses();

        if(classes.size() != 1)
            throw new IllegalArgumentException();

        return classes.iterator().next();
    }


    /**
     * Adds a quad mapping to the dataset of a resource.
     *
     * @param map the datasets by their resources
     * @param iri the resource
     * @param mapping the quad mapping
     */
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


    /**
     * Adds a quad mapping to the dataset of a pair of resources.
     *
     * @param map the datasets by their pairs of resources
     * @param major the first resource of the pair
     * @param minor the second resource of the pair
     * @param mapping the quad mapping
     */
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


    /**
     * Returns the mappings of the triples with IRI objects.
     *
     * @return the mappings of the triples with IRI objects
     */
    protected Dataset getIriDataset()
    {
        return iriDataset;
    }


    /**
     * Returns the mappings of the triples with literal objects.
     *
     * @return the mappings of the triples with literal objects
     */
    protected Dataset getLitDataset()
    {
        return litDataset;
    }


    /**
     * Returns the mappings of the triples with IRI objects by their property.
     *
     * @return the mappings by their property
     */
    protected Map<Resource, Dataset> getIriPredicates()
    {
        return iriPredicates;
    }


    /**
     * Returns the mappings of the triples with literal objects by their property.
     *
     * @return the mappings by their property
     */
    protected Map<Resource, Dataset> getLitPredicates()
    {
        return litPredicates;
    }


    /**
     * Returns the mappings of the triples with literal objects by their property and the datatype of the objects.
     *
     * @return the mappings by their property and datatype
     */
    protected Map<Resource, Map<Resource, Dataset>> getDatatypePredicates()
    {
        return datatypePredicates;
    }


    /**
     * Returns the mappings of the type triples by the groups of classes they may assign.
     *
     * @return the mappings by their classes
     */
    protected Map<Set<Resource>, Dataset> getClasses()
    {
        return classes;
    }
}
