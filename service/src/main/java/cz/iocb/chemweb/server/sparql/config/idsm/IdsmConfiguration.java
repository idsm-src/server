package cz.iocb.chemweb.server.sparql.config.idsm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.chebi.ChebiConfiguration;
import cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration;
import cz.iocb.chemweb.server.sparql.config.drugbank.DrugBankConfiguration;
import cz.iocb.chemweb.server.sparql.config.isdb.IsdbConfiguration;
import cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration;
import cz.iocb.chemweb.server.sparql.config.mona.MonaConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration;
import cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.ChebiOntologySachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.ChemblSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.DrugbankSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.MonaSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.PubChemSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.chemweb.server.sparql.config.sachem.WikidataSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.wikidata.WikidataConfiguration;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.classes.DateTimeConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;



public class IdsmConfiguration extends SparqlDatabaseConfiguration
{
    public IdsmConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service != null ? service : "https://idsm.elixir-czech.cz/sparql/endpoint/idsm", connectionPool, schema);

        addPrefixes();
        addQuadMappings();
        addServices();

        addServiceDescription();
    }


    private void addPrefixes()
    {
        // rhea
        addPrefix("rh", "http://rdf.rhea-db.org/");
        addPrefix("taxon", "http://purl.uniprot.org/taxonomy/");

        // nextprot
        addPrefix("nextprot", "http://nextprot.org/rdf#");
        addPrefix("cv", "http://nextprot.org/rdf/terminology/");

        // wikidata
        addPrefix("wikibase", "http://wikiba.se/ontology#");
        addPrefix("wd", "http://www.wikidata.org/entity/");
        addPrefix("wdt", "http://www.wikidata.org/prop/direct/");
        addPrefix("wdtn", "http://www.wikidata.org/prop/direct-normalized/");
        addPrefix("wds", "http://www.wikidata.org/entity/statement/");
        addPrefix("p", "http://www.wikidata.org/prop/");
        addPrefix("wdref", "http://www.wikidata.org/reference/");
        addPrefix("wdv", "http://www.wikidata.org/value/");
        addPrefix("ps", "http://www.wikidata.org/prop/statement/");
        addPrefix("psv", "http://www.wikidata.org/prop/statement/value/");
        addPrefix("psn", "http://www.wikidata.org/prop/statement/value-normalized/");
        addPrefix("pq", "http://www.wikidata.org/prop/qualifier/");
        addPrefix("pqv", "http://www.wikidata.org/prop/qualifier/value/");
        addPrefix("pqn", "http://www.wikidata.org/prop/qualifier/value-normalized/");
        addPrefix("pr", "http://www.wikidata.org/prop/reference/");
        addPrefix("prv", "http://www.wikidata.org/prop/reference/value/");
        addPrefix("prn", "http://www.wikidata.org/prop/reference/value-normalized/");
        addPrefix("wdno", "http://www.wikidata.org/prop/novalue/");
        addPrefix("wdata", "http://www.wikidata.org/wiki/Special:EntityData/");

        addPrefix("dcterms", "http://purl.org/dc/terms/");
        addPrefix("idsm", "https://idsm.elixir-czech.cz/sparql/endpoint/");
    }


    private void addQuadMappings()
    {
        {
            Table table = new Table("info", "idsm_version");
            ConstantIriMapping graph = createIriMapping(getDescriptionGraphIri());
            ConstantIriMapping defaultDataset = createIriMapping("<" + getServiceIri().getValue() + "#DefaultDataset>");
            ConstantIriMapping defaultGraph = createIriMapping("<" + getServiceIri().getValue() + "#DefaultGraph>");

            DateTimeConstantZoneClass xsdDateTimeM0 = DateTimeConstantZoneClass.get(0);

            addQuadMapping(table, graph, defaultDataset, createIriMapping("dcterms:issued"),
                    createLiteralMapping(xsdDateTimeM0, "date"));

            addQuadMapping(table, graph, defaultGraph, createIriMapping("dcterms:issued"),
                    createLiteralMapping(xsdDateTimeM0, "date"));
        }
    }


    private void addServices() throws SQLException
    {
        addService(new ChebiConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new ChemblConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new MeshConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new OntologyConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new PubChemConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new MonaConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new IsdbConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new DrugBankConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new WikidataConfiguration(null, connectionPool, getDatabaseSchema()), true);

        Map<ResourceClass, List<Column>> mapping = new HashMap<ResourceClass, List<Column>>();
        mapping.put(getIriClass("ontology:resource"), List.of(getColumn(Ontology.unitCHEBI), getColumn("chebi")));
        mapping.put(getIriClass("chembl:compound"), List.of(getColumn("chembl")));
        mapping.put(getIriClass("drugbank:compound"), List.of(getColumn("drugbank")));
        mapping.put(getIriClass("isdb:compound"), List.of(getColumn("isdb")));
        mapping.put(getIriClass("mona:compound"), List.of(getColumn("mona")));
        mapping.put(getIriClass("pubchem:compound"), List.of(getColumn("pubchem")));
        mapping.put(getIriClass("wikidata:entity"), List.of(getColumn("wikidata")));

        Sachem.addResourceClasses(this);
        Sachem.addProcedures(this, "sachem", mapping);
        Sachem.addFunctions(this);


        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/mona", connectionPool,
                getDatabaseSchema()), false);
        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/mona", connectionPool,
                getDatabaseSchema()), false);

        addService(new WikidataSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/wikidata",
                connectionPool, getDatabaseSchema()), false);
        addService(new WikidataSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/wikidata",
                connectionPool, getDatabaseSchema()), false);

        addService(new DrugbankSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/drugbank",
                connectionPool, getDatabaseSchema()), false);
        addService(new DrugbankSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/drugbank",
                connectionPool, getDatabaseSchema()), false);

        addService(new ChebiOntologySachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/chebi",
                connectionPool, getDatabaseSchema()), false);
        addService(new ChebiOntologySachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/chebi",
                connectionPool, getDatabaseSchema()), false);

        addService(new ChemblSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/chembl", connectionPool,
                getDatabaseSchema()), false);
        addService(new ChemblSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/chembl", connectionPool,
                getDatabaseSchema()), false);

        addService(new PubChemSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/pubchem",
                connectionPool, getDatabaseSchema()), false);
        addService(new PubChemSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/pubchem",
                connectionPool, getDatabaseSchema()), false);
    }
}
