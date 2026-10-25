package cz.iocb.chemweb.server.sparql.config.stats;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the VoID statistics of the IDSM endpoint, which the endpoint merges into its service and uses as
 * its service description.
 */
public class VoidConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the statistics.
     */
    static final String schema = "void";


    /**
     * Creates the configuration of the statistics.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public VoidConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and those of the statistics to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Void.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the ontology resources and of the statistics to the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Void.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the statistics to the configuration.
     */
    private void addQuadMappings()
    {
        Void.addQuadMappings(this);
    }
}
