package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



class Gene extends Updater
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newGenes = new IntHashSet(200000);
        IntHashSet oldGenes = getIntSet("select id from pubchem.gene_bases", 200000);

        new QueryResultProcessor(patternQuery("?gene rdf:type bp:Gene"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                if(!oldGenes.remove(geneID))
                    newGenes.add(geneID);
            }
        }.load(model);

        batch("delete from pubchem.gene_bases where id = ?", oldGenes);


        IntStringMap newSymbols = new IntStringMap(200000);
        IntStringMap oldSymbols = getIntStringMap("select id, symbol from pubchem.gene_bases", 200000);

        new QueryResultProcessor(patternQuery("?gene sio:gene-symbol ?symbol"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                String symbol = getString("symbol");

                if(!symbol.equals(oldSymbols.remove(geneID)))
                    newSymbols.put(geneID, symbol);
            }
        }.load(model);

        oldGenes.forEach(key -> oldSymbols.remove(key));

        if(!oldSymbols.isEmpty())
            throw new IOException();


        IntStringMap newTitles = new IntStringMap(200000);
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.gene_bases", 200000);

        new QueryResultProcessor(patternQuery("?gene dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                String title = getString("title");

                if(!title.equals(oldTitles.remove(geneID)))
                    newTitles.put(geneID, title);
            }
        }.load(model);

        oldGenes.forEach(key -> oldTitles.remove(key));

        if(!oldTitles.isEmpty())
            throw new IOException();


        IntIntHashMap newOrganisms = new IntIntHashMap(200000);
        IntIntHashMap oldOrganisms = getIntIntMap("select id, organism_id from pubchem.gene_bases", 200000);

        new QueryResultProcessor(patternQuery("?gene bp:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                int organismID = getIntID("organism", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID");

                if(organismID != oldOrganisms.removeKeyIfAbsent(geneID, NO_VALUE))
                    newOrganisms.put(geneID, organismID);
            }
        }.load(model);

        oldGenes.forEach(key -> oldOrganisms.remove(key));

        if(!oldOrganisms.isEmpty())
            throw new IOException();


        batch("insert into pubchem.gene_bases(id, symbol, title, organism_id) values (?,?,?,?)", newGenes,
                (PreparedStatement statement, int gene) -> {
                    statement.setInt(1, gene);
                    statement.setString(2, newSymbols.remove(gene));
                    statement.setString(3, newTitles.remove(gene));
                    statement.setInt(4, newOrganisms.getOrThrow(gene));
                    newOrganisms.remove(gene);
                });

        batch("update pubchem.gene_bases set symbol = ? where id = ?", newSymbols, Direction.REVERSE);
        batch("update pubchem.gene_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
        batch("update pubchem.gene_bases set organism_id = ? where id = ?", newOrganisms, Direction.REVERSE);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newAlternatives = new IntStringPairIntMap(1000000);
        IntStringPairIntMap oldAlternatives = getIntStringPairIntMap(
                "select gene, alternative, __ from pubchem.gene_alternatives", 1000000);

        new QueryResultProcessor(patternQuery("?gene dcterms:alternative ?alternative"))
        {
            int nextAlternativeID = Updater.getIntValue("select coalesce(max(__)+1,0) from pubchem.gene_alternatives");

            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(geneID, alternative);

                if(oldAlternatives.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newAlternatives.put(pair, nextAlternativeID++);
            }
        }.load(model);

        batch("delete from pubchem.gene_alternatives where __ = ?", oldAlternatives.values());
        batch("insert into pubchem.gene_alternatives(gene, alternative, __) values (?,?,?)", newAlternatives);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select gene, reference from pubchem.gene_references", 10000000);

        new QueryResultProcessor(patternQuery("?gene cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                int referenceID = getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                IntIntPair pair = PrimitiveTuples.pair(geneID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.gene_references where gene = ? and reference = ?", oldReferences);
        batch("insert into pubchem.gene_references(gene, reference) values (?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newMatches = new IntStringPairIntMap(1000000);
        IntStringPairIntMap oldMatches = getIntStringPairIntMap("select gene, match, __ from pubchem.gene_matches",
                1000000);

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match"))
        {
            int nextMatcheID = Updater.getIntValue("select coalesce(max(__)+1,0) from pubchem.gene_matches");

            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                String match = getStringID("match", "http://rdf.ebi.ac.uk/resource/ensembl/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(geneID, match);

                if(oldMatches.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newMatches.put(pair, nextMatcheID++);
            }
        }.load(model);

        batch("delete from pubchem.gene_matches where __ = ?", oldMatches.values());
        batch("insert into pubchem.gene_matches(gene, match, __) values (?,?,?)", newMatches);
    }


    private static void loadProcesses(Model model) throws IOException, SQLException
    {
        IntPairSet newProcesses = new IntPairSet(10000000);
        IntPairSet oldProcesses = getIntPairSet("select gene, process_id from pubchem.gene_processes", 10000000);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0000056 ?process "
                + "filter(strstarts(str(?process), 'http://purl.obolibrary.org/obo/GO_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                Identifier process = Ontology.getId(getIRI("process"));

                if(process.unit != Ontology.unitGO)
                    throw new IOException();

                IntIntPair pair = PrimitiveTuples.pair(geneID, process.id);

                if(!oldProcesses.remove(pair))
                    newProcesses.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.gene_processes where gene = ? and process_id = ?", oldProcesses);
        batch("insert into pubchem.gene_processes(gene, process_id) values (?,?)", newProcesses);
    }


    private static void loadFunctions(Model model) throws IOException, SQLException
    {
        IntPairSet newFunctions = new IntPairSet(10000000);
        IntPairSet oldFunctions = getIntPairSet("select gene, function_id from pubchem.gene_functions", 10000000);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0000085 ?function"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                Identifier function = Ontology.getId(getIRI("function"));

                if(function.unit != Ontology.unitGO)
                    throw new IOException();

                IntIntPair pair = PrimitiveTuples.pair(geneID, function.id);

                if(!oldFunctions.remove(pair))
                    newFunctions.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.gene_functions where gene = ? and function_id = ?", oldFunctions);
        batch("insert into pubchem.gene_functions(gene, function_id) values (?,?)", newFunctions);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        IntPairSet newLocations = new IntPairSet(1000000);
        IntPairSet oldLocations = getIntPairSet("select gene, location_id from pubchem.gene_locations", 1000000);

        new QueryResultProcessor(patternQuery("?gene obo:RO_0001025 ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                Identifier location = Ontology.getId(getIRI("location"));

                if(location.unit != Ontology.unitGO)
                    throw new IOException();

                IntIntPair pair = PrimitiveTuples.pair(geneID, location.id);

                if(!oldLocations.remove(pair))
                    newLocations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.gene_locations where gene = ? and location_id = ?", oldLocations);
        batch("insert into pubchem.gene_locations(gene, location_id) values (?,?)", newLocations);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load genes ...");

        Model model = getModel("pubchem/RDF/gene/pc_gene.ttl.gz");
        check(model, "pubchem/gene/check.sparql");

        loadBases(model);
        loadProcesses(model);
        loadFunctions(model);
        loadLocations(model);
        loadAlternatives(model);
        loadReferences(model);
        loadCloseMatches(model);

        model.close();
        System.out.println();
    }
}
