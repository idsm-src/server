package cz.iocb.chemweb.server.sparql.config.examples;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class ExamplesConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "info";


    public ExamplesConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Examples.addPrefixes(this);
    }


    private void addResourceClasses() throws SQLException
    {
        Examples.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Examples.addQuadMappings(this);
    }
}
