package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



/**
 * Mapping of the version descriptors of the PubChem substances (graph {@code descriptor:substance}).
 */
public class SubstanceDescriptor
{
    /**
     * Not instantiable.
     */
    private SubstanceDescriptor()
    {
    }


    /**
     * Adds the resource classes of the PubChem substance versions to the configuration.
     *
     * @param config the configuration
     */
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID";

        config.addIriClass(new IntegerUserIriClass("pubchem:substance_version", INT4, prefix, "_Substance_Version"));
    }


    /**
     * Adds the quad mappings of the PubChem substance versions to the configuration.
     *
     * @param config the configuration
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("descriptor:substance");

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_versions");
            TermMapping subject = config.createIriMapping("pubchem:substance_version", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:SubstanceVersion"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:IAO_0000129"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdInteger, "version"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdInteger, "version"));
        }
    }
}
