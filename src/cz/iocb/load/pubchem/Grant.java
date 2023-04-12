package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.Tuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Grant extends Updater
{
    private static StringIntMap usedGrants;
    private static StringIntMap newGrants;
    private static StringIntMap oldGrants;
    private static int nextGrantID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedGrants = new StringIntMap();
        newGrants = new StringIntMap();
        oldGrants = getStringIntMap("select iri, id from pubchem.grant_bases");
        nextGrantID = getIntValue("select coalesce(max(id)+1,0) from pubchem.grant_bases");

        new QueryResultProcessor(patternQuery("?grant rdf:type frapo:Grant"))
        {
            @Override
            protected void parse() throws IOException
            {
                String grant = getStringID("grant", "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/");
                int grantID = oldGrants.removeKeyIfAbsent(grant, NO_VALUE);

                if(grantID == NO_VALUE)
                    newGrants.put(grant, grantID = nextGrantID++);

                usedGrants.put(grant, grantID);
            }
        }.load(model);

        batch("insert into pubchem.grant_bases(iri, id) values (?,?)", newGrants);
        newGrants.clear();
    }


    private static void loadNumbers(Model model) throws IOException, SQLException
    {
        IntStringPairMap newNumbers = new IntStringPairMap();
        IntStringMap oldNumbers = getIntStringMap(
                "select id, number from pubchem.grant_bases where number is not null");

        new QueryResultProcessor(patternQuery("?grant frapo:hasGrantNumber ?number"))
        {
            @Override
            protected void parse() throws IOException
            {
                String grant = getStringID("grant", "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/");
                int grantID = getGrantID(grant);
                String number = getString("number");

                if(!number.equals(oldNumbers.remove(grantID)))
                    newNumbers.put(grantID, Tuples.pair(grant, number));
            }
        }.load(model);

        batch("update pubchem.grant_bases set number = null where id = ?", oldNumbers.keySet());
        batch("insert into pubchem.grant_bases(id, iri, number) values (?,?,?) "
                + "on conflict (id) do update set number=EXCLUDED.number", newNumbers);
    }


    private static void loadOrganizations(Model model) throws IOException, SQLException
    {
        IntStringIntPairMap newOrganizations = new IntStringIntPairMap();
        IntIntHashMap oldOrganizations = getIntIntMap(
                "select id, organization from pubchem.grant_bases where organization is not null");

        new QueryResultProcessor(patternQuery("?grant frapo:hasFundingAgency ?organization"))
        {
            @Override
            protected void parse() throws IOException
            {
                String grant = getStringID("grant", "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/");
                int grantID = getGrantID(grant);
                int organizationID = Organization.getOrganizationID(
                        getStringID("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));

                if(organizationID != oldOrganizations.removeKeyIfAbsent(grantID, NO_VALUE))
                    newOrganizations.put(grantID, Tuples.pair(grant, organizationID));
            }
        }.load(model);

        batch("update pubchem.grant_bases set organization = null where id = ?", oldOrganizations.keySet());
        batch("insert into pubchem.grant_bases(id, iri, organization) values (?,?,?) "
                + "on conflict (id) do update set organization=EXCLUDED.organization", newOrganizations);
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

        batch("delete from pubchem.grant_bases where id = ?", oldGrants.values());
        batch("insert into pubchem.grant_bases(iri, id) values (?,?)" + " on conflict do nothing", newGrants);

        usedGrants = null;
        newGrants = null;
        oldGrants = null;

        System.out.println();
    }


    static int getGrantID(String grant)
    {
        synchronized(newGrants)
        {
            int grantID = usedGrants.getIfAbsent(grant, NO_VALUE);

            if(grantID == NO_VALUE)
            {
                System.out.println("    add missing grant " + grant);

                if((grantID = oldGrants.removeKeyIfAbsent(grant, NO_VALUE)) == NO_VALUE)
                    newGrants.put(grant, grantID = nextGrantID++);

                usedGrants.put(grant, grantID);
            }

            return grantID;
        }
    }
}
