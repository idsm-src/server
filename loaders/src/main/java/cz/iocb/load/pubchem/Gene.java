package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Gene extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID";
    static final int prefixLength = prefix.length();

    static final String symbolPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/";
    static final int symbolPrefixLength = symbolPrefix.length();

    private static final EntityTable<Integer> genes = new EntityTable<>("pubchem.gene_bases", intKey("id"), null,
            uniqueVarchar("title"), integer("gene_symbol"), integer("organism"));

    private static final EntityTable<Integer> geneSymbols = new EntityTable<>("pubchem.gene_symbol_bases", intKey("id"),
            null, uniqueVarchar("iri").determinedByKey(), varchar("symbol"));
    private static final StringIntMap geneSymbolIDs = new StringIntMap();
    private static int nextGeneSymbolID;


    private static void loadGeneSymbolBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.gene_symbol_bases", geneSymbolIDs);

        nextGeneSymbolID = geneSymbolIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?gene_symbol rdf:type sio:SIO_001383"))
        {
            @Override
            protected void parse() throws IOException
            {
                addGeneSymbol(getStringID("gene_symbol", symbolPrefix));
            }
        }.load(model);
    }


    private static void loadGeneSymbolLiterals(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?gene_symbol sio:SIO_000300 ?symbol"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneSymbolID = getGeneSymbolID(getIRI("gene_symbol"));
                String symbol = getString("symbol");

                geneSymbols.set(geneSymbolID, "symbol", symbol);
            }
        }.load(model);
    }


    private static void loadGeneBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?gene rdf:type sio:SIO_010035"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getIntID("gene", prefix);
                checkGeneID(geneID);

                genes.reference(geneID);
            }
        }.load(model);
    }


    private static void loadSymbols(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?gene bao:BAO_0002870 ?symbol"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Integer symbolID = getGeneSymbolID(getIRI("symbol"));

                genes.set(geneID, "gene_symbol", symbolID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?gene skos:prefLabel ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String title = getString("title");

                genes.set(geneID, "title", title);
            }
        }.load(model);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?gene up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                genes.set(geneID, "organism", organismID);
            }
        }.load(model);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select gene,alternative from pubchem.gene_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?gene skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(geneID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_alternatives where gene=? and alternative=?", oldAlternatives);
        store("insert into pubchem.gene_alternatives(gene,alternative) values(?,?)", newAlternatives);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select gene,reference from pubchem.gene_references", oldReferences);
        load("select gene,patent from pubchem.gene_patents", oldPatents);

        new QueryResultProcessor(patternQuery("?gene cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));

                if(getIRI("reference").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                {
                    Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                    Pair<Integer, Integer> pair = Pair.getPair(geneID, referenceID);

                    if(!oldReferences.remove(pair))
                        newReferences.add(pair);
                }
                else
                {
                    Integer patentID = Patent.getPatentID(getIRI("reference"));

                    Pair<Integer, Integer> pair = Pair.getPair(geneID, patentID);

                    if(!oldPatents.remove(pair))
                        newPatents.add(pair);
                }
            }
        }.load(model);

        store("delete from pubchem.gene_references where gene=? and reference=?", oldReferences);
        store("insert into pubchem.gene_references(gene,reference) values(?,?)", newReferences);

        store("delete from pubchem.gene_patents where gene=? and patent=?", oldPatents);
        store("insert into pubchem.gene_patents(gene,patent) values(?,?)", newPatents);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select gene,match_unit,match_id from pubchem.gene_matches", oldMatches);

        new QueryResultProcessor(patternQuery("""
                ?gene rdfs:seeAlso ?match. \
                filter(!strstarts(str(?match), 'http://rdf.ebi.ac.uk/resource/ensembl/'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/ensembl:'))\
                filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/mesh:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/kegg.genes:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/bgee.gene:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/pombase:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/zfin:ZDB-'))\
                filter(!strstarts(str(?match), 'https://enzyme.expasy.org/EC/'))\
                filter(!strstarts(str(?match), 'https://medlineplus.gov/genetics/gene/'))\
                filter(!strstarts(str(?match), 'https://www.alliancegenome.org/gene/'))\
                filter(!strstarts(str(?match), 'https://pharos.nih.gov/targets/'))\
                filter(!strstarts(str(?match), 'https://www.veupathdb.org/gene/'))\
                filter(!strstarts(str(?match), 'http://purl.uniprot.org/enzyme/'))\
                filter(!strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))"""))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_matches where gene=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.gene_matches(gene,match_unit,match_id) values(?,?,?)", newMatches);
    }


    private static void loadEnsemblCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_ensembl_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?gene rdfs:seeAlso ?match. "
                + "filter(strstarts(str(?match), 'http://rdf.ebi.ac.uk/resource/ensembl/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://rdf.ebi.ac.uk/resource/ensembl/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_ensembl_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_ensembl_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_mesh_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_mesh_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadExpasyCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_expasy_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. filter(strstarts(str(?match), 'https://enzyme.expasy.org/EC/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://enzyme.expasy.org/EC/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_expasy_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_expasy_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadMedlineplusCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_medlineplus_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?gene rdfs:seeAlso ?match. "
                + "filter(strstarts(str(?match), 'https://medlineplus.gov/genetics/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://medlineplus.gov/genetics/gene/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_medlineplus_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_medlineplus_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadAlliancegenomeCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_alliancegenome_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?gene rdfs:seeAlso ?match. "
                + "filter(strstarts(str(?match), 'https://www.alliancegenome.org/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://www.alliancegenome.org/gene/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_alliancegenome_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_alliancegenome_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadKeggCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_kegg_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://identifiers.org/kegg.genes:'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://identifiers.org/kegg.genes:");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_kegg_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_kegg_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadPharosCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_pharos_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'https://pharos.nih.gov/targets/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://pharos.nih.gov/targets/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_pharos_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_pharos_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadBgeeCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_bgee_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'http://identifiers.org/bgee.gene:'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://identifiers.org/bgee.gene:");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_bgee_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_bgee_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadPombaseCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_pombase_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'http://identifiers.org/pombase:'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://identifiers.org/pombase:");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_pombase_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_pombase_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadVeupathdbCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_veupathdb_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'https://www.veupathdb.org/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://www.veupathdb.org/gene/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_veupathdb_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_veupathdb_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadZfinCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_zfin_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'http://identifiers.org/zfin:ZDB-'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://identifiers.org/zfin:ZDB-");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_zfin_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_zfin_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadEnzymeCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_enzyme_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://purl.uniprot.org/enzyme/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "http://purl.uniprot.org/enzyme/");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_enzyme_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_enzyme_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadWikidataCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select gene,match from pubchem.gene_wikidata_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?gene rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Integer match = getIntID("match", "http://www.wikidata.org/entity/Q");

                Pair<Integer, Integer> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_wikidata_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_wikidata_matches(gene,match) values(?,?)", newMatches);
    }


    private static void loadProcesses(Model model) throws IOException, SQLException
    {
        IntPairSet newProcesses = new IntPairSet();
        IntPairSet oldProcesses = new IntPairSet();

        load("select gene,process_id from pubchem.gene_processes", oldProcesses);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0000056 ?process "
                + "filter(strstarts(str(?process), 'http://purl.obolibrary.org/obo/GO_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Pair<Integer, Integer> process = Ontology.getId(getIRI("process"));

                if(process.getOne() != OntologyResource.unitGO)
                    throw new IOException();

                Pair<Integer, Integer> pair = Pair.getPair(geneID, process.getTwo());

                if(!oldProcesses.remove(pair))
                    newProcesses.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_processes where gene=? and process_id=?", oldProcesses);
        store("insert into pubchem.gene_processes(gene,process_id) values(?,?)", newProcesses);
    }


    private static void loadFunctions(Model model) throws IOException, SQLException
    {
        IntPairSet newFunctions = new IntPairSet();
        IntPairSet oldFunctions = new IntPairSet();

        load("select gene,function_id from pubchem.gene_functions", oldFunctions);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0000085 ?function"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Pair<Integer, Integer> function = Ontology.getId(getIRI("function"));

                if(function.getOne() != OntologyResource.unitGO)
                    throw new IOException();

                Pair<Integer, Integer> pair = Pair.getPair(geneID, function.getTwo());

                if(!oldFunctions.remove(pair))
                    newFunctions.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_functions where gene=? and function_id=?", oldFunctions);
        store("insert into pubchem.gene_functions(gene,function_id) values(?,?)", newFunctions);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        IntPairSet newLocations = new IntPairSet();
        IntPairSet oldLocations = new IntPairSet();

        load("select gene,location_id from pubchem.gene_locations", oldLocations);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0001025 ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Pair<Integer, Integer> location = Ontology.getId(getIRI("location"));

                if(location.getOne() != OntologyResource.unitGO)
                    throw new IOException();

                Pair<Integer, Integer> pair = Pair.getPair(geneID, location.getTwo());

                if(!oldLocations.remove(pair))
                    newLocations.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_locations where gene=? and location_id=?", oldLocations);
        store("insert into pubchem.gene_locations(gene,location_id) values(?,?)", newLocations);
    }


    private static void loadOrthologs(Model model) throws IOException, SQLException
    {
        IntPairSet newOrthologs = new IntPairSet();
        IntPairSet oldOrthologs = new IntPairSet();

        load("select gene,ortholog from pubchem.gene_orthologs", oldOrthologs);

        new QueryResultProcessor(patternQuery("?gene sio:SIO_000558 ?ortholog"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Integer orthologID = getGeneID(getIRI("ortholog"));

                Pair<Integer, Integer> pair = Pair.getPair(geneID, orthologID);

                if(!oldOrthologs.remove(pair))
                    newOrthologs.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_orthologs where gene=? and ortholog=?", oldOrthologs);
        store("insert into pubchem.gene_orthologs(gene,ortholog) values(?,?)", newOrthologs);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load genes ...");

        Model model = getModel("pubchem/RDF/gene/pc_gene.ttl.gz");
        check(model, "pubchem/gene/check.sparql");

        loadGeneSymbolBases(model);
        loadGeneSymbolLiterals(model);

        loadGeneBases(model);
        loadSymbols(model);
        loadTitles(model);
        loadOrganisms(model);
        loadProcesses(model);
        loadFunctions(model);
        loadLocations(model);
        loadAlternatives(model);
        loadReferences(model);
        loadCloseMatches(model);
        loadEnsemblCloseMatches(model);
        loadMeshCloseMatches(model);
        loadExpasyCloseMatches(model);
        loadMedlineplusCloseMatches(model);
        loadAlliancegenomeCloseMatches(model);
        loadKeggCloseMatches(model);
        loadPharosCloseMatches(model);
        loadBgeeCloseMatches(model);
        loadPombaseCloseMatches(model);
        loadVeupathdbCloseMatches(model);
        loadZfinCloseMatches(model);
        loadEnzymeCloseMatches(model);
        loadWikidataCloseMatches(model);
        loadOrthologs(model);

        model.close();

        geneSymbols.flush();
        genes.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish genes ...");

        geneSymbols.store();

        genes.store();

        System.out.println();
    }


    static Integer getGeneSymbolID(String value) throws IOException
    {
        if(!value.startsWith(symbolPrefix))
            throw new IOException("unexpected IRI: " + value);

        String symbol = value.substring(symbolPrefixLength);

        synchronized(geneSymbolIDs)
        {
            Integer geneSymbolID = geneSymbolIDs.get(symbol);

            if(geneSymbolID != null && geneSymbols.contains(geneSymbolID))
                return geneSymbolID;

            System.out.println("    add missing gene symbol " + symbol);

            return addGeneSymbol(symbol);
        }
    }


    /*
     * Adds the row of a gene symbol, which keeps its id if it has one.
     */
    private static Integer addGeneSymbol(String symbol) throws IOException
    {
        synchronized(geneSymbolIDs)
        {
            Integer geneSymbolID = geneSymbolIDs.get(symbol);

            if(geneSymbolID == null)
                geneSymbolIDs.put(symbol, geneSymbolID = nextGeneSymbolID++);

            geneSymbols.set(geneSymbolID, "iri", symbol);

            return geneSymbolID;
        }
    }


    static Integer getGeneID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer geneID = Integer.parseInt(value.substring(prefixLength));

        checkGeneID(geneID);

        if(genes.reference(geneID))
            System.out.println("    add missing gene GID" + geneID);

        return geneID;
    }


    private static void checkGeneID(int geneID) throws IOException
    {
        if(geneID == 4 || geneID == 7 || geneID == 8)
            throw new IOException();
    }
}
