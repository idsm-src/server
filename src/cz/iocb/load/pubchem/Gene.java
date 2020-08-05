package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Gene extends Updater
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newGenes = new IntHashSet(100000);
        IntHashSet oldGenes = getIntSet("select id from gene_bases", 100000);

        new QueryResultProcessor("select distinct ?gene { ?gene ?p ?o }")
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                if(!oldGenes.remove(geneID))
                    newGenes.add(geneID);
            }
        }.load(model);

        batch("delete from gene_bases where id = ?", oldGenes);
        batch("insert into gene_bases(id) values (?)", newGenes);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(100000);
        IntStringMap oldTitles = getIntStringMap("select id, title from gene_bases", 100000);

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

        batch("update gene_bases set title = null where id = ?", oldTitles.keySet());
        batch("update gene_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadDescriptions(Model model) throws IOException, SQLException
    {
        IntStringMap newDescriptions = new IntStringMap(100000);
        IntStringMap oldDescriptions = getIntStringMap("select id, description from gene_bases", 100000);

        new QueryResultProcessor(patternQuery("?gene dcterms:description ?description"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                String description = getString("description");

                if(!description.equals(oldDescriptions.remove(geneID)))
                    newDescriptions.put(geneID, description);
            }
        }.load(model);

        batch("update gene_bases set description = null where id = ?", oldDescriptions.keySet());
        batch("update gene_bases set description = ? where id = ?", newDescriptions, Direction.REVERSE);
    }


    private static void loadBiosystems(Model model) throws IOException, SQLException
    {
        IntPairSet newBiosystems = new IntPairSet(1000000);
        IntPairSet oldBiosystems = getIntPairSet("select gene, biosystem from gene_biosystems", 1000000);

        new QueryResultProcessor(patternQuery("?gene obo:BFO_0000056 ?biosystem"))
        {
            @Override
            protected void parse() throws IOException
            {
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");
                int biosystemID = getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID");

                IntIntPair pair = PrimitiveTuples.pair(geneID, biosystemID);

                if(!oldBiosystems.remove(pair))
                    newBiosystems.add(pair);
            }
        }.load(model);

        batch("delete from gene_biosystems where gene = ? and biosystem = ?", oldBiosystems);
        batch("insert into gene_biosystems(gene, biosystem) values (?,?)", newBiosystems);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newAlternatives = new IntStringPairIntMap(1000000);
        IntStringPairIntMap oldAlternatives = getIntStringPairIntMap(
                "select gene, alternative, __ from gene_alternatives", 1000000);

        new QueryResultProcessor(patternQuery("?gene dcterms:alternative ?alternative"))
        {
            int nextAlternativeID = Updater.getIntValue("select coalesce(max(__)+1,0) from gene_alternatives");

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

        batch("delete from gene_alternatives where __ = ?", oldAlternatives.values());
        batch("insert into gene_alternatives(gene, alternative, __) values (?,?,?)", newAlternatives);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select gene, reference from gene_references", 10000000);

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

        batch("delete from gene_references where gene = ? and reference = ?", oldReferences);
        batch("insert into gene_references(gene, reference) values (?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newMatches = new IntStringPairIntMap(1000000);
        IntStringPairIntMap oldMatches = getIntStringPairIntMap("select gene, match, __ from gene_matches", 1000000);

        new QueryResultProcessor(patternQuery("?gene skos:closeMatch ?match"))
        {
            int nextMatcheID = Updater.getIntValue("select coalesce(max(__)+1,0) from gene_matches");

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

        batch("delete from gene_matches where __ = ?", oldMatches.values());
        batch("insert into gene_matches(gene, match, __) values (?,?,?)", newMatches);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load genes ...");

        Model model = getModel("RDF/gene/pc_gene.ttl.gz");
        check(model, "gene/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadDescriptions(model);
        loadBiosystems(model);
        loadAlternatives(model);
        loadReferences(model);
        loadCloseMatches(model);

        model.close();
        System.out.println();
    }
}
