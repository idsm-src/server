package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class DrugbankSachemConfiguration extends SachemConfiguration
{
    public DrugbankSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "drugbank",
                "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/DB", 5);

        addPrefixes();
    }


    private void addPrefixes()
    {
        addPrefix("drugbankdrugs", "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/");
    }
}
