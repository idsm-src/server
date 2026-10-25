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



/**
 * Mapping of the SPARQL examples of IDSM (table {@code idsm.queries}), described by SHACL as executable SELECT queries
 * in the graph {@code https://idsm.elixir-czech.cz/.well-known/sparql-examples}.
 */
public class Examples
{
    /**
     * Not instantiable.
     */
    private Examples()
    {
    }


    /**
     * Adds the prefixes of SHACL and schema.org to the configuration.
     *
     * @param config the configuration
     */
    public static void addPrefixes(ExamplesConfiguration config)
    {
        config.addPrefix("sh", "http://www.w3.org/ns/shacl#");
        config.addPrefix("schema", "https://schema.org/");
    }


    /**
     * Adds the resource classes of the examples and of the prefix declarations to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(ExamplesConfiguration config)
    {
        config.addIriClass(new StringUserIriClass("idsm:prefix", "https://idsm.elixir-czech.cz/sparql-prefixes/"));
        config.addIriClass(new IntegerUserIriClass("idsm:example", INT4,
                "https://idsm.elixir-czech.cz/.well-known/sparql-examples/", 6));
    }


    /**
     * Adds the quad mappings of the examples to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(ExamplesConfiguration config)
    {
        ConstantIriMapping graph = config
                .createIriMapping(new Iri("https://idsm.elixir-czech.cz/.well-known/sparql-examples"));

        {
            DatabaseTable table = new DatabaseTable(schema, "queries");
            TermMapping subject = config.createIriMapping("idsm:example", "id");

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
    }
}
