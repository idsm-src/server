package cz.iocb.chemweb.server.sparql.config.isdb;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class IsdbConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "isdb";


    public IsdbConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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

        Isdb.addPrefixes(this);
    }


    private void addResourceClasses() throws SQLException
    {
        Isdb.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Isdb.addQuadMappings(this);
    }


    private void addFunctions()
    {
        Isdb.addFunctions(this);
    }
}
