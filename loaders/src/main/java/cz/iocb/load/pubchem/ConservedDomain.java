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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem conserved domains from {@code pubchem/RDF/conserveddomain/pc_conserveddomain.ttl.gz} into the
 * table {@code pubchem.conserveddomains} and the table of their references.
 */
class ConservedDomain extends Updater
{
    /**
     * IRI prefix of the conserved domains.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the conserved domains.
     */
    private static final EntityTable<Integer> domains = new EntityTable<>("pubchem.conserveddomains", intKey("id"),
            null, uniqueVarchar("title"), uniqueVarchar("abstract"));

    /**
     * Conserved domains referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingDomains = new MissingEntities<>("conserved domain", true);


    /**
     * Not instantiable.
     */
    private ConservedDomain()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the conserved domain file.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "abstract", dcterms + "title", rdf + "type", rdfs + "seeAlso",
                cito + "isDiscussedBy");
        dispatcher.checkTypes(all(), vocab + "ConservedDomain", obo + "SO_0000417");
        dispatcher.checkLink(all(), rdfs + "seeAlso", all(),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID",
                "https://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi\\?uid=");
    }


    /**
     * Registers the handler that adds the conserved domains described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(obo + "SO_0000417", (subject, object) -> {
            Integer domainID = getIntID(subject, prefix);

            domains.reference(domainID);
            missingDomains.described(domainID);
        });
    }


    /**
     * Registers the handler that sets the titles of the conserved domains.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer domainID = getDomainID(subject.getURI());
            String title = getString(object);

            domains.set(domainID, "title", title);
        });
    }


    /**
     * Registers the handler that sets the abstracts of the conserved domains.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadAbstracts(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "abstract", (subject, object) -> {
            Integer domainID = getDomainID(subject.getURI());
            String value = getString(object);

            domains.set(domainID, "abstract", value);
        });
    }


    /**
     * Registers the handler of the references discussing the conserved domains and the action that stores their changes
     * in {@code pubchem.conserveddomain_references}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Loads the conserved domains from {@code pubchem/RDF/conserveddomain/pc_conserveddomain.ttl.gz} into the tables
     * and flushes the table of the conserved domains.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Stores the conserved domains, including those referenced by the parts loaded later, and deletes the stored
     * conserved domains that are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish conserved domains ...");

        domains.store();

        System.out.println();
    }


    /**
     * Returns the id of the conserved domain with the given IRI; a conserved domain not described by the data gets a
     * row and is counted as missing.
     *
     * @param value the IRI of the conserved domain
     * @return the id of the conserved domain
     * @throws IOException if the IRI is not an IRI of a conserved domain
     */
    static Integer getDomainID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer domainID = Integer.parseInt(value.substring(prefixLength));

        if(domains.reference(domainID))
            missingDomains.referenced(domainID);

        return domainID;
    }
}
