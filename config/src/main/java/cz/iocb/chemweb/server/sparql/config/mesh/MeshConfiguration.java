package cz.iocb.chemweb.server.sparql.config.mesh;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.mapping.classes.LangStringWithTagClass;



/**
 * Configuration of the Medical Subject Headings (MeSH) dataset. It is merged into the IDSM endpoint by
 * {@link cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration}.
 */
public class MeshConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Database schema of the MeSH tables.
     */
    static final String schema = "mesh";

    /**
     * Literal class of the strings in English.
     */
    static final LangStringWithTagClass rdfLangStringEn = LangStringWithTagClass.get("en");


    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public MeshConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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

        addPrefix("meshv", "http://id.nlm.nih.gov/mesh/vocab#");
        addPrefix("mesh", "http://id.nlm.nih.gov/mesh/");
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Mesh.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        Mesh.addQuadMappings(this);
    }
}
