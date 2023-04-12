package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class ConservedDomain extends Updater
{
    private static IntHashSet usedDomains;
    private static IntHashSet newDomains;
    private static IntHashSet oldDomains;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedDomains = new IntHashSet();
        newDomains = new IntHashSet();
        oldDomains = getIntSet("select id from pubchem.conserveddomain_bases");

        new QueryResultProcessor(patternQuery("?domain rdf:type obo:SO_0000417"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID");

                if(!oldDomains.remove(domainID))
                    newDomains.add(domainID);

                usedDomains.add(domainID);
            }
        }.load(model);

        batch("insert into pubchem.conserveddomain_bases(id) values (?)", newDomains);
        newDomains.clear();
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap(
                "select id, title from pubchem.conserveddomain_bases where title is not null");

        new QueryResultProcessor(patternQuery("?domain dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getDomainID(
                        getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                String title = getString("title");

                if(!title.equals(oldTitles.remove(domainID)))
                    newTitles.put(domainID, title);
            }
        }.load(model);

        batch("update pubchem.conserveddomain_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.conserveddomain_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbstracts(Model model) throws IOException, SQLException
    {
        IntStringMap newAbstracts = new IntStringMap();
        IntStringMap oldAbstracts = getIntStringMap(
                "select id, abstract from pubchem.conserveddomain_bases where abstract is not null");

        new QueryResultProcessor(patternQuery("?domain dcterms:abstract ?abstract"))
        {
            @Override
            protected void parse() throws IOException
            {
                int domainID = getDomainID(
                        getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                String value = getString("abstract");

                if(!value.equals(oldAbstracts.remove(domainID)))
                    newAbstracts.put(domainID, value);
            }
        }.load(model);

        batch("update pubchem.conserveddomain_bases set abstract = null where id = ?", oldAbstracts.keySet());
        batch("insert into pubchem.conserveddomain_bases(id, abstract) values (?,?) "
                + "on conflict (id) do update set abstract=EXCLUDED.abstract", newAbstracts);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load conserved domains ...");

        Model model = getModel("pubchem/RDF/conserveddomain/pc_conserveddomain.ttl.gz");
        check(model, "pubchem/conserveddomain/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadAbstracts(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish conserved domains ...");

        batch("delete from pubchem.conserveddomain_bases where id = ?", oldDomains);
        batch("insert into pubchem.conserveddomain_bases(id) values (?)" + " on conflict do nothing", newDomains);

        usedDomains = null;
        newDomains = null;
        oldDomains = null;

        System.out.println();
    }


    static int getDomainID(int domainID) throws IOException
    {
        synchronized(newDomains)
        {
            if(!usedDomains.contains(domainID))
            {
                System.out.println("    add missing domain PSSMID" + domainID);

                if(!oldDomains.remove(domainID))
                    newDomains.add(domainID);

                usedDomains.add(domainID);
            }
        }

        return domainID;
    }
}
