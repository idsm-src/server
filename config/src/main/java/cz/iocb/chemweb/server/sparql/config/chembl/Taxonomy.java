package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;



public class Taxonomy
{
    private static String taxonomyReferenceType = schema + ".taxonomy_reference_type";


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "taxonomy_labels");

        config.addQuadMapping(table, graph, config.createIriMapping("ontology:taxonomy", "taxonomy"),
                config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                config.createAreEqualCondition("type", "'IDENTIFIERS.ORG'::" + taxonomyReferenceType));
        config.addQuadMapping(table, graph, config.createIriMapping("reference:ncbi_taxonomy", "taxonomy"),
                config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                config.createAreEqualCondition("type", "'NCBI TAXONOMY'::" + taxonomyReferenceType));
    }
}
