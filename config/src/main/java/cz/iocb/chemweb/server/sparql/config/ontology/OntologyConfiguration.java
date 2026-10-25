package cz.iocb.chemweb.server.sparql.config.ontology;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.mesh.Mesh;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the ontology dataset: the classes, properties, individuals and annotations of the ontologies loaded
 * into the {@code ontology} schema. It is merged into the IDSM endpoint by
 * {@link cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration}.
 */
public class OntologyConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the ontology tables.
     */
    static final String schema = "ontology";


    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public OntologyConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    /**
     * Adds the prefixes of the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("dataset", "http://bioinfo.iocb.cz/dataset/");
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        // the classes of the SKOS mappings to the IRIs of other datasets
        Common.addResourceClasses(this);
        Mesh.addResourceClasses(this);

        Ontology.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        Ontology.addQuadMappings(this);
    }
}
