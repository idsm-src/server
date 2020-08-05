package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Protein extends Updater
{
    private static StringIntMap usedProteins;
    private static StringIntMap newProteins;
    private static StringIntMap oldProteins;
    private static IntHashSet newComplexes;
    private static IntHashSet oldComplexes;
    private static int nextProteinID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedProteins = new StringIntMap(1000000);
        newProteins = new StringIntMap(1000000);
        oldProteins = getStringIntMap("select name, id from protein_bases", 1000000);
        nextProteinID = getIntValue("select coalesce(max(id)+1,0) from protein_bases");

        newComplexes = new IntHashSet(200000);
        oldComplexes = getIntSet("select protein from protein_complexes", 200000);

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

                if(name.matches("GI.*GI.*") && !oldComplexes.remove(proteinID))
                    newComplexes.add(proteinID);

                usedProteins.put(name, proteinID);
            }
        }.load(model);

        batch("insert into protein_bases(name, id) values (?,?)", newProteins);
        newProteins.clear();
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap(100000);
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism_id from protein_bases where organism_id is not null", 100000);

        new QueryResultProcessor(patternQuery("?protein bp:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int organismID = getIntID("organism", "http://identifiers.org/taxonomy/");

                if(organismID != oldOrganisms.removeKeyIfAbsent(proteinID, NO_VALUE))
                    newOrganisms.put(proteinID, organismID);
            }
        }.load(model);

        batch("update protein_bases set organism_id = null where id = ?", oldOrganisms.keySet());
        batch("update protein_bases set organism_id = ? where id = ?", newOrganisms, Direction.REVERSE);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(100000);
        IntStringMap oldTitles = getIntStringMap("select id, title from protein_bases where title is not null", 100000);

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

        batch("update protein_bases set title = null where id = ?", oldTitles.keySet());
        batch("update protein_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select protein, reference from protein_references", 10000000);

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

        batch("delete from protein_references where protein = ? and reference = ?", oldReferences);
        batch("insert into protein_references(protein, reference) values (?,?)", newReferences);
    }


    private static void loadPdbLinks(Model model) throws IOException, SQLException
    {
        IntStringPairSet newPdbLinks = new IntStringPairSet(10000);
        IntStringPairSet oldPdbLinks = getIntStringPairSet("select protein, pdblink from protein_pdblinks", 10000);

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

        batch("delete from protein_pdblinks where protein = ? and pdblink = ?", oldPdbLinks);
        batch("insert into protein_pdblinks(protein, pdblink) values (?,?)", newPdbLinks);
    }


    private static void loadSimilarProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newSimilarProteins = new IntPairSet(10000000);
        IntPairSet oldSimilarProteins = getIntPairSet("select protein, simprotein from protein_similarproteins",
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

        batch("delete from protein_similarproteins where protein = ? and simprotein = ?", oldSimilarProteins);
        batch("insert into protein_similarproteins(protein, simprotein) values (?,?)", newSimilarProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntIntHashMap newGenes = new IntIntHashMap(10000);
        IntIntHashMap oldGenes = getIntIntMap("select protein, gene from protein_genes", 10000);

        new QueryResultProcessor(patternQuery("?protein vocab:encodedBy ?gene"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                if(geneID != oldGenes.removeKeyIfAbsent(proteinID, NO_VALUE))
                    newGenes.put(proteinID, geneID);
            }
        }.load(model);

        batch("delete from protein_genes where protein = ?", oldGenes.keySet());
        batch("insert into protein_genes(protein, gene) values (?,?) "
                + "on conflict (protein) do update set gene=EXCLUDED.gene", newGenes);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newMatches = new IntStringPairIntMap(100000);
        IntStringPairIntMap oldMatches = getIntStringPairIntMap("select protein, match, __ from protein_closematches",
                100000);

        new QueryResultProcessor(patternQuery("?protein skos:closeMatch ?match"))
        {
            int nextMatcheID = Updater.getIntValue("select coalesce(max(__)+1,0) from protein_closematches");

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

        batch("delete from protein_closematches where __ = ?", oldMatches.values());
        batch("insert into protein_closematches(protein, match, __) values (?,?,?)", newMatches);
    }


    private static void loadConservedDomains(Model model) throws IOException, SQLException
    {
        IntIntHashMap newDomains = new IntIntHashMap(100000);
        IntIntHashMap oldDomains = getIntIntMap("select protein, domain from protein_conserveddomains", 100000);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000110 ?domain"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");

                if(domainID != oldDomains.removeKeyIfAbsent(proteinID, NO_VALUE))
                    newDomains.put(proteinID, domainID);
            }
        }.load(model);

        batch("delete from protein_conserveddomains where protein = ?", oldDomains.keySet());
        batch("insert into protein_conserveddomains(protein, domain) values (?,?) "
                + "on conflict (protein) do update set domain=EXCLUDED.domain", newDomains);
    }


    private static void loadContinuantParts(Model model) throws IOException, SQLException
    {
        IntPairSet newContinuantParts = new IntPairSet(100);
        IntPairSet oldContinuantParts = getIntPairSet("select protein, part from protein_continuantparts", 100);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000178 ?part"))
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

        batch("delete from protein_continuantparts where protein = ? and part = ?", oldContinuantParts);
        batch("insert into protein_continuantparts(protein, part) values (?,?)", newContinuantParts);
    }


    private static void loadProcesses(Model model) throws IOException, SQLException
    {
        IntPairSet newProcesses = new IntPairSet(10000000);
        IntPairSet oldProcesses = getIntPairSet("select protein, process_id from protein_processes", 10000000);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000056 ?process "
                + "filter(strstarts(str(?process), 'http://purl.obolibrary.org/obo/GO_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int processID = getIntID("process", "http://purl.obolibrary.org/obo/GO_");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, processID);

                if(!oldProcesses.remove(pair))
                    newProcesses.add(pair);
            }
        }.load(model);

        batch("delete from protein_processes where protein = ? and process_id = ?", oldProcesses);
        batch("insert into protein_processes(protein, process_id) values (?,?)", newProcesses);
    }


    private static void loadBiosystems(Model model) throws IOException, SQLException
    {
        IntPairSet newBiosystems = new IntPairSet(1000000);
        IntPairSet oldBiosystems = getIntPairSet("select protein, biosystem from protein_biosystems", 1000000);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000056 ?biosystem "
                + "filter(strstarts(str(?biosystem), 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, biosystemID);

                if(!oldBiosystems.remove(pair))
                    newBiosystems.add(pair);
            }
        }.load(model);

        batch("delete from protein_biosystems where protein = ? and biosystem = ?", oldBiosystems);
        batch("insert into protein_biosystems(protein, biosystem) values (?,?)", newBiosystems);
    }


    private static void loadFunctions(Model model) throws IOException, SQLException
    {
        IntPairSet newFunctions = new IntPairSet(10000000);
        IntPairSet oldFunctions = getIntPairSet("select protein, function_id from protein_functions", 10000000);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000160 ?function"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int functionID = getIntID("function", "http://purl.obolibrary.org/obo/GO_");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, functionID);

                if(!oldFunctions.remove(pair))
                    newFunctions.add(pair);
            }
        }.load(model);

        batch("delete from protein_functions where protein = ? and function_id = ?", oldFunctions);
        batch("insert into protein_functions(protein, function_id) values (?,?)", newFunctions);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        IntPairSet newLocations = new IntPairSet(1000000);
        IntPairSet oldLocations = getIntPairSet("select protein, location_id from protein_locations", 1000000);

        new QueryResultProcessor(patternQuery("?protein obo:BFO_0000171 ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int locationID = getIntID("location", "http://purl.obolibrary.org/obo/GO_");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, locationID);

                if(!oldLocations.remove(pair))
                    newLocations.add(pair);
            }
        }.load(model);

        batch("delete from protein_locations where protein = ? and location_id = ?", oldLocations);
        batch("insert into protein_locations(protein, location_id) values (?,?)", newLocations);
    }


    private static void loadTypes(Model model) throws IOException, SQLException
    {
        IntPairSet newTypes = new IntPairSet(100000);
        IntPairSet oldTypes = getIntPairSet("select protein, type_id from protein_types", 100000);

        new QueryResultProcessor(patternQuery(
                "?protein rdf:type ?type " + "filter(strstarts(str(?type), 'http://purl.obolibrary.org/obo/PR_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int proteinID = usedProteins
                        .getOrThrow(getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                int typeID = getIntID("type", "http://purl.obolibrary.org/obo/PR_");

                IntIntPair pair = PrimitiveTuples.pair(proteinID, typeID);

                if(!oldTypes.remove(pair))
                    newTypes.add(pair);
            }
        }.load(model);

        batch("delete from protein_types where protein = ? and type_id = ?", oldTypes);
        batch("insert into protein_types(protein, type_id) values (?,?)", newTypes);
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

                if(name.matches("GI.*GI.*") && !oldComplexes.remove(proteinID))
                    newComplexes.add(proteinID);

                usedProteins.put(name, proteinID);
            }

            return proteinID;
        }
    }


    static void finish() throws IOException, SQLException
    {
        batch("delete from protein_bases where id = ?", oldProteins.values());
        batch("insert into protein_bases(name, id) values (?,?)", newProteins);

        batch("delete from protein_complexes where protein = ?", oldComplexes);
        batch("insert into protein_complexes(protein) values (?)", newComplexes);

        usedProteins = null;
        newProteins = null;
        oldProteins = null;

        newComplexes = null;
        oldComplexes = null;
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load proteins ...");

        Model model = getModel("RDF/protein/pc_protein.ttl.gz");
        check(model, "protein/check.sparql");

        loadBases(model);
        loadOrganisms(model);
        loadTitles(model);
        loadReferences(model);
        loadPdbLinks(model);
        loadSimilarProteins(model);
        loadGenes(model);
        loadCloseMatches(model);
        loadConservedDomains(model);
        loadContinuantParts(model);
        loadProcesses(model);
        loadBiosystems(model);
        loadFunctions(model);
        loadLocations(model);
        loadTypes(model);

        model.close();
        System.out.println();
    }
}
