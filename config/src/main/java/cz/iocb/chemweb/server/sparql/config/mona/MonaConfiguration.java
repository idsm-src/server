package cz.iocb.chemweb.server.sparql.config.mona;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the MassBank of North America (MoNA) dataset of mass spectra. It is merged into the IDSM endpoint by
 * {@link cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration}.
 */
public class MonaConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the MoNA tables.
     */
    static final String schema = "mona";


    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public MonaConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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

        Mona.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Mona.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        Mona.addQuadMappings(this);
    }


    /**
     * Adds the functions of the configuration.
     */
    private void addFunctions()
    {
        Mona.addFunctions(this);
    }
}
