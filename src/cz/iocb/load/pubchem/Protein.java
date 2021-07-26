package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
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


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedProteins = new StringIntMap(1000000);
        newProteins = new StringIntMap(1000000);
        oldProteins = getStringIntMap("select name, id from pubchem.protein_bases", 1000000);
        nextProteinID = getIntValue("select coalesce(max(id)+1,0) from pubchem.protein_bases");

        new QueryResultProcessor(
                "select distinct ?protein {{?protein ?P ?O} union {?S vocab:hasSimilarProtein ?protein}}")
        {
            @Override
            protected void parse() throws IOException
            {
                String name = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/");
                int proteinID;

                if((proteinID = oldProteins.removeKeyIfAbsent(name, NO_VALUE)) == NO_VALUE)
                    newProteins.put(name, proteinID = nextProteinID++);

                usedProteins.put(name, proteinID);
            }
        }.load(model);

        batch("insert into pubchem.protein_bases(name, id) values (?,?)", newProteins);
        newProteins.clear();
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap(100000);
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism_id from pubchem.protein_bases where organism_id is not null", 100000);

        new QueryResultProcessor(patternQuery("?protein bp:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(getIRI("organism").equals("http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"))
                    return;

                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int organismID = getIntID("organism", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID");

                if(organismID != oldOrganisms.removeKeyIfAbsent(proteinID, NO_VALUE))
                    newOrganisms.put(proteinID, organismID);
            }
        }.load(model);

        batch("update pubchem.protein_bases set organism_id = null where id = ?", oldOrganisms.keySet());
        batch("update pubchem.protein_bases set organism_id = ? where id = ?", newOrganisms, Direction.REVERSE);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(100000);
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.protein_bases where title is not null",
                100000);

        new QueryResultProcessor(patternQuery("?protein dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                String title = getString("title");

                if(!title.equals(oldTitles.remove(proteinID)))
                    newTitles.put(proteinID, title);
            }
        }.load(model);

        batch("update pubchem.protein_bases set title = null where id = ?", oldTitles.keySet());
        batch("update pubchem.protein_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select protein, reference from pubchem.protein_references", 10000000);

        new QueryResultProcessor(patternQuery("?protein cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int referenceID = getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_references where protein = ? and reference = ?", oldReferences);
        batch("insert into pubchem.protein_references(protein, reference) values (?,?)", newReferences);
    }


    private static void loadPdbLinks(Model model) throws IOException, SQLException
    {
        IntStringPairSet newPdbLinks = new IntStringPairSet(10000);
        IntStringPairSet oldPdbLinks = getIntStringPairSet("select protein, pdblink from pubchem.protein_pdblinks",
                10000);

        new QueryResultProcessor(patternQuery("?protein pdbo:link_to_pdb ?pdblink"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
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
        IntPairSet newSimilarProteins = new IntPairSet(10000000);
        IntPairSet oldSimilarProteins = getIntPairSet("select protein, simprotein from pubchem.protein_similarproteins",
                10000000);

        new QueryResultProcessor(patternQuery("?protein vocab:hasSimilarProtein ?similar"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int simproteinID = usedProteins
                        .getOrThrow(getStringID("similar", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));

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
        IntPairSet newGenes = new IntPairSet(10000);
        IntPairSet oldGenes = getIntPairSet("select protein, gene from pubchem.protein_genes", 10000);

        new QueryResultProcessor(patternQuery("?protein up:encodedBy ?gene"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_genes where protein = ? and gene = ?", oldGenes);
        batch("insert into pubchem.protein_genes(protein, gene) values (?,?)", newGenes);
    }


    private static void loadUniprotCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newMatches = new IntStringPairIntMap(100000);
        IntStringPairIntMap oldMatches = getIntStringPairIntMap(
                "select protein, match, __ from pubchem.protein_uniprot_closematches", 100000);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match "
                + "filter(strstarts(str(?match), 'http://purl.uniprot.org/uniprot/'))"))
        {
            int nextMatcheID = Updater
                    .getIntValue("select coalesce(max(__)+1,0) from pubchem.protein_uniprot_closematches");

            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                String match = getStringID("match", "http://purl.uniprot.org/uniprot/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(oldMatches.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newMatches.put(pair, nextMatcheID++);
            }
        }.load(model);

        batch("delete from pubchem.protein_uniprot_closematches where __ = ?", oldMatches.values());
        batch("insert into pubchem.protein_uniprot_closematches(protein, match, __) values (?,?,?)", newMatches);
    }


    private static void loadNcbiCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newMatches = new IntStringPairIntMap(100000);
        IntStringPairIntMap oldMatches = getIntStringPairIntMap(
                "select protein, match, __ from pubchem.protein_closematches", 100000);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match "
                + "filter(strstarts(str(?match), 'https://www.ncbi.nlm.nih.gov/protein/'))"))
        {
            int nextMatcheID = Updater.getIntValue("select coalesce(max(__)+1,0) from pubchem.protein_closematches");

            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                String match = getStringID("match", "https://www.ncbi.nlm.nih.gov/protein/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(proteinID, match);

                if(oldMatches.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newMatches.put(pair, nextMatcheID++);
            }
        }.load(model);

        batch("delete from pubchem.protein_closematches where __ = ?", oldMatches.values());
        batch("insert into pubchem.protein_closematches(protein, match, __) values (?,?,?)", newMatches);
    }


    private static void loadConservedDomains(Model model) throws IOException, SQLException
    {
        IntPairSet newDomains = new IntPairSet(100000);
        IntPairSet oldDomains = getIntPairSet("select protein, domain from pubchem.protein_conserveddomains", 100000);

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?domain "
                + "filter(strstarts(str(?domain), 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");

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
        IntPairSet newContinuantParts = new IntPairSet(100);
        IntPairSet oldContinuantParts = getIntPairSet("select protein, part from pubchem.protein_continuantparts", 100);

        new QueryResultProcessor(patternQuery("?protein obo:RO_0002180 ?part "
                + "filter(strstarts(str(?part), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int partID = usedProteins
                        .getOrThrow(getStringID("part", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));

                IntIntPair pair = PrimitiveTuples.pair(proteinID, partID);

                if(!oldContinuantParts.remove(pair))
                    newContinuantParts.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.protein_continuantparts where protein = ? and part = ?", oldContinuantParts);
        batch("insert into pubchem.protein_continuantparts(protein, part) values (?,?)", newContinuantParts);
    }


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        IntTripletSet newTypes = new IntTripletSet(100000);
        IntTripletSet oldTypes = getIntTripletSet("select protein, type_unit, type_id from pubchem.protein_types",
                100000);

        new QueryResultProcessor(
                patternQuery("?protein rdf:type ?type " + "filter(?type != bp:Protein && ?type != obo:GO_0043234)"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                Identifier type = Ontology.getId(getIRI("type"));

                IntTriplet triplet = new IntTriplet(proteinID, type.unit, type.id);

                if(!oldTypes.remove(triplet))
                    newTypes.add(triplet);
            }
        }.load(model);

        batch("delete from pubchem.protein_types where protein = ? and type_unit = ? and type_id = ?", oldTypes);
        batch("insert into pubchem.protein_types(protein, type_unit, type_id) values (?,?,?)", newTypes);
    }


    private static void loadComplexes(Model model) throws IOException, SQLException
    {
        IntHashSet newComplexes = new IntHashSet(1000);
        IntHashSet oldComplexes = getIntSet("select protein from pubchem.protein_complexes", 1000);

        new QueryResultProcessor(patternQuery("?protein rdf:type obo:GO_0043234"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));

                if(!oldComplexes.remove(proteinID))
                    newComplexes.add(proteinID);
            }
        }.load(model);

        batch("delete from pubchem.protein_complexes where protein = ?", oldComplexes);
        batch("insert into pubchem.protein_complexes(protein) values (?)", newComplexes);
    }


    static int getProteinID(String name) throws IOException
    {
        synchronized(newProteins)
        {
            int proteinID = usedProteins.getIfAbsent(name, NO_VALUE);

            if(proteinID == NO_VALUE)
            {
                if((proteinID = oldProteins.removeKeyIfAbsent(name, NO_VALUE)) == NO_VALUE)
                    newProteins.put(name, proteinID = nextProteinID++);

                usedProteins.put(name, proteinID);
            }

            return proteinID;
        }
    }


    static void finish() throws IOException, SQLException
    {
        batch("delete from pubchem.protein_bases where id = ?", oldProteins.values());
        batch("insert into pubchem.protein_bases(name, id) values (?,?)", newProteins);

        usedProteins = null;
        newProteins = null;
        oldProteins = null;
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load proteins ...");

        Model model = getModel("pubchem/RDF/protein/pc_protein.ttl.gz");
        check(model, "pubchem/protein/check.sparql");

        loadBases(model);
        loadOrganisms(model);
        loadTitles(model);
        loadReferences(model);
        loadPdbLinks(model);
        loadSimilarProteins(model);
        loadGenes(model);
        loadUniprotCloseMatches(model);
        loadNcbiCloseMatches(model);
        loadConservedDomains(model);
        loadContinuantParts(model);
        loadTypes(model);
        loadComplexes(model);

        model.close();
        System.out.println();
    }
}
