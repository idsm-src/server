package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class ConservedDomain extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepDomains = new IntSet();
    private static final IntSet newDomains = new IntSet();
    private static final IntSet oldDomains = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.conserveddomain_bases", oldDomains);

        new QueryResultProcessor(patternQuery("?domain rdf:type obo:SO_0000417"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getIntID("domain", prefix);

                if(oldDomains.remove(domainID))
                    keepDomains.add(domainID);
                else
                    newDomains.add(domainID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.conserveddomain_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?domain dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getDomainID(getIRI("domain"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(domainID)))
                {
                    keepTitles.put(domainID, title);
                }
                else
                {
                    String keep = keepTitles.get(domainID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(domainID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.conserveddomain_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.conserveddomain_bases(id,title) values(?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbstracts(Model model) throws IOException, SQLException
    {
        IntStringMap keepAbstracts = new IntStringMap();
        IntStringMap newAbstracts = new IntStringMap();
        IntStringMap oldAbstracts = new IntStringMap();

        load("select id,abstract from pubchem.conserveddomain_bases where abstract is not null", oldAbstracts);

        new QueryResultProcessor(patternQuery("?domain dcterms:abstract ?abstract"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getDomainID(getIRI("domain"), true);
                String value = getString("abstract");

                if(value.equals(oldAbstracts.remove(domainID)))
                {
                    keepAbstracts.put(domainID, value);
                }
                else
                {
                    String keep = keepAbstracts.get(domainID);

                    if(value.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newAbstracts.put(domainID, value);

                    if(put != null && !value.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.conserveddomain_bases set abstract=null where id=? and abstract=?", oldAbstracts);
        store("insert into pubchem.conserveddomain_bases(id,abstract) values(?,?) "
                + "on conflict(id) do update set abstract=EXCLUDED.abstract", newAbstracts);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select domain,reference from pubchem.conserveddomain_references", oldReferences);

        new QueryResultProcessor(patternQuery("?domain cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getDomainID(getIRI("domain"));
                Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(domainID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.conserveddomain_references where domain=? and reference=?", oldReferences);
        store("insert into pubchem.conserveddomain_references(domain,reference) values(?,?)", newReferences);
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


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish conserved domains ...");

        store("delete from pubchem.conserveddomain_bases where id=?", oldDomains);
        store("insert into pubchem.conserveddomain_bases(id) values(?)", newDomains);

        System.out.println();
    }


    static Integer getDomainID(String value) throws IOException
    {
        return getDomainID(value, false);
    }


    static Integer getDomainID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer domainID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newDomains)
        {
            if(newDomains.contains(domainID))
            {
                if(forceKeep)
                {
                    newDomains.remove(domainID);
                    keepDomains.add(domainID);
                }
            }
            else if(!keepDomains.contains(domainID))
            {
                System.out.println("    add missing domain PSSMID" + domainID);

                if(!oldDomains.remove(domainID) && !forceKeep)
                    newDomains.add(domainID);
                else
                    keepDomains.add(domainID);
            }
        }

        return domainID;
    }
}
