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



public class Grant extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> grants = new EntityTable<>("pubchem.grants", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("number"), integer("organization"));
    private static final MissingEntities<String> missingGrants = new MissingEntities<>("grant", true);
    private static final StringIntMap grantIDs = new StringIntMap();
    private static int nextGrantID;


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", frapo + "hasGrantNumber", frapo + "hasFundingAgency");
        dispatcher.checkTypes(all(), vocab + "Grant", frapo + "Grant");
    }


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


    private static void loadNumbers(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(frapo + "hasGrantNumber", (subject, object) -> {
            Integer grantID = getGrantID(subject.getURI());
            String number = getString(object);

            grants.set(grantID, "number", number);
        });
    }


    private static void loadOrganizations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(frapo + "hasFundingAgency", (subject, object) -> {
            Integer grantID = getGrantID(subject.getURI());
            Integer organizationID = Organization.getOrganizationID(object.getURI());

            grants.set(grantID, "organization", organizationID);
        });
    }


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


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish grants ...");

        grants.store();

        System.out.println();
    }


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


    /*
     * Adds the row of a grant, which keeps its id if it has one.
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
