package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class ConservedDomain extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> domains = new EntityTable<>("pubchem.conserveddomain_bases", intKey("id"),
            null, uniqueVarchar("title"), uniqueVarchar("abstract"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?domain rdf:type obo:SO_0000417"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getIntID("domain", prefix);

                domains.reference(domainID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?domain dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getDomainID(getIRI("domain"));
                String title = getString("title");

                domains.set(domainID, "title", title);
            }
        }.load(model);
    }


    private static void loadAbstracts(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?domain dcterms:abstract ?abstract"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer domainID = getDomainID(getIRI("domain"));
                String value = getString("abstract");

                domains.set(domainID, "abstract", value);
            }
        }.load(model);
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

        domains.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish conserved domains ...");

        domains.store();

        System.out.println();
    }


    static Integer getDomainID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer domainID = Integer.parseInt(value.substring(prefixLength));

        if(domains.reference(domainID))
            System.out.println("    add missing domain PSSMID" + domainID);

        return domainID;
    }
}
