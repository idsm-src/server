package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;



public class Taxonomy
{
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "taxonomies");

        config.addQuadMapping(table, graph, config.createIriMapping("ontology:taxonomy", "tax_id"),
                config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "(organism || ' (Identifiers.org)')::varchar"));
        config.addQuadMapping(table, graph, config.createIriMapping("reference:ncbi-taxonomy", "tax_id"),
                config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "(organism || ' (NCBI Taxonomy)')::varchar"));
    }
}
