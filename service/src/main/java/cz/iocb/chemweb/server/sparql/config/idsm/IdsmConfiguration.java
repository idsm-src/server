package cz.iocb.chemweb.server.sparql.config.idsm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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
import cz.iocb.sparql.engine.database.ConstantColumn;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.classes.BuiltinClasses;
import cz.iocb.sparql.engine.mapping.classes.DateTimeConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.IriClass;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.parser.model.IRI;



public class IdsmConfiguration extends SparqlDatabaseConfiguration
{
    public IdsmConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service != null ? service : "https://idsm.elixir-czech.cz/sparql/endpoint/idsm", connectionPool, schema);

        addPrefixes();
        addQuadMappings();
        addServices();

        addServiceDescription();

        detectIriResourceClasses();
    }


    private void detectIriResourceClasses() throws SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            try(Statement stmt = connection.createStatement())
            {
                for(List<QuadMapping> m : mappings.values())
                {
                    ListIterator<QuadMapping> it = m.listIterator();

                    while(it.hasNext())
                    {
                        QuadMapping original = it.next();

                        if(original instanceof SingleTableQuadMapping map)
                        {
                            SingleTableQuadMapping mapping = new SingleTableQuadMapping(map.getTable(),
                                    remap(stmt, map.getGraph()), remap(stmt, map.getSubject()),
                                    remap(stmt, map.getPredicate()), remap(stmt, map.getObject()), map.getConditions());

                            it.set(mapping);
                        }
                        else if(original instanceof JoinTableQuadMapping map)
                        {
                            JoinTableQuadMapping mapping = new JoinTableQuadMapping(map.getTables(),
                                    map.getJoinColumnsPairs(), remap(stmt, map.getGraph()),
                                    remap(stmt, map.getSubject()), (ConstantIriMapping) remap(stmt, map.getPredicate()),
                                    remap(stmt, map.getObject()), map.getConditions());

                            it.set(mapping);
                        }
                        else
                        {
                            throw new IllegalArgumentException();
                        }
                    }
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    private <T extends NodeMapping> T remap(Statement statement, T mapping)
    {
        if(mapping instanceof ConstantIriMapping original)
        {
            if(original.getResourceClass() != null && original.getColumns() != null)
                return mapping;

            IRI iri = original.getIRI();
            IriClass iriClass = iriCache.getIriClass(iri);
            List<Column> columns = iriCache.getIriColumns(iri);

            if(iriClass == null || columns == null)
            {
                iriClass = detectIriClass(statement, iri);
                columns = iriClass.toColumns(statement, iri);
                iriCache.storeToCache(iri, iriClass, columns);

                if(shouldBeReported(iriClass, columns))
                    System.err.println("detect " + iri + " as '" + iriClass.getName() + "' " + columns);
            }

            return (T) new ConstantIriMapping(iri, iriClass, columns);
        }

        return mapping;
    }


    private boolean shouldBeReported(IriClass iriClass, List<Column> columns)
    {
        if(iriClass.getName().equals("unsupported"))
            return true;

        if(iriClass.getName().equals("ontology:resource"))
        {
            if(columns.get(0) instanceof ConstantColumn col0 && columns.get(1) instanceof ConstantColumn col1)
            {
                if(!col0.getValue().equals("0"))
                    return false;

                if(col1.getValue().length() > 3)
                    return true;
            }
        }

        return false;
    }


    private IriClass detectIriClass(Statement statement, IRI value)
    {
        for(UserIriClass iriClass : getIriClasses())
            if(iriClass.match(statement, value))
                return iriClass;

        return BuiltinClasses.unsupportedIri;
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
