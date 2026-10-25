package cz.iocb.chemweb.server.sparql.config.idsm;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.builtinResourceLimit;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.chebi.ChebiConfiguration;
import cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.drugbank.DrugBankConfiguration;
import cz.iocb.chemweb.server.sparql.config.examples.ExamplesConfiguration;
import cz.iocb.chemweb.server.sparql.config.isdb.IsdbConfiguration;
import cz.iocb.chemweb.server.sparql.config.mesh.MeshConfiguration;
import cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration;
import cz.iocb.chemweb.server.sparql.config.mona.MonaConfiguration;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyConfiguration;
import cz.iocb.chemweb.server.sparql.config.pdb.PdbConfiguration;
import cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.ChebiOntologySachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.ChemblSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.DrugbankSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.MonaSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.PdbSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.PubChemSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.chemweb.server.sparql.config.sachem.WikidataSachemConfiguration;
import cz.iocb.chemweb.server.sparql.config.stats.VoidConfiguration;
import cz.iocb.chemweb.server.sparql.config.wikidata.WikidataConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.ValueColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.JoinTableQuadMapping;
import cz.iocb.sparql.engine.mapping.QuadMapping;
import cz.iocb.sparql.engine.mapping.SingleTableQuadMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.ResourceClass;
import cz.iocb.sparql.engine.rdf.Iri;
import cz.iocb.sparql.engine.request.Request;



/**
 * Configuration of the IDSM endpoint, which merges the configurations of all datasets into one service and adds the
 * Sachem structure search over their compounds, the Sachem endpoints of the single datasets as further services, and
 * the SHACL declarations of its prefixes. The endpoint obtains it through JNDI; the export and the generator of the
 * VoID statistics create it directly.
 */
public class IdsmConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    /**
     * Copy of the database schema that declares the {@code molfile} column of the molfile tables of DrugBank, ChEBI,
     * ChEMBL and PubChem as a unique key of its table.
     */
    public static class ExtendedDatabaseSchema extends DatabaseSchema
    {
        /**
         * Creates the copy of the schema with the additional unique keys.
         *
         * @param schema the schema to copy
         */
        public ExtendedDatabaseSchema(DatabaseSchema schema)
        {
            super(schema);

            for(DatabaseTable table : List.of(new DatabaseTable("drugbank", "compounds"),
                    new DatabaseTable("chebi", "class_molfiles"), new DatabaseTable("chembl", "molecule_molfiles"),
                    new DatabaseTable("pubchem", "compound_molfiles")))
                primaryKeys.get(table).add(List.of(getColumn(table, "molfile")));
        }
    }


    /**
     * Creates the configuration. Without a service IRI, it is the configuration of the IDSM endpoint itself, described
     * in the graph of its VoID statistics; otherwise it serves under the given IRI with the default description graph.
     *
     * @param service IRI of the service, or null for the IDSM endpoint
     * @param connectionPool the connection pool of the database
     * @param schema the database schema
     * @throws SQLException if a database error occurs
     */
    public IdsmConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service != null ? service : "https://idsm.elixir-czech.cz/sparql/endpoint/idsm",
                service != null ? null : "https://idsm.elixir-czech.cz/.well-known/void", connectionPool,
                new ExtendedDatabaseSchema(schema));

        addPrefixes();
        addServices();

        addPrefixDefinitionMappings();
        addBasicServiceDescription();
        detectIriResourceClasses();
    }


    /**
     * Replaces the constant IRIs of all quad mappings by mappings with their resource classes and columns detected in
     * advance, so that no request needs to detect them; the detections are kept in the IRI cache, and the IRIs of no
     * supported class or of uncategorized ontology resources that are not built in are reported.
     *
     * @throws SQLException if a database error occurs
     */
    private void detectIriResourceClasses() throws SQLException
    {
        try(Request request = new Request(this))
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
                                remap(request, map.getGraph()), remap(request, map.getSubject()),
                                remap(request, map.getPredicate()), remap(request, map.getObject()),
                                map.getConditions(), map.isDistinct());

                        it.set(mapping);
                    }
                    else if(original instanceof JoinTableQuadMapping map)
                    {
                        JoinTableQuadMapping mapping = new JoinTableQuadMapping(map.getTables(),
                                map.getJoinColumnsPairs(), remap(request, map.getGraph()),
                                remap(request, map.getSubject()),
                                (ConstantIriMapping) remap(request, map.getPredicate()),
                                remap(request, map.getObject()), map.getConditions(), map.getDistinct());

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


    /**
     * Returns the mapping of a constant IRI with its resource class and columns detected; other mappings, and the
     * constant mappings that already know them, are returned unchanged.
     *
     * @param <T> the type of the mapping
     * @param request the request used for the detection
     * @param mapping the mapping
     * @return the mapping with the detected resource class and columns
     */
    @SuppressWarnings("unchecked")
    private <T extends TermMapping> T remap(Request request, T mapping)
    {
        if(mapping instanceof ConstantIriMapping original)
        {
            if(original.getResourceClass() != null && original.getColumns() != null)
                return mapping;

            Iri iri = original.getIri();
            ResourceClass iriClass = iriCache.getIriClass(iri);
            List<Column> columns = iriClass == null ? null : iriCache.getIriColumns(iri, iriClass);

            if(iriClass == null || columns == null)
            {
                iriClass = request.getIriClass(iri);
                columns = request.getColumns(iriClass, iri);
                iriCache.storeToCache(iri, iriClass, columns);

                if(shouldBeReported(iriClass, columns))
                    System.err.println("detect " + iri + " as '" + iriClass.getResourceName() + "' " + columns);
            }

            return (T) new ConstantIriMapping(iri, iriClass, columns);
        }

        return mapping;
    }


    /**
     * Tells whether the detection of a constant IRI is reported: the IRI belongs to no supported class, or it is an
     * uncategorized ontology resource whose id is not below {@code builtinResourceLimit}, i.e. not a built-in one.
     *
     * @param iriClass the detected resource class
     * @param columns the detected columns
     * @return true if the detection is reported, false otherwise
     */
    private boolean shouldBeReported(ResourceClass iriClass, List<Column> columns)
    {
        if(iriClass.getResourceName().equals("unsupported"))
            return true;

        if(iriClass.getResourceName().equals("ontology:uncategorized"))
        {
            if(columns.get(0) instanceof ValueColumn col && Integer.parseInt(col.getValue()) >= builtinResourceLimit)
                return true;
        }

        return false;
    }


    /**
     * Adds the prefixes of the federated services (Rhea, neXtProt, Wikidata), of the Dublin Core terms and of the IDSM
     * endpoints to the configuration.
     */
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


    /**
     * Merges the configurations of all datasets into this service, adds the Sachem procedures and functions over the
     * compounds of the datasets, and imports the configurations of the single datasets with structure search as
     * services under the IRIs of their {@code sparql} and {@code sachem} endpoints.
     *
     * @throws SQLException if a database error occurs
     */
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
        addService(new MolmedbConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new PdbConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new VoidConfiguration(null, connectionPool, getDatabaseSchema()), true);
        addService(new ExamplesConfiguration(null, connectionPool, getDatabaseSchema()), true);

        Map<ResourceClass, List<Column>> mapping = new HashMap<>();
        mapping.put(getIriClass("ontology:chebi"), getColumns(getIriClass("ontology:chebi"), "chebi"));
        mapping.put(getIriClass("chembl:molecule"), getColumns(getIriClass("chembl:molecule"), "chembl"));
        mapping.put(getIriClass("drugbank:compound"), getColumns(getIriClass("drugbank:compound"), "drugbank"));
        mapping.put(getIriClass("isdb:compound"), getColumns(getIriClass("isdb:compound"), "isdb"));
        mapping.put(getIriClass("mona:compound"), getColumns(getIriClass("mona:compound"), "mona"));
        mapping.put(getIriClass("pubchem:compound"), getColumns(getIriClass("pubchem:compound"), "pubchem"));
        mapping.put(getIriClass("wikidata:entity"), getColumns(getIriClass("wikidata:entity"), "wikidata"));
        mapping.put(getIriClass("molmedb:substance"), getColumns(getIriClass("molmedb:substance"), "molmedb"));
        mapping.put(getIriClass("pdb:compound"), getColumns(getIriClass("pdb:compound"), "pdb"));

        Sachem.addResourceClasses(this);
        Sachem.addProcedures(this, "idsm", mapping);
        Sachem.addFunctions(this);


        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/mona", connectionPool,
                getDatabaseSchema()), false);
        addService(new MonaSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/mona", connectionPool,
                getDatabaseSchema()), false);

        addService(new MolmedbConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/molmedb", connectionPool,
                getDatabaseSchema()), false);
        addService(new MolmedbConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/molmedb", connectionPool,
                getDatabaseSchema()), false);

        addService(new PdbSachemConfiguration("https://idsm.elixir-czech.cz/sparql/endpoint/pdb", connectionPool,
                getDatabaseSchema()), false);
        addService(new PdbSachemConfiguration("https://idsm.elixir-czech.cz/sachem/endpoint/pdb", connectionPool,
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


    /**
     * Adds the SHACL declarations of all prefixes of the configuration ({@code sh:prefix} and {@code sh:namespace} of
     * {@code https://idsm.elixir-czech.cz/sparql-prefixes/<prefix>}) to the graph of the SPARQL examples.
     */
    private void addPrefixDefinitionMappings()
    {
        String graphIri = "https://idsm.elixir-czech.cz/.well-known/sparql-examples";

        ConstantIriMapping graph = createIriMapping(new Iri(graphIri));
        {
            for(Entry<String, String> entry : getPrefixes().entrySet())
            {
                String namespace = entry.getValue();
                String prefix = entry.getKey();

                TermMapping subject = createIriMapping(
                        new Iri("https://idsm.elixir-czech.cz/sparql-prefixes/" + prefix));

                addQuadMapping(graph, subject, createIriMapping("sh:namespace"), createLiteralMapping(namespace));
                addQuadMapping(graph, subject, createIriMapping("sh:prefix"), createLiteralMapping(prefix));
            }
        }
    }


    @Override
    public String getServiceDescriptionQuery()
    {
        return """
                prefix sd: <http://www.w3.org/ns/sparql-service-description#>
                prefix void: <http://rdfs.org/ns/void#>

                construct {?s ?p ?o} from <FROM> where
                {
                  ?s ?p ?o

                  filter not exists
                  {
                    ?s a  void:Dataset
                    filter not exists {
                      ?s a sd:Graph
                    }
                  }

                  filter not exists
                  {
                    ?o a  void:Dataset
                    filter not exists {
                      ?o a sd:Graph
                    }
                  }
                }
                """.replace("<FROM>", descriptionGraphIri.toString());
    }
}
