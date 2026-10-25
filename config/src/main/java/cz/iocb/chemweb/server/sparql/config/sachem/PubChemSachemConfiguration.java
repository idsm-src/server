package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;



/**
 * Configuration of the SACHEM endpoint of the PubChem compounds.
 */
public class PubChemSachemConfiguration extends SachemConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public PubChemSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "pubchem", "compound",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID", 0,
                new DatabaseTable("pubchem", "compound_molfiles"), "compound",
                new StringSubsetLiteralClass("pubchem-molfile"));

        addPrefixes();
    }


    /**
     * Adds the prefixes of the dataset.
     */
    private void addPrefixes()
    {
        addPrefix("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/");
    }
}
