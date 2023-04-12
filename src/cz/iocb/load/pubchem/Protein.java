package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



class Protein extends Updater
{
    private static StringIntMap usedProteins;
    private static StringIntMap newProteins;
    private static StringIntMap oldProteins;
    private static int nextProteinID;

    private static StringIntMap usedEnzymes;
    private static StringIntMap newEnzymes;
    private static StringIntMap oldEnzymes;
    private static int nextEnzymeID;


    private static void loadEnzymeBases(Model model) throws IOException, SQLException
    {
        usedEnzymes = new StringIntMap();
        newEnzymes = new StringIntMap();
        oldEnzymes = getStringIntMap("select iri, id from pubchem.enzyme_bases");
        nextEnzymeID = getIntValue("select coalesce(max(id)+1,0) from pubchem.enzyme_bases");

        new QueryResultProcessor(patternQuery("?enzyme rdf:type sio:SIO_010343"))
        {
            @Override
            protected void parse() throws IOException
            {
                String enzyme = getStringID("enzyme", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_");
                int enzymeID = oldEnzymes.removeKeyIfAbsent(enzyme, NO_VALUE);

                if(enzymeID == NO_VALUE)
                    newEnzymes.put(enzyme, enzymeID = nextEnzymeID++);

                usedEnzymes.put(enzyme, enzymeID);
            }
        }.load(model);

        batch("insert into pubchem.enzyme_bases(iri, id) values (?,?)", newEnzymes);
        newEnzymes.clear();
    }


    private static void loadEnzymeParents(Model model) throws IOException, SQLException
    {
        IntStringIntPairMap newParents = new IntStringIntPairMap();
        IntIntHashMap oldParents = getIntIntMap("select id, parent from pubchem.enzyme_bases where parent is not null");

        new QueryResultProcessor(patternQuery("?enzyme rdfs:subClassOf ?parent"))
        {
            @Override
            protected void parse() throws IOException
            {
                if(getIRI("parent").equals("http://purl.uniprot.org/core/Enzyme"))
                    return;

                String enzyme = getStringID("enzyme", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_");
                int enzymeID = getEnzymeID(enzyme);
                int parentID = getEnzymeID(getStringID("parent", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));

                if(enzymeID != parentID && parentID != oldParents.removeKeyIfAbsent(enzymeID, NO_VALUE))
                    newParents.put(enzymeID, Tuples.pair(enzyme, parentID));
            }
        }.load(model);

        batch("update pubchem.enzyme_bases set parent = null where id = ?", oldParents.keySet());
        batch("insert into pubchem.enzyme_bases(id, iri, parent) values (?,?,?) "
                + "on conflict (id) do update set parent=EXCLUDED.parent", newParents);
    }


    private static void loadEnzymeTitles(Model model) throws IOException, SQLException
    {
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.enzyme_bases where title is not null");

        new QueryResultProcessor(patternQuery("?enzyme skos:prefLabel ?title. "
                + "filter(strstarts(str(?enzyme), \"http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_\"))"))
        {
            @Override
            protected void parse() throws IOException
            {
                String enzyme = getStringID("enzyme", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_");
                int enzymeID = getEnzymeID(enzyme);
                String title = getString("title");

                if(!title.equals(oldTitles.remove(enzymeID)))
                    newTitles.put(enzymeID, Tuples.pair(enzyme, title));
            }
        }.load(model);

        batch("update pubchem.enzyme_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.enzyme_bases(id, iri, title) values (?,?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadEnzymeAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select enzyme, alternative from pubchem.enzyme_alternatives");

        new QueryResultProcessor(patternQuery("?enzyme skos:altLabel ?alternative. "
                + "filter(strstarts(str(?enzyme), \"http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_\"))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int enzymeID = getEnzymeID(getStringID("enzyme", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(enzymeID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.enzyme_alternatives where enzyme = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.enzyme_alternatives(enzyme, alternative) values (?,?)", newAlternatives);
    }


    private static void loadProteinBases(Model model) throws IOException, SQLException
    {
        usedProteins = new StringIntMap();
        newProteins = new StringIntMap();
        oldProteins = getStringIntMap("select iri, id from pubchem.protein_bases");
        nextProteinID = getIntValue("select coalesce(max(id)+1,0) from pubchem.protein_bases");

        new QueryResultProcessor(patternQuery("?protein rdf:type sio:SIO_010043"))
        {
            @Override
            protected void parse() throws IOException
            {
                String protein = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                int proteinID = oldProteins.removeKeyIfAbsent(protein, NO_VALUE);

                if(proteinID == NO_VALUE)
                    newProteins.put(protein, proteinID = nextProteinID++);

                usedProteins.put(protein, proteinID);
            }
        }.load(model);

        batch("insert into pubchem.protein_bases(iri, id) values (?,?)", newProteins);
        newProteins.clear();
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntStringIntPairMap newOrganisms = new IntStringIntPairMap();
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism from pubchem.protein_bases where organism is not null");

        new QueryResultProcessor(patternQuery("?protein up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(getIRI("organism").equals("http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"))
                    return;

                String protein = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                int proteinID = getProteinID(protein);
                int organismID = Taxonomy
                        .getTaxonomyID(getIntID("organism", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));

                if(organismID != oldOrganisms.removeKeyIfAbsent(proteinID, NO_VALUE))
                    newOrganisms.put(proteinID, Tuples.pair(protein, organismID));
            }
        }.load(model);

        batch("update pubchem.protein_bases set organism = null where id = ?", oldOrganisms.keySet());
        batch("insert into pubchem.protein_bases(id, iri, organism) values (?,?,?) "
                + "on conflict (id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadProteinTitles(Model model) throws IOException, SQLException
    {
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.protein_bases where title is not null");

        new QueryResultProcessor(patternQuery("?protein skos:prefLabel ?title. "
                + "filter(strstarts(str(?protein), \"http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC\"))"))
        {
            @Override
            protected void parse() throws IOException
            {
                String protein = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                int proteinID = getProteinID(protein);
                String title = getString("title");

                // workaround
                if(title.isEmpty())
                    return;

                if(!title.equals(oldTitles.remove(proteinID)))
                    newTitles.put(proteinID, Tuples.pair(protein, title));
            }
        }.load(model);

        batch("update pubchem.protein_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.protein_bases(id, iri, title) values (?,?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadSequences(Model model) throws IOException, SQLException
    {
        IntStringPairMap newSequences = new IntStringPairMap();
        IntStringMap oldSequences = getIntStringMap(
                "select id, sequence from pubchem.protein_bases where sequence is not null");

        new QueryResultProcessor(patternQuery("?protein bao:BAO_0002817 ?sequence"))
        {
            @Override
            protected void parse() throws IOException
            {
                String protein = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                int proteinID = getProteinID(protein);
                String sequence = getString("sequence");

                if(!sequence.equals(oldSequences.remove(proteinID)))
                    newSequences.put(proteinID, Tuples.pair(protein, sequence));
            }
        }.load(model);

        batch("update pubchem.protein_bases set sequence = null where id = ?", oldSequences.keySet());
        batch("insert into pubchem.protein_bases(id, iri, sequence) values (?,?,?) "
                + "on conflict (id) do update set sequence=EXCLUDED.sequence", newSequences);
    }


    private static void loadProteinAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select protein, alternative from pubchem.protein_alternatives");

        new QueryResultProcessor(patternQuery("?protein skos:altLabel ?alternative. "
                + "filter(strstarts(str(?protein), \"http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC\"))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_alternatives where protein = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.protein_alternatives(protein, alternative) values (?,?)", newAlternatives);
    }


    private static void loadPdbLinks(Model model) throws IOException, SQLException
    {
        IntStringPairSet newPdbLinks = new IntStringPairSet();
        IntStringPairSet oldPdbLinks = getIntStringPairSet("select protein, pdblink from pubchem.protein_pdblinks");

        new QueryResultProcessor(patternQuery("?protein pdbo40:link_to_pdb ?pdblink"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String pdblinkID = getStringID("pdblink", "http://rdf.wwpdb.org/pdb/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, pdblinkID);

                if(!oldPdbLinks.remove(pair))
                    newPdbLinks.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_pdblinks where protein = ? and pdblink = ?", oldPdbLinks);
        batch("insert into pubchem.protein_pdblinks(protein, pdblink) values (?,?)", newPdbLinks);
    }


    private static void loadSimilarProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newSimilarProteins = new IntPairSet();
        IntPairSet oldSimilarProteins = getIntPairSet(
                "select protein, simprotein from pubchem.protein_similarproteins");

        new QueryResultProcessor(patternQuery("?protein vocab:hasSimilarProtein ?similar"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int simproteinID = getProteinID(
                        getStringID("similar", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));

                IntIntPair pair = PrimitiveTuples.pair(proteinID, simproteinID);

                if(!oldSimilarProteins.remove(pair))
                    newSimilarProteins.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_similarproteins where protein = ? and simprotein = ?", oldSimilarProteins);
        batch("insert into pubchem.protein_similarproteins(protein, simprotein) values (?,?)", newSimilarProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = getIntPairSet("select protein, gene from pubchem.protein_genes");

        new QueryResultProcessor(patternQuery("?protein up:encodedBy ?gene"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, geneID);
                Gene.addGeneID(geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_genes where protein = ? and gene = ?", oldGenes);
        batch("insert into pubchem.protein_genes(protein, gene) values (?,?)", newGenes);
    }


    private static void loadEnzymes(Model model) throws IOException, SQLException
    {
        IntStringPairSet newEnzymes = new IntStringPairSet();
        IntStringPairSet oldEnzymes = getIntStringPairSet(
                "select protein, enzyme from pubchem.protein_uniprot_enzymes");

        IntPairSet newProteinEnzymes = new IntPairSet();
        IntPairSet oldProteinEnzymes = getIntPairSet("select protein, enzyme from pubchem.protein_enzymes");

        new QueryResultProcessor(patternQuery("?protein up:enzyme ?enzyme"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));

                if(getIRI("enzyme").startsWith("http://purl.uniprot.org/enzyme/"))
                {
                    String enzymeID = getStringID("enzyme", "http://purl.uniprot.org/enzyme/");

                    IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, enzymeID);

                    if(!oldEnzymes.remove(pair))
                        newEnzymes.add(pair);
                }
                else
                {
                    int enzymeID = getEnzymeID(
                            getStringID("enzyme", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));

                    IntIntPair pair = PrimitiveTuples.pair(proteinID, enzymeID);

                    if(!oldProteinEnzymes.remove(pair))
                        newProteinEnzymes.add(pair);
                }
            }
        }.load(model);

        batch("delete from pubchem.protein_uniprot_enzymes where protein = ? and enzyme = ?", oldEnzymes);
        batch("insert into pubchem.protein_uniprot_enzymes(protein, enzyme) values (?,?)", newEnzymes);

        batch("delete from pubchem.protein_enzymes where protein = ? and enzyme = ?", oldProteinEnzymes);
        batch("insert into pubchem.protein_enzymes(protein, enzyme) values (?,?)", newProteinEnzymes);
    }


    private static void loadNcbiCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select protein, match from pubchem.protein_ncbi_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.ncbi.nlm.nih.gov/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "https://www.ncbi.nlm.nih.gov/protein/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_ncbi_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_ncbi_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadUniprotCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select protein, match from pubchem.protein_uniprot_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://purl.uniprot.org/uniprot/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "http://purl.uniprot.org/uniprot/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_uniprot_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_uniprot_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select protein, match from pubchem.protein_mesh_matches");

        new QueryResultProcessor(patternQuery(
                "?protein skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_mesh_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_mesh_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadThesaurusCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select protein, match from pubchem.protein_thesaurus_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));

                Identifier match = Ontology.getId(getIRI("match"));

                if(match.unit != Ontology.unitThesaurus)
                    throw new IOException();

                IntIntPair pair = PrimitiveTuples.pair(proteinID, match.id);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_thesaurus_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_thesaurus_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadGuidetopharmacologyCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select protein, match from pubchem.protein_guidetopharmacology_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId='))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int match = getIntID("match", "https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId=");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_guidetopharmacology_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_guidetopharmacology_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadDrugbankCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select protein, match from pubchem.protein_drugbank_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.drugbank.ca/bio_entities/BE'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int match = getIntID("match", "https://www.drugbank.ca/bio_entities/BE");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_drugbank_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_drugbank_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadChemblCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select protein, match from pubchem.protein_chembl_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int match = getIntID("match", "https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_chembl_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_chembl_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadGlygenCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select protein, match from pubchem.protein_glygen_matches");

        new QueryResultProcessor(patternQuery(
                "?protein skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'https://glygen.org/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "https://glygen.org/protein/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_glygen_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_glygen_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadGlycosmosCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet(
                "select protein, match from pubchem.protein_glycosmos_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://glycosmos.org/glycoproteins/show/uniprot/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "https://glycosmos.org/glycoproteins/show/uniprot/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_glycosmos_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_glycosmos_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadAlphafoldCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet(
                "select protein, match from pubchem.protein_alphafold_matches");

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://alphafold.ebi.ac.uk/entry/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                String match = getStringID("match", "https://alphafold.ebi.ac.uk/entry/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_alphafold_matches where protein = ? and match = ?", oldMatches);
        batch("insert into pubchem.protein_alphafold_matches(protein, match) values (?,?)", newMatches);
    }


    private static void loadConservedDomains(Model model) throws IOException, SQLException
    {
        IntPairSet newDomains = new IntPairSet();
        IntPairSet oldDomains = getIntPairSet("select protein, domain from pubchem.protein_conserveddomains");

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?domain "
                + "filter(strstarts(str(?domain), 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int domainID = ConservedDomain
                        .getDomainID(getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));

                IntIntPair pair = PrimitiveTuples.pair(proteinID, domainID);

                if(!oldDomains.remove(pair))
                    newDomains.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_conserveddomains where protein = ? and domain = ?", oldDomains);
        batch("insert into pubchem.protein_conserveddomains(protein, domain) values (?,?)", newDomains);
    }


    private static void loadContinuantParts(Model model) throws IOException, SQLException
    {
        IntPairSet newContinuantParts = new IntPairSet();
        IntPairSet oldContinuantParts = getIntPairSet("select protein, part from pubchem.protein_continuantparts");

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?part "
                + "filter(strstarts(str(?part), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int partID = getProteinID(getStringID("part", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));

                IntIntPair pair = PrimitiveTuples.pair(proteinID, partID);

                if(!oldContinuantParts.remove(pair))
                    newContinuantParts.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_continuantparts where protein = ? and part = ?", oldContinuantParts);
        batch("insert into pubchem.protein_continuantparts(protein, part) values (?,?)", newContinuantParts);
    }


    private static void loadFamilies(Model model) throws IOException, SQLException
    {
        IntPairSet newFamilies = new IntPairSet();
        IntPairSet oldFamilies = getIntPairSet("select protein, family from pubchem.protein_families");

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?family "
                + "filter(strstarts(str(?family), 'https://pfam.xfam.org/family/PF'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                int familyID = getIntID("family", "https://pfam.xfam.org/family/PF");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, familyID);

                if(!oldFamilies.remove(pair))
                    newFamilies.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_families where protein = ? and family = ?", oldFamilies);
        batch("insert into pubchem.protein_families(protein, family) values (?,?)", newFamilies);
    }


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        IntTripletSet newTypes = new IntTripletSet();
        IntTripletSet oldTypes = getIntTripletSet("select protein, type_unit, type_id from pubchem.protein_types");

        new QueryResultProcessor(patternQuery("?protein rdf:type ?type."
                + "filter(strstarts(str(?protein), \"http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC\"))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = getProteinID(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
                Identifier type = Ontology.getId(getIRI("type"));

                IntTriplet triplet = new IntTriplet(proteinID, type.unit, type.id);

                if(!oldTypes.remove(triplet))
                    newTypes.add(triplet);
            }
        }.load(model);

        batch("delete from pubchem.protein_types where protein = ? and type_unit = ? and type_id = ?", oldTypes);
        batch("insert into pubchem.protein_types(protein, type_unit, type_id) values (?,?,?)", newTypes);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load proteins ...");

        Model model = getModel("pubchem/RDF/protein/pc_protein.ttl.gz");
        check(model, "pubchem/protein/check.sparql");

        loadEnzymeBases(model);
        loadEnzymeTitles(model);
        loadEnzymeParents(model);
        loadEnzymeAlternatives(model);

        loadProteinBases(model);
        loadOrganisms(model);
        loadProteinTitles(model);
        loadSequences(model);
        loadProteinAlternatives(model);
        loadPdbLinks(model);
        loadSimilarProteins(model);
        loadGenes(model);
        loadEnzymes(model);
        loadNcbiCloseMatches(model);
        loadUniprotCloseMatches(model);
        loadMeshCloseMatches(model);
        loadThesaurusCloseMatches(model);
        loadGuidetopharmacologyCloseMatches(model);
        loadDrugbankCloseMatches(model);
        loadChemblCloseMatches(model);
        loadGlygenCloseMatches(model);
        loadGlycosmosCloseMatches(model);
        loadAlphafoldCloseMatches(model);
        loadConservedDomains(model);
        loadContinuantParts(model);
        loadFamilies(model);
        loadTypes(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish proteins ...");

        batch("delete from pubchem.enzyme_bases where id = ?", oldEnzymes.values());
        batch("insert into pubchem.enzyme_bases(iri, id) values (?,?)" + " on conflict do nothing", newEnzymes);

        usedEnzymes = null;
        newEnzymes = null;
        oldEnzymes = null;


        batch("delete from pubchem.protein_bases where id = ?", oldProteins.values());
        batch("insert into pubchem.protein_bases(iri, id) values (?,?)" + " on conflict do nothing", newProteins);

        usedProteins = null;
        newProteins = null;
        oldProteins = null;

        System.out.println();
    }


    static int getEnzymeID(String enzyme) throws IOException
    {
        synchronized(newEnzymes)
        {
            int enzymeID = usedEnzymes.getIfAbsent(enzyme, NO_VALUE);

            if(enzymeID == NO_VALUE)
            {
                System.out.println("    add missing enzyme " + enzyme);

                if((enzymeID = oldEnzymes.removeKeyIfAbsent(enzyme, NO_VALUE)) == NO_VALUE)
                    newEnzymes.put(enzyme, enzymeID = nextEnzymeID++);

                usedEnzymes.put(enzyme, enzymeID);
            }

            return enzymeID;
        }
    }


    static int getProteinID(String protein) throws IOException
    {
        synchronized(newProteins)
        {
            int proteinID = usedProteins.getIfAbsent(protein, NO_VALUE);

            if(proteinID == NO_VALUE)
            {
                System.out.println("    add missing protein " + protein);

                if((proteinID = oldProteins.removeKeyIfAbsent(protein, NO_VALUE)) == NO_VALUE)
                    newProteins.put(protein, proteinID = nextProteinID++);

                usedProteins.put(protein, proteinID);
            }

            return proteinID;
        }
    }
}
