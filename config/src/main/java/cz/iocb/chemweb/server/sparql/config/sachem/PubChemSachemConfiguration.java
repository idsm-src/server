package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class PubChemSachemConfiguration extends SachemConfiguration
{
    public PubChemSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "pubchem", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID", 0);

        addPrefixes();
    }


    private void addPrefixes()
    {
        addPrefix("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/");
    }
}
