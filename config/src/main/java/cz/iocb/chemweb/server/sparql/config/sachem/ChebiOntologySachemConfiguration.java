package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



/**
 * Configuration of the SACHEM endpoint of the ChEBI compounds, identified by their ontology resource class
 * {@code ontology:chebi}.
 */
public class ChebiOntologySachemConfiguration extends SparqlDatabaseConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public ChebiOntologySachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
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
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);

        addPrefix("obo", "http://purl.obolibrary.org/obo/");
    }


    /**
     * Adds the resource classes of the configuration.
     *
     * @throws SQLException if a database error occurs
     */
    private void addResourceClasses() throws SQLException
    {
        Sachem.addResourceClasses(this);
        Ontology.addResourceClasses(this);

        addIriClass(
                new IntegerUserIriClass("chebi:molfile", INT4, "http://purl.obolibrary.org/obo/CHEBI_", "_Molfile"));
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "ontology:chebi", "chebi:molfile", new DatabaseTable("chebi", "class_molfiles"),
                getColumns(getIriClass("ontology:chebi"), "class"), "class", "molfile",
                new StringSubsetLiteralClass("chebi-molfile"));
    }


    /**
     * Adds the procedures of the configuration.
     */
    private void addProcedures()
    {
        Sachem.addProcedures(this, "chebi", "ontology:chebi", getColumns(getIriClass("ontology:chebi"), "compound"));
    }
}
