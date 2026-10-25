package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



/**
 * Configuration of the SACHEM endpoint of the compounds of the PDB Chemical Component Dictionary.
 */
public class PdbSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public PdbSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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
    }


    /**
     * Adds the resource classes of the configuration.
     */
    private void addResourceClasses()
    {
        Sachem.addResourceClasses(this);

        addIriClass(new MapUserIriClass("pdb:molfile", INT4, new DatabaseTable("pdb", "compounds"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR),
                "https://idsm.elixir-czech.cz/rdf/pdb-ccd/", ".*", "_molfile"));

        addIriClass(new MapUserIriClass("pdb:compound", INT4, new DatabaseTable("pdb", "compounds"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR), "https://identifiers.org/pdb-ccd/",
                ".*"));
    }


    /**
     * Adds the quad mappings of the configuration.
     */
    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "pdb:compound", "pdb:molfile", new DatabaseTable("pdb", "compounds"),
                getColumns(getIriClass("pdb:compound"), "id"), "id", "molfile", xsdString);
    }


    /**
     * Adds the procedures of the configuration.
     */
    private void addProcedures()
    {
        Sachem.addProcedures(this, "pdb", "pdb:compound", getColumns(getIriClass("pdb:compound"), "compound"));
    }
}
