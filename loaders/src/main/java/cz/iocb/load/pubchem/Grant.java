package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Grant extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepGrants = new StringIntMap();
    private static final StringIntMap newGrants = new StringIntMap();
    private static final StringIntMap oldGrants = new StringIntMap();
    private static int nextGrantID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.grant_bases", oldGrants);

        nextGrantID = oldGrants.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?grant rdf:type frapo:Grant"))
        {
            @Override
            protected void parse() throws IOException
            {
                String grant = getStringID("grant", prefix);
                Integer grantID = oldGrants.remove(grant);

                if(grantID == null)
                    newGrants.put(grant, nextGrantID++);
                else
                    keepGrants.put(grant, grantID);
            }
        }.load(model);
    }


    private static void loadNumbers(Model model) throws IOException, SQLException
    {
        IntStringMap keepNumbers = new IntStringMap();
        IntStringPairMap newNumbers = new IntStringPairMap();
        IntStringMap oldNumbers = new IntStringMap();

        load("select id,number from pubchem.grant_bases where number is not null", oldNumbers);

        new QueryResultProcessor(patternQuery("?grant frapo:hasGrantNumber ?number"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer grantID = getGrantID(getIRI("grant"), true);
                String number = getString("number");

                if(number.equals(oldNumbers.remove(grantID)))
                {
                    keepNumbers.put(grantID, number);
                }
                else
                {
                    String keep = keepNumbers.get(grantID);

                    Pair<String, String> pair = Pair.getPair(getStringID("grant", prefix), number);

                    if(number.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newNumbers.put(grantID, pair);

                    if(put != null && !number.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.grant_bases set number=null where id=? and number=?", oldNumbers);
        store("insert into pubchem.grant_bases(id,iri,number) values(?,?,?) "
                + "on conflict(id) do update set number=EXCLUDED.number", newNumbers);
    }


    private static void loadOrganizations(Model model) throws IOException, SQLException
    {
        IntIntMap keepOrganizations = new IntIntMap();
        IntStringIntPairMap newOrganizations = new IntStringIntPairMap();
        IntIntMap oldOrganizations = new IntIntMap();

        load("select id,organization from pubchem.grant_bases where organization is not null", oldOrganizations);

        new QueryResultProcessor(patternQuery("?grant frapo:hasFundingAgency ?organization"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer grantID = getGrantID(getIRI("grant"), true);
                Integer organizationID = Organization.getOrganizationID(getIRI("organization"));

                if(organizationID.equals(oldOrganizations.remove(grantID)))
                {
                    keepOrganizations.put(grantID, organizationID);
                }
                else
                {
                    Integer keep = keepOrganizations.get(grantID);

                    Pair<String, Integer> pair = Pair.getPair(getStringID("grant", prefix), organizationID);

                    if(organizationID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, Integer> put = newOrganizations.put(grantID, pair);

                    if(put != null && !organizationID.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.grant_bases set organization=null where id=? and organization=?", oldOrganizations);
        store("insert into pubchem.grant_bases(id,iri,organization) values(?,?,?) "
                + "on conflict(id) do update set organization=EXCLUDED.organization", newOrganizations);
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
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish grants ...");

        store("delete from pubchem.grant_bases where iri=? and id=?", oldGrants);
        store("insert into pubchem.grant_bases(iri,id) values(?,?)", newGrants);

        System.out.println();
    }


    static Integer getGrantID(String value) throws IOException
    {
        return getGrantID(value, false);
    }


    static Integer getGrantID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String grant = value.substring(prefixLength);

        synchronized(newGrants)
        {
            Integer grantID = keepGrants.get(grant);

            if(grantID != null)
                return grantID;

            grantID = newGrants.get(grant);

            if(grantID != null)
            {
                if(keepForce)
                {
                    newGrants.remove(grant);
                    keepGrants.put(grant, grantID);
                }

                return grantID;
            }

            System.out.println("    add missing grant " + grant);

            if((grantID = oldGrants.remove(grant)) != null)
                keepGrants.put(grant, grantID);
            else if(keepForce)
                keepGrants.put(grant, grantID = nextGrantID++);
            else
                newGrants.put(grant, grantID = nextGrantID++);

            return grantID;
        }
    }
}
