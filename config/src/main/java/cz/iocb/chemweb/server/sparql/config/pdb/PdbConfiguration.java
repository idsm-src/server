package cz.iocb.chemweb.server.sparql.config.pdb;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class PdbConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public PdbConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("pdb-ccd", "https://identifiers.org/pdb-ccd/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Pdb.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Pdb.addQuadMappings(this);
    }
}
