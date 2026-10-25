package cz.iocb.chemweb.server.sparql.config.molmedb;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.chembl.Molecule;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.config.pubchem.Compound;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the MolMeDB dataset of the interactions of molecules with membranes and transporters, with the
 * SACHEM search over its substances. It serves the MolMeDB endpoint and is merged into the IDSM endpoint by
 * {@link cz.iocb.chemweb.server.sparql.config.idsm.IdsmConfiguration}.
 */
public class MolmedbConfiguration extends SparqlDatabaseConfiguration
{
    /**
     * Database schema of the MolMeDB tables.
     */
    static final String schema = "molmedb";


    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public MolmedbConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addProcedures();
    }


    /**
     * Adds the prefixes of the configuration.
     */
    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("molmedb", "https://identifiers.org/molmedb/");
        addPrefix("mmdbvoc", "https://rdf.molmedb.upol.cz/vocabulary#");

        addPrefix("bao", "http://www.bioassayontology.org/bao#");
        addPrefix("sio", "http://semanticscience.org/resource/");
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        addPrefix("dcterms", "http://purl.org/dc/terms/");
        addPrefix("dc", "http://purl.org/dc/elements/1.1/");
        addPrefix("cito", "http://purl.org/spar/cito/");
        addPrefix("repr", "https://w3id.org/reproduceme#");
        addPrefix("edam", "http://edamontology.org/");
        addPrefix("bibo", "http://purl.org/ontology/bibo/");
        addPrefix("fabio", "http://purl.org/spar/fabio/");
        addPrefix("cito", "http://purl.org/spar/cito/");
        addPrefix("dcmitypes", "http://purl.org/dc/dcmitype/");
        addPrefix("efo", "http://www.ebi.ac.uk/efo/");
        addPrefix("prov", "http://www.w3.org/ns/prov#");

        Sachem.addPrefixes(this);
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);
        Compound.addResourceClasses(this);
        Molecule.addResourceClasses(this);
        Sachem.addResourceClasses(this);

        Substance.addResourceClasses(this);
        Interaction.addResourceClasses(this);
        Transporter.addResourceClasses(this);
        Reference.addResourceClasses(this);
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        Substance.addQuadMappings(this);
        Interaction.addQuadMappings(this);
        Transporter.addQuadMappings(this);
        Reference.addQuadMappings(this);
    }


    /**
     * Adds the procedures of the configuration.
     */
    private void addProcedures()
    {
        Sachem.addProcedures(this, "molmedb", "molmedb:substance",
                getColumns(getIriClass("molmedb:substance"), "compound"));
    }
}
