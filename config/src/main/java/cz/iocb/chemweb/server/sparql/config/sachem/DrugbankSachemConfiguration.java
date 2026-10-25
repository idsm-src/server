package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;



/**
 * Configuration of the SACHEM endpoint of the DrugBank compounds.
 */
public class DrugbankSachemConfiguration extends SachemConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public DrugbankSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "drugbank", "compound",
                "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/DB", 5,
                new DatabaseTable("drugbank", "compounds"), "id", new StringSubsetLiteralClass("drugbank-molfile"));

        addPrefixes();
    }


    /**
     * Adds the prefixes of the dataset.
     */
    private void addPrefixes()
    {
        addPrefix("drugbankdrugs", "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/");
    }
}
