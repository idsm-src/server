package cz.iocb.chemweb.server.sparql.config.drugbank;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the DrugBank compounds, which the IDSM endpoint merges into its service.
 */
public class DrugBankConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration of the DrugBank compounds.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public DrugBankConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and those of DrugBank to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("drugbank", "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    /**
     * Adds the resource classes of DrugBank to the configuration.
     */
    private void addResourceClasses()
    {
        DrugBank.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of DrugBank to the configuration.
     */
    private void addQuadMappings()
    {
        DrugBank.addQuadMappings(this);
    }
}
