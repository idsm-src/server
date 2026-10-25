package cz.iocb.load.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitGO;
import static cz.iocb.chemweb.server.sparql.config.pubchem.Gene.matchUnits;
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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the PubChem genes and gene symbols from {@code pubchem/RDF/gene/pc_gene.ttl.gz} into the tables
 * {@code pubchem.genes} and {@code pubchem.genesymbols} and the tables of the alternative labels, references, matches,
 * Gene Ontology annotations and orthologs of the genes.
 */
class Gene extends Updater
{
    /**
     * IRI prefix of the genes.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * IRI prefix of the gene symbols.
     */
    static final String symbolPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/";

    /**
     * Length of {@link #symbolPrefix}.
     */
    static final int symbolPrefixLength = symbolPrefix.length();

    /**
     * Table of the genes.
     */
    private static final EntityTable<Integer> genes = new EntityTable<>("pubchem.genes", intKey("id"), null,
            uniqueVarchar("title"), integer("genesymbol"), integer("organism"));

    /**
     * Table of the gene symbols.
     */
    private static final EntityTable<Integer> geneSymbols = new EntityTable<>("pubchem.genesymbols", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("symbol"));

    /**
     * Ids of the gene symbols by their IRIs without the prefix.
     */
    private static final StringIntMap geneSymbolIDs = new StringIntMap();

    /**
     * Id of the next new gene symbol.
     */
    private static int nextGeneSymbolID;

    /**
     * Genes referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingGenes = new MissingEntities<>("gene", true);

    /**
     * Gene symbols referenced but not described by the data.
     */
    private static final MissingEntities<String> missingGeneSymbols = new MissingEntities<>("gene symbol", true);


    /**
     * Not instantiable.
     */
    private Gene()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the gene file.
     *
     * @param dispatcher the dispatcher of the triples
     */
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
        dispatcher.checkPaired(rdfs + "seeAlso", "http://rdf.ebi.ac.uk/resource/ensembl/",
                "http://identifiers.org/ensembl:");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:");
    }


    /**
     * Loads the ids of the stored gene symbols and registers the handler that adds the gene symbols described by the
     * data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneSymbolBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.genesymbols", geneSymbolIDs);

        nextGeneSymbolID = geneSymbolIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(sio + "SIO_001383", (subject, object) -> {
            String symbol = getStringID(subject, symbolPrefix);

            addGeneSymbol(symbol);
            missingGeneSymbols.described(symbol);
        });
    }


    /**
     * Registers the handler that sets the symbols of the gene symbols.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneSymbolLiterals(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(sio + "SIO_000300", (subject, object) -> {
            Integer geneSymbolID = getGeneSymbolID(subject.getURI());
            String symbol = getString(object);

            geneSymbols.set(geneSymbolID, "symbol", symbol);
        });
    }


    /**
     * Registers the handler that adds the genes described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGeneBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(sio + "SIO_010035", (subject, object) -> {
            Integer geneID = getIntID(subject, prefix);
            checkGeneID(geneID);

            genes.reference(geneID);
            missingGenes.described(geneID);
        });
    }


    /**
     * Registers the handler that sets the gene symbols of the genes.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSymbols(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(bao + "BAO_0002870", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Integer symbolID = getGeneSymbolID(object.getURI());

            genes.set(geneID, "genesymbol", symbolID);
        });
    }


    /**
     * Registers the handler that sets the titles of the genes.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            String title = getString(object);

            genes.set(geneID, "title", title);
        });
    }


    /**
     * Registers the handler that sets the organisms of the genes.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            genes.set(geneID, "organism", organismID);
        });
    }


    /**
     * Registers the handler of the alternative labels of the genes and the action that stores their changes in
     * {@code pubchem.gene_alternatives}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the references and patents discussing the genes and the action that stores their changes
     * in {@code pubchem.gene_references} and {@code pubchem.gene_patents}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the ontology resources linked to the genes by {@code rdfs:seeAlso} (except the links to
     * the databases that have tables of their own) and the action that stores their changes in
     * {@code pubchem.gene_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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
            Pair<Integer, Integer> match = Ontology.getResourceId(object, matchUnits);

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


    /**
     * Registers the handler of the Ensembl genes linked to the genes by {@code rdfs:seeAlso} and the action that stores
     * their changes in {@code pubchem.gene_ensembl_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the MeSH descriptors linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_mesh_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the ENZYME entries of Expasy linked to the genes by {@code rdfs:seeAlso} and the action
     * that stores their changes in {@code pubchem.gene_expasy_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the MedlinePlus genes linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_medlineplus_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the Alliance of Genome Resources genes linked to the genes by {@code rdfs:seeAlso} and
     * the action that stores their changes in {@code pubchem.gene_alliancegenome_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the KEGG genes linked to the genes by {@code rdfs:seeAlso} and the action that stores
     * their changes in {@code pubchem.gene_kegg_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the Pharos targets linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_pharos_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the Bgee genes linked to the genes by {@code rdfs:seeAlso} and the action that stores
     * their changes in {@code pubchem.gene_bgee_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the PomBase genes linked to the genes by {@code rdfs:seeAlso} and the action that stores
     * their changes in {@code pubchem.gene_pombase_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the VEuPathDB genes linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_veupathdb_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the ZFIN genes linked to the genes by {@code rdfs:seeAlso} and the action that stores
     * their changes in {@code pubchem.gene_zfin_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the UniProt enzymes linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_enzyme_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the Wikidata entities linked to the genes by {@code rdfs:seeAlso} and the action that
     * stores their changes in {@code pubchem.gene_wikidata_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the biological processes (Gene Ontology) the genes participate in and the action that
     * stores their changes in {@code pubchem.gene_processes}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadProcesses(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepProcesses = new IntPairSet();
        IntPairSet newProcesses = new IntPairSet();
        IntPairSet oldProcesses = new IntPairSet();

        load("select gene,process_id from pubchem.gene_processes", oldProcesses);

        dispatcher.on(obo + "RO_0000056", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> process = Ontology.getResourceId(object, unitGO);

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


    /**
     * Registers the handler of the molecular functions (Gene Ontology) of the genes and the action that stores their
     * changes in {@code pubchem.gene_functions}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadFunctions(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepFunctions = new IntPairSet();
        IntPairSet newFunctions = new IntPairSet();
        IntPairSet oldFunctions = new IntPairSet();

        load("select gene,function_id from pubchem.gene_functions", oldFunctions);

        dispatcher.on(obo + "RO_0000085", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> function = Ontology.getResourceId(object, unitGO);

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


    /**
     * Registers the handler of the cellular components (Gene Ontology) the genes are located in and the action that
     * stores their changes in {@code pubchem.gene_locations}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadLocations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepLocations = new IntPairSet();
        IntPairSet newLocations = new IntPairSet();
        IntPairSet oldLocations = new IntPairSet();

        load("select gene,location_id from pubchem.gene_locations", oldLocations);

        dispatcher.on(obo + "RO_0001025", (subject, object) -> {
            Integer geneID = getGeneID(subject.getURI());
            Pair<Integer, Integer> location = Ontology.getResourceId(object, unitGO);

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


    /**
     * Registers the handler of the orthologs of the genes and the action that stores their changes in
     * {@code pubchem.gene_orthologs}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Loads the genes and the gene symbols from {@code pubchem/RDF/gene/pc_gene.ttl.gz} into the tables and flushes the
     * tables of the genes and the gene symbols.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Stores the genes and the gene symbols, including those referenced by the parts loaded later, and deletes the
     * stored ones that are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish genes ...");

        geneSymbols.store();

        genes.store();

        System.out.println();
    }


    /**
     * Returns the id of the gene symbol with the given IRI; a gene symbol not described by the data gets a row and is
     * counted as missing.
     *
     * @param value the IRI of the gene symbol
     * @return the id of the gene symbol
     * @throws IOException if the IRI is not an IRI of a gene symbol
     */
    static Integer getGeneSymbolID(String value) throws IOException
    {
        if(!value.startsWith(symbolPrefix))
            throw new DataException("unexpected IRI", value);

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


    /**
     * Adds the row of a gene symbol, which keeps its id if it has one.
     *
     * @param symbol the IRI of the gene symbol without the prefix
     * @return the id of the gene symbol
     * @throws IOException if the row of the gene symbol holds another IRI
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


    /**
     * Returns the id of the gene with the given IRI; a gene not described by the data gets a row and is counted as
     * missing.
     *
     * @param value the IRI of the gene
     * @return the id of the gene
     * @throws IOException if the IRI is not an IRI of a gene or the gene is refused by {@link #checkGeneID(int)}
     */
    static Integer getGeneID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer geneID = Integer.parseInt(value.substring(prefixLength));

        checkGeneID(geneID);

        if(genes.reference(geneID))
            missingGenes.referenced(geneID);

        return geneID;
    }


    /**
     * Refuses the gene ids 4, 7 and 8, whose gene IRIs are the IRIs of the gene symbols GID4, GID7 and GID8.
     *
     * @param geneID the id of the gene
     * @throws IOException if the gene id is refused
     */
    private static void checkGeneID(int geneID) throws IOException
    {
        if(geneID == 4 || geneID == 7 || geneID == 8)
            throw new DataException("unexpected gene", String.valueOf(geneID));
    }
}
