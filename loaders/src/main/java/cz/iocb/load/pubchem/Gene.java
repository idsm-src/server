package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Gene extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID";
    static final int prefixLength = prefix.length();

    static final String symbolPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/MD5_";
    static final int symbolPrefixLength = symbolPrefix.length();

    private static final IntSet keepGenes = new IntSet();
    private static final IntSet newGenes = new IntSet();
    private static final IntSet oldGenes = new IntSet();

    private static final StringIntMap keepGeneSymbols = new StringIntMap();
    private static final StringIntMap newGeneSymbols = new StringIntMap();
    private static final StringIntMap oldGeneSymbols = new StringIntMap();
    private static int nextGeneSymbolID;


    private static void loadGeneSymbolBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.gene_symbol_bases", oldGeneSymbols);

        nextGeneSymbolID = oldGeneSymbols.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?gene_symbol rdf:type sio:SIO_001383"))
        {
            @Override
            protected void parse() throws IOException
            {
                String geneSymbol = getStringID("gene_symbol", symbolPrefix);
                Integer geneSymbolID = oldGeneSymbols.remove(geneSymbol);

                if(geneSymbolID == null)
                    newGeneSymbols.put(geneSymbol, nextGeneSymbolID++);
                else
                    keepGeneSymbols.put(geneSymbol, geneSymbolID);
            }
        }.load(model);
    }


    private static void loadGeneSymbolLiterals(Model model) throws IOException, SQLException
    {
        IntStringMap keepSymbolLiterals = new IntStringMap();
        IntStringPairMap newSymbolLiterals = new IntStringPairMap();
        IntStringMap oldSymbolLiterals = new IntStringMap();

        load("select id,symbol from pubchem.gene_symbol_bases where symbol is not null", oldSymbolLiterals);

        new QueryResultProcessor(patternQuery("?gene_symbol sio:SIO_000300 ?symbol"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneSymbolID = getGeneSymbolID(getIRI("gene_symbol"), true);
                String symbol = getString("symbol");

                if(symbol.equals(oldSymbolLiterals.remove(geneSymbolID)))
                {
                    keepSymbolLiterals.put(geneSymbolID, symbol);
                }
                else
                {
                    String keep = keepSymbolLiterals.get(geneSymbolID);

                    Pair<String, String> pair = Pair.getPair(getStringID("gene_symbol", symbolPrefix), symbol);

                    if(symbol.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newSymbolLiterals.put(geneSymbolID, pair);

                    if(put != null && !symbol.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.gene_symbol_bases set symbol=null where id=? and symbol=?", oldSymbolLiterals);
        store("insert into pubchem.gene_symbol_bases(id,iri,symbol) values(?,?,?) "
                + "on conflict(id) do update set symbol=EXCLUDED.symbol", newSymbolLiterals);
    }


    private static void loadGeneBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.gene_bases", oldGenes);

        new QueryResultProcessor(patternQuery("?gene rdf:type sio:SIO_010035"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                checkGeneID(geneID);

                if(oldGenes.remove(geneID))
                    keepGenes.add(geneID);
                else
                    newGenes.add(geneID);
            }
        }.load(model);
    }


    private static void loadSymbols(Model model) throws IOException, SQLException
    {
        IntIntMap keepSymbols = new IntIntMap();
        IntIntMap newSymbols = new IntIntMap();
        IntIntMap oldSymbols = new IntIntMap();

        load("select id,gene_symbol from pubchem.gene_bases where gene_symbol is not null", oldSymbols);

        new QueryResultProcessor(patternQuery("?gene bao:BAO_0002870 ?symbol"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"), true);
                Integer symbolID = getGeneSymbolID(getIRI("symbol"));

                if(symbolID.equals(oldSymbols.remove(geneID)))
                {
                    keepSymbols.put(geneID, symbolID);
                }
                else
                {
                    Integer keep = keepSymbols.get(geneID);

                    if(symbolID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newSymbols.put(geneID, symbolID);

                    if(put != null && !symbolID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.gene_bases set gene_symbol=null where id=? and gene_symbol=?", oldSymbols);
        store("insert into pubchem.gene_bases(id,gene_symbol) values(?,?) "
                + "on conflict(id) do update set gene_symbol=EXCLUDED.gene_symbol", newSymbols);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.gene_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?gene skos:prefLabel ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(geneID)))
                {
                    keepTitles.put(geneID, title);
                }
                else
                {
                    String keep = keepTitles.get(geneID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(geneID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.gene_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.gene_bases(id,title) values(?,?) on conflict(id) do update set title=EXCLUDED.title",
                newTitles);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntMap keepOrganisms = new IntIntMap();
        IntIntMap newOrganisms = new IntIntMap();
        IntIntMap oldOrganisms = new IntIntMap();

        load("select id,organism from pubchem.gene_bases where organism is not null", oldOrganisms);

        new QueryResultProcessor(patternQuery("?gene up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"), true);
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                if(organismID.equals(oldOrganisms.remove(geneID)))
                {
                    keepOrganisms.put(geneID, organismID);
                }
                else
                {
                    Integer keep = keepOrganisms.get(geneID);

                    if(organismID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newOrganisms.put(geneID, organismID);

                    if(put != null && !organismID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.gene_bases set organism=null where id=? and organism=?", oldOrganisms);
        store("insert into pubchem.gene_bases(id,organism) values(?,?) "
                + "on conflict(id) do update set organism=EXCLUDED.organism", newOrganisms);
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

        load("select gene,reference from pubchem.gene_references", oldReferences);

        new QueryResultProcessor(patternQuery("?gene cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(geneID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_references where gene=? and reference=?", oldReferences);
        store("insert into pubchem.gene_references(gene,reference) values(?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select gene,match_unit,match_id from pubchem.gene_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match. "
                + "filter(!strstarts(str(?match), 'http://rdf.ebi.ac.uk/resource/ensembl/'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/ensembl:'))"
                + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/mesh:'))"
                + "filter(!strstarts(str(?match), 'https://www.kegg.jp/entry/'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/kegg.genes:'))"
                + "filter(!strstarts(str(?match), 'https://www.bgee.org/gene/'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/bgee.gene:'))"
                + "filter(!strstarts(str(?match), 'https://www.pombase.org/gene/'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/pombase:'))"
                + "filter(!strstarts(str(?match), 'https://zfin.org/ZDB-'))"
                + "filter(!strstarts(str(?match), 'https://identifiers.org/zfin:ZDB-'))"
                + "filter(!strstarts(str(?match), 'https://enzyme.expasy.org/EC/'))"
                + "filter(!strstarts(str(?match), 'https://medlineplus.gov/genetics/gene/'))"
                + "filter(!strstarts(str(?match), 'https://www.alliancegenome.org/gene/'))"
                + "filter(!strstarts(str(?match), 'https://pharos.nih.gov/targets/'))"
                + "filter(!strstarts(str(?match), 'https://www.veupathdb.org/gene/'))"))
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

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match. "
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
                "?gene skos:closeMatch ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
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
                "?gene skos:closeMatch ?match. filter(strstarts(str(?match), 'https://enzyme.expasy.org/EC/'))"))
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

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match. "
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

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match. "
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
                "?gene skos:closeMatch ?match. filter(strstarts(str(?match), 'https://www.kegg.jp/entry/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://www.kegg.jp/entry/");

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
                "?gene skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://pharos.nih.gov/targets/'))"))
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
                "?gene skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://www.bgee.org/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://www.bgee.org/gene/");

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
                "?gene skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://www.pombase.org/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://www.pombase.org/gene/");

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
                "?gene skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://www.veupathdb.org/gene/'))"))
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
                "?gene skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://zfin.org/ZDB-'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer geneID = getGeneID(getIRI("gene"));
                String match = getStringID("match", "https://zfin.org/ZDB-");

                Pair<Integer, String> pair = Pair.getPair(geneID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.gene_zfin_matches where gene=? and match=?", oldMatches);
        store("insert into pubchem.gene_zfin_matches(gene,match) values(?,?)", newMatches);
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

                if(process.getOne() != Ontology.unitGO)
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

                if(function.getOne() != Ontology.unitGO)
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

                if(location.getOne() != Ontology.unitGO)
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
        loadOrthologs(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish genes ...");

        store("delete from pubchem.gene_symbol_bases where iri=? and id=?", oldGeneSymbols);
        store("insert into pubchem.gene_symbol_bases(iri,id) values(?,?)", newGeneSymbols);

        store("delete from pubchem.gene_bases where id=?", oldGenes);
        store("insert into pubchem.gene_bases(id) values(?)", newGenes);

        System.out.println();
    }


    static Integer getGeneSymbolID(String value) throws IOException
    {
        return getGeneSymbolID(value, false);
    }


    static Integer getGeneSymbolID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(symbolPrefix))
            throw new IOException("unexpected IRI: " + value);

        String symbol = value.substring(symbolPrefixLength);

        synchronized(newGeneSymbols)
        {
            Integer geneSymbolID = keepGeneSymbols.get(symbol);

            if(geneSymbolID != null)
                return geneSymbolID;

            geneSymbolID = newGeneSymbols.get(symbol);

            if(geneSymbolID != null)
            {
                if(keepForce)
                {
                    newGeneSymbols.remove(symbol);
                    keepGeneSymbols.put(symbol, geneSymbolID);
                }

                return geneSymbolID;
            }

            System.out.println("    add missing gene symbol " + symbol);

            if((geneSymbolID = oldGeneSymbols.remove(symbol)) != null)
                keepGeneSymbols.put(symbol, geneSymbolID);
            else if(keepForce)
                keepGeneSymbols.put(symbol, geneSymbolID = nextGeneSymbolID++);
            else
                newGeneSymbols.put(symbol, geneSymbolID = nextGeneSymbolID++);

            return geneSymbolID;
        }
    }


    static Integer getGeneID(String value) throws IOException
    {
        return getGeneID(value, false);
    }


    static Integer getGeneID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer geneID = Integer.parseInt(value.substring(prefixLength));

        checkGeneID(geneID);

        synchronized(newGenes)
        {
            if(newGenes.contains(geneID))
            {
                if(forceKeep)
                {
                    newGenes.remove(geneID);
                    keepGenes.add(geneID);
                }
            }
            else if(!keepGenes.contains(geneID))
            {
                System.out.println("    add missing gene GID" + geneID);

                if(!oldGenes.remove(geneID) && !forceKeep)
                    newGenes.add(geneID);
                else
                    keepGenes.add(geneID);
            }
        }

        return geneID;
    }


    private static void checkGeneID(int geneID) throws IOException
    {
        if(geneID == 4 || geneID == 7 || geneID == 8)
            throw new IOException();
    }
}
