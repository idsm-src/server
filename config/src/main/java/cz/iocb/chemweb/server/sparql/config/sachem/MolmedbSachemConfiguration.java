package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class MolmedbSachemConfiguration extends SachemConfiguration
{
    public MolmedbSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "molmedb", "https://identifiers.org/molmedb/MM", -5);

        addPrefixes();
    }


    private void addPrefixes()
    {
        addPrefix("molmedb_molecule", "https://identifiers.org/molmedb/");
    }
}
