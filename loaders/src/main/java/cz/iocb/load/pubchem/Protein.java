package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.is;
import static cz.iocb.load.common.TripleDispatcher.notStartingWith;
import static cz.iocb.load.common.TripleDispatcher.startingWith;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.bao;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.obo;
import static cz.iocb.load.pubchem.PubChemRDF.pdbo;
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
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Protein extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC";
    static final int prefixLength = prefix.length();

    static final String enzymePrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_";
    static final int enzymePrefixLength = enzymePrefix.length();

    private static final EntityTable<Integer> proteins = new EntityTable<>("pubchem.proteins", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), integer("organism"), uniqueVarchar("title"),
            uniqueVarchar("sequence"));
    private static final MissingEntities<String> missingProteins = new MissingEntities<>("protein", true);
    private static final MissingEntities<String> missingEnzymes = new MissingEntities<>("enzyme", true);
    private static final StringIntMap proteinIDs = new StringIntMap();
    private static int nextProteinID;

    private static final EntityTable<Integer> enzymes = new EntityTable<>("pubchem.enzymes", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), integer("parent"), uniqueVarchar("title"));
    private static final StringIntMap enzymeIDs = new StringIntMap();
    private static int nextEnzymeID;


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(notStartingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"),
                obo + "RO_0002180", up + "encodedBy", up + "organism", up + "enzyme", vocab + "hasSimilarProtein",
                pdbo + "link_to_pdb", rdfs + "seeAlso", rdf + "type", skos + "prefLabel", skos + "altLabel",
                bao + "BAO_0002817", cito + "isDiscussedBy", dcterms + "identifier");
        dispatcher.checkPredicates(startingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"), rdf + "type",
                rdfs + "seeAlso", rdfs + "subClassOf", skos + "prefLabel", skos + "altLabel");
        dispatcher.checkTypes(startingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"), vocab + "Enzyme",
                sio + "SIO_010343");
        dispatcher.checkIdentifier(dcterms + "identifier", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
        dispatcher.checkPrefixes(all(), obo + "RO_0002180",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "https://pfam.xfam.org/family/PF",
                "https://www.ebi.ac.uk/interpro/entry/InterPro/IPR");
        dispatcher.checkPrefixes(notStartingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"), rdfs + "seeAlso",
                "http://purl.uniprot.org/uniprot/", "http://id.nlm.nih.gov/mesh/",
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C", "https://www.drugbank.ca/bio_entities/BE",
                "https://alphafold.ebi.ac.uk/entry/", "https://pharos.nih.gov/targets/",
                "https://platform.opentargets.org/target/ENSG",
                "https://wormbase.org/db/seq/protein?class=Protein;name=CBP",
                "https://wormbase.org/db/seq/protein?class=Protein;name=CE",
                "https://wormbase.org/db/seq/protein?class=Protein;name=BM",
                "https://www.brenda-enzymes.org/enzyme.php?ecno=", "https://www.ebi.ac.uk/intact/search?query=",
                "https://www.ebi.ac.uk/interpro/protein/reviewed/", "http://identifiers.org/mesh:",
                "http://identifiers.org/nextprot:NX_", "http://identifiers.org/uniprot:",
                "http://identifiers.org/refseq:", "http://identifiers.org/chembl:CHEMBL",
                "http://identifiers.org/iuphar.receptor:", "http://identifiers.org/ncit:C",
                "http://identifiers.org/hpa:ENSG", "https://glygen.org/protein/",
                "https://glycosmos.org/glycoproteins/", "http://identifiers.org/PR:",
                "http://identifiers.org/ncbiprotein:", "http://purl.obolibrary.org/obo/PR_",
                "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL", "http://www.wikidata.org/entity/Q",
                "https://glyconnect.expasy.org/all/proteins/", "https://string-db.org/network/",
                "https://www.enzyme-database.org/query.php?ec=");
        dispatcher.checkLink(startingWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"), rdfs + "seeAlso", all(),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_", "http://purl.uniprot.org/enzyme/");
        dispatcher.checkLink(all(), rdfs + "seeAlso", startingWith("http://identifiers.org/ncbiprotein:"),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC", "http://identifiers.org/ncbiprotein:");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/uniprot:", "http://purl.uniprot.org/uniprot/");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
    }


    private static void loadEnzymeBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.enzymes", enzymeIDs);

        nextEnzymeID = enzymeIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(sio + "SIO_010343", (subject, object) -> {
            String enzyme = getStringID(subject, enzymePrefix);

            addEnzyme(enzyme);
            missingEnzymes.described(enzyme);
        });
    }


    private static void loadEnzymeParents(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            if(object.getURI().equals("http://purl.uniprot.org/core/Enzyme"))
                return;

            Integer enzymeID = getEnzymeID(subject.getURI());
            Integer parentID = getEnzymeID(object.getURI());

            enzymes.set(enzymeID, "parent", parentID);
        });
    }


    private static void loadEnzymeTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            if(!startsWith(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                return;

            Integer enzymeID = getEnzymeID(subject.getURI());
            String title = getString(object);

            enzymes.set(enzymeID, "title", title);
        });
    }


    private static void loadEnzymeAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select enzyme,alternative from pubchem.enzyme_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            if(!startsWith(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"))
                return;

            Integer enzymeID = getEnzymeID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(enzymeID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.enzyme_alternatives where enzyme=? and alternative=?", oldAlternatives);
            store("insert into pubchem.enzyme_alternatives(enzyme,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadProteinBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.proteins", proteinIDs);

        nextProteinID = proteinIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(vocab + "Protein", (subject, object) -> {
            String protein = getStringID(subject, prefix);

            addProtein(protein);
            missingProteins.described(protein);
        });
    }


    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            proteins.set(proteinID, "organism", organismID);
        });
    }


    private static void loadProteinTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            if(!startsWith(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String title = getString(object);

            // workaround
            if(title.isEmpty())
            {
                Problems.warning("empty " + skos + "prefLabel", text(subject));
                return;
            }

            proteins.set(proteinID, "title", title);
        });
    }


    private static void loadSequences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(bao + "BAO_0002817", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());
            String sequence = getString(object);

            proteins.set(proteinID, "sequence", sequence);
        });
    }


    private static void loadProteinAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select protein,alternative from pubchem.protein_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            if(!startsWith(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(proteinID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_alternatives where protein=? and alternative=?", oldAlternatives);
            store("insert into pubchem.protein_alternatives(protein,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadPdbLinks(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepPdbLinks = new IntStringSet();
        IntStringSet newPdbLinks = new IntStringSet();
        IntStringSet oldPdbLinks = new IntStringSet();

        load("select protein,pdblink from pubchem.protein_pdblinks", oldPdbLinks);

        dispatcher.on(pdbo + "link_to_pdb", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());
            String pdblinkID = getStringID(object, "http://rdf.wwpdb.org/pdb/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, pdblinkID);

            if(oldPdbLinks.remove(pair))
                keepPdbLinks.add(pair);
            else if(!keepPdbLinks.contains(pair))
                newPdbLinks.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_pdblinks where protein=? and pdblink=?", oldPdbLinks);
            store("insert into pubchem.protein_pdblinks(protein,pdblink) values(?,?)", newPdbLinks);
        });
    }


    private static void loadSimilarProteins(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepSimilarProteins = new IntPairSet();
        IntPairSet newSimilarProteins = new IntPairSet();
        IntPairSet oldSimilarProteins = new IntPairSet();

        load("select protein,similar_protein from pubchem.protein_similar_proteins", oldSimilarProteins);

        dispatcher.on(vocab + "hasSimilarProtein", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());
            Integer simproteinID = getProteinID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, simproteinID);

            if(oldSimilarProteins.remove(pair))
                keepSimilarProteins.add(pair);
            else if(!keepSimilarProteins.contains(pair))
                newSimilarProteins.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_similar_proteins where protein=? and similar_protein=?",
                    oldSimilarProteins);
            store("insert into pubchem.protein_similar_proteins(protein,similar_protein) values(?,?)",
                    newSimilarProteins);
        });
    }


    private static void loadGenes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepGenes = new IntPairSet();
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = new IntPairSet();

        load("select protein,gene from pubchem.protein_genes", oldGenes);

        dispatcher.on(up + "encodedBy", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());
            Integer geneID = Gene.getGeneID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, geneID);

            if(oldGenes.remove(pair))
                keepGenes.add(pair);
            else if(!keepGenes.contains(pair))
                newGenes.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_genes where protein=? and gene=?", oldGenes);
            store("insert into pubchem.protein_genes(protein,gene) values(?,?)", newGenes);
        });
    }


    private static void loadEnzymes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepEnzymes = new IntStringSet();
        IntStringSet newEnzymes = new IntStringSet();
        IntStringSet oldEnzymes = new IntStringSet();

        load("select protein,enzyme from pubchem.protein_uniprot_enzymes", oldEnzymes);

        IntPairSet keepProteinEnzymes = new IntPairSet();
        IntPairSet newProteinEnzymes = new IntPairSet();
        IntPairSet oldProteinEnzymes = new IntPairSet();

        load("select protein,enzyme from pubchem.protein_enzymes", oldProteinEnzymes);

        dispatcher.on(up + "enzyme", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());

            if(object.getURI().startsWith("http://purl.uniprot.org/enzyme/"))
            {
                String enzymeID = getStringID(object, "http://purl.uniprot.org/enzyme/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, enzymeID);

                if(oldEnzymes.remove(pair))
                    keepEnzymes.add(pair);
                else if(!keepEnzymes.contains(pair))
                    newEnzymes.add(pair);
            }
            else
            {
                Integer enzymeID = getEnzymeID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, enzymeID);

                if(oldProteinEnzymes.remove(pair))
                    keepProteinEnzymes.add(pair);
                else if(!keepProteinEnzymes.contains(pair))
                    newProteinEnzymes.add(pair);
            }
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_uniprot_enzymes where protein=? and enzyme=?", oldEnzymes);
            store("insert into pubchem.protein_uniprot_enzymes(protein,enzyme) values(?,?)", newEnzymes);

            store("delete from pubchem.protein_enzymes where protein=? and enzyme=?", oldProteinEnzymes);
            store("insert into pubchem.protein_enzymes(protein,enzyme) values(?,?)", newProteinEnzymes);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select protein,match_unit,match_id from pubchem.protein_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, "http://identifiers.org/refseq:", "http://purl.uniprot.org/uniprot/",
                    "http://identifiers.org/uniprot:", "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:",
                    "http://identifiers.org/nextprot:NX_", "https://glygen.org/protein/",
                    "https://glycosmos.org/glycoproteins/", "https://alphafold.ebi.ac.uk/entry/",
                    "https://pharos.nih.gov/targets/", "http://identifiers.org/PR:",
                    "https://wormbase.org/db/seq/protein?name=", "https://www.brenda-enzymes.org/enzyme.php?ecno=",
                    "https://www.ebi.ac.uk/intact/search?query=", "https://www.ebi.ac.uk/interpro/protein/reviewed/",
                    "http://identifiers.org/ncbiprotein:", "http://rdf.ebi.ac.uk/resource/chembl/target/",
                    "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL", "http://purl.uniprot.org/enzyme/",
                    "http://www.wikidata.org/entity/Q", "https://string-db.org/network/",
                    "https://www.enzyme-database.org/query.php?ec="))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getResourceId(object, null);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_matches where protein=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.protein_matches(protein,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadNcbiCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_ncbi_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/refseq:"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/refseq:");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_ncbi_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_ncbi_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadUniprotCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_uniprot_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://purl.uniprot.org/uniprot/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "http://purl.uniprot.org/uniprot/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_uniprot_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_uniprot_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_mesh_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://id.nlm.nih.gov/mesh/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_mesh_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_mesh_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadGlygenCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_glygen_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://glygen.org/protein/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://glygen.org/protein/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_glygen_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_glygen_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadGlycosmosCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_glycosmos_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://glycosmos.org/glycoproteins/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://glycosmos.org/glycoproteins/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_glycosmos_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_glycosmos_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadAlphafoldCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_alphafold_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://alphafold.ebi.ac.uk/entry/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://alphafold.ebi.ac.uk/entry/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_alphafold_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_alphafold_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadPharosCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_pharos_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://pharos.nih.gov/targets/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://pharos.nih.gov/targets/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_pharos_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_pharos_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadProconsortiumCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_proconsortium_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/PR:"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/PR:");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_proconsortium_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_proconsortium_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadWormbaseCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_wormbase_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://wormbase.org/db/seq/protein?name="))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://wormbase.org/db/seq/protein?name=", ";class=Protein");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_wormbase_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_wormbase_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadBrendaCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_brenda_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.brenda-enzymes.org/enzyme.php?ecno="))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://www.brenda-enzymes.org/enzyme.php?ecno=");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_brenda_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_brenda_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadIntactCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_intact_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.ebi.ac.uk/intact/search?query="))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://www.ebi.ac.uk/intact/search?query=");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_intact_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_intact_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadInterproProteinCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_interpro_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.ebi.ac.uk/interpro/protein/reviewed/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://www.ebi.ac.uk/interpro/protein/reviewed/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_interpro_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_interpro_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadNextprotCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_nextprot_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/nextprot:NX_"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/nextprot:NX_");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_nextprot_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_nextprot_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadStringDbCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_stringdb_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://string-db.org/network/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://string-db.org/network/");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_stringdb_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_stringdb_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadEnzymeDatabaseCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_enzymedatabase_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "https://www.enzyme-database.org/query.php?ec="))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            String match = getStringID(object, "https://www.enzyme-database.org/query.php?ec=");

            Pair<Integer, String> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_enzymedatabase_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_enzymedatabase_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadChemblCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_chembl_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer match = getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL");

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_chembl_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_chembl_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadWikidataCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_wikidata_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://www.wikidata.org/entity/Q"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer match = getIntID(object, "http://www.wikidata.org/entity/Q");

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_wikidata_matches where protein=? and match=?", oldMatches);
            store("insert into pubchem.protein_wikidata_matches(protein,match) values(?,?)", newMatches);
        });
    }


    private static void loadConservedDomains(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepDomains = new IntPairSet();
        IntPairSet newDomains = new IntPairSet();
        IntPairSet oldDomains = new IntPairSet();

        load("select protein,conserveddomain from pubchem.protein_conserveddomains", oldDomains);

        dispatcher.on(obo + "RO_0002180", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer domainID = ConservedDomain.getDomainID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, domainID);

            if(oldDomains.remove(pair))
                keepDomains.add(pair);
            else if(!keepDomains.contains(pair))
                newDomains.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_conserveddomains where protein=? and conserveddomain=?", oldDomains);
            store("insert into pubchem.protein_conserveddomains(protein,conserveddomain) values(?,?)", newDomains);
        });
    }


    private static void loadContinuantParts(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepContinuantParts = new IntPairSet();
        IntPairSet newContinuantParts = new IntPairSet();
        IntPairSet oldContinuantParts = new IntPairSet();

        load("select protein,part from pubchem.protein_continuant_parts", oldContinuantParts);

        dispatcher.on(obo + "RO_0002180", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer partID = getProteinID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, partID);

            if(oldContinuantParts.remove(pair))
                keepContinuantParts.add(pair);
            else if(!keepContinuantParts.contains(pair))
                newContinuantParts.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_continuant_parts where protein=? and part=?", oldContinuantParts);
            store("insert into pubchem.protein_continuant_parts(protein,part) values(?,?)", newContinuantParts);
        });
    }


    private static void loadFamilies(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepFamilies = new IntPairSet();
        IntPairSet newFamilies = new IntPairSet();
        IntPairSet oldFamilies = new IntPairSet();

        load("select protein,family from pubchem.protein_families", oldFamilies);

        dispatcher.on(obo + "RO_0002180", (subject, object) -> {
            if(!startsWith(object, "https://pfam.xfam.org/family/PF"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer familyID = getIntID(object, "https://pfam.xfam.org/family/PF");

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, familyID);

            if(oldFamilies.remove(pair))
                keepFamilies.add(pair);
            else if(!keepFamilies.contains(pair))
                newFamilies.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_families where protein=? and family=?", oldFamilies);
            store("insert into pubchem.protein_families(protein,family) values(?,?)", newFamilies);
        });
    }


    private static void loadInterProFamilies(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepFamilies = new IntPairSet();
        IntPairSet newFamilies = new IntPairSet();
        IntPairSet oldFamilies = new IntPairSet();

        load("select protein,family from pubchem.protein_interpro_families", oldFamilies);

        dispatcher.on(obo + "RO_0002180", (subject, object) -> {
            if(!startsWith(object, "https://www.ebi.ac.uk/interpro/entry/InterPro/IPR"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Integer familyID = getIntID(object, "https://www.ebi.ac.uk/interpro/entry/InterPro/IPR");

            Pair<Integer, Integer> pair = Pair.getPair(proteinID, familyID);

            if(oldFamilies.remove(pair))
                keepFamilies.add(pair);
            else if(!keepFamilies.contains(pair))
                newFamilies.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_interpro_families where protein=? and family=?", oldFamilies);
            store("insert into pubchem.protein_interpro_families(protein,family) values(?,?)", newFamilies);
        });
    }


    private static void loadTypes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepTypes = new IntIntPairSet();
        IntIntPairSet newTypes = new IntIntPairSet();
        IntIntPairSet oldTypes = new IntIntPairSet();

        load("select protein,type_unit,type_id from pubchem.protein_types", oldTypes);

        dispatcher.on(rdf + "type", (subject, object) -> {
            if(is(object, vocab + "Protein"))
                return;

            if(!startsWith(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"))
                return;

            Integer proteinID = getProteinID(subject.getURI());
            Pair<Integer, Integer> type = Ontology.getResourceId(object, null);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(proteinID, type);

            if(oldTypes.remove(pair))
                keepTypes.add(pair);
            else if(!keepTypes.contains(pair))
                newTypes.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_types where protein=? and type_unit=? and type_id=?", oldTypes);
            store("insert into pubchem.protein_types(protein,type_unit,type_id) values(?,?,?)", newTypes);
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

        load("select protein,reference from pubchem.protein_references", oldReferences);
        load("select protein,patent from pubchem.protein_patents", oldPatents);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer proteinID = getProteinID(subject.getURI());

            if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
            {
                Integer referenceID = Reference.getReferenceID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, referenceID);

                if(oldReferences.remove(pair))
                    keepReferences.add(pair);
                else if(!keepReferences.contains(pair))
                    newReferences.add(pair);
            }
            else
            {
                Integer patentID = Patent.getPatentID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, patentID);

                if(oldPatents.remove(pair))
                    keepPatents.add(pair);
                else if(!keepPatents.contains(pair))
                    newPatents.add(pair);
            }
        });

        dispatcher.after(() -> {
            store("delete from pubchem.protein_references where protein=? and reference=?", oldReferences);
            store("insert into pubchem.protein_references(protein,reference) values(?,?)", newReferences);

            store("delete from pubchem.protein_patents where protein=? and patent=?", oldPatents);
            store("insert into pubchem.protein_patents(protein,patent) values(?,?)", newPatents);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load proteins ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadEnzymeBases(dispatcher);
        loadEnzymeTitles(dispatcher);
        loadEnzymeParents(dispatcher);
        loadEnzymeAlternatives(dispatcher);
        loadProteinBases(dispatcher);
        loadOrganisms(dispatcher);
        loadProteinTitles(dispatcher);
        loadSequences(dispatcher);
        loadProteinAlternatives(dispatcher);
        loadPdbLinks(dispatcher);
        loadSimilarProteins(dispatcher);
        loadGenes(dispatcher);
        loadEnzymes(dispatcher);
        loadCloseMatches(dispatcher);
        loadNcbiCloseMatches(dispatcher);
        loadUniprotCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadGlygenCloseMatches(dispatcher);
        loadGlycosmosCloseMatches(dispatcher);
        loadAlphafoldCloseMatches(dispatcher);
        loadPharosCloseMatches(dispatcher);
        loadProconsortiumCloseMatches(dispatcher);
        loadWormbaseCloseMatches(dispatcher);
        loadBrendaCloseMatches(dispatcher);
        loadIntactCloseMatches(dispatcher);
        loadInterproProteinCloseMatches(dispatcher);
        loadNextprotCloseMatches(dispatcher);
        loadStringDbCloseMatches(dispatcher);
        loadEnzymeDatabaseCloseMatches(dispatcher);
        loadChemblCloseMatches(dispatcher);
        loadWikidataCloseMatches(dispatcher);
        loadConservedDomains(dispatcher);
        loadContinuantParts(dispatcher);
        loadFamilies(dispatcher);
        loadInterProFamilies(dispatcher);
        loadTypes(dispatcher);
        loadReferences(dispatcher);

        dispatcher.load("pubchem/RDF/protein", "pc_protein_[0-9]+\\.ttl\\.gz");
        missingEnzymes.settle();
        missingProteins.settle();
        dispatcher.finish();

        enzymes.flush();
        proteins.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish proteins ...");

        enzymes.store();

        proteins.store();

        System.out.println();
    }


    static Integer getEnzymeID(String value) throws IOException
    {
        if(!value.startsWith(enzymePrefix))
            throw new DataException("unexpected IRI", value);

        String enzyme = value.substring(enzymePrefixLength);

        synchronized(enzymeIDs)
        {
            Integer enzymeID = enzymeIDs.get(enzyme);

            if(enzymeID != null && enzymes.contains(enzymeID))
                return enzymeID;

            missingEnzymes.referenced(enzyme);

            return addEnzyme(enzyme);
        }
    }


    /*
     * Adds the row of an enzyme, which keeps its id if it has one.
     */
    private static Integer addEnzyme(String enzyme) throws IOException
    {
        synchronized(enzymeIDs)
        {
            Integer enzymeID = enzymeIDs.get(enzyme);

            if(enzymeID == null)
                enzymeIDs.put(enzyme, enzymeID = nextEnzymeID++);

            enzymes.set(enzymeID, "iri", enzyme);

            return enzymeID;
        }
    }


    static Integer getProteinID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String protein = value.substring(prefixLength);

        synchronized(proteinIDs)
        {
            Integer proteinID = proteinIDs.get(protein);

            if(proteinID != null && proteins.contains(proteinID))
                return proteinID;

            missingProteins.referenced(protein);

            return addProtein(protein);
        }
    }


    /*
     * Adds the row of a protein, which keeps its id if it has one.
     */
    private static Integer addProtein(String protein) throws IOException
    {
        synchronized(proteinIDs)
        {
            Integer proteinID = proteinIDs.get(protein);

            if(proteinID == null)
                proteinIDs.put(protein, proteinID = nextProteinID++);

            proteins.set(proteinID, "iri", protein);

            return proteinID;
        }
    }
}
