package cz.iocb.chemweb.server.sparql.config.examples;

import static cz.iocb.chemweb.server.sparql.config.examples.ExamplesConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;



public class Examples
{
    public static void addPrefixes(ExamplesConfiguration config)
    {
        config.addPrefix("sh", "http://www.w3.org/ns/shacl#");
    }


    public static void addResourceClasses(ExamplesConfiguration config)
    {
        config.addIriClass(new StringUserIriClass("info:prefix", "https://idsm.elixir-czech.cz/sparql-prefixes/"));
        config.addIriClass(new IntegerUserIriClass("info:example", "integer",
                "https://idsm.elixir-czech.cz/.well-known/sparql-examples/", 6));
    }


    public static void addQuadMappings(ExamplesConfiguration config)
    {
        ConstantIriMapping graph = config
                .createIriMapping(new IRI("https://idsm.elixir-czech.cz/.well-known/sparql-examples"));

        {
            Table table = new Table(schema, "idsm_queries");
            NodeMapping subject = config.createIriMapping("info:example", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sh:SPARQLExecutable"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sh:select"),
                    config.createLiteralMapping(xsdString, "query"));
        }
    }
}
