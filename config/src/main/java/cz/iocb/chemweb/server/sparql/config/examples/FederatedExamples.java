package cz.iocb.chemweb.server.sparql.config.examples;

import static cz.iocb.chemweb.server.sparql.config.examples.ExamplesConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;



public class FederatedExamples
{
    public static void addPrefixes(ExamplesConfiguration config)
    {
        config.addPrefix("sh", "http://www.w3.org/ns/shacl#");
        config.addPrefix("schema", "https://schema.org/");
        config.addPrefix("spex", "https://purl.expasy.org/sparql-examples/ontology#");
    }


    public static void addResourceClasses(ExamplesConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("idsm:federated_example", INT4,
                "https://idsm.elixir-czech.cz/.well-known/federated-sparql-examples/", 6));
    }


    public static void addQuadMappings(ExamplesConfiguration config)
    {
        ConstantIriMapping graph = config
                .createIriMapping(new Iri("https://idsm.elixir-czech.cz/.well-known/federated-sparql-examples"));

        {
            DatabaseTable table = new DatabaseTable(schema, "federated_queries");
            TermMapping subject = config.createIriMapping("idsm:federated_example", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sh:SPARQLExecutable"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sh:SPARQLSelectExecutable"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sh:select"),
                    config.createLiteralMapping(xsdString, "query"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("schema:target"),
                    config.createIriMapping("idsm:endpoint", "target"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "federated_query_targets");
            TermMapping subject = config.createIriMapping("idsm:federated_example", "query");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("spex:federatesWith"),
                    config.createIriMapping("idsm:endpoint", "target"));
        }
    }
}
