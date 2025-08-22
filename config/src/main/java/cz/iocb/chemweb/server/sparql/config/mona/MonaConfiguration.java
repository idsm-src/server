package cz.iocb.chemweb.server.sparql.config.mona;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class MonaConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    static final String schema = "mona";


    public MonaConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addFunctions();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Mona.addPrefixes(this);
    }


    private void addResourceClasses() throws SQLException
    {
        Mona.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Mona.addQuadMappings(this);
    }


    private void addFunctions()
    {
        Mona.addFunctions(this);
    }
}
