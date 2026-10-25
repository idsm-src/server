package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.frapo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem grants ({@code pubchem/RDF/grant}) into the table {@code pubchem.grants}.
 */
public class Grant extends Updater
{
    /**
     * IRI prefix of the grants.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the grants.
     */
    private static final EntityTable<Integer> grants = new EntityTable<>("pubchem.grants", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("number"), integer("organization"));

    /**
     * Grants referenced but not described by the data.
     */
    private static final MissingEntities<String> missingGrants = new MissingEntities<>("grant", true);

    /**
     * Ids of the grants by their IRIs without the prefix.
     */
    private static final StringIntMap grantIDs = new StringIntMap();

    /**
     * Id of the next new grant.
     */
    private static int nextGrantID;


    /**
     * Not instantiable.
     */
    private Grant()
    {
    }


    /**
     * Registers the checks of the predicates and types of the grant files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", frapo + "hasGrantNumber", frapo + "hasFundingAgency");
        dispatcher.checkTypes(all(), vocab + "Grant", frapo + "Grant");
    }


    /**
     * Loads the ids of the stored grants and registers the handler that adds the grants described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.grants", grantIDs);

        nextGrantID = grantIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(frapo + "Grant", (subject, object) -> {
            String grant = getStringID(subject, prefix);

            addGrant(grant);
            missingGrants.described(grant);
        });
    }


    /**
     * Registers the handler that sets the numbers of the grants.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadNumbers(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(frapo + "hasGrantNumber", (subject, object) -> {
            Integer grantID = getGrantID(subject.getURI());
            String number = getString(object);

            grants.set(grantID, "number", number);
        });
    }


    /**
     * Registers the handler that sets the funding agencies of the grants.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadOrganizations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(frapo + "hasFundingAgency", (subject, object) -> {
            Integer grantID = getGrantID(subject.getURI());
            Integer organizationID = Organization.getOrganizationID(object.getURI());

            grants.set(grantID, "organization", organizationID);
        });
    }


    /**
     * Loads the grants from the PubChemRDF files and flushes their table.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load grants ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadNumbers(dispatcher);
        loadOrganizations(dispatcher);

        dispatcher.load("pubchem/RDF/grant", "pc_grant_[0-9]+\\.ttl\\.gz");
        missingGrants.settle();
        dispatcher.finish();

        grants.flush();

        System.out.println();
    }


    /**
     * Stores the grants, including those referenced by the parts loaded later, and deletes the stored grants that are
     * neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish grants ...");

        grants.store();

        System.out.println();
    }


    /**
     * Returns the id of the grant with the given IRI; a grant not described by the data gets a row and is counted as
     * missing.
     *
     * @param value the IRI of the grant
     * @return the id of the grant
     * @throws IOException if the IRI is not an IRI of a grant
     */
    static Integer getGrantID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String grant = value.substring(prefixLength);

        synchronized(grantIDs)
        {
            Integer grantID = grantIDs.get(grant);

            if(grantID != null && grants.contains(grantID))
                return grantID;

            missingGrants.referenced(grant);

            return addGrant(grant);
        }
    }


    /**
     * Adds the row of a grant, which keeps its id if it has one.
     *
     * @param grant the IRI of the grant without the prefix
     * @return the id of the grant
     * @throws IOException if the row of the grant conflicts with the data
     */
    private static Integer addGrant(String grant) throws IOException
    {
        synchronized(grantIDs)
        {
            Integer grantID = grantIDs.get(grant);

            if(grantID == null)
                grantIDs.put(grant, grantID = nextGrantID++);

            grants.set(grantID, "iri", grant);

            return grantID;
        }
    }
}
