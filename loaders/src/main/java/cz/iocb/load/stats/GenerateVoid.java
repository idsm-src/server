package cz.iocb.load.stats;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.rdfLangString;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDate;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDateTime;
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
import org.postgresql.ds.PGPoolingDataSource;
import cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration;
import cz.iocb.load.common.Updater;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.classes.DataType;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.translator.UsedVariables;
import cz.iocb.sparql.engine.translator.imcode.SqlAggregation;
import cz.iocb.sparql.engine.translator.imcode.SqlIntercode;
import cz.iocb.sparql.engine.translator.imcode.SqlJoin;
import cz.iocb.sparql.engine.translator.imcode.SqlNoSolution;
import cz.iocb.sparql.engine.translator.imcode.SqlTableAccess;
import cz.iocb.sparql.engine.translator.imcode.SqlUnion;
import cz.iocb.sparql.engine.translator.imcode.expression.SqlBuiltinCall;
import cz.iocb.sparql.engine.translator.imcode.expression.SqlExpressionIntercode;
import cz.iocb.sparql.engine.translator.imcode.expression.SqlVariable;



public class GenerateVoid extends Updater
{
    private static record GraphIRI(Integer id, String iri)
    {
        public GraphIRI(Integer id, IRI iri)
        {
            this(id, iri == null ? "" : iri.getValue());
        }
    }


    private static record ClassInGraph(Integer graph, Resource resource)
    {
    }


    private static record PropertyInGraph(Integer graph, Resource resource)
    {
    }


    private static record ClassAndPropertyInGraph(Integer graph, Resource rclass, Resource property)
    {
    }


    private static record PropertyFromClassToClass(Integer predicateGraph, Resource predicate, Integer subjectGraph,
            Resource subjectClass, Integer objectGraph, Resource objectClass)
    {
        public PropertyFromClassToClass(PropertyInGraph p, ClassInGraph s, ClassInGraph o)
        {
            this(p.graph(), p.resource(), s.graph(), s.resource(), o.graph(), o.resource());
        }
    }


    private static record PropertyFromClassToDatatype(Integer predicateGraph, Resource predicate, Integer subjectGraph,
            Resource subjectClass, Resource datatype)
    {
        public PropertyFromClassToDatatype(PropertyInGraph p, ClassInGraph s, Resource o)
        {
            this(p.graph(), p.resource(), s.graph(), s.resource(), o);
        }
    }


    private static final class Stats
    {
        long classes;
        long predicates;
        long triples;
        long subjects;
        long iriObjects;
        long litObjects;

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

        public final long classes()
        {
            return classes;
        }

        public final long predicates()
        {
            return predicates;
        }

        public final long triples()
        {
            return triples;
        }

        public final long subjects()
        {
            return subjects;
        }

        public final long iriObjects()
        {
            return iriObjects;
        }

        public final long litObjects()
        {
            return litObjects;
        }

        void incClasses()
        {
            classes++;
        }

        void incPredicate()
        {
            predicates++;
        }

        public void setClasses(long classes)
        {
            this.classes = classes;
        }
    }


    @SuppressWarnings("serial")
    private static final class GraphStats extends SqlMap<GraphIRI, Stats>
    {
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


    @SuppressWarnings("serial")
    private static final class ClassPartitionStats extends SqlMap<ClassInGraph, Stats>
    {
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


    @SuppressWarnings("serial")
    private static final class ClassPropertyPartitionStats extends SqlMap<ClassAndPropertyInGraph, Stats>
    {
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


    @SuppressWarnings("serial")
    private static final class PropertyPartitionStats extends SqlMap<PropertyInGraph, Stats>
    {
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


    @SuppressWarnings("serial")
    private static final class ClassLinksetStats extends SqlMap<PropertyFromClassToClass, Stats>
    {
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


    @SuppressWarnings("serial")
    private static final class DatatypeLinksetStats extends SqlMap<PropertyFromClassToDatatype, Stats>
    {
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


    private static interface FutureWrapper<T>
    {
        T get() throws CancellationException, ExecutionException, InterruptedException, SQLException;
    }


    private static interface ExtractFromResultSet<T>
    {
        T apply(ResultSet rs) throws SQLException;
    }


    private static record ResourcePair(Resource r1, Resource r2)
    {
        public ResourcePair(short short1, int int1, short short2, int int2)
        {
            this(new Resource(short1, int1), new Resource(short2, int2));
        }
    }


    private static final HashSet<Table> uniques = new HashSet<Table>(Set.of(new Table("molecules", "chebi"),
            new Table("molecules", "chembl"), new Table("molecules", "drugbank"), new Table("molecules", "pubchem")));

    private static CompletionService<Boolean> taskService;
    private static ExecutorService sqlService;
    private static Request request;
    private static int taskCount = 0;
    private static int planned = 0;
    private static int executed = 0;


    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            /*
             * init pool and configuration
             */
            init();

            Properties properties = new Properties();

            try(FileInputStream in = new FileInputStream("datasource.properties"))
            {
                properties.load(in);
            }


            PGPoolingDataSource connectionPool = new PGPoolingDataSource();
            connectionPool.setServerName(properties.getProperty("host"));
            connectionPool.setPortNumber(Integer.parseInt(properties.getProperty("port")));
            connectionPool.setDatabaseName(properties.getProperty("dbname"));
            connectionPool.setUser(properties.getProperty("user"));
            connectionPool.setPassword(properties.getProperty("password"));
            connectionPool.setMaxConnections(32);
            connectionPool.setInitialConnections(32);

            DatabaseSchema schema = new DatabaseSchema(connectionPool);

            IdsmConfiguration config = new IdsmConfiguration(null, connectionPool, schema);

            long begin = System.currentTimeMillis();

            request = new Request(config);

            sqlService = Executors.newFixedThreadPool(16);


            /*
             * process config
             */

            Map<IRI, Resource> datatypeMap = new HashMap<IRI, Resource>();
            UserIriClass rc = config.getIriClass("ontology:resource");

            for(DataType datatype : config.getDataTypes())
            {
                IRI iri = datatype.getTypeIri();
                List<Column> cols = rc.toColumns(request.getStatement(), iri);
                datatypeMap.put(iri, new Resource(cols));
            }


            Map<IRI, Graph> graphDefinitions = new HashMap<IRI, Graph>();

            for(QuadMapping map : config.getMappings(config.getServiceIri()))
            {
                ConstantIriMapping graphMapping = map.getGraph();
                IRI iri = graphMapping != null ? graphMapping.getIRI() : null;

                Graph set = graphDefinitions.get(iri);

                if(set == null)
                {
                    set = new Graph();
                    graphDefinitions.put(iri, set);
                }

                set.add(request, map, datatypeMap);
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



            /*
             * translate parts
             */

            Map<IRI, Integer> revGraphs = new HashMap<>();
            int nextGraphID = 0;

            for(GraphIRI k : oldGraphStats.keySet())
            {
                revGraphs.put(k.iri().isEmpty() ? null : new IRI(k.iri()), k.id());

                if(k.id() > nextGraphID)
                    nextGraphID = k.id();
            }

            if(nextGraphID == 0)
                revGraphs.put(null, 0);

            nextGraphID++;



            Map<Integer, IRI> graphs = new HashMap<>();
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

            for(Entry<IRI, Graph> g : graphDefinitions.entrySet())
            {
                Integer graphID = revGraphs.get(g.getKey());

                if(graphID == null)
                    graphID = nextGraphID++;

                graphs.put(graphID, g.getKey());


                Set<Resource> predicatesSet = new HashSet<Resource>();
                predicates.put(graphID, predicatesSet);

                iriGraphs.put(graphID, g.getValue().getIriDataset().translate(request, "S", "O"));
                litGraphs.put(graphID, g.getValue().getLitDataset().translate(request, "S", "O"));

                HashSet<SqlIntercode> graphSubjectDynClasses = new HashSet<SqlIntercode>();
                HashSet<SqlIntercode> graphObjectDynClasses = new HashSet<SqlIntercode>();

                for(Entry<Set<Resource>, Dataset> s : g.getValue().getClasses().entrySet())
                {
                    if(s.getKey().size() > 1)
                    {
                        graphSubjectDynClasses.add(s.getValue().translate(request, "S", "SC"));
                        graphObjectDynClasses.add(s.getValue().translate(request, "O", "OC"));
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

                        subjectClasses.put(new ClassInGraph(graphID, classID), dataset.translate(request, "S", null));
                        objectClasses.put(new ClassInGraph(graphID, classID), dataset.translate(request, "O", null));
                    }
                }

                for(Entry<Resource, Dataset> d : g.getValue().getIriPredicates().entrySet())
                {
                    Resource propertyID = d.getKey();
                    Dataset dataset = d.getValue();

                    predicatesSet.add(propertyID);
                    iriProperties.put(new PropertyInGraph(graphID, propertyID), dataset.translate(request, "S", "O"));
                }


                for(Entry<Resource, Dataset> d : g.getValue().getLitPredicates().entrySet())
                {
                    Resource propertyID = d.getKey();
                    Dataset dataset = d.getValue();

                    predicatesSet.add(propertyID);
                    litProperties.put(new PropertyInGraph(graphID, propertyID), dataset.translate(request, "S", "O"));
                }


                for(Entry<Resource, Map<Resource, Dataset>> d : g.getValue().getDatatypePredicates().entrySet())
                {
                    Resource propertyID = d.getKey();

                    Map<Resource, SqlIntercode> fres = new HashMap<Resource, SqlIntercode>();
                    datatypes.put(new PropertyInGraph(graphID, propertyID), fres);

                    for(Entry<Resource, Dataset> x : d.getValue().entrySet())
                        fres.put(x.getKey(), x.getValue().translate(request, "S", "O"));
                }
            }


            /*
             * compute statistics
             */

            ExecutorService execututor = Executors.newFixedThreadPool(64);
            taskService = new ExecutorCompletionService<Boolean>(execututor);
            SqlNoSolution empty = SqlNoSolution.get();

            Map<String, Future<Map<Void, Long>>> cache0 = new HashMap<String, Future<Map<Void, Long>>>();
            Map<String, Future<Map<Resource, Long>>> cache1 = new HashMap<String, Future<Map<Resource, Long>>>();
            Map<String, Future<Map<ResourcePair, Long>>> cache2 = new HashMap<String, Future<Map<ResourcePair, Long>>>();


            /**********************************************************************************************************/

            /*
             * graph statistics
             *
             *  graph <g1> { ?S ?P ?O }
             */

            for(Entry<Integer, IRI> g : graphs.entrySet())
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
                    SqlIntercode iriPart = SqlJoin.join(sx, iriGraphs.getOrDefault(s.getKey(), empty));
                    SqlIntercode litPart = SqlJoin.join(sx, litGraphs.getOrDefault(s.getKey(), empty));

                    sumbitTask(newClassPartitionStats, cache1, iriPart, litPart, List.of("SC"),
                            r -> new Resource(r.getShort(2), r.getInt(3)), t -> new ClassInGraph(s.getKey(), t), true);
                }
            }

            for(Entry<ClassInGraph, SqlIntercode> s : subjectClasses.entrySet())
            {
                SqlIntercode iriPart = SqlJoin.join(s.getValue(), iriGraphs.getOrDefault(s.getKey().graph(), empty));
                SqlIntercode litPart = SqlJoin.join(s.getValue(), litGraphs.getOrDefault(s.getKey().graph(), empty));

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
                        SqlIntercode iriPart = SqlJoin.join(sx, iriProperties.getOrDefault(key, empty));
                        SqlIntercode litPart = SqlJoin.join(sx, litProperties.getOrDefault(key, empty));

                        sumbitTask(newClassPropertyPartitionStats, cache1, iriPart, litPart, List.of("SC"),
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
                    SqlIntercode iriPart = SqlJoin.join(s.getValue(), iriProperties.getOrDefault(key, empty));
                    SqlIntercode litPart = SqlJoin.join(s.getValue(), litProperties.getOrDefault(key, empty));

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
                        for(SqlIntercode sx : s.getValue())
                        {
                            SqlIntercode sp = SqlJoin.join(sx, px.getValue());

                            sumbitTask(newDatatypeLinksetStats, cache1, empty, sp, List.of("SC"),
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
                        SqlIntercode sp = SqlJoin.join(s.getValue(), px.getValue());

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
                        SqlIntercode sp = SqlJoin.join(sx, p.getValue());

                        if(sp == SqlNoSolution.get())
                            continue;

                        for(Entry<Integer, Set<SqlIntercode>> o : objectDynamicClasses.entrySet())
                        {
                            for(SqlIntercode ox : o.getValue())
                            {
                                SqlIntercode spo = SqlJoin.join(sp, ox);

                                sumbitTask(newClassLinksetStats, cache2, spo, empty, List.of("SC", "OC"),
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
                    SqlIntercode sp = SqlJoin.join(s.getValue(), p.getValue());

                    if(sp == SqlNoSolution.get())
                        continue;

                    for(Entry<Integer, Set<SqlIntercode>> o : objectDynamicClasses.entrySet())
                    {
                        for(SqlIntercode ox : o.getValue())
                        {
                            {
                                SqlIntercode spo = SqlJoin.join(sp, ox);

                                sumbitTask(newClassLinksetStats, cache1, spo, empty, List.of("OC"),
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
                        SqlIntercode sp = SqlJoin.join(sx, p.getValue());

                        if(sp == SqlNoSolution.get())
                            continue;

                        for(Entry<ClassInGraph, SqlIntercode> o : objectClasses.entrySet())
                        {
                            SqlIntercode spo = SqlJoin.join(sp, o.getValue());

                            sumbitTask(newClassLinksetStats, cache1, spo, empty, List.of("SC"),
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
                    SqlIntercode sp = SqlJoin.join(s.getValue(), p.getValue());

                    if(sp == SqlNoSolution.get())
                        continue;

                    for(Entry<ClassInGraph, SqlIntercode> o : objectClasses.entrySet())
                    {
                        SqlIntercode spo = SqlJoin.join(sp, o.getValue());

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
                        .setClasses(newClassPropertyPartitionStats.get(new ClassAndPropertyInGraph(e.getKey().graph(),
                                e.getKey().resource(), new Resource((short) 0, 374))).iriObjects());



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
            e.printStackTrace();
            rollback();
        }
    }


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


    private static <T, K> void sumbitTask(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, List<String> v, ExtractFromResultSet<T> fres,
            Function<T, K> keyget, boolean asumeNotEmpty)
    {
        taskService.submit(() -> computeStats(output, cache, iriImcode, litImcode, v, fres, keyget, asumeNotEmpty));
        taskCount++;
    }


    private static <T, K> void sumbitTask(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, K key, boolean asumeNotEmpty)
    {
        taskService.submit(() -> computeStats(output, cache, iriImcode, litImcode, key, asumeNotEmpty));
        taskCount++;
    }


    private static <T, K> boolean computeStats(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriImcode, SqlIntercode litImcode, K key, boolean asumeNotEmpty)
            throws SQLException, InterruptedException, ExecutionException
    {
        return computeStats(output, cache, iriImcode, litImcode, List.of(), r -> null, t -> key, asumeNotEmpty);
    }


    private static <T, K> boolean computeStats(Map<K, Stats> output, Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode iriPart, SqlIntercode litPart, List<String> v, ExtractFromResultSet<T> fres,
            Function<T, K> keyget, boolean asumeNotEmpty) throws SQLException, InterruptedException, ExecutionException
    {
        try
        {
            SqlIntercode imcode = SqlUnion.union(List.of(iriPart, litPart));

            FutureWrapper<HashMap<K, Long>> ftriples = computeCount(cache, imcode, v, fres, keyget);


            if(asumeNotEmpty || !ftriples.get().isEmpty())
            {
                FutureWrapper<HashMap<K, Long>> flitObjects = computeDistinctCount(cache, litPart, v, fres, keyget,
                        "O");
                FutureWrapper<HashMap<K, Long>> firiObjects = computeDistinctCount(cache, iriPart, v, fres, keyget,
                        "O");
                FutureWrapper<HashMap<K, Long>> fsubjects = computeDistinctCount(cache, imcode, v, fres, keyget, "S");

                HashMap<K, Long> litObjects = flitObjects.get();
                HashMap<K, Long> iriObjects = firiObjects.get();
                HashMap<K, Long> subjects = fsubjects.get();
                HashMap<K, Long> triples = ftriples.get();

                synchronized(output)
                {
                    for(Entry<K, Long> e : triples.entrySet())
                    {
                        Stats stats = new Stats(0, 0, e.getValue(), subjects.get(e.getKey()),
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


    private static <T, K> FutureWrapper<HashMap<K, Long>> computeCount(Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode imcode, List<String> v, ExtractFromResultSet<T> fres, Function<T, K> fkey)
            throws SQLException, InterruptedException, ExecutionException
    {
        HashSet<String> groupBy = new HashSet<String>(v);

        ArrayList<String> vars = new ArrayList<String>();
        vars.add("COUNT");
        vars.addAll(v);

        imcode = imcode.optimize(request, groupBy, false);

        if(imcode == SqlNoSolution.get())
            return () -> new HashMap<K, Long>();


        List<Future<Map<T, Long>>> futures = new ArrayList<>();

        for(SqlIntercode branch : imcode instanceof SqlUnion union ? union.getChilds() : List.of(imcode))
        {
            String sql = getSqlQuery(branch, groupBy, null, vars);
            futures.add(getCountMap(sql, cache, fres));
        }

        return new FutureWrapper<HashMap<K, Long>>()
        {
            boolean isFinished = false;
            HashMap<K, Long> triples = new HashMap<K, Long>();

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


    private static <T, K> FutureWrapper<HashMap<K, Long>> computeDistinctCount(Map<String, Future<Map<T, Long>>> cache,
            SqlIntercode imcode, List<String> v, ExtractFromResultSet<T> fres, Function<T, K> fkey, String what)
            throws SQLException, InterruptedException, ExecutionException
    {
        HashSet<String> groupBy = new HashSet<String>(v);

        ArrayList<String> vars = new ArrayList<String>();
        vars.add("COUNT");
        vars.addAll(v);

        HashSet<String> restrictions = new HashSet<String>(v);
        restrictions.add(what);

        imcode = imcode.optimize(request, restrictions, true);

        if(imcode == SqlNoSolution.get())
            return (() -> new HashMap<K, Long>());


        List<Future<Map<T, Long>>> futures = new ArrayList<>();

        for(SqlIntercode branch : splitImCode(imcode, what))
        {
            String sql = getSqlQuery(branch, groupBy, what, vars);
            futures.add(getCountMap(sql, cache, fres));
        }

        return new FutureWrapper<HashMap<K, Long>>()
        {
            boolean isFinished = false;
            HashMap<K, Long> triples = new HashMap<K, Long>();

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


    private static List<SqlIntercode> splitImCode(SqlIntercode imcode, String what)
    {
        Map<ResourceClass, List<SqlIntercode>> groups = new HashMap<ResourceClass, List<SqlIntercode>>();

        List<SqlIntercode> result = new ArrayList<SqlIntercode>();

        for(SqlIntercode branch : imcode instanceof SqlUnion union ? union.getChilds() : List.of(imcode))
        {
            ResourceClass rc = branch.getVariable(what).getResourceClass();
            ResourceClass gc = rc.getGeneralClass();

            // FIXME
            if(gc == xsdDateTime || gc == xsdDate || gc == rdfLangString)
                rc = gc;

            if(rc instanceof LiteralClass && branch instanceof SqlTableAccess ac && uniques.contains(ac.getTable()))
                result.add(branch);
            else
                groups.computeIfAbsent(rc, k -> new ArrayList<SqlIntercode>()).add(branch);
        }

        for(List<SqlIntercode> g : groups.values())
            result.add(SqlUnion.union(g));

        return result;
    }


    private static <T> Future<Map<T, Long>> getCountMap(String sql, Map<String, Future<Map<T, Long>>> cache,
            ExtractFromResultSet<T> fres) throws SQLException
    {
        synchronized(cache)
        {
            Future<Map<T, Long>> result = cache.get(sql);

            planned++;

            if(result == null)
            {
                executed++;

                Callable<Map<T, Long>> task = () -> {
                    HashMap<T, Long> map = new HashMap<T, Long>();

                    try(Connection connection = request.getConfiguration().getConnectionPool().getConnection())
                    {
                        long begin = System.currentTimeMillis();

                        try(Statement statement = connection.createStatement())
                        {
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

                        if(time > 900_000)
                            System.err.println("\n" + (time / 1000.0) + "\n" + sql + "\n");
                    }

                    return map;
                };

                result = sqlService.submit(task);

                cache.put(sql, result);
            }

            return result;
        }
    }


    private static String getSqlQuery(SqlIntercode imcode, Set<String> gvars, String what, List<String> select)
    {
        HashSet<String> groupVariables = new HashSet<String>(gvars);

        LinkedHashMap<String, SqlExpressionIntercode> aggregations = new LinkedHashMap<>();

        if(what == null || imcode instanceof SqlTableAccess a && uniques.contains(a.getTable()))
        {
            aggregations.put("COUNT", SqlBuiltinCall.create(request, "card", false, List.of()));
        }
        else
        {
            SqlExpressionIntercode var = SqlVariable.create(what, imcode.getVariables());
            aggregations.put("COUNT", SqlBuiltinCall.create(request, "count", true, List.of(var)));
        }

        SqlIntercode agg = SqlAggregation.aggregate(request, groupVariables, aggregations, imcode);

        HashSet<String> restriction = new HashSet<String>(gvars);
        restriction.add("COUNT");

        SqlIntercode result = agg.optimize(request, restriction, false);

        UsedVariables vars = result.getVariables();

        return "SELECT " + select.stream().flatMap(n -> vars.get(n).getMapping().stream()).map(e -> e.toString())
                .collect(Collectors.joining(", ")) + " FROM (" + result.translate(request) + ") as tab";
    }


    private static String getLoadSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<String>();
        cols.addAll(key);
        cols.addAll(value);

        return "select " + cols.stream().collect(joining(", ")) + " from void." + table;
    }


    private static String getDeleteSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<String>();
        cols.addAll(key);
        cols.addAll(value);

        return "delete from void." + table + " where " + cols.stream().map(c -> c + "=?").collect(joining(" and "));
    }


    private static String getUpdateSql(String table, List<String> key, List<String> value)
    {
        List<String> cols = new ArrayList<String>();
        cols.addAll(key);
        cols.addAll(value);

        return "insert into void." + table + "(" + cols.stream().collect(joining(", ")) + ") values("
                + cols.stream().map(c -> "?").collect(joining(",")) + ") " + "on conflict("
                + key.stream().collect(joining(", ")) + ") do update set "
                + value.stream().map(c -> c + "=EXCLUDED." + c).collect(joining(", "));
    }
}
