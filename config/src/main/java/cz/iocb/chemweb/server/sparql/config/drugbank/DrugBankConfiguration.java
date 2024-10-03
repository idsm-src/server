package cz.iocb.chemweb.server.sparql.config.drugbank;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class DrugBankConfiguration extends SparqlDatabaseConfiguration
{
    public DrugBankConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("drugbank", "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        DrugBank.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        DrugBank.addQuadMappings(this);
    }
}
