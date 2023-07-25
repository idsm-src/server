package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Protein extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC";
    static final int prefixLength = prefix.length();

    static final String enzymePrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_";
    static final int enzymePrefixLength = enzymePrefix.length();

    private static final StringIntMap keepProteins = new StringIntMap();
    private static final StringIntMap newProteins = new StringIntMap();
    private static final StringIntMap oldProteins = new StringIntMap();
    private static int nextProteinID;

    private static final StringIntMap keepEnzymes = new StringIntMap();
    private static final StringIntMap newEnzymes = new StringIntMap();
    private static final StringIntMap oldEnzymes = new StringIntMap();
    private static int nextEnzymeID;


    private static void loadEnzymeBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.enzyme_bases", oldEnzymes);

        nextEnzymeID = oldEnzymes.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?enzyme rdf:type sio:SIO_010343"))
        {
            @Override
            protected void parse() throws IOException
            {
                String enzyme = getStringID("enzyme", enzymePrefix);
                Integer enzymeID = oldEnzymes.remove(enzyme);

                if(enzymeID == null)
                    newEnzymes.put(enzyme, nextEnzymeID++);
                else
                    keepEnzymes.put(enzyme, enzymeID);
            }
        }.load(model);
    }


    private static void loadEnzymeParents(Model model) throws IOException, SQLException
    {
        IntIntMap keepParents = new IntIntMap();
        IntStringIntPairMap newParents = new IntStringIntPairMap();
        IntIntMap oldParents = new IntIntMap();

        load("select id,parent from pubchem.enzyme_bases where parent is not null", oldParents);

        new QueryResultProcessor(patternQuery("?enzyme rdfs:subClassOf ?parent"))
        {
            @Override
            protected void parse() throws IOException
            {
                if(getIRI("parent").equals("http://purl.uniprot.org/core/Enzyme"))
                    return;

                Integer enzymeID = getEnzymeID(getIRI("enzyme"), true);
                Integer parentID = getEnzymeID(getIRI("parent"));

                if(parentID.equals(oldParents.remove(enzymeID)))
                {
                    keepParents.put(enzymeID, parentID);
                }
                else
                {
                    Integer keep = keepParents.get(enzymeID);

                    Pair<String, Integer> pair = Pair.getPair(getStringID("enzyme", enzymePrefix), parentID);

                    if(parentID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, Integer> put = newParents.put(enzymeID, pair);

                    if(put != null && !parentID.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.enzyme_bases set parent=null where id=? and parent=?", oldParents);
        store("insert into pubchem.enzyme_bases(id,iri,parent) values(?,?,?) "
                + "on conflict(id) do update set parent=EXCLUDED.parent", newParents);
    }


    private static void loadEnzymeTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.enzyme_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?enzyme skos:prefLabel ?title. "
                + "filter(strstarts(str(?enzyme), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer enzymeID = getEnzymeID(getIRI("enzyme"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(enzymeID)))
                {
                    keepTitles.put(enzymeID, title);
                }
                else
                {
                    String keep = keepTitles.get(enzymeID);

                    Pair<String, String> pair = Pair.getPair(getStringID("enzyme", enzymePrefix), title);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newTitles.put(enzymeID, pair);

                    if(put != null && !title.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.enzyme_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.enzyme_bases(id,iri,title) values(?,?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadEnzymeAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select enzyme,alternative from pubchem.enzyme_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?enzyme skos:altLabel ?alternative. "
                + "filter(strstarts(str(?enzyme), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer enzymeID = getEnzymeID(getIRI("enzyme"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(enzymeID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.enzyme_alternatives where enzyme=? and alternative=?", oldAlternatives);
        store("insert into pubchem.enzyme_alternatives(enzyme,alternative) values(?,?)", newAlternatives);
    }


    private static void loadProteinBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.protein_bases", oldProteins);

        nextProteinID = oldProteins.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?protein rdf:type sio:SIO_010043"))
        {
            @Override
            protected void parse() throws IOException
            {
                String protein = getStringID("protein", prefix);
                Integer proteinID = oldProteins.remove(protein);

                if(proteinID == null)
                    newProteins.put(protein, nextProteinID++);
                else
                    keepProteins.put(protein, proteinID);
            }
        }.load(model);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntMap keepOrganisms = new IntIntMap();
        IntStringIntPairMap newOrganisms = new IntStringIntPairMap();
        IntIntMap oldOrganisms = new IntIntMap();

        load("select id,organism from pubchem.protein_bases where organism is not null", oldOrganisms);

        new QueryResultProcessor(patternQuery("?protein up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"), true);
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                if(organismID.equals(oldOrganisms.remove(proteinID)))
                {
                    keepOrganisms.put(proteinID, organismID);
                }
                else
                {
                    Integer keep = keepOrganisms.get(proteinID);

                    Pair<String, Integer> pair = Pair.getPair(getStringID("protein", prefix), organismID);

                    if(organismID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, Integer> put = newOrganisms.put(proteinID, pair);

                    if(put != null && !organismID.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.protein_bases set organism=null where id=? and organism=?", oldOrganisms);
        store("insert into pubchem.protein_bases(id,iri,organism) values(?,?,?) "
                + "on conflict(id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadProteinTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringPairMap newTitles = new IntStringPairMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.protein_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?protein skos:prefLabel ?title. "
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"), true);
                String title = getString("title");

                // workaround
                if(title.isEmpty())
                    return;

                if(title.equals(oldTitles.remove(proteinID)))
                {
                    keepTitles.put(proteinID, title);
                }
                else
                {
                    String keep = keepTitles.get(proteinID);

                    Pair<String, String> pair = Pair.getPair(getStringID("protein", prefix), title);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newTitles.put(proteinID, pair);

                    if(put != null && !title.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.protein_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.protein_bases(id,iri,title) values(?,?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadSequences(Model model) throws IOException, SQLException
    {
        IntStringMap keepSequences = new IntStringMap();
        IntStringPairMap newSequences = new IntStringPairMap();
        IntStringMap oldSequences = new IntStringMap();

        load("select id,sequence from pubchem.protein_bases where sequence is not null", oldSequences);

        new QueryResultProcessor(patternQuery("?protein bao:BAO_0002817 ?sequence"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"), true);
                String sequence = getString("sequence");

                if(sequence.equals(oldSequences.remove(proteinID)))
                {
                    keepSequences.put(proteinID, sequence);
                }
                else
                {
                    String keep = keepSequences.get(proteinID);

                    Pair<String, String> pair = Pair.getPair(getStringID("protein", prefix), sequence);

                    if(sequence.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newSequences.put(proteinID, pair);

                    if(put != null && !sequence.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.protein_bases set sequence=null where id=? and sequence=?", oldSequences);
        store("insert into pubchem.protein_bases(id,iri,sequence) values(?,?,?) "
                + "on conflict(id) do update set sequence=EXCLUDED.sequence", newSequences);
    }


    private static void loadProteinAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select protein,alternative from pubchem.protein_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?protein skos:altLabel ?alternative. "
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(proteinID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_alternatives where protein=? and alternative=?", oldAlternatives);
        store("insert into pubchem.protein_alternatives(protein,alternative) values(?,?)", newAlternatives);
    }


    private static void loadPdbLinks(Model model) throws IOException, SQLException
    {
        IntStringSet newPdbLinks = new IntStringSet();
        IntStringSet oldPdbLinks = new IntStringSet();

        load("select protein,pdblink from pubchem.protein_pdblinks", oldPdbLinks);

        new QueryResultProcessor(patternQuery("?protein pdbo40:link_to_pdb ?pdblink"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String pdblinkID = getStringID("pdblink", "http://rdf.wwpdb.org/pdb/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, pdblinkID);

                if(!oldPdbLinks.remove(pair))
                    newPdbLinks.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_pdblinks where protein=? and pdblink=?", oldPdbLinks);
        store("insert into pubchem.protein_pdblinks(protein,pdblink) values(?,?)", newPdbLinks);
    }


    private static void loadSimilarProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newSimilarProteins = new IntPairSet();
        IntPairSet oldSimilarProteins = new IntPairSet();

        load("select protein,simprotein from pubchem.protein_similarproteins", oldSimilarProteins);

        new QueryResultProcessor(patternQuery("?protein vocab:hasSimilarProtein ?similar"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer simproteinID = getProteinID(getIRI("similar"));

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, simproteinID);

                if(!oldSimilarProteins.remove(pair))
                    newSimilarProteins.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_similarproteins where protein=? and simprotein=?", oldSimilarProteins);
        store("insert into pubchem.protein_similarproteins(protein,simprotein) values(?,?)", newSimilarProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = new IntPairSet();

        load("select protein,gene from pubchem.protein_genes", oldGenes);

        new QueryResultProcessor(patternQuery("?protein up:encodedBy ?gene"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer geneID = Gene.getGeneID(getIRI("gene"));

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_genes where protein=? and gene=?", oldGenes);
        store("insert into pubchem.protein_genes(protein,gene) values(?,?)", newGenes);
    }


    private static void loadEnzymes(Model model) throws IOException, SQLException
    {
        IntStringSet newEnzymes = new IntStringSet();
        IntStringSet oldEnzymes = new IntStringSet();

        load("select protein,enzyme from pubchem.protein_uniprot_enzymes", oldEnzymes);

        IntPairSet newProteinEnzymes = new IntPairSet();
        IntPairSet oldProteinEnzymes = new IntPairSet();

        load("select protein,enzyme from pubchem.protein_enzymes", oldProteinEnzymes);

        new QueryResultProcessor(patternQuery("?protein up:enzyme ?enzyme"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));

                if(getIRI("enzyme").startsWith("http://purl.uniprot.org/enzyme/"))
                {
                    String enzymeID = getStringID("enzyme", "http://purl.uniprot.org/enzyme/");

                    Pair<Integer, String> pair = Pair.getPair(proteinID, enzymeID);

                    if(!oldEnzymes.remove(pair))
                        newEnzymes.add(pair);
                }
                else
                {
                    Integer enzymeID = getEnzymeID(getIRI("enzyme"));

                    Pair<Integer, Integer> pair = Pair.getPair(proteinID, enzymeID);

                    if(!oldProteinEnzymes.remove(pair))
                        newProteinEnzymes.add(pair);
                }
            }
        }.load(model);

        store("delete from pubchem.protein_uniprot_enzymes where protein=? and enzyme=?", oldEnzymes);
        store("insert into pubchem.protein_uniprot_enzymes(protein,enzyme) values(?,?)", newEnzymes);

        store("delete from pubchem.protein_enzymes where protein=? and enzyme=?", oldProteinEnzymes);
        store("insert into pubchem.protein_enzymes(protein,enzyme) values(?,?)", newProteinEnzymes);
    }


    private static void loadNcbiCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_ncbi_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.ncbi.nlm.nih.gov/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "https://www.ncbi.nlm.nih.gov/protein/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_ncbi_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_ncbi_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadUniprotCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_uniprot_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://purl.uniprot.org/uniprot/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "http://purl.uniprot.org/uniprot/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_uniprot_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_uniprot_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?protein skos:closeMatch ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_mesh_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_mesh_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadThesaurusCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_thesaurus_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                if(match.getOne() != Ontology.unitThesaurus)
                    throw new IOException();

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, match.getTwo());

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_thesaurus_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_thesaurus_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadGuidetopharmacologyCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_guidetopharmacology_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. filter(strstarts(str(?match), "
                + "'https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId='))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer match = getIntID("match",
                        "https://guidetopharmacology.org/GRAC/ObjectDisplayForward?objectId=");

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_guidetopharmacology_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_guidetopharmacology_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadDrugbankCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_drugbank_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.drugbank.ca/bio_entities/BE'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer match = getIntID("match", "https://www.drugbank.ca/bio_entities/BE");

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_drugbank_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_drugbank_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadChemblCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select protein,match from pubchem.protein_chembl_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer match = getIntID("match", "https://www.ebi.ac.uk/chembl/target_report_card/CHEMBL");

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_chembl_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_chembl_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadGlygenCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_glygen_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?protein skos:closeMatch ?match. filter(strstarts(str(?match), 'https://glygen.org/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "https://glygen.org/protein/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_glygen_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_glygen_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadGlycosmosCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_glycosmos_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://glycosmos.org/glycoproteins/show/uniprot/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "https://glycosmos.org/glycoproteins/show/uniprot/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_glycosmos_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_glycosmos_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadAlphafoldCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select protein,match from pubchem.protein_alphafold_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://alphafold.ebi.ac.uk/entry/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                String match = getStringID("match", "https://alphafold.ebi.ac.uk/entry/");

                Pair<Integer, String> pair = Pair.getPair(proteinID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_alphafold_matches where protein=? and match=?", oldMatches);
        store("insert into pubchem.protein_alphafold_matches(protein,match) values(?,?)", newMatches);
    }


    private static void loadConservedDomains(Model model) throws IOException, SQLException
    {
        IntPairSet newDomains = new IntPairSet();
        IntPairSet oldDomains = new IntPairSet();

        load("select protein,domain from pubchem.protein_conserveddomains", oldDomains);

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?domain "
                + "filter(strstarts(str(?domain), 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer domainID = ConservedDomain.getDomainID(getIRI("domain"));

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, domainID);

                if(!oldDomains.remove(pair))
                    newDomains.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_conserveddomains where protein=? and domain=?", oldDomains);
        store("insert into pubchem.protein_conserveddomains(protein,domain) values(?,?)", newDomains);
    }


    private static void loadContinuantParts(Model model) throws IOException, SQLException
    {
        IntPairSet newContinuantParts = new IntPairSet();
        IntPairSet oldContinuantParts = new IntPairSet();

        load("select protein,part from pubchem.protein_continuantparts", oldContinuantParts);

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?part "
                + "filter(strstarts(str(?part), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer partID = getProteinID(getIRI("part"));

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, partID);

                if(!oldContinuantParts.remove(pair))
                    newContinuantParts.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_continuantparts where protein=? and part=?", oldContinuantParts);
        store("insert into pubchem.protein_continuantparts(protein,part) values(?,?)", newContinuantParts);
    }


    private static void loadFamilies(Model model) throws IOException, SQLException
    {
        IntPairSet newFamilies = new IntPairSet();
        IntPairSet oldFamilies = new IntPairSet();

        load("select protein,family from pubchem.protein_families", oldFamilies);

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?family "
                + "filter(strstarts(str(?family), 'https://pfam.xfam.org/family/PF'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Integer familyID = getIntID("family", "https://pfam.xfam.org/family/PF");

                Pair<Integer, Integer> pair = Pair.getPair(proteinID, familyID);

                if(!oldFamilies.remove(pair))
                    newFamilies.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_families where protein=? and family=?", oldFamilies);
        store("insert into pubchem.protein_families(protein,family) values(?,?)", newFamilies);
    }


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        IntIntPairSet newTypes = new IntIntPairSet();
        IntIntPairSet oldTypes = new IntIntPairSet();

        load("select protein,type_unit,type_id from pubchem.protein_types", oldTypes);

        new QueryResultProcessor(patternQuery("?protein rdf:type ?type."
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer proteinID = getProteinID(getIRI("protein"));
                Pair<Integer, Integer> type = Ontology.getId(getIRI("type"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(proteinID, type);

                if(!oldTypes.remove(pair))
                    newTypes.add(pair);
            }
        }.load(model);

        store("delete from pubchem.protein_types where protein=? and type_unit=? and type_id=?", oldTypes);
        store("insert into pubchem.protein_types(protein,type_unit,type_id) values(?,?,?)", newTypes);
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

        store("delete from pubchem.enzyme_bases where iri=? and id=?", oldEnzymes);
        store("insert into pubchem.enzyme_bases(iri,id) values(?,?)", newEnzymes);

        store("delete from pubchem.protein_bases where iri=? and id=?", oldProteins);
        store("insert into pubchem.protein_bases(iri,id) values(?,?)", newProteins);

        System.out.println();
    }


    static Integer getEnzymeID(String value) throws IOException
    {
        return getEnzymeID(value, false);
    }


    static Integer getEnzymeID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(enzymePrefix))
            throw new IOException("unexpected IRI: " + value);

        String enzyme = value.substring(enzymePrefixLength);

        synchronized(newEnzymes)
        {
            Integer enzymeID = keepEnzymes.get(enzyme);

            if(enzymeID != null)
                return enzymeID;

            enzymeID = newEnzymes.get(enzyme);

            if(enzymeID != null)
            {
                if(keepForce)
                {
                    newEnzymes.remove(enzyme);
                    keepEnzymes.put(enzyme, enzymeID);
                }

                return enzymeID;
            }

            System.out.println("    add missing enzyme " + enzyme);

            if((enzymeID = oldEnzymes.remove(enzyme)) != null)
                keepEnzymes.put(enzyme, enzymeID);
            else if(keepForce)
                keepEnzymes.put(enzyme, enzymeID = nextEnzymeID++);
            else
                newEnzymes.put(enzyme, enzymeID = nextEnzymeID++);

            return enzymeID;
        }
    }


    static Integer getProteinID(String value) throws IOException
    {
        return getProteinID(value, false);
    }


    static Integer getProteinID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String protein = value.substring(prefixLength);

        synchronized(newProteins)
        {
            Integer proteinID = keepProteins.get(protein);

            if(proteinID != null)
                return proteinID;

            proteinID = newProteins.get(protein);

            if(proteinID != null)
            {
                if(keepForce)
                {
                    newProteins.remove(protein);
                    keepProteins.put(protein, proteinID);
                }

                return proteinID;
            }

            System.out.println("    add missing protein " + protein);

            if((proteinID = oldProteins.remove(protein)) != null)
                keepProteins.put(protein, proteinID);
            else if(keepForce)
                keepProteins.put(protein, proteinID = nextProteinID++);
            else
                newProteins.put(protein, proteinID = nextProteinID++);

            return proteinID;
        }
    }
}
