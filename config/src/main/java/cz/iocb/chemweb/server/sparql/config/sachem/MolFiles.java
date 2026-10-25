package cz.iocb.chemweb.server.sparql.config.sachem;

import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



/**
 * Mapping of the molfiles of the compounds of a SACHEM endpoint as SIO attributes of the compounds.
 */
public class MolFiles
{
    /**
     * Not instantiable.
     */
    private MolFiles()
    {
    }


    /**
     * Adds the prefixes of the molfile mappings to the configuration.
     *
     * @param config the configuration
     */
    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("sio", "http://semanticscience.org/resource/");
    }


    /**
     * Adds the quad mappings of the molfiles of the compounds to the configuration.
     *
     * @param config the configuration
     * @param compoundClass the name of the IRI class of the compounds
     * @param molfileClass the name of the IRI class of the molfiles
     * @param table the table of the molfiles
     * @param compoundFields the columns of the compounds in the table
     * @param id the column of the molfile ids
     * @param molfile the column of the molfiles
     * @param molfileLiteralClass the literal class of the molfiles
     */
    public static void addQuadMappings(SparqlDatabaseConfiguration config, String compoundClass, String molfileClass,
            DatabaseTable table, List<Column> compoundFields, String id, String molfile,
            LiteralClass molfileLiteralClass)
    {
        TermMapping subject = config.createIriMapping(molfileClass, id);

        config.addQuadMapping(table, null, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("sio:SIO_011120"));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:SIO_000011"),
                config.createIriMapping(compoundClass, compoundFields));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:SIO_000300"),
                config.createLiteralMapping(molfileLiteralClass, molfile));

        // extension
        config.addQuadMapping(table, null, config.createIriMapping(compoundClass, compoundFields),
                config.createIriMapping("sio:SIO_000008"), subject);

        // deprecated
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:is-attribute-of"),
                config.createIriMapping(compoundClass, compoundFields));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:has-value"),
                config.createLiteralMapping(molfileLiteralClass, molfile));
    }
}
