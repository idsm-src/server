package cz.iocb.chemweb.server.sparql.config.isdb;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the In Silico DataBase (ISDB) of predicted mass spectra of natural products. It is merged into the
 * IDSM endpoint by {@link cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration}.
 */
public class IsdbConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the ISDB tables.
     */
    static final String schema = "isdb";


    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public IsdbConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addFunctions();
    }


    /**
     * Adds the prefixes of the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Isdb.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Isdb.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        Isdb.addQuadMappings(this);
    }


    /**
     * Adds the functions of the configuration.
     */
    private void addFunctions()
    {
        Isdb.addFunctions(this);
    }
}
