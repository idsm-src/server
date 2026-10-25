package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;



/**
 * Configuration of the SACHEM endpoint of the ChEMBL molecules.
 */
public class ChemblSachemConfiguration extends SachemConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public ChemblSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema, "chembl", "molecule",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", 0,
                new DatabaseTable("chembl", "molecule_molfiles"), "molecule",
                new StringSubsetLiteralClass("chembl-molfile"));

        addPrefixes();
    }


    /**
     * Adds the prefixes of the dataset.
     */
    private void addPrefixes()
    {
        addPrefix("chembl_molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/");
    }
}
