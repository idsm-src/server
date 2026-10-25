package cz.iocb.chemweb.server.sparql.config.examples;

import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.ListUserIriClass;



/**
 * Configuration of the SPARQL examples of IDSM, which the IDSM endpoint merges into its service.
 */
public class ExamplesConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the tables of the examples.
     */
    static final String schema = "idsm";


    /**
     * Creates the configuration of the examples.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public ExamplesConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and those of the examples to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Examples.addPrefixes(this);
        FederatedExamples.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the examples and of the endpoints they target to the configuration.
     */
    private void addResourceClasses()
    {
        addIriClass(new ListUserIriClass("idsm:endpoint", new DatabaseTable(schema, "sparql_endpoints"),
                new TableColumn("iri", VARCHAR)));

        Examples.addResourceClasses(this);
        FederatedExamples.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the examples to the configuration.
     */
    private void addQuadMappings()
    {
        Examples.addQuadMappings(this);
        FederatedExamples.addQuadMappings(this);
    }
}
