package cz.iocb.chemweb.server.sparql.config.stats;

import static cz.iocb.chemweb.server.sparql.config.stats.VoidConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdLong;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.DateTimeConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;



public class Void
{
    public static void addPrefixes(VoidConfiguration config)
    {
        config.addPrefix("void", "http://rdfs.org/ns/void#");
        config.addPrefix("dcterms", "http://purl.org/dc/terms/");
        config.addPrefix("void_ext", "http://ldf.fi/void-ext#");
    }


    public static void addResourceClasses(VoidConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("sd:graph", "integer", new Table(schema, "graphs"),
                new TableColumn("id"), new TableColumn("iri"), null));

        //config.addIriClass(new VoidResource("void:named-graph", "http://void/named-graph-", List.of("integer")));

        config.addIriClass(new VoidResource("void:graph", "http://void/graph-", List.of("integer")));

        config.addIriClass(new VoidResource("void:class-partition", "http://void/class-partition-",
                List.of("integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:property-partition", "http://void/property-partition-",
                List.of("integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:class-property-partition", "http://void/class-property-partition-",
                List.of("integer", "smallint", "integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:linkset", "http://void/linkset-", List.of("integer", "smallint",
                "integer", "integer", "smallint", "integer", "integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:class-property-datatype-partition",
                "http://void/class-property-datatype-partition-",
                List.of("integer", "smallint", "integer", "smallint", "integer", "smallint", "integer")));
    }


    public static void addQuadMappings(VoidConfiguration config)
    {
        String endpoint = "https://idsm.elixir-czech.cz/sparql/endpoint/idsm";
        ConstantIriMapping service = config.createIriMapping(new IRI(endpoint));
        ConstantIriMapping graph = config.createIriMapping(new IRI("https://idsm.elixir-czech.cz/.well-known/void"));
        ConstantIriMapping defaultDataset = config.createIriMapping(new IRI(endpoint + "#default-dataset"));
        ConstantIriMapping availableGraphs = config.createIriMapping(new IRI(endpoint + "#available-graphs"));
        ConstantIriMapping defaultGraph = config.createIriMapping(new IRI("http://void/graph-00000000"));

        {
            config.addQuadMapping(graph, service, config.createIriMapping("sd:availableGraphs"), availableGraphs);
            config.addQuadMapping(graph, availableGraphs, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:GraphCollection"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:defaultDataset"), defaultDataset);
            config.addQuadMapping(graph, defaultDataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:Dataset"));
        }

        {
            Table table = new Table("info", "idsm_version");

            DateTimeConstantZoneClass xsdDateTimeM0 = DateTimeConstantZoneClass.get(0);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00'");
            String date = ZonedDateTime.now(ZoneOffset.UTC).format(formatter);
            String version = "(greatest('" + date + "'::timestamptz, date))";

            config.addQuadMapping(table, graph, defaultDataset, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, version));

            config.addQuadMapping(table, graph, defaultGraph, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, version));
        }

        {
            Table table = new Table(schema, "graphs");

            NodeMapping named = config.createIriMapping("sd:graph" /*"void:named-graph"*/, "id");

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
            Table table = new Table(schema, "graphs");

            NodeMapping dataset = config.createIriMapping("void:graph", "id");

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
            Table table = new Table(schema, "class_partitions");
            Conditions cnd = config.createAreNotEqualCondition("class_unit", Ontology.unitCHEBI, Ontology.unitTaxonomy);

            NodeMapping dataset = config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id");

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
            Table table = new Table(schema, "property_partitions");

            NodeMapping dataset = config.createIriMapping("void:property-partition", "graph", "property_unit",
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
            Table table = new Table(schema, "class_property_partitions");
            Conditions cnd = config.createAreNotEqualCondition("class_unit", Ontology.unitCHEBI, Ontology.unitTaxonomy);

            NodeMapping dataset = config.createIriMapping("void:class-property-partition", "graph", "class_unit",
                    "class_id", "property_unit", "property_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:property-partition", "graph", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id"),
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
            Table table = new Table(schema, "linksets");
            Conditions cnds = config.createAreNotEqualCondition("subject_unit", Ontology.unitCHEBI,
                    Ontology.unitTaxonomy);
            Conditions cndo = config.createAreNotEqualCondition("object_unit", Ontology.unitCHEBI,
                    Ontology.unitTaxonomy);
            Conditions cnd = Conditions.and(cnds, cndo);

            NodeMapping dataset = config.createIriMapping("void:linkset", "property_graph", "property_unit",
                    "property_id", "subject_graph", "subject_unit", "subject_id", "object_graph", "object_unit",
                    "object_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Linkset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "property_graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:property-partition", "property_graph",
                    "property_unit", "property_id"), config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"),
                    config.createIriMapping("void:subset"), dataset,
                    Conditions.and(cnd, config.createAreEqualCondition("subject_graph", "property_graph")));

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-property-partition", "property_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset,
                    Conditions.and(cnd, config.createAreEqualCondition("subject_graph", "property_graph")));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"),
                    cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:subjectsTarget"),
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"),
                    cnd);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class-partition", "object_graph", "object_unit", "object_id"),
                    Conditions.and(config.createAreNotEqualCondition("subject_graph", "object_graph"),
                            config.createAreNotEqualCondition("subject_unit", "object_unit"),
                            config.createAreNotEqualCondition("subject_id", "object_id"), cnd));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:objectsTarget"),
                    config.createIriMapping("void:class-partition", "object_graph", "object_unit", "object_id"), cnd);

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
            Table table = new Table(schema, "literal_linksets");
            Conditions cnd = Conditions.and(
                    config.createAreNotEqualCondition("subject_unit", Ontology.unitCHEBI, Ontology.unitTaxonomy),
                    config.createAreEqualCondition("subject_graph", "property_graph"));

            NodeMapping dataset = config.createIriMapping("void:class-property-datatype-partition", "subject_graph",
                    "subject_unit", "subject_id", "property_unit", "property_id", "datatype_unit", "datatype_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"), cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "subject_graph"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph, config.createIriMapping("void:property-partition", "property_graph",
                    "property_unit", "property_id"), config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-property-partition", "subject_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset, cnd);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-property-partition", "subject_graph", "subject_unit",
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
