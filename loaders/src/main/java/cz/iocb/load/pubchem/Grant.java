package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Grant extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> grants = new EntityTable<>("pubchem.grant_bases", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("number"), integer("organization"));
    private static final StringIntMap grantIDs = new StringIntMap();
    private static int nextGrantID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.grant_bases", grantIDs);

        nextGrantID = grantIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?grant rdf:type frapo:Grant"))
        {
            @Override
            protected void parse() throws IOException
            {
                addGrant(getStringID("grant", prefix));
            }
        }.load(model);
    }


    private static void loadNumbers(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?grant frapo:hasGrantNumber ?number"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer grantID = getGrantID(getIRI("grant"));
                String number = getString("number");

                grants.set(grantID, "number", number);
            }
        }.load(model);
    }


    private static void loadOrganizations(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?grant frapo:hasFundingAgency ?organization"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer grantID = getGrantID(getIRI("grant"));
                Integer organizationID = Organization.getOrganizationID(getIRI("organization"));

                grants.set(grantID, "organization", organizationID);
            }
        }.load(model);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load grants ...");

        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/grant", "pc_grant_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/grant/check.sparql");

        loadBases(model);
        loadNumbers(model);
        loadOrganizations(model);

        model.close();

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
            throw new IOException("unexpected IRI: " + value);

        String grant = value.substring(prefixLength);

        synchronized(grantIDs)
        {
            Integer grantID = grantIDs.get(grant);

            if(grantID != null && grants.contains(grantID))
                return grantID;

            System.out.println("    add missing grant " + grant);

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
