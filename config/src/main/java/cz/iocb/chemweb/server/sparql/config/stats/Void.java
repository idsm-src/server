package cz.iocb.chemweb.server.sparql.config.stats;

import static cz.iocb.chemweb.server.sparql.config.stats.VoidConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdLong;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.DateTimeInZoneClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;



public class Void
{
    private static final String unitCHEBI = "'" + OntologyResource.unitCHEBI + "'::smallint";
    private static final String unitTaxonomy = "'" + OntologyResource.unitTaxonomy + "'::smallint";


    public static void addPrefixes(VoidConfiguration config)
    {
        config.addPrefix("void", "http://rdfs.org/ns/void#");
        config.addPrefix("dcterms", "http://purl.org/dc/terms/");
        config.addPrefix("void_ext", "http://ldf.fi/void-ext#");
    }


    public static void addResourceClasses(VoidConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("sd:graph", INT4, new DatabaseTable(schema, "graphs"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR), null));

        //config.addIriClass(new VoidResource("void:named_graph", "http://void/named-graph-", List.of(INT4)));

        config.addIriClass(new VoidResource("void:graph", "http://void/graph-", List.of(INT4)));

        config.addIriClass(
                new VoidResource("void:class_partition", "http://void/class-partition-", List.of(INT4, INT2, INT4)));

        config.addIriClass(new VoidResource("void:property_partition", "http://void/property-partition-",
                List.of(INT4, INT2, INT4)));

        config.addIriClass(new VoidResource("void:class_property_partition", "http://void/class-property-partition-",
                List.of(INT4, INT2, INT4, INT2, INT4)));

        config.addIriClass(new VoidResource("void:linkset", "http://void/linkset-",
                List.of(INT4, INT2, INT4, INT4, INT2, INT4, INT4, INT2, INT4)));

        config.addIriClass(new VoidResource("void:class_property_datatype_partition",
                "http://void/class-property-datatype-partition-", List.of(INT4, INT2, INT4, INT2, INT4, INT2, INT4)));
    }


    public static void addQuadMappings(VoidConfiguration config)
    {
        String endpoint = "https://idsm.elixir-czech.cz/sparql/endpoint/idsm";
        ConstantIriMapping service = config.createIriMapping(new Iri(endpoint));
        ConstantIriMapping graph = config.createIriMapping(new Iri("https://idsm.elixir-czech.cz/.well-known/void"));
        ConstantIriMapping defaultDataset = config.createIriMapping(new Iri(endpoint + "#default-dataset"));
        ConstantIriMapping availableGraphs = config.createIriMapping(new Iri(endpoint + "#available-graphs"));
        ConstantIriMapping defaultGraph = config.createIriMapping(new Iri("http://void/graph-00000000"));

        {
            config.addQuadMapping(graph, service, config.createIriMapping("sd:availableGraphs"), availableGraphs);
            config.addQuadMapping(graph, availableGraphs, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:GraphCollection"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:defaultDataset"), defaultDataset);
            config.addQuadMapping(graph, defaultDataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:Dataset"));
        }

        {
            DatabaseTable table = new DatabaseTable("idsm", "version");

            DateTimeInZoneClass xsdDateTimeM0 = DateTimeInZoneClass.get(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00'");
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
            String version = "(greatest('" + date + "'::timestamptz, date))::timestamptz";

            config.addQuadMapping(table, graph, defaultDataset, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, version));

            config.addQuadMapping(table, graph, defaultGraph, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, version));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "graphs");

            TermMapping named = config.createIriMapping("sd:graph" /*"void:named_graph"*/, "id");

            Conditions condition = config.createAreNotEqualCondition("id", "'0'::integer");

            config.addQuadMapping(graph, defaultDataset, config.createIriMapping("sd:defaultGraph"), defaultGraph);

            config.addQuadMapping(table, graph, defaultDataset, config.createIriMapping("sd:namedGraph"), named,
                    condition);

            config.addQuadMapping(table, graph, availableGraphs, config.createIriMapping("sd:namedGraph"), named,
                    condition);

            config.addQuadMapping(table, graph, named, config.createIriMapping("sd:name"),
                    config.createIriMapping("sd:graph", "id"), condition);

            config.addQuadMapping(table, graph, named, config.createIriMapping("sd:graph"),
                    config.createIriMapping("void:graph", "id"), condition);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "graphs");

            TermMapping dataset = config.createIriMapping("void:graph", "id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:Graph"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:classes"),
                    config.createLiteralMapping(xsdLong, "classes"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:properties"),
                    config.createLiteralMapping(xsdLong, "properties"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"));

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"),
                    config.createLiteralMapping(xsdLong, "iri_objects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(xsdLong, "literal_objects"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_partitions");
            Conditions cnd = config.createAreNotEqualCondition("class_unit", unitCHEBI, unitTaxonomy);

            TermMapping dataset = config.createIriMapping("void:class_partition", "graph", "class_unit", "class_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:classPartition"), dataset, cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:class"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:classes"),
                    config.createLiteralMapping(xsdLong, "classes"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:properties"),
                    config.createLiteralMapping(xsdLong, "properties"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"),
                    config.createLiteralMapping(xsdLong, "iri_objects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(xsdLong, "literal_objects"), cnd);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "property_partitions");

            TermMapping dataset = config.createIriMapping("void:property_partition", "graph", "property_unit",
                    "property_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"));

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:propertyPartition"), dataset);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:property"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"));

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"),
                    config.createLiteralMapping(xsdLong, "iri_objects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(xsdLong, "literal_objects"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "class_property_partitions");
            Conditions cnd = config.createAreNotEqualCondition("class_unit", unitCHEBI, unitTaxonomy);

            TermMapping dataset = config.createIriMapping("void:class_property_partition", "graph", "class_unit",
                    "class_id", "property_unit", "property_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:property_partition", "graph", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_partition", "graph", "class_unit", "class_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_partition", "graph", "class_unit", "class_id"),
                    config.createIriMapping("void:propertyPartition"), dataset, cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:property"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"),
                    config.createLiteralMapping(xsdLong, "iri_objects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(xsdLong, "literal_objects"), cnd);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "linksets");
            Conditions cnds = config.createAreNotEqualCondition("subject_unit", unitCHEBI, unitTaxonomy);
            Conditions cndo = config.createAreNotEqualCondition("object_unit", unitCHEBI, unitTaxonomy);
            Conditions cnd = Conditions.and(cnds, cndo);

            TermMapping dataset = config.createIriMapping("void:linkset", "property_graph", "property_unit",
                    "property_id", "subject_graph", "subject_unit", "subject_id", "object_graph", "object_unit",
                    "object_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Linkset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "property_graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:property_partition", "property_graph",
                    "property_unit", "property_id"), config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_partition", "subject_graph", "subject_unit", "subject_id"),
                    config.createIriMapping("void:subset"), dataset,
                    Conditions.and(cnd, config.createAreEqualCondition(table, "subject_graph", "property_graph")));

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_property_partition", "property_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset,
                    Conditions.and(cnd, config.createAreEqualCondition(table, "subject_graph", "property_graph")));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class_partition", "subject_graph", "subject_unit", "subject_id"),
                    cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:subjectsTarget"),
                    config.createIriMapping("void:class_partition", "subject_graph", "subject_unit", "subject_id"),
                    cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class_partition", "object_graph", "object_unit", "object_id"),
                    Conditions.and(config.createAreNotEqualCondition(table, "subject_graph", "object_graph"),
                            config.createAreNotEqualCondition(table, "subject_unit", "object_unit"),
                            config.createAreNotEqualCondition(table, "subject_id", "object_id"), cnd));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:objectsTarget"),
                    config.createIriMapping("void:class_partition", "object_graph", "object_unit", "object_id"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:linkPredicate"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(0l), cnd);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "literal_linksets");
            Conditions cnd = Conditions.and(config.createAreNotEqualCondition("subject_unit", unitCHEBI, unitTaxonomy),
                    config.createAreEqualCondition(table, "subject_graph", "property_graph"));

            TermMapping dataset = config.createIriMapping("void:class_property_datatype_partition", "subject_graph",
                    "subject_unit", "subject_id", "property_unit", "property_id", "datatype_unit", "datatype_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "subject_graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:property_partition", "property_graph",
                    "property_unit", "property_id"), config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_partition", "subject_graph", "subject_unit", "subject_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_property_partition", "subject_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class_property_partition", "subject_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void_ext:datatypePartition"), dataset, cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:datatype"),
                    config.createIriMapping("ontology:resource", "datatype_unit", "datatype_id"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"), cnd);

            config.addQuadMapping(table, graph, dataset,
                    config.createIriMapping("void_ext:distinctIRIReferenceObjects"), config.createLiteralMapping(0l),
                    cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void_ext:distinctLiterals"),
                    config.createLiteralMapping(xsdLong, "objects"), cnd);
        }
    }
}
