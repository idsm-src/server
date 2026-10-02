package cz.iocb.chemweb.server.sparql.config.examples;

import static cz.iocb.chemweb.server.sparql.config.examples.ExamplesConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;
import cz.iocb.sparql.engine.rdf.Iri;



public class Examples
{
    public static void addPrefixes(ExamplesConfiguration config)
    {
        config.addPrefix("sh", "http://www.w3.org/ns/shacl#");
    }


    public static void addResourceClasses(ExamplesConfiguration config)
    {
        config.addIriClass(new StringUserIriClass("info:prefix", "https://idsm.elixir-czech.cz/sparql-prefixes/"));
        config.addIriClass(new IntegerUserIriClass("info:example", INT4,
                "https://idsm.elixir-czech.cz/.well-known/sparql-examples/", 6));
    }


    public static void addQuadMappings(ExamplesConfiguration config)
    {
        ConstantIriMapping graph = config
                .createIriMapping(new Iri("https://idsm.elixir-czech.cz/.well-known/sparql-examples"));

        {
            DatabaseTable table = new DatabaseTable(schema, "idsm_queries");
            TermMapping subject = config.createIriMapping("info:example", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sh:SPARQLExecutable"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sh:select"),
                    config.createLiteralMapping(xsdString, "query"));
        }
    }
}
