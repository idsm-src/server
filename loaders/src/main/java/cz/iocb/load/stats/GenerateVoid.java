package cz.iocb.load.stats;

import static cz.iocb.chemweb.server.sparql.config.stats.Void.excludedUnits;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDateTime;
import static cz.iocb.sparql.engine.mapping.datatypes.BuiltinDatatypes.rdfLangStringIri;
import static java.util.stream.Collectors.joining;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.postgresql.ds.PGSimpleDataSource;
import cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration;
import cz.iocb.load.common.Updater;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.SourceTable;
import cz.iocb.sparql.engine.imcode.SqlAggregation;
import cz.iocb.sparql.engine.imcode.SqlIntercode;
import cz.iocb.sparql.engine.imcode.SqlIntercode.Restrictions;
import cz.iocb.sparql.engine.imcode.SqlJoin;
import cz.iocb.sparql.engine.imcode.SqlNoSolution;
import cz.iocb.sparql.engine.imcode.SqlTableAccess;
import cz.iocb.sparql.engine.imcode.SqlUnion;
import cz.iocb.sparql.engine.imcode.expression.SqlBuiltinCall;
import cz.iocb.sparql.engine.imcode.expression.SqlExpressionIntercode;
import cz.iocb.sparql.engine.imcode.expression.SqlVariable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.mapping.datatypes.Datatype;
import cz.iocb.sparql.engine.parser.Rdf;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.rdf.Variable;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.VariableBinding;
import cz.iocb.sparql.engine.translator.VariableBindings;



/**
 * Generator of the VoID statistics of the IDSM endpoint: translates the quad mappings of its configuration into count
 * queries, evaluates them in parallel and updates the changed statistics of the graphs, class and property partitions
 * and linksets in the tables of the schema {@code void}.
 */
public class GenerateVoid extends Updater
{
    /**
     * Graph of the statistics.
     *
     * @param id the id of the graph in the statistics tables
     * @param iri the IRI of the graph, empty for the default graph
     */
    private static record GraphIRI(Integer id, String iri)
    {
        /**
         * Creates the graph from its IRI.
         *
         * @param id the id of the graph in the statistics tables
         * @param iri the IRI of the graph, null for the default graph
         */
        public GraphIRI(Integer id, Iri iri)
        {
            this(id, iri == null ? "" : iri.getValue());
        }
    }


    /**
     * Class partition: the instances of a class in a graph.
     *
     * @param graph the id of the graph
     * @param resource the class
     */
    private static record ClassInGraph(Integer graph, Resource resource)
    {
    }


    /**
     * Property partition: the triples of a property in a graph.
     *
     * @param graph the id of the graph
     * @param resource the property
     */
    private static record PropertyInGraph(Integer graph, Resource resource)
    {
    }


    /**
     * Property partition of a class partition: the triples of a property whose subjects are instances of a class in a
     * graph.
     *
     * @param graph the id of the graph
     * @param rclass the class of the subjects
     * @param property the property
     */
    private static record ClassAndPropertyInGraph(Integer graph, Resource rclass, Resource property)
    {
    }


    /**
     * Class linkset: the triples of a property linking the instances of a class to the instances of another class, each
     * class taken in its own graph.
     *
     * @param predicateGraph the id of the graph of the triples
     * @param predicate the property
     * @param subjectGraph the id of the graph of the subject class
     * @param subjectClass the class of the subjects
     * @param objectGraph the id of the graph of the object class
     * @param objectClass the class of the objects
     */
    private static record PropertyFromClassToClass(Integer predicateGraph, Resource predicate, Integer subjectGraph,
            Resource subjectClass, Integer objectGraph, Resource objectClass)
    {
        /**
         * Creates the linkset from its property partition and the class partitions of its subjects and objects.
         *
         * @param p the property partition
         * @param s the class partition of the subjects
         * @param o the class partition of the objects
         */
        public PropertyFromClassToClass(PropertyInGraph p, ClassInGraph s, ClassInGraph o)
        {
            this(p.graph(), p.resource(), s.graph(), s.resource(), o.graph(), o.resource());
        }
    }


    /**
     * Literal linkset: the triples of a property linking the instances of a class to literals of a datatype.
     *
     * @param predicateGraph the id of the graph of the triples
     * @param predicate the property
     * @param subjectGraph the id of the graph of the subject class
     * @param subjectClass the class of the subjects
     * @param datatype the datatype of the objects
     */
    private static record PropertyFromClassToDatatype(Integer predicateGraph, Resource predicate, Integer subjectGraph,
            Resource subjectClass, Resource datatype)
    {
        /**
         * Creates the linkset from its property partition, the class partition of its subjects and the datatype.
         *
         * @param p the property partition
         * @param s the class partition of the subjects
         * @param o the datatype of the objects
         */
        public PropertyFromClassToDatatype(PropertyInGraph p, ClassInGraph s, Resource o)
        {
            this(p.graph(), p.resource(), s.graph(), s.resource(), o);
        }
    }


    /**
     * Statistics of a graph, a partition or a linkset.
     */
    private static final class Stats
    {
        /**
         * Number of the distinct classes.
         */
        long classes;

        /**
         * Number of the distinct properties.
         */
        long predicates;

        /**
         * Number of the triples.
         */
        long triples;

        /**
         * Number of the distinct subjects.
         */
        long subjects;

        /**
         * Number of the distinct IRI objects.
         */
        long iriObjects;

        /**
         * Number of the distinct literal objects.
         */
        long litObjects;

        /**
         * Creates the statistics.
         *
         * @param classes the number of the distinct classes
         * @param predicates the number of the distinct properties
         * @param triples the number of the triples
         * @param subjects the number of the distinct subjects
         * @param iriObjects the number of the distinct IRI objects
         * @param litObjects the number of the distinct literal objects
         */
        public Stats(long classes, long predicates, long triples, long subjects, long iriObjects, long litObjects)
        {
            this.classes = classes;
            this.predicates = predicates;
            this.triples = triples;
            this.subjects = subjects;
            this.iriObjects = iriObjects;
            this.litObjects = litObjects;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(classes, litObjects, predicates, iriObjects, subjects, triples);
        }

        @Override
        public boolean equals(Object obj)
        {
            if(this == obj)
                return true;

            if(obj == null)
                return false;

            if(getClass() != obj.getClass())
                return false;

            Stats other = (Stats) obj;
            return classes == other.classes && litObjects == other.litObjects && predicates == other.predicates
                    && iriObjects == other.iriObjects && subjects == other.subjects && triples == other.triples;
        }

        /**
         * Returns the number of the distinct classes.
         *
         * @return the number of the distinct classes
         */
        public final long classes()
        {
            return classes;
        }

        /**
         * Returns the number of the distinct properties.
         *
         * @return the number of the distinct properties
         */
        public final long predicates()
        {
            return predicates;
        }

        /**
         * Returns the number of the triples.
         *
         * @return the number of the triples
         */
        public final long triples()
        {
            return triples;
        }

        /**
         * Returns the number of the distinct subjects.
         *
         * @return the number of the distinct subjects
         */
        public final long subjects()
        {
            return subjects;
        }

        /**
         * Returns the number of the distinct IRI objects.
         *
         * @return the number of the distinct IRI objects
         */
        public final long iriObjects()
        {
            return iriObjects;
        }

        /**
         * Returns the number of the distinct literal objects.
         *
         * @return the number of the distinct literal objects
         */
        public final long litObjects()
        {
            return litObjects;
        }

        /**
         * Counts one more class.
         */
        void incClasses()
        {
            classes++;
        }

        /**
         * Counts one more property.
         */
        void incPredicate()
        {
            predicates++;
        }

        /**
         * Sets the number of the distinct classes.
         *
         * @param classes the number of the distinct classes
         */
        public void setClasses(long classes)
        {
            this.classes = classes;
        }
    }


    /**
     * Statistics of the graphs as stored in the table {@code void.graphs}.
     */
    @SuppressWarnings("serial")
    private static final class GraphStats extends SqlMap<GraphIRI, Stats>
    {
        /**
         * Creates an empty map.
         */
        private GraphStats()
        {
        }

        @Override
        public GraphIRI getKey(ResultSet result) throws SQLException
        {
            return new GraphIRI(result.getInt(1), result.getString(2));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(result.getLong(3), result.getLong(4), result.getLong(5), result.getLong(6),
                    result.getLong(8), result.getLong(9));
        }

        @Override
        public void set(PreparedStatement statement, GraphIRI key, Stats value) throws SQLException
        {
            statement.setInt(1, key.id());
            statement.setString(2, key.iri());
            statement.setLong(3, value.classes());
            statement.setLong(4, value.predicates());
            statement.setLong(5, value.triples());
            statement.setLong(6, value.subjects());
            statement.setLong(7, value.iriObjects() + value.litObjects());
            statement.setLong(8, value.iriObjects());
            statement.setLong(9, value.litObjects());
        }
    }


    /**
     * Statistics of the class partitions as stored in the table {@code void.class_partitions}.
     */
    @SuppressWarnings("serial")
    private static final class ClassPartitionStats extends SqlMap<ClassInGraph, Stats>
    {
        /**
         * Creates an empty map.
         */
        private ClassPartitionStats()
        {
        }

        @Override
        public ClassInGraph getKey(ResultSet result) throws SQLException
        {
            return new ClassInGraph(result.getInt(1), new Resource(result.getShort(2), result.getInt(3)));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(result.getLong(4), result.getLong(5), result.getLong(6), result.getLong(7),
                    result.getLong(9), result.getLong(10));
        }

        @Override
        public void set(PreparedStatement statement, ClassInGraph key, Stats value) throws SQLException
        {
            statement.setInt(1, key.graph());
            statement.setShort(2, key.resource().unit());
            statement.setInt(3, key.resource().id());
            statement.setLong(4, value.classes());
            statement.setLong(5, value.predicates());
            statement.setLong(6, value.triples());
            statement.setLong(7, value.subjects());
            statement.setLong(8, value.iriObjects() + value.litObjects());
            statement.setLong(9, value.iriObjects());
            statement.setLong(10, value.litObjects());
        }
    }


    /**
     * Statistics of the property partitions of the class partitions as stored in the table
     * {@code void.class_property_partitions}.
     */
    @SuppressWarnings("serial")
    private static final class ClassPropertyPartitionStats extends SqlMap<ClassAndPropertyInGraph, Stats>
    {
        /**
         * Creates an empty map.
         */
        private ClassPropertyPartitionStats()
        {
        }

        @Override
        public ClassAndPropertyInGraph getKey(ResultSet result) throws SQLException
        {
            return new ClassAndPropertyInGraph(result.getInt(1), new Resource(result.getShort(2), result.getInt(3)),
                    new Resource(result.getShort(4), result.getInt(5)));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(0l, 0l, result.getLong(6), result.getLong(7), result.getLong(9), result.getLong(10));
        }

        @Override
        public void set(PreparedStatement statement, ClassAndPropertyInGraph key, Stats value) throws SQLException
        {
            statement.setInt(1, key.graph());
            statement.setShort(2, key.rclass().unit());
            statement.setInt(3, key.rclass().id());
            statement.setShort(4, key.property().unit());
            statement.setInt(5, key.property().id());
            statement.setLong(6, value.triples());
            statement.setLong(7, value.subjects());
            statement.setLong(8, value.iriObjects() + value.litObjects());
            statement.setLong(9, value.iriObjects());
            statement.setLong(10, value.litObjects());
        }
    }


    /**
     * Statistics of the property partitions as stored in the table {@code void.property_partitions}.
     */
    @SuppressWarnings("serial")
    private static final class PropertyPartitionStats extends SqlMap<PropertyInGraph, Stats>
    {
        /**
         * Creates an empty map.
         */
        private PropertyPartitionStats()
        {
        }

        @Override
        public PropertyInGraph getKey(ResultSet result) throws SQLException
        {
            return new PropertyInGraph(result.getInt(1), new Resource(result.getShort(2), result.getInt(3)));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(0l, 0l, result.getLong(4), result.getLong(5), result.getLong(7), result.getLong(8));
        }

        @Override
        public void set(PreparedStatement statement, PropertyInGraph key, Stats value) throws SQLException
        {
            statement.setInt(1, key.graph());
            statement.setShort(2, key.resource().unit());
            statement.setInt(3, key.resource().id());
            statement.setLong(4, value.triples());
            statement.setLong(5, value.subjects());
            statement.setLong(6, value.iriObjects() + value.litObjects());
            statement.setLong(7, value.iriObjects());
            statement.setLong(8, value.litObjects());
        }
    }


    /**
     * Statistics of the class linksets as stored in the table {@code void.linksets}.
     */
    @SuppressWarnings("serial")
    private static final class ClassLinksetStats extends SqlMap<PropertyFromClassToClass, Stats>
    {
        /**
         * Creates an empty map.
         */
        private ClassLinksetStats()
        {
        }

        @Override
        public PropertyFromClassToClass getKey(ResultSet result) throws SQLException
        {
            return new PropertyFromClassToClass(result.getInt(1), new Resource(result.getShort(2), result.getInt(3)),
                    result.getInt(4), new Resource(result.getShort(5), result.getInt(6)), result.getInt(7),
                    new Resource(result.getShort(8), result.getInt(9)));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(0l, 0l, result.getLong(10), result.getLong(11), result.getLong(12), 0l);
        }

        @Override
        public void set(PreparedStatement statement, PropertyFromClassToClass key, Stats value) throws SQLException
        {
            statement.setInt(1, key.predicateGraph());
            statement.setShort(2, key.predicate().unit());
            statement.setInt(3, key.predicate().id());
            statement.setInt(4, key.subjectGraph());
            statement.setShort(5, key.subjectClass().unit());
            statement.setInt(6, key.subjectClass().id());
            statement.setInt(7, key.objectGraph());
            statement.setShort(8, key.objectClass().unit());
            statement.setInt(9, key.objectClass().id());
            statement.setLong(10, value.triples());
            statement.setLong(11, value.subjects());
            statement.setLong(12, value.iriObjects());
        }
    }


    /**
     * Statistics of the literal linksets as stored in the table {@code void.literal_linksets}.
     */
    @SuppressWarnings("serial")
    private static final class DatatypeLinksetStats extends SqlMap<PropertyFromClassToDatatype, Stats>
    {
        /**
         * Creates an empty map.
         */
        private DatatypeLinksetStats()
        {
        }

        @Override
        public PropertyFromClassToDatatype getKey(ResultSet result) throws SQLException
        {
            return new PropertyFromClassToDatatype(result.getInt(1), new Resource(result.getShort(2), result.getInt(3)),
                    result.getInt(4), new Resource(result.getShort(5), result.getInt(6)),
                    new Resource(result.getShort(7), result.getInt(8)));
        }

        @Override
        public Stats getValue(ResultSet result) throws SQLException
        {
            return new Stats(0l, 0l, result.getLong(9), result.getLong(10), 0l, result.getLong(11));
        }

        @Override
        public void set(PreparedStatement statement, PropertyFromClassToDatatype key, Stats value) throws SQLException
        {
            statement.setInt(1, key.predicateGraph());
            statement.setShort(2, key.predicate().unit());
            statement.setInt(3, key.predicate().id());
            statement.setInt(4, key.subjectGraph());
            statement.setShort(5, key.subjectClass().unit());
            statement.setInt(6, key.subjectClass().id());
            statement.setShort(7, key.datatype().unit());
            statement.setInt(8, key.datatype().id());
            statement.setLong(9, value.triples());
            statement.setLong(10, value.subjects());
            statement.setLong(11, value.litObjects());
        }
    }


    /**
     * Result computed in the background.
     *
     * @param <T> the type of the result
     */
    private static interface FutureWrapper<T>
    {
        /**
         * Waits for the result and returns it.
         *
         * @return the result
         * @throws CancellationException if the computation was cancelled
         * @throws ExecutionException if the computation failed
         * @throws InterruptedException if the thread is interrupted
         * @throws SQLException if a database error occurs
         */
        T get() throws CancellationException, ExecutionException, InterruptedException, SQLException;
    }


    /**
     * Reader of the group key of a row of the result of a count query.
     *
     * @param <T> the type of the key
     */
    private static interface ExtractFromResultSet<T>
    {
        /**
         * Reads the group key of the current row.
         *
         * @param rs the result set positioned at the row
         * @return the group key
         * @throws SQLException if a database error occurs
         */
        T apply(ResultSet rs) throws SQLException;
    }


    /**
     * Pair of resources: the classes of the subjects and of the objects of a class linkset.
     *
     * @param r1 the first resource
     * @param r2 the second resource
     */
    private static record ResourcePair(Resource r1, Resource r2)
    {
        /**
         * Creates the pair from the units and ids of the resources.
         *
         * @param short1 the unit of the first resource
         * @param int1 the id of the first resource
         * @param short2 the unit of the second resource
         * @param int2 the id of the second resource
         */
        public ResourcePair(short short1, int int1, short short2, int int2)
        {
            this(new Resource(short1, int1), new Resource(short2, int2));
        }
    }


    /**
     * Molfile tables, which hold each of their literal values once, so that their rows are counted instead of their
     * distinct values.
     */
    private static final HashSet<SourceTable> uniques = new HashSet<>(
            Set.of(new DatabaseTable("chebi", "class_molfiles"), new DatabaseTable("chembl", "molecule_molfiles"),
                    new DatabaseTable("drugbank", "compounds"), new DatabaseTable("pubchem", "compound_molfiles")));

    /**
     * Variable of the subjects.
     */
    private static final Variable varS = new Variable("S");

    /**
     * Variable of the objects.
     */
    private static final Variable varO = new Variable("O");

    /**
     * Variable of the classes of the subjects.
     */
    private static final Variable varSC = new Variable("SC");

    /**
     * Variable of the classes of the objects.
     */
    private static final Variable varOC = new Variable("OC");

    /**
     * Variable of the counts.
     */
    private static final Variable varCount = new Variable("COUNT");

    /**
     * Service of the tasks computing the statistics.
     */
    private static CompletionService<Boolean> taskService;

    /**
     * Pool running the count queries by their priorities.
     */
    private static PriorityThreadPoolExecutor sqlService;

    /**
     * Request translating the count queries.
     */
    private static Request request;

    /**
     * Number of the submitted tasks computing the statistics.
     */
    private static int taskCount = 0;

    /**
     * Number of the requested count queries, including those served by the cache.
     */
    private static int planned = 0;

    /**
     * Number of the executed count queries.
     */
    private static int executed = 0;


    /**
     * Not instantiable.
     */
    private GenerateVoid()
    {
    }


    /**
     * Generates the statistics and updates their tables; the connection to the database is read from the file
     * {@code datasource.properties}.
     *
     * @param args the command-line arguments (ignored)
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            /*
             * init pool and configuration
             */
            init("stats");

            Properties properties = new Properties();

            try(FileInputStream in = new FileInputStream("datasource.properties"))
            {
                properties.load(in);
            }


            // a connection serves a whole statistics query, so a pool would save only its setup of a few milliseconds
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setServerNames(new String[] { properties.getProperty("host") });
            dataSource.setPortNumbers(new int[] { Integer.parseInt(properties.getProperty("port")) });
            dataSource.setDatabaseName(properties.getProperty("dbname"));
            dataSource.setUser(properties.getProperty("user"));
            dataSource.setPassword(properties.getProperty("password"));

            DatabaseSchema schema = new DatabaseSchema(dataSource);

            IdsmConfiguration config = new IdsmConfiguration(null, dataSource, schema);

            long begin = System.currentTimeMillis();

            request = new Request(config);

            sqlService = new PriorityThreadPoolExecutor(16);


            /*
             * process config
             */

            // the units of the classes the statistics leave out; the mapping of the statistics excludes them from all
            // its resources, so the classes of these units are skipped and a property or a datatype of them is refused
            List<Short> excluded = new ArrayList<>();

            for(short unit : excludedUnits)
                excluded.add(unit);


            Map<Iri, Resource> datatypeMap = new HashMap<>();
            UserIriClass rc = config.getIriClass("ontology:resource");

            for(Datatype datatype : config.getDatatypes())
            {
                Iri iri = datatype.getTypeIri();
                Resource resource = new Resource(rc.toColumns(request, iri));

                Graph.checkUnit(resource, iri, excluded);
                datatypeMap.put(iri, resource);
            }

            Resource langString = new Resource(rc.toColumns(request, rdfLangStringIri));

            Graph.checkUnit(langString, rdfLangStringIri, excluded);
            datatypeMap.put(rdfLangStringIri, langString);

            Resource rdfType = new Resource(rc.toColumns(request, new Iri(Rdf.TYPE)));


            Map<Iri, Graph> graphDefinitions = new HashMap<>();

            for(QuadMapping map : config.getMappings(config.getServiceIri()))
            {
                ConstantIriMapping graphMapping = (ConstantIriMapping) map.getGraph();
                Iri iri = graphMapping != null ? graphMapping.getIri() : null;

                Graph set = graphDefinitions.get(iri);

                if(set == null)
                {
                    set = new Graph();
                    graphDefinitions.put(iri, set);
                }

                set.add(request, map, datatypeMap, excluded);
            }




            /*
             * load old data
             */

            GraphStats oldGraphStats = new GraphStats();
            GraphStats newGraphStats = new GraphStats();

            List<String> graphKeyCols = List.of("id");
            List<String> graphValueCols = List.of("iri", "classes", "properties", "triples", "subjects", "objects",
                    "iri_objects", "literal_objects");

            load(getLoadSql("graphs", graphKeyCols, graphValueCols), oldGraphStats);



            ClassPartitionStats oldClassPartitionStats = new ClassPartitionStats();
            ClassPartitionStats newClassPartitionStats = new ClassPartitionStats();

            List<String> classPartitionsKeyCols = List.of("graph", "class_unit", "class_id");
            List<String> classPartitionsValueCols = List.of("classes", "properties", "triples", "subjects", "objects",
                    "iri_objects", "literal_objects");

            load(getLoadSql("class_partitions", classPartitionsKeyCols, classPartitionsValueCols),
                    oldClassPartitionStats);



            ClassPropertyPartitionStats oldClassPropertyPartitionStats = new ClassPropertyPartitionStats();
            ClassPropertyPartitionStats newClassPropertyPartitionStats = new ClassPropertyPartitionStats();

            List<String> classPropertyPartitionKeyCols = List.of("graph", "class_unit", "class_id", "property_unit",
                    "property_id");
            List<String> classPropertyPartitionValueCols = List.of("triples", "subjects", "objects", "iri_objects",
                    "literal_objects");

            load(getLoadSql("class_property_partitions", classPropertyPartitionKeyCols,
                    classPropertyPartitionValueCols), oldClassPropertyPartitionStats);



            PropertyPartitionStats oldPropertyPartitionStats = new PropertyPartitionStats();
            PropertyPartitionStats newPropertyPartitionStats = new PropertyPartitionStats();

            List<String> propertyPartitionStatsKeyCols = List.of("graph", "property_unit", "property_id");
            List<String> propertyPartitionStatsValueCols = List.of("triples", "subjects", "objects", "iri_objects",
                    "literal_objects");

            load(getLoadSql("property_partitions", propertyPartitionStatsKeyCols, propertyPartitionStatsValueCols),
                    oldPropertyPartitionStats);


            DatatypeLinksetStats oldDatatypeLinksetStats = new DatatypeLinksetStats();
            DatatypeLinksetStats newDatatypeLinksetStats = new DatatypeLinksetStats();

            List<String> datatypeLinksetStatsKeyCols = List.of("property_graph", "property_unit", "property_id",
                    "subject_graph", "subject_unit", "subject_id", "datatype_unit", "datatype_id");
            List<String> datatypeLinksetStatsValueCols = List.of("triples", "subjects", "objects");

            load(getLoadSql("literal_linksets", datatypeLinksetStatsKeyCols, datatypeLinksetStatsValueCols),
                    oldDatatypeLinksetStats);


            ClassLinksetStats oldClassLinksetStats = new ClassLinksetStats();
            ClassLinksetStats newClassLinksetStats = new ClassLinksetStats();

            List<String> ClassLinksetStatsKeyCols = List.of("property_graph", "property_unit", "property_id",
                    "subject_graph", "subject_unit", "subject_id", "object_graph", "object_unit", "object_id");
            List<String> ClassLinksetStatsValueCols = List.of("triples", "subjects", "objects");

            load(getLoadSql("linksets", ClassLinksetStatsKeyCols, ClassLinksetStatsValueCols), oldClassLinksetStats);



            Restrictions restrictions = new Restrictions();
            restrictions.add(varS);
            restrictions.add(varO);
            restrictions.add(varSC);
            restrictions.add(varOC);


            /*
             * translate parts
             */

            Map<Iri, Integer> revGraphs = new HashMap<>();
            int nextGraphID = 0;

            for(GraphIRI k : oldGraphStats.keySet())
            {
                revGraphs.put(k.iri().isEmpty() ? null : new Iri(k.iri()), k.id());

                if(k.id() > nextGraphID)
                    nextGraphID = k.id();
            }

            if(nextGraphID == 0)
                revGraphs.put(null, 0);

            nextGraphID++;



            Map<Integer, Iri> graphs = new HashMap<>();
            Map<Integer, Set<Resource>> predicates = new HashMap<>();

            Map<Integer, SqlIntercode> iriGraphs = new HashMap<>();
            Map<Integer, SqlIntercode> litGraphs = new HashMap<>();

            Map<PropertyInGraph, SqlIntercode> iriProperties = new HashMap<>();
            Map<PropertyInGraph, SqlIntercode> litProperties = new HashMap<>();
            Map<PropertyInGraph, Map<Resource, SqlIntercode>> datatypes = new HashMap<>();

            Map<ClassInGraph, SqlIntercode> subjectClasses = new HashMap<>();
            Map<ClassInGraph, SqlIntercode> objectClasses = new HashMap<>();
            Map<Integer, Set<SqlIntercode>> subjectDynamicClasses = new HashMap<>();
            Map<Integer, Set<SqlIntercode>> objectDynamicClasses = new HashMap<>();

            for(Entry<Iri, Graph> g : graphDefinitions.entrySet())
            {
                Integer graphID = revGraphs.get(g.getKey());

                if(graphID == null)
                    graphID = nextGraphID++;

                graphs.put(graphID, g.getKey());


                Set<Resource> predicatesSet = new HashSet<>();
                predicates.put(graphID, predicatesSet);

                iriGraphs.put(graphID, g.getValue().getIriDataset().translate(request, varS, varO));
                litGraphs.put(graphID, g.getValue().getLitDataset().translate(request, varS, varO));

                HashSet<SqlIntercode> graphSubjectDynClasses = new HashSet<>();
                HashSet<SqlIntercode> graphObjectDynClasses = new HashSet<>();

                for(Entry<Set<Resource>, Dataset> s : g.getValue().getClasses().entrySet())
                {
                    if(s.getKey().size() > 1)
                    {
                        graphSubjectDynClasses.add(s.getValue().translate(request, varS, varSC));
                        graphObjectDynClasses.add(s.getValue().translate(request, varO, varOC));
                    }
                }

                if(!graphSubjectDynClasses.isEmpty())
                    subjectDynamicClasses.put(graphID, graphSubjectDynClasses);

                if(!graphObjectDynClasses.isEmpty())
                    objectDynamicClasses.put(graphID, graphObjectDynClasses);

                for(Entry<Set<Resource>, Dataset> d : g.getValue().getClasses().entrySet())
                {
                    if(d.getKey().size() == 1)
                    {
                        Resource classID = d.getKey().iterator().next();
                        Dataset dataset = d.getValue();

                        subjectClasses.put(new ClassInGraph(graphID, classID), dataset.translate(request, varS, null));
                        objectClasses.put(new ClassInGraph(graphID, classID), dataset.translate(request, varO, null));
                    }
                }

                for(Entry<Resource, Dataset> d : g.getValue().getIriPredicates().entrySet())
                {
                    Resource propertyID = d.getKey();
                    Dataset dataset = d.getValue();

                    predicatesSet.add(propertyID);
                    iriProperties.put(new PropertyInGraph(graphID, propertyID), dataset.translate(request, varS, varO));
                }


                for(Entry<Resource, Dataset> d : g.getValue().getLitPredicates().entrySet())
                {
                    Resource propertyID = d.getKey();
                    Dataset dataset = d.getValue();

                    predicatesSet.add(propertyID);
                    litProperties.put(new PropertyInGraph(graphID, propertyID), dataset.translate(request, varS, varO));
                }


                for(Entry<Resource, Map<Resource, Dataset>> d : g.getValue().getDatatypePredicates().entrySet())
                {
                    Resource propertyID = d.getKey();

                    Map<Resource, SqlIntercode> fres = new HashMap<>();
                    datatypes.put(new PropertyInGraph(graphID, propertyID), fres);

                    for(Entry<Resource, Dataset> x : d.getValue().entrySet())
                        fres.put(x.getKey(), x.getValue().translate(request, varS, varO));
                }
            }


            /*
             * compute statistics
             */

            ExecutorService execututor = Executors.newFixedThreadPool(64);
            taskService = new ExecutorCompletionService<>(execututor);
            SqlNoSolution empty = SqlNoSolution.get();

            Map<String, Future<Map<Void, Long>>> cache0 = new HashMap<>();
            Map<String, Future<Map<Resource, Long>>> cache1 = new HashMap<>();
            Map<String, Future<Map<ResourcePair, Long>>> cache2 = new HashMap<>();


            /**********************************************************************************************************/

            /*
             * graph statistics
             *
             *  graph <g1> { ?S ?P ?O }
             */

            for(Entry<Integer, Iri> g : graphs.entrySet())
            {
                SqlIntercode iriPart = iriGraphs.get(g.getKey());
                SqlIntercode litPart = litGraphs.get(g.getKey());
                GraphIRI key = new GraphIRI(g.getKey(), g.getValue());

                sumbitTask(newGraphStats, cache0, iriPart, litPart, key, true);
            }

            /**********************************************************************************************************/

            /*
             * class partition statistics
             *
             *  graph <g1> { ?S ?P ?O }
             *  graph <g1> { ?S a <r2> }
             */

            for(Entry<Integer, Set<SqlIntercode>> s : subjectDynamicClasses.entrySet())
            {
                for(SqlIntercode sx : s.getValue())
                {
                    SqlIntercode iriPart = SqlJoin.join(request, sx, iriGraphs.getOrDefault(s.getKey(), empty));
                    SqlIntercode litPart = SqlJoin.join(request, sx, litGraphs.getOrDefault(s.getKey(), empty));

                    sumbitTask(newClassPartitionStats, cache1, iriPart, litPart, List.of(varSC),
                            r -> new Resource(r.getShort(2), r.getInt(3)), t -> new ClassInGraph(s.getKey(), t), true);
                }
            }

            for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
            {
                SqlIntercode iriPart = SqlJoin.join(request, s.getValue(),
                        iriGraphs.getOrDefault(s.getKey().graph(), empty));
                SqlIntercode litPart = SqlJoin.join(request, s.getValue(),
                        litGraphs.getOrDefault(s.getKey().graph(), empty));

                sumbitTask(newClassPartitionStats, cache0, iriPart, litPart, s.getKey(), true);
            }

            /**********************************************************************************************************/

            /*
             * class and property partition statistics
             *
             *  graph <g1> { ?S <r1> ?O }
             *  graph <g1> { ?S a  <r2> }
             */

            for(Entry<Integer, Set<SqlIntercode>> s : subjectDynamicClasses.entrySet())
            {
                for(SqlIntercode sx : s.getValue())
                {
                    for(Resource p : predicates.get(s.getKey()))
                    {
                        PropertyInGraph key = new PropertyInGraph(s.getKey(), p);
                        SqlIntercode iriPart = SqlJoin.join(request, sx, iriProperties.getOrDefault(key, empty));
                        SqlIntercode litPart = SqlJoin.join(request, sx, litProperties.getOrDefault(key, empty));

                        sumbitTask(newClassPropertyPartitionStats, cache1, iriPart, litPart, List.of(varSC),
                                r -> new Resource(r.getShort(2), r.getInt(3)),
                                t -> new ClassAndPropertyInGraph(s.getKey(), t, p), false);
                    }
                }
            }

            for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
            {
                for(Resource p : predicates.get(s.getKey().graph()))
                {
                    PropertyInGraph key = new PropertyInGraph(s.getKey().graph(), p);
                    SqlIntercode iriPart = SqlJoin.join(request, s.getValue(), iriProperties.getOrDefault(key, empty));
                    SqlIntercode litPart = SqlJoin.join(request, s.getValue(), litProperties.getOrDefault(key, empty));

                    sumbitTask(newClassPropertyPartitionStats, cache0, iriPart, litPart,
                            new ClassAndPropertyInGraph(s.getKey().graph(), s.getKey().resource(), p), false);
                }
            }

            /**********************************************************************************************************/

            /*
             * property partition statistics
             *
             *  graph <g1> { ?S <r1> ?O }
             */

            for(Entry<Integer, Set<Resource>> p : predicates.entrySet())
            {
                for(Resource px : p.getValue())
                {
                    PropertyInGraph key = new PropertyInGraph(p.getKey(), px);
                    SqlIntercode iriPart = iriProperties.getOrDefault(key, empty);
                    SqlIntercode litPart = litProperties.getOrDefault(key, empty);

                    sumbitTask(newPropertyPartitionStats, cache0, iriPart, litPart, key, true);
                }
            }

            /**********************************************************************************************************/

            /*
             * datatype linkset statistics
             *
             *  graph <g1> { ?S <r1> ?O }
             *  graph <g2> { ?S a  <r2>  filter (datatype(?O) = <r3>) }
             */

            for(Entry<PropertyInGraph, Map<Resource, SqlIntercode>> p : datatypes.entrySet())
            {
                for(Entry<Resource, SqlIntercode> px : p.getValue().entrySet())
                {
                    for(Entry<Integer, Set<SqlIntercode>> s : subjectDynamicClasses.entrySet())
                    {
                        if(!p.getKey().graph.equals(s.getKey()))
                            continue;

                        for(SqlIntercode sx : s.getValue())
                        {
                            SqlIntercode sp = SqlJoin.join(request, sx, px.getValue());

                            sumbitTask(newDatatypeLinksetStats, cache1, empty, sp, List.of(varSC),
                                    r -> new Resource(r.getShort(2), r.getInt(3)),
                                    t -> new PropertyFromClassToDatatype(p.getKey(), new ClassInGraph(s.getKey(), t),
                                            px.getKey()),
                                    false);
                        }
                    }
                }
            }

            for(Entry<PropertyInGraph, Map<Resource, SqlIntercode>> p : datatypes.entrySet())
            {
                for(Entry<Resource, SqlIntercode> px : p.getValue().entrySet())
                {
                    for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
                    {
                        if(!p.getKey().graph.equals(s.getKey().graph()))
                            continue;

                        SqlIntercode sp = SqlJoin.join(request, s.getValue(), px.getValue());

                        sumbitTask(newDatatypeLinksetStats, cache0, empty, sp,
                                new PropertyFromClassToDatatype(p.getKey(), s.getKey(), px.getKey()), false);
                    }
                }
            }

            /**********************************************************************************************************/

            /*
             * class linkset statistics
             *
             *  graph <g1> { ?S <r1> ?O }
             *  graph <g2> { ?S a  <r2> }
             *  graph <g3> { ?O a  <r3> }
             */

            for(Entry<PropertyInGraph, SqlIntercode> p : iriProperties.entrySet())
            {
                for(Entry<Integer, Set<SqlIntercode>> s : subjectDynamicClasses.entrySet())
                {
                    for(SqlIntercode sx : s.getValue())
                    {
                        SqlIntercode sp = SqlJoin.join(request, sx, p.getValue()).optimize(request, restrictions, false,
                                false);

                        if(sp == SqlNoSolution.get())
                            continue;

                        for(Entry<Integer, Set<SqlIntercode>> o : objectDynamicClasses.entrySet())
                        {
                            for(SqlIntercode ox : o.getValue())
                            {
                                SqlIntercode spo = SqlJoin.join(request, sp, ox);

                                sumbitTask(newClassLinksetStats, cache2, spo, empty, List.of(varSC, varOC),
                                        r -> new ResourcePair(r.getShort(2), r.getInt(3), r.getShort(4), r.getInt(5)),
                                        t -> new PropertyFromClassToClass(p.getKey(),
                                                new ClassInGraph(s.getKey(), t.r1()),
                                                new ClassInGraph(o.getKey(), t.r2())),
                                        false);
                            }
                        }
                    }
                }
            }

            for(Entry<PropertyInGraph, SqlIntercode> p : iriProperties.entrySet())
            {
                for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
                {
                    SqlIntercode sp = SqlJoin.join(request, s.getValue(), p.getValue()).optimize(request, restrictions,
                            false, false);

                    if(sp == SqlNoSolution.get())
                        continue;

                    for(Entry<Integer, Set<SqlIntercode>> o : objectDynamicClasses.entrySet())
                    {
                        for(SqlIntercode ox : o.getValue())
                        {
                            {
                                SqlIntercode spo = SqlJoin.join(request, sp, ox);

                                sumbitTask(newClassLinksetStats, cache1, spo, empty, List.of(varOC),
                                        r -> new Resource(r.getShort(2), r.getInt(3)),
                                        t -> new PropertyFromClassToClass(p.getKey(), s.getKey(),
                                                new ClassInGraph(o.getKey(), t)),
                                        false);
                            }
                        }
                    }
                }
            }

            for(Entry<PropertyInGraph, SqlIntercode> p : iriProperties.entrySet())
            {
                for(Entry<Integer, Set<SqlIntercode>> s : subjectDynamicClasses.entrySet())
                {
                    for(SqlIntercode sx : s.getValue())
                    {
                        SqlIntercode sp = SqlJoin.join(request, sx, p.getValue()).optimize(request, restrictions, false,
                                false);

                        if(sp == SqlNoSolution.get())
                            continue;

                        for(Entry<ClassInGraph, SqlIntercode> o : objectClasses.entrySet())
                        {
                            SqlIntercode spo = SqlJoin.join(request, sp, o.getValue());

                            sumbitTask(newClassLinksetStats, cache1, spo, empty, List.of(varSC),
                                    r -> new Resource(r.getShort(2), r.getInt(3)),
                                    t -> new PropertyFromClassToClass(p.getKey(), new ClassInGraph(s.getKey(), t),
                                            o.getKey()),
                                    false);
                        }
                    }
                }
            }

            for(Entry<PropertyInGraph, SqlIntercode> p : iriProperties.entrySet())
            {
                for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
                {
                    SqlIntercode sp = SqlJoin.join(request, s.getValue(), p.getValue()).optimize(request, restrictions,
                            false, false);

                    if(sp == SqlNoSolution.get())
                        continue;

                    for(Entry<ClassInGraph, SqlIntercode> o : objectClasses.entrySet())
                    {
                        SqlIntercode spo = SqlJoin.join(request, sp, o.getValue());

                        sumbitTask(newClassLinksetStats, cache0, spo, empty,
                                new PropertyFromClassToClass(p.getKey(), s.getKey(), o.getKey()), false);
                    }
                }
            }

            /**********************************************************************************************************/

            System.out.println("waiting ...");

            for(int i = 0; i < taskCount; i++)
            {
                Future<Boolean> completedFuture = taskService.take();
                completedFuture.get();
            }

            execututor.close();
            sqlService.close();


            for(ClassInGraph e : newClassPartitionStats.keySet())
                newGraphStats.get(new GraphIRI(e.graph(), graphs.get(e.graph()))).incClasses();

            for(PropertyInGraph e : newPropertyPartitionStats.keySet())
                newGraphStats.get(new GraphIRI(e.graph(), graphs.get(e.graph()))).incPredicate();

            for(ClassAndPropertyInGraph e : newClassPropertyPartitionStats.keySet())
                newClassPartitionStats.get(new ClassInGraph(e.graph(), e.rclass())).incPredicate();

            for(Entry<ClassInGraph, Stats> e : newClassPartitionStats.entrySet())
                e.getValue()
                        .setClasses(newClassPropertyPartitionStats
                                .get(new ClassAndPropertyInGraph(e.getKey().graph(), e.getKey().resource(), rdfType))
                                .iriObjects());



            processMap(oldGraphStats, newGraphStats);
            store(getDeleteSql("graphs", graphKeyCols, graphValueCols), oldGraphStats);
            store(getUpdateSql("graphs", graphKeyCols, graphValueCols), newGraphStats);

            processMap(oldClassPartitionStats, newClassPartitionStats);
            store(getDeleteSql("class_partitions", classPartitionsKeyCols, classPartitionsValueCols),
                    oldClassPartitionStats);
            store(getUpdateSql("class_partitions", classPartitionsKeyCols, classPartitionsValueCols),
                    newClassPartitionStats);

            processMap(oldClassPropertyPartitionStats, newClassPropertyPartitionStats);
            store(getDeleteSql("class_property_partitions", classPropertyPartitionKeyCols,
                    classPropertyPartitionValueCols), oldClassPropertyPartitionStats);
            store(getUpdateSql("class_property_partitions", classPropertyPartitionKeyCols,
                    classPropertyPartitionValueCols), newClassPropertyPartitionStats);

            processMap(oldPropertyPartitionStats, newPropertyPartitionStats);
            store(getDeleteSql("property_partitions", propertyPartitionStatsKeyCols, propertyPartitionStatsValueCols),
                    oldPropertyPartitionStats);
            store(getUpdateSql("property_partitions", propertyPartitionStatsKeyCols, propertyPartitionStatsValueCols),
                    newPropertyPartitionStats);

            processMap(oldDatatypeLinksetStats, newDatatypeLinksetStats);
            store(getDeleteSql("literal_linksets", datatypeLinksetStatsKeyCols, datatypeLinksetStatsValueCols),
                    oldDatatypeLinksetStats);
            store(getUpdateSql("literal_linksets", datatypeLinksetStatsKeyCols, datatypeLinksetStatsValueCols),
                    newDatatypeLinksetStats);

            processMap(oldClassLinksetStats, newClassLinksetStats);
            store(getDeleteSql("linksets", ClassLinksetStatsKeyCols, ClassLinksetStatsValueCols), oldClassLinksetStats);
            store(getUpdateSql("linksets", ClassLinksetStatsKeyCols, ClassLinksetStatsValueCols), newClassLinksetStats);


            commit();

            System.out.println("planned:  " + planned);
            System.out.println("executed: " + executed);
            System.out.println("time:     " + (System.currentTimeMillis() - begin) / 1000.0);
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }


    /**
     * Compares the stored and the new statistics: the stored statistics are left with the entries to delete, whose keys
     * have no new statistics, and the new ones with the entries to store, which differ from the stored ones.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the statistics
     * @param oldMap the stored statistics
     * @param newMap the new statistics
     */
    private static <K, V> void processMap(Map<K, V> oldMap, Map<K, V> newMap)
    {
        Iterator<Entry<K, V>> it = newMap.entrySet().iterator();

        while(it.hasNext())
        {
            Entry<K, V> e = it.next();

            V old = oldMap.remove(e.getKey());

            if(e.getValue().equals(old))
                it.remove();
        }
    }


    /**
     * Submits the task computing the statistics of the triples grouped by the values of some variables.
     *
     * @param <T> the type of the group keys
     * @param <K> the type of the keys of the statistics
     * @param output the statistics to fill
     * @param cache the futures of the submitted count queries by their SQL code
     * @param iriImcode the code of the triples with IRI objects
     * @param litImcode the code of the triples with literal objects
     * @param v the variables grouping the triples
     * @param fres the reader of the group keys
     * @param keyget the function giving the key of the statistics of a group
     * @param asumeNotEmpty whether the triples are known to exist, so that the distinct counts need not wait for the
     *            count of the triples
     */
    private static <T, K> void sumbitTask(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, List<Variable> v, ExtractFromResultSet<T> fres,
            Function<T, K> keyget, boolean asumeNotEmpty)
    {
        taskService.submit(() -> computeStats(output, cache, iriImcode, litImcode, v, fres, keyget, asumeNotEmpty));
        taskCount++;
    }


    /**
     * Submits the task computing the statistics of all the triples under one key.
     *
     * @param <T> the type of the group keys of the cached queries
     * @param <K> the type of the keys of the statistics
     * @param output the statistics to fill
     * @param cache the futures of the submitted count queries by their SQL code
     * @param iriImcode the code of the triples with IRI objects
     * @param litImcode the code of the triples with literal objects
     * @param key the key of the statistics
     * @param asumeNotEmpty whether the triples are known to exist, so that the distinct counts need not wait for the
     *            count of the triples
     */
    private static <T, K> void sumbitTask(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, K key, boolean asumeNotEmpty)
    {
        taskService.submit(() -> computeStats(output, cache, iriImcode, litImcode, key, asumeNotEmpty));
        taskCount++;
    }


    /**
     * Computes the statistics of all the triples under one key.
     *
     * @param <T> the type of the group keys of the cached queries
     * @param <K> the type of the keys of the statistics
     * @param output the statistics to fill
     * @param cache the futures of the submitted count queries by their SQL code
     * @param iriImcode the code of the triples with IRI objects
     * @param litImcode the code of the triples with literal objects
     * @param key the key of the statistics
     * @param asumeNotEmpty whether the triples are known to exist, so that the distinct counts need not wait for the
     *            count of the triples
     * @return true
     * @throws SQLException if a database error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException if a count query fails
     */
    private static <T, K> boolean computeStats(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, K key, boolean asumeNotEmpty)
            throws SQLException, InterruptedException, ExecutionException
    {
        return computeStats(output, cache, iriImcode, litImcode, List.of(), r -> null, t -> key, asumeNotEmpty);
    }


    /**
     * Computes the statistics of the triples grouped by the values of some variables: the number of the triples and,
     * unless there are none, the numbers of the distinct subjects and of the distinct IRI and literal objects.
     *
     * @param <T> the type of the group keys
     * @param <K> the type of the keys of the statistics
     * @param output the statistics to fill
     * @param cache the futures of the submitted count queries by their SQL code
     * @param iriPart the code of the triples with IRI objects
     * @param litPart the code of the triples with literal objects
     * @param v the variables grouping the triples
     * @param fres the reader of the group keys
     * @param keyget the function giving the key of the statistics of a group
     * @param asumeNotEmpty whether the triples are known to exist, so that the distinct counts need not wait for the
     *            count of the triples
     * @return true
     * @throws SQLException if a database error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException if a count query fails
     */
    private static <T, K> boolean computeStats(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriPart, SqlIntercode litPart, List<Variable> v, ExtractFromResultSet<T> fres,
            Function<T, K> keyget, boolean asumeNotEmpty) throws SQLException, InterruptedException, ExecutionException
    {
        try
        {
            SqlIntercode imcode = SqlUnion.union(request, List.of(iriPart, litPart));

            FutureWrapper<HashMap<K, Long>> ftriples = computeCount(cache, imcode, v, fres, keyget, 3);


            if(asumeNotEmpty || !ftriples.get().isEmpty())
            {
                FutureWrapper<HashMap<K, Long>> flitObjects = computeDistinctCount(cache, litPart, v, fres, keyget,
                        varO, 0);
                FutureWrapper<HashMap<K, Long>> firiObjects = computeDistinctCount(cache, iriPart, v, fres, keyget,
                        varO, 1);
                FutureWrapper<HashMap<K, Long>> fsubjects = computeDistinctCount(cache, imcode, v, fres, keyget, varS,
                        2);

                HashMap<K, Long> litObjects = flitObjects.get();
                HashMap<K, Long> iriObjects = firiObjects.get();
                HashMap<K, Long> subjects = fsubjects.get();
                HashMap<K, Long> triples = ftriples.get();

                synchronized(output)
                {
                    for(Entry<K, Long> e : triples.entrySet())
                    {
                        Stats stats = new Stats(0, 0, e.getValue(), subjects.getOrDefault(e.getKey(), 0l),
                                iriObjects.getOrDefault(e.getKey(), 0l), litObjects.getOrDefault(e.getKey(), 0l));

                        Stats old = output.put(e.getKey(), stats);

                        if(old != null)
                            System.err.println(e.getKey() + ": " + old + " vs " + stats);
                    }
                }
            }

            return true;
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Submits the queries counting the triples grouped by the values of some variables, one query per branch of the
     * code.
     *
     * @param <T> the type of the group keys
     * @param <K> the type of the keys of the statistics
     * @param cache the futures of the submitted count queries by their SQL code
     * @param imcode the code of the triples
     * @param v the variables grouping the triples
     * @param fres the reader of the group keys
     * @param fkey the function giving the key of the statistics of a group
     * @param priority the priority of the queries
     * @return the future sums of the counts by the keys of the statistics
     * @throws SQLException if a database error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException if a count query fails
     */
    private static <T, K> FutureWrapper<HashMap<K, Long>> computeCount(Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode imcode, List<Variable> v, ExtractFromResultSet<T> fres, Function<T, K> fkey, int priority)
            throws SQLException, InterruptedException, ExecutionException
    {
        HashSet<Variable> groupBy = new HashSet<>(v);

        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(varCount);
        vars.addAll(v);

        imcode = imcode.optimize(request, new Restrictions(groupBy), false, false);

        if(imcode == SqlNoSolution.get())
            return () -> new HashMap<>();


        List<Future<Map<T, Long>>> futures = new ArrayList<>();

        for(SqlIntercode branch : imcode instanceof SqlUnion union ? union.getChilds() : List.of(imcode))
        {
            String sql = getSqlQuery(branch, groupBy, null, vars);

            if(sql != null)
                futures.add(getCountMap(sql, false, cache, fres, priority));
        }

        return new FutureWrapper<>()
        {
            boolean isFinished = false;
            HashMap<K, Long> triples = new HashMap<>();

            @Override
            public HashMap<K, Long> get()
                    throws CancellationException, ExecutionException, InterruptedException, SQLException
            {
                if(!isFinished)
                {
                    for(Future<Map<T, Long>> future : futures)
                    {
                        for(Entry<T, Long> entry : future.get().entrySet())
                        {
                            K key = fkey.apply(entry.getKey());
                            Long value = entry.getValue();

                            if(value > 0)
                                triples.merge(key, value, Long::sum);
                        }
                    }

                    isFinished = true;
                }

                return triples;
            }
        };
    }


    /**
     * Submits the queries counting the distinct values of a variable grouped by the values of other variables, one
     * query per group of branches of the code whose values cannot coincide (see {@link #splitImCode}).
     *
     * @param <T> the type of the group keys
     * @param <K> the type of the keys of the statistics
     * @param cache the futures of the submitted count queries by their SQL code
     * @param imcode the code of the triples
     * @param v the variables grouping the triples
     * @param fres the reader of the group keys
     * @param fkey the function giving the key of the statistics of a group
     * @param what the variable whose distinct values are counted
     * @param priority the priority of the queries
     * @return the future sums of the counts by the keys of the statistics
     * @throws SQLException if a database error occurs
     * @throws InterruptedException if the thread is interrupted
     * @throws ExecutionException if a count query fails
     */
    private static <T, K> FutureWrapper<HashMap<K, Long>> computeDistinctCount(Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode imcode, List<Variable> v, ExtractFromResultSet<T> fres, Function<T, K> fkey, Variable what,
            int priority) throws SQLException, InterruptedException, ExecutionException
    {
        HashSet<Variable> groupBy = new HashSet<>(v);

        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(varCount);
        vars.addAll(v);

        Restrictions restrictions = new Restrictions(v);
        restrictions.add(what);

        imcode = imcode.optimize(request, restrictions, true, false);

        if(imcode == SqlNoSolution.get())
            return (() -> new HashMap<>());


        List<Future<Map<T, Long>>> futures = new ArrayList<>();

        for(SqlIntercode branch : splitImCode(imcode, what))
        {
            String sql = getSqlQuery(branch, groupBy, what, vars);

            if(sql != null)
                futures.add(getCountMap(sql, isCountDistinct(branch, what), cache, fres, priority));
        }

        return new FutureWrapper<>()
        {
            boolean isFinished = false;
            HashMap<K, Long> triples = new HashMap<>();

            @Override
            public HashMap<K, Long> get()
                    throws CancellationException, ExecutionException, InterruptedException, SQLException
            {
                if(!isFinished)
                {
                    for(Future<Map<T, Long>> future : futures)
                    {
                        for(Entry<T, Long> entry : future.get().entrySet())
                        {
                            K key = fkey.apply(entry.getKey());
                            Long value = entry.getValue();

                            if(value > 0)
                                triples.merge(key, value, Long::sum);
                        }
                    }

                    isFinished = true;
                }

                return triples;
            }
        };
    }


    /**
     * Splits the branches of a union into groups by the resource class of a variable, so that the distinct values of
     * different groups never coincide and their counts add up; the literal branches of the molfile tables form groups
     * of their own, counted by their rows.
     *
     * @param imcode the code of the triples
     * @param what the variable whose distinct values are counted
     * @return the code of each group
     */
    private static List<SqlIntercode> splitImCode(SqlIntercode imcode, Variable what)
    {
        Map<ResourceClass, List<SqlIntercode>> groups = new HashMap<>();

        List<SqlIntercode> result = new ArrayList<>();

        for(SqlIntercode branch : imcode instanceof SqlUnion union ? union.getChilds() : List.of(imcode))
        {
            ResourceClass rc = Graph.getResourceClass(branch.getVariable(what));
            ResourceClass gc = rc.getResultResourceClass();

            // FIXME
            if(gc == xsdDateTime || gc == xsdDate || gc == rdfLangString)
                rc = gc;

            if(rc instanceof LiteralClass && branch instanceof SqlTableAccess ac && uniques.contains(ac.getTable()))
                result.add(branch);
            else
                groups.computeIfAbsent(rc, k -> new ArrayList<SqlIntercode>()).add(branch);
        }

        for(List<SqlIntercode> g : groups.values())
            result.add(SqlUnion.union(request, g));

        return result;
    }


    /**
     * Returns the future counts of a count query by their group keys, submitting the query unless the cache already
     * holds it.
     *
     * @param <T> the type of the group keys
     * @param sql the SQL code of the query
     * @param countDistinct whether the query counts distinct values, which is evaluated without hash aggregation
     * @param cache the futures of the submitted count queries by their SQL code
     * @param fres the reader of the group keys
     * @param priority the priority of the query
     * @return the future counts by the group keys
     * @throws SQLException if a database error occurs
     */
    private static <T> Future<Map<T, Long>> getCountMap(String sql, boolean countDistinct,
            Map<String, Future<Map<T, Long>>> cache, ExtractFromResultSet<T> fres, int priority) throws SQLException
    {
        synchronized(cache)
        {
            Future<Map<T, Long>> result = cache.get(sql);

            planned++;

            if(result == null)
            {
                executed++;

                Callable<Map<T, Long>> task = () -> {
                    HashMap<T, Long> map = new HashMap<>();

                    try(Connection connection = request.getConfiguration().getConnectionPool().getConnection())
                    {
                        long begin = System.currentTimeMillis();

                        try(Statement statement = connection.createStatement())
                        {
                            statement.setFetchSize(0);

                            // a count of distinct values groups by a key with about as many groups as input rows, for
                            // which a hash aggregate only spills to disk, whereas a sort-based grouping runs in the
                            // parallel workers; the connection serves this statement only, so the setting does not leak
                            if(countDistinct)
                                statement.execute("SET enable_hashagg = off");

                            try(ResultSet rs = statement.executeQuery(sql))
                            {
                                while(rs.next())
                                {
                                    long triples = rs.getLong(1);
                                    T key = fres.apply(rs);

                                    map.put(key, triples);
                                }
                            }
                        }
                        catch(SQLException e)
                        {
                            System.err.println("ERROR: " + sql);
                            e.printStackTrace();
                            throw e;
                        }

                        long time = System.currentTimeMillis() - begin;

                        if(time > 300_000)
                            System.err.println("\n" + (time / 1000.0) + " " + "\n" + sql + "\n");
                    }

                    return map;
                };

                result = sqlService.submit(task, priority);

                cache.put(sql, result);
            }

            return result;
        }
    }


    /**
     * Builds the SQL query counting the solutions of the code, or the distinct values of a variable, grouped by the
     * values of other variables.
     *
     * @param imcode the code of the triples
     * @param gvars the variables grouping the solutions
     * @param what the variable whose distinct values are counted, or null to count the solutions
     * @param select the variables whose columns the query returns, the count first
     * @return the SQL code of the query, or null when the code has no solution
     */
    private static String getSqlQuery(SqlIntercode imcode, Set<Variable> gvars, Variable what, List<Variable> select)
    {
        if(imcode == SqlNoSolution.get())
            return null;

        HashSet<Variable> groupVariables = new HashSet<>(gvars);

        LinkedHashMap<Variable, SqlExpressionIntercode> aggregations = new LinkedHashMap<>();

        if(!isCountDistinct(imcode, what))
        {
            aggregations.put(varCount, SqlBuiltinCall.create(request, "card", false, List.of()));
        }
        else
        {
            SqlExpressionIntercode var = SqlVariable.create(request.getConfiguration(), imcode.getVariable(what));
            aggregations.put(varCount, SqlBuiltinCall.create(request, "count", true, List.of(var)));
        }

        SqlIntercode agg = SqlAggregation.aggregate(request, groupVariables, aggregations, imcode);

        Restrictions restriction = new Restrictions(gvars);
        restriction.add(varCount);

        SqlIntercode result = agg.optimize(request, restriction, false, false);

        if(result == SqlNoSolution.get())
            return null;

        VariableBindings vars = result.getVariableBindings();

        return "SELECT " + select.stream().flatMap(n -> getMapping(vars.get(n)).stream()).map(e -> e.toString())
                .collect(Collectors.joining(", ")) + " FROM (" + result.translate(request) + ") as tab";
    }


    /**
     * Whether the statistics query of the code counts distinct values of the variable, rather than rows: the rows of a
     * table known to hold each value once are counted directly.
     *
     * @param imcode the code of the counted triples
     * @param what the counted variable, or null to count the rows
     * @return true if the query counts distinct values of the variable, false if it counts rows
     */
    private static boolean isCountDistinct(SqlIntercode imcode, Variable what)
    {
        return what != null && !(imcode instanceof SqlTableAccess a && uniques.contains(a.getTable()));
    }


    /**
     * Returns the columns of a variable binding of a single resource class.
     *
     * @param binding the variable binding
     * @return the columns of the binding
     */
    private static List<Column> getMapping(VariableBinding binding)
    {
        return binding.getMapping(Graph.getResourceClass(binding));
    }


    /**
     * Builds the SQL query reading the stored statistics of a table of the schema {@code void}.
     *
     * @param table the name of the table
     * @param key the key columns
     * @param value the value columns
     * @return the SQL code of the query
     */
    private static String getLoadSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<>();
        cols.addAll(key);
        cols.addAll(value);

        return "select " + cols.stream().collect(joining(", ")) + " from void." + table;
    }


    /**
     * Builds the SQL command deleting a row of statistics from a table of the schema {@code void}.
     *
     * @param table the name of the table
     * @param key the key columns
     * @param value the value columns
     * @return the SQL code of the command
     */
    private static String getDeleteSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<>();
        cols.addAll(key);
        cols.addAll(value);

        return "delete from void." + table + " where " + cols.stream().map(c -> c + "=?").collect(joining(" and "));
    }


    /**
     * Builds the SQL command inserting or updating a row of statistics in a table of the schema {@code void}.
     *
     * @param table the name of the table
     * @param key the key columns
     * @param value the value columns
     * @return the SQL code of the command
     */
    private static String getUpdateSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<>();
        cols.addAll(key);
        cols.addAll(value);

        return "insert into void." + table + "(" + cols.stream().collect(joining(", ")) + ") values("
                + cols.stream().map(c -> "?").collect(joining(",")) + ") " + "on conflict("
                + key.stream().collect(joining(", ")) + ") do update set "
                + value.stream().map(c -> c + "=EXCLUDED." + c).collect(joining(", "));
    }
}
