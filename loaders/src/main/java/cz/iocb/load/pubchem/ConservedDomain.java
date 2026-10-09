package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.obo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



class ConservedDomain extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> domains = new EntityTable<>("pubchem.conserveddomains", intKey("id"),
            null, uniqueVarchar("title"), uniqueVarchar("abstract"));
    private static final MissingEntities<Integer> missingDomains = new MissingEntities<>("conserved domain", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "abstract", dcterms + "title", rdf + "type", rdfs + "seeAlso",
                cito + "isDiscussedBy");
        dispatcher.checkTypes(all(), vocab + "ConservedDomain", obo + "SO_0000417");
        dispatcher.checkLink(all(), rdfs + "seeAlso", all(),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID",
                "https://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi\\?uid=");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(obo + "SO_0000417", (subject, object) -> {
            Integer domainID = getIntID(subject, prefix);

            domains.reference(domainID);
            missingDomains.described(domainID);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer domainID = getDomainID(subject.getURI());
            String title = getString(object);

            domains.set(domainID, "title", title);
        });
    }


    private static void loadAbstracts(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "abstract", (subject, object) -> {
            Integer domainID = getDomainID(subject.getURI());
            String value = getString(object);

            domains.set(domainID, "abstract", value);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select conserveddomain,reference from pubchem.conserveddomain_references", oldReferences);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer domainID = getDomainID(subject.getURI());
            Integer referenceID = Reference.getReferenceID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(domainID, referenceID);

            if(oldReferences.remove(pair))
                keepReferences.add(pair);
            else if(!keepReferences.contains(pair))
                newReferences.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.conserveddomain_references where conserveddomain=? and reference=?",
                    oldReferences);
            store("insert into pubchem.conserveddomain_references(conserveddomain,reference) values(?,?)",
                    newReferences);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load conserved domains ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTitles(dispatcher);
        loadAbstracts(dispatcher);
        loadReferences(dispatcher);

        dispatcher.load("pubchem/RDF/conserveddomain/pc_conserveddomain.ttl.gz");
        missingDomains.settle();
        dispatcher.finish();

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
            missingDomains.referenced(domainID);

        return domainID;
    }
}
