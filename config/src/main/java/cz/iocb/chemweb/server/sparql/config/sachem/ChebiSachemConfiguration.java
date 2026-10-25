package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;



/**
 * Configuration of the SACHEM endpoint of the ChEBI compounds.
 */
public class ChebiSachemConfiguration extends SachemConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public ChebiSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "chebi", "compound", "http://purl.obolibrary.org/obo/CHEBI_", 0,
                new DatabaseTable("chebi", "class_molfiles"), "class", new StringSubsetLiteralClass("chebi-molfile"));

        addPrefixes();
    }


    /**
     * Adds the prefixes of the dataset.
     */
    private void addPrefixes()
    {
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
    }
}
