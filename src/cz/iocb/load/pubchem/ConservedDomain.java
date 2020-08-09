package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class ConservedDomain extends Updater
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newDomains = new IntHashSet(10000);
        IntHashSet oldDomains = getIntSet("select id from pubchem.conserveddomain_bases", 10000);

        new QueryResultProcessor(patternQuery("?domain rdf:type obo:SO_0000417"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");

                if(!oldDomains.remove(domainID))
                    newDomains.add(domainID);
            }
        }.load(model);

        batch("delete from pubchem.conserveddomain_bases where id = ?", oldDomains);
        batch("insert into pubchem.conserveddomain_bases(id) values (?)", newDomains);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap(10000);
        IntStringMap oldTitles = getIntStringMap(
                "select id, title from pubchem.conserveddomain_bases where title is not null", 10000);

        new QueryResultProcessor(patternQuery("?domain dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");
                String title = getString("title");

                if(!title.equals(oldTitles.remove(domainID)))
                    newTitles.put(domainID, title);
            }
        }.load(model);

        batch("update pubchem.conserveddomain_bases set title = null where id = ?", oldTitles.keySet());
        batch("update pubchem.conserveddomain_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
    }


    private static void loadAbstracts(Model model) throws IOException, SQLException
    {
        IntStringMap newAbstracts = new IntStringMap(10000);
        IntStringMap oldAbstracts = getIntStringMap(
                "select id, abstract from pubchem.conserveddomain_bases where abstract is not null", 10000);

        new QueryResultProcessor(patternQuery("?domain dcterms:abstract ?abstract"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");
                String value = getString("abstract");

                if(!value.equals(oldAbstracts.remove(domainID)))
                    newAbstracts.put(domainID, value);
            }
        }.load(model);

        batch("update pubchem.conserveddomain_bases set abstract = null where id = ?", oldAbstracts.keySet());
        batch("update pubchem.conserveddomain_bases set abstract = ? where id = ?", newAbstracts, Direction.REVERSE);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select domain, reference from pubchem.conserveddomain_references",
                10000000);

        new QueryResultProcessor(patternQuery("?domain cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");
                int referenceID = getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                IntIntPair pair = PrimitiveTuples.pair(domainID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.conserveddomain_references where domain = ? and reference = ?", oldReferences);
        batch("insert into pubchem.conserveddomain_references(domain, reference) values (?,?)", newReferences);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load conserved domains ...");

        Model model = getModel("pubchem/RDF/conserveddomain/pc_conserveddomain.ttl.gz");
        check(model, "pubchem/conserveddomain/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadAbstracts(model);
        loadReferences(model);

        model.close();
        System.out.println();
    }
}
