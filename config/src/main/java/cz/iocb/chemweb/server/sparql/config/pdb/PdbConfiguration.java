package cz.iocb.chemweb.server.sparql.config.pdb;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the compounds of the PDB Chemical Component Dictionary, which the IDSM endpoint merges into its
 * service.
 */
public class PdbConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration of the PDB compounds.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public PdbConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and those of the PDB compounds to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("pdb-ccd", "https://identifiers.org/pdb-ccd/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    /**
     * Adds the resource classes of the PDB compounds to the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Pdb.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the PDB compounds to the configuration.
     */
    private void addQuadMappings()
    {
        Pdb.addQuadMappings(this);
    }
}
