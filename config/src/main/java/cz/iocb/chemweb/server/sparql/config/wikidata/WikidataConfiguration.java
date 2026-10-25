package cz.iocb.chemweb.server.sparql.config.wikidata;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the Wikidata compounds, which the IDSM endpoint merges into its service.
 */
public class WikidataConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration of the Wikidata compounds.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public WikidataConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and the Wikidata prefix to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("wikidata", "http://www.wikidata.org/entity/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    /**
     * Adds the resource classes of the Wikidata compounds to the configuration.
     */
    private void addResourceClasses()
    {
        Wikidata.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the Wikidata compounds to the configuration.
     */
    private void addQuadMappings()
    {
        Wikidata.addQuadMappings(this);
    }
}
