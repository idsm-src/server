package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class ChemblSachemConfiguration extends SachemConfiguration
{
    public ChemblSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "chembl", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", 0);

        addPrefixes();
    }


    private void addPrefixes()
    {
        addPrefix("chembl_molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/");
    }
}
