package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class ChebiSachemConfiguration extends SachemConfiguration
{
    public ChebiSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "chebi", "http://purl.obolibrary.org/obo/CHEBI_", 0,
                new StringSubsetLiteralClass("chebi-molfile"));

        addPrefixes();
    }


    private void addPrefixes()
    {
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
    }
}
