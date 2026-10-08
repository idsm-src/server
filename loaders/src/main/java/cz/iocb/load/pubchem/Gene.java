package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.notStartingWith;
import static cz.iocb.load.common.TripleDispatcher.startingWith;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.bao;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.obo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.up;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
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

    private static final MissingEntities<Integer> missingGenes = new MissingEntities<>("gene", true);
    private static final MissingEntities<String> missingGeneSymbols = new MissingEntities<>("gene symbol", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(startingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"), skos + "altLabel",
                skos + "prefLabel", cito + "isDiscussedBy", rdfs + "seeAlso", rdf + "type", obo + "RO_0000056",
                obo + "RO_0000085", obo + "RO_0001025", bao + "BAO_0002870", up + "organism", sio + "SIO_000558",
                dcterms + "identifier");
        dispatcher.checkPredicates(notStartingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"), rdf + "type",
                sio + "SIO_000300");
        dispatcher.checkTypes(startingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"), vocab + "Gene",
                sio + "SIO_010035");
        dispatcher.checkTypes(notStartingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"), vocab + "GeneSymbol",
                sio + "SIO_001383");
        dispatcher.checkIdentifier(dcterms + "identifier", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
        dispatcher.checkPrefixes(all(), rdfs + "seeAlso", "http://rdf.ebi.ac.uk/resource/ensembl/",
                "http://id.nlm.nih.gov/mesh/", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "https://enzyme.expasy.org/EC/", "https://medlineplus.gov/genetics/gene/",
                "https://www.alliancegenome.org/gene/", "http://www.wormbase.org/db/gene/gene?class=Gene;name=WBGene",
                "https://pharos.nih.gov/targets/", "https://platform.opentargets.org/target/ENSG",
                "https://search.thegencc.org/genes/HGNC:", "https://www.veupathdb.org/gene/",
                "http://identifiers.org/mesh:", "http://identifiers.org/kegg.genes:",
                "http://identifiers.org/bgee.gene:", "http://identifiers.org/ensembl:",
                "http://identifiers.org/pombase:", "http://identifiers.org/zfin:", "http://identifiers.org/ctd.gene:",
                "http://identifiers.org/mim:", "http://identifiers.org/hgnc:", "http://identifiers.org/rgd:",
                "http://identifiers.org/MGI:", "http://identifiers.org/ncit:C",
                "http://identifiers.org/pharmgkb.gene:PA", "http://identifiers.org/sgd:S",
                "http://identifiers.org/xenbase:XB-GENE-", "http://identifiers.org/xenbase:XB-GENEPAGE-",
                "http://purl.uniprot.org/enzyme/", "http://glycosmos.org/glycogene/", "http://identifiers.org/fb:",
                "http://identifiers.org/ncbigene:", "http://www.wikidata.org/entity/Q");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/ensembl:",
                "http://rdf.ebi.ac.uk/resource/ensembl/");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
    }


    private static void loadGeneSymbolBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.gene_symbol_bases", geneSymbolIDs);

        nextGeneSymbolID = geneSymbolIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(sio + "SIO_001383", (subject, object) -> {
            String symbol = getStringID(subject, symbolPrefix);

            addGeneSymbol(symbol);
            missingGeneSymbols.described(symbol);
        });
    }


    private static void loadGeneSymbolLiterals(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(sio + "SIO_000300", (subject, object) -> {
            Integer geneSymbolID = getGeneSymbolID(subject.getURI());
            String symbol = getString(object);

            geneSymbols.set(geneSymbolID, "symbol", symbol);
        });
    }


    private static void loadGeneBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(sio + "SIO_010035", (subject, object) -> {
            Integer geneID = getIntID(subject, prefix);
            checkGeneID(geneID);

            genes.reference(geneID);
            missingGenes.described(geneID);
        });
    }


    private static void loadSymbols(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(bao + "BAO_0002870", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Integer symbolID = getGeneSymbolID(object.getURI());

            genes.set(geneID, "gene_symbol", symbolID);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            String title = getString(object);

            genes.set(geneID, "title", title);
        });
    }


    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            genes.set(geneID, "organism", organismID);
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select gene,alternative from pubchem.gene_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(geneID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_alternatives where gene=? and alternative=?", oldAlternatives);
            store("insert into pubchem.gene_alternatives(gene,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        IntPairSet keepPatents = new IntPairSet();
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select gene,reference from pubchem.gene_references", oldReferences);
        load("select gene,patent from pubchem.gene_patents", oldPatents);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());

            if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
            {
                Integer referenceID = Reference.getReferenceID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(geneID, referenceID);

                if(oldReferences.remove(pair))
                    keepReferences.add(pair);
                else if(!keepReferences.contains(pair))
                    newReferences.add(pair);
            }
            else
            {
                Integer patentID = Patent.getPatentID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(geneID, patentID);

                if(oldPatents.remove(pair))
                    keepPatents.add(pair);
                else if(!keepPatents.contains(pair))
                    newPatents.add(pair);
            }
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_references where gene=? and reference=?", oldReferences);
            store("insert into pubchem.gene_references(gene,reference) values(?,?)", newReferences);

            store("delete from pubchem.gene_patents where gene=? and patent=?", oldPatents);
            store("insert into pubchem.gene_patents(gene,patent) values(?,?)", newPatents);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select gene,match_unit,match_id from pubchem.gene_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, "http://rdf.ebi.ac.uk/resource/ensembl/", "http://identifiers.org/ensembl:",
                    "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:", "http://identifiers.org/kegg.genes:",
                    "http://identifiers.org/bgee.gene:", "http://identifiers.org/pombase:",
                    "http://identifiers.org/zfin:ZDB-", "https://enzyme.expasy.org/EC/",
                    "https://medlineplus.gov/genetics/gene/", "https://www.alliancegenome.org/gene/",
                    "https://pharos.nih.gov/targets/", "https://www.veupathdb.org/gene/",
                    "http://purl.uniprot.org/enzyme/", "http://www.wikidata.org/entity/Q"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getId(object.getURI());

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_matches where gene=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.gene_matches(gene,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadEnsemblCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_ensembl_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ebi.ac.uk/resource/ensembl/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://rdf.ebi.ac.uk/resource/ensembl/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_ensembl_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_ensembl_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_mesh_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://id.nlm.nih.gov/mesh/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_mesh_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_mesh_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadExpasyCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_expasy_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://enzyme.expasy.org/EC/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "https://enzyme.expasy.org/EC/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_expasy_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_expasy_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadMedlineplusCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_medlineplus_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://medlineplus.gov/genetics/gene/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "https://medlineplus.gov/genetics/gene/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_medlineplus_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_medlineplus_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadAlliancegenomeCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_alliancegenome_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.alliancegenome.org/gene/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "https://www.alliancegenome.org/gene/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_alliancegenome_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_alliancegenome_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadKeggCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_kegg_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/kegg.genes:"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/kegg.genes:");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_kegg_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_kegg_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadPharosCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_pharos_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://pharos.nih.gov/targets/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "https://pharos.nih.gov/targets/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_pharos_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_pharos_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadBgeeCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_bgee_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/bgee.gene:"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/bgee.gene:");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_bgee_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_bgee_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadPombaseCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_pombase_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/pombase:"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/pombase:");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_pombase_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_pombase_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadVeupathdbCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_veupathdb_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.veupathdb.org/gene/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "https://www.veupathdb.org/gene/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_veupathdb_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_veupathdb_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadZfinCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_zfin_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/zfin:ZDB-"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/zfin:ZDB-");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_zfin_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_zfin_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadEnzymeCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select gene,match from pubchem.gene_enzyme_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://purl.uniprot.org/enzyme/"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            String match = getStringID(object, "http://purl.uniprot.org/enzyme/");

            Pair<Integer, String> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_enzyme_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_enzyme_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadWikidataCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select gene,match from pubchem.gene_wikidata_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://www.wikidata.org/entity/Q"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            Integer match = getIntID(object, "http://www.wikidata.org/entity/Q");

            Pair<Integer, Integer> pair = Pair.getPair(geneID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_wikidata_matches where gene=? and match=?", oldMatches);
            store("insert into pubchem.gene_wikidata_matches(gene,match) values(?,?)", newMatches);
        });
    }


    private static void loadProcesses(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepProcesses = new IntPairSet();
        IntPairSet newProcesses = new IntPairSet();
        IntPairSet oldProcesses = new IntPairSet();

        load("select gene,process_id from pubchem.gene_processes", oldProcesses);

        dispatcher.on(obo + "RO_0000056", (subject, object) -> {
            if(!startsWith(object, "http://purl.obolibrary.org/obo/GO_"))
                return;

            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> process = Ontology.getId(object.getURI());

            if(process.getOne() != OntologyResource.unitGO)
                throw new IOException();

            Pair<Integer, Integer> pair = Pair.getPair(geneID, process.getTwo());

            if(oldProcesses.remove(pair))
                keepProcesses.add(pair);
            else if(!keepProcesses.contains(pair))
                newProcesses.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_processes where gene=? and process_id=?", oldProcesses);
            store("insert into pubchem.gene_processes(gene,process_id) values(?,?)", newProcesses);
        });
    }


    private static void loadFunctions(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepFunctions = new IntPairSet();
        IntPairSet newFunctions = new IntPairSet();
        IntPairSet oldFunctions = new IntPairSet();

        load("select gene,function_id from pubchem.gene_functions", oldFunctions);

        dispatcher.on(obo + "RO_0000085", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> function = Ontology.getId(object.getURI());

            if(function.getOne() != OntologyResource.unitGO)
                throw new IOException();

            Pair<Integer, Integer> pair = Pair.getPair(geneID, function.getTwo());

            if(oldFunctions.remove(pair))
                keepFunctions.add(pair);
            else if(!keepFunctions.contains(pair))
                newFunctions.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_functions where gene=? and function_id=?", oldFunctions);
            store("insert into pubchem.gene_functions(gene,function_id) values(?,?)", newFunctions);
        });
    }


    private static void loadLocations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepLocations = new IntPairSet();
        IntPairSet newLocations = new IntPairSet();
        IntPairSet oldLocations = new IntPairSet();

        load("select gene,location_id from pubchem.gene_locations", oldLocations);

        dispatcher.on(obo + "RO_0001025", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> location = Ontology.getId(object.getURI());

            if(location.getOne() != OntologyResource.unitGO)
                throw new IOException();

            Pair<Integer, Integer> pair = Pair.getPair(geneID, location.getTwo());

            if(oldLocations.remove(pair))
                keepLocations.add(pair);
            else if(!keepLocations.contains(pair))
                newLocations.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_locations where gene=? and location_id=?", oldLocations);
            store("insert into pubchem.gene_locations(gene,location_id) values(?,?)", newLocations);
        });
    }


    private static void loadOrthologs(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepOrthologs = new IntPairSet();
        IntPairSet newOrthologs = new IntPairSet();
        IntPairSet oldOrthologs = new IntPairSet();

        load("select gene,ortholog from pubchem.gene_orthologs", oldOrthologs);

        dispatcher.on(sio + "SIO_000558", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Integer orthologID = getGeneID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(geneID, orthologID);

            if(oldOrthologs.remove(pair))
                keepOrthologs.add(pair);
            else if(!keepOrthologs.contains(pair))
                newOrthologs.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.gene_orthologs where gene=? and ortholog=?", oldOrthologs);
            store("insert into pubchem.gene_orthologs(gene,ortholog) values(?,?)", newOrthologs);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load genes ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadGeneSymbolBases(dispatcher);
        loadGeneSymbolLiterals(dispatcher);
        loadGeneBases(dispatcher);
        loadSymbols(dispatcher);
        loadTitles(dispatcher);
        loadOrganisms(dispatcher);
        loadProcesses(dispatcher);
        loadFunctions(dispatcher);
        loadLocations(dispatcher);
        loadAlternatives(dispatcher);
        loadReferences(dispatcher);
        loadCloseMatches(dispatcher);
        loadEnsemblCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadExpasyCloseMatches(dispatcher);
        loadMedlineplusCloseMatches(dispatcher);
        loadAlliancegenomeCloseMatches(dispatcher);
        loadKeggCloseMatches(dispatcher);
        loadPharosCloseMatches(dispatcher);
        loadBgeeCloseMatches(dispatcher);
        loadPombaseCloseMatches(dispatcher);
        loadVeupathdbCloseMatches(dispatcher);
        loadZfinCloseMatches(dispatcher);
        loadEnzymeCloseMatches(dispatcher);
        loadWikidataCloseMatches(dispatcher);
        loadOrthologs(dispatcher);

        dispatcher.load("pubchem/RDF/gene/pc_gene.ttl.gz");
        missingGeneSymbols.settle();
        missingGenes.settle();
        dispatcher.finish();

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

            missingGeneSymbols.referenced(symbol);

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
            missingGenes.referenced(geneID);

        return geneID;
    }


    private static void checkGeneID(int geneID) throws IOException
    {
        if(geneID == 4 || geneID == 7 || geneID == 8)
            throw new IOException();
    }
}
