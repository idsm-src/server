package cz.iocb.chemweb.server.sparql.config.stats;

import static cz.iocb.chemweb.server.sparql.config.stats.VoidConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdLong;
import java.util.List;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.DateTimeConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.mapping.extension.FunctionDefinition;
import cz.iocb.sparql.engine.mapping.extension.ProcedureDefinition;
import cz.iocb.sparql.engine.parser.model.IRI;



public class Void
{
    public static void addPrefixes(VoidConfiguration config)
    {
        config.addPrefix("void", "http://rdfs.org/ns/void#");
        config.addPrefix("dcterms", "http://purl.org/dc/terms/");
    }


    public static void addResourceClasses(VoidConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("sd:graph", "integer", new Table(schema, "graphs"),
                new TableColumn("id"), new TableColumn("iri"), null));

        config.addIriClass(new VoidResource("void:named-graph", "http://void/named-graph-", List.of("integer")));

        config.addIriClass(new VoidResource("void:graph", "http://void/graph-", List.of("integer")));

        config.addIriClass(new VoidResource("void:class-partition", "http://void/class-partition-",
                List.of("integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:property-partition", "http://void/property-partition-",
                List.of("integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:class-property-partition", "http://void/class-property-partition-",
                List.of("integer", "smallint", "integer", "smallint", "integer")));

        config.addIriClass(new VoidResource("void:linkset", "http://void/linkset-", List.of("integer", "smallint",
                "integer", "integer", "smallint", "integer", "integer", "smallint", "integer")));
    }


    public static void addQuadMappings(VoidConfiguration config)
    {
        String endpoint = "https://idsm.elixir-czech.cz/sparql/endpoint/idsm";
        ConstantIriMapping graph = config.createIriMapping(new IRI("https://idsm.elixir-czech.cz/.well-known/void"));
        ConstantIriMapping service = config.createIriMapping(new IRI(endpoint));
        ConstantIriMapping defaultDataset = config.createIriMapping(new IRI(endpoint + "#default-dataset"));
        ConstantIriMapping availableGraphs = config.createIriMapping(new IRI(endpoint + "#available-graphs"));
        ConstantIriMapping defaultGraph = config.createIriMapping(new IRI("http://void/graph-00000000"));

        {
            Table table = new Table("info", "idsm_version");

            DateTimeConstantZoneClass xsdDateTimeM0 = DateTimeConstantZoneClass.get(0);

            config.addQuadMapping(table, graph, defaultDataset, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, "date"));

            config.addQuadMapping(table, graph, defaultGraph, config.createIriMapping("dcterms:issued"),
                    config.createLiteralMapping(xsdDateTimeM0, "date"));
        }

        {
            config.addQuadMapping(graph, service, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:Service"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:endpoint"),
                    config.createIriMapping(new IRI(endpoint)));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:feature"),
                    config.createIriMapping("sd:BasicFederatedQuery"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:feature"),
                    config.createIriMapping("sd:EmptyGraphs"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:feature"),
                    config.createIriMapping("sd:UnionDefaultGraph"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:defaultEntailmentRegime"),
                    config.createIriMapping("ent:Simple"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:supportedLanguage"),
                    config.createIriMapping("sd:SPARQL11Query"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:SPARQL_Results_XML"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:SPARQL_Results_JSON"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:SPARQL_Results_CSV"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:SPARQL_Results_TSV"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:RDF_JSON"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:RDF_XML"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:Turtle"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:TriG"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:N-Triples"));
            config.addQuadMapping(graph, service, config.createIriMapping("sd:resultFormat"),
                    config.createIriMapping("format:N-Quads"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:defaultDataset"), defaultDataset);
            config.addQuadMapping(graph, defaultDataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:Dataset"));

            config.addQuadMapping(graph, service, config.createIriMapping("sd:availableGraphs"), availableGraphs);
            config.addQuadMapping(graph, availableGraphs, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sd:GraphCollection"));

            for(FunctionDefinition def : config.getFunctions(config.getServiceIri()).values())
            {
                ConstantIriMapping function = config.createIriMapping(new IRI(def.getFunctionName()));
                config.addQuadMapping(graph, service, config.createIriMapping("sd:extensionFunction"), function);
                config.addQuadMapping(graph, function, config.createIriMapping("rdf:type"),
                        config.createIriMapping("sd:Function"));
            }

            for(ProcedureDefinition def : config.getProcedures(config.getServiceIri()).values())
            {
                ConstantIriMapping procedure = config.createIriMapping(new IRI(def.getProcedureName()));
                config.addQuadMapping(graph, service, config.createIriMapping("sd:propertyFeature"), procedure);
                config.addQuadMapping(graph, procedure, config.createIriMapping("rdf:type"),
                        config.createIriMapping("sd:Feature"));
            }
        }

        {
            Table table = new Table(schema, "graphs");

            NodeMapping named = config.createIriMapping("void:named-graph", "id");
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
        }

        {
            Table table = new Table(schema, "class_partitions");

            NodeMapping dataset = config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"));

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:classPartition"), dataset);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:class"),
                    config.createIriMapping("ontology:resource", "class_unit", "class_id"));

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
        }

        {
            Table table = new Table(schema, "class_property_partitions");

            NodeMapping dataset = config.createIriMapping("void:class-property-partition", "graph", "class_unit",
                    "class_id", "property_unit", "property_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"));

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "graph"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:property-partition", "graph", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-partition", "graph", "class_unit", "class_id"),
                    config.createIriMapping("void:propertyPartition"), dataset);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:property"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"));
        }

        {
            Table table = new Table(schema, "linksets");

            NodeMapping dataset = config.createIriMapping("void:linkset", "property_graph", "property_unit",
                    "property_id", "subject_graph", "subject_unit", "subject_id", "object_graph", "object_unit",
                    "object_id");

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Dataset"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("rdf:type"),
                    config.createIriMapping("void:Linkset"));

            config.addQuadMapping(table, graph, config.createIriMapping("void:graph", "property_graph"),
                    config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph,
                    config.createIriMapping("void:class-property-partition", "property_graph", "subject_unit",
                            "subject_id", "property_unit", "property_id"),
                    config.createIriMapping("void:subset"), dataset,
                    config.createAreEqualCondition("subject_graph", "property_graph"));

            config.addQuadMapping(table, graph, config.createIriMapping("void:property-partition", "property_graph",
                    "property_unit", "property_id"), config.createIriMapping("void:subset"), dataset);

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:subjectsTarget"),
                    config.createIriMapping("void:class-partition", "subject_graph", "subject_unit", "subject_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:target"),
                    config.createIriMapping("void:class-partition", "object_graph", "object_unit", "object_id"),
                    Conditions.and(config.createAreNotEqualCondition("subject_graph", "object_graph"),
                            config.createAreNotEqualCondition("subject_unit", "object_unit"),
                            config.createAreNotEqualCondition("subject_id", "object_id")));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:objectsTarget"),
                    config.createIriMapping("void:class-partition", "object_graph", "object_unit", "object_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:linkPredicate"),
                    config.createIriMapping("ontology:resource", "property_unit", "property_id"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:triples"),
                    config.createLiteralMapping(xsdLong, "triples"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctSubjects"),
                    config.createLiteralMapping(xsdLong, "subjects"));

            config.addQuadMapping(table, graph, dataset, config.createIriMapping("void:distinctObjects"),
                    config.createLiteralMapping(xsdLong, "objects"));
        }
    }
}
