package cz.iocb.chemweb.server.sparql.config.chebi;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the ChEBI ontology, which the IDSM endpoint merges into its service.
 */
public class ChebiConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the ChEBI tables.
     */
    static final String schema = "chebi";


    /**
     * Creates the configuration of the ChEBI ontology.
     *
     * @param service IRI of the service, or null
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public ChebiConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the common prefixes and those of ChEBI to the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("ebi", "http://rdf.ebi.ac.uk/dataset/");
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("chebi", "http://purl.obolibrary.org/obo/chebi/");
        addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");
        addPrefix("chemrof", "https://w3id.org/chemrof/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    /**
     * Adds the resource classes of the ontology resources and of ChEBI to the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Chebi.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of ChEBI to the configuration.
     */
    private void addQuadMappings()
    {
        Chebi.addQuadMappings(this);
    }
}
