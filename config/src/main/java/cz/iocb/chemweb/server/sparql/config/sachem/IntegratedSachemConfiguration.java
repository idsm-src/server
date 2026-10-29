package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



/**
 * Configuration of the integrated SACHEM endpoint, which offers the SACHEM endpoints of the datasets as federated
 * services under their IDSM service IRIs.
 */
public class IntegratedSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Creates the configuration.
     *
     * @param service the service IRI
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public IntegratedSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema);

        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/mona", connectionPool,
                schema), false);
        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/mona", connectionPool,
                schema), false);

        addService(new MolmedbConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/molmedb", connectionPool,
                schema), false);
        addService(new MolmedbConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/molmedb", connectionPool,
                schema), false);

        addService(
                new PdbSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/pdb", connectionPool, schema),
                false);
        addService(
                new PdbSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/pdb", connectionPool, schema),
                false);

        addService(new WikidataSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/wikidata",
                connectionPool, schema), false);
        addService(new WikidataSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/wikidata",
                connectionPool, schema), false);

        addService(new DrugbankSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/drugbank",
                connectionPool, schema), false);
        addService(new DrugbankSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/drugbank",
                connectionPool, schema), false);

        addService(new ChebiOntologySachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/chebi",
                connectionPool, schema), false);
        addService(new ChebiOntologySachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/chebi",
                connectionPool, schema), false);

        addService(new ChemblSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/chembl", connectionPool,
                schema), false);
        addService(new ChemblSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/chembl", connectionPool,
                schema), false);

        addService(new PubChemSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/pubchem",
                connectionPool, schema), false);
        addService(new PubChemSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/pubchem",
                connectionPool, schema), false);
    }
}
