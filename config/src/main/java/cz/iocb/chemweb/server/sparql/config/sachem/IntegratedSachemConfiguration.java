package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class IntegratedSachemConfiguration extends SparqlDatabaseConfiguration
{
    public IntegratedSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema);

        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/mona", connectionPool,
                schema), false);
        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/mona", connectionPool,
                schema), false);

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
