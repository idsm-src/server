package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Organization extends Updater
{
    private static StringIntMap usedOrganizations;
    private static StringIntMap newOrganizations;
    private static StringIntMap oldOrganizations;
    private static int nextOrganizationID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedOrganizations = new StringIntMap();
        newOrganizations = new StringIntMap();
        oldOrganizations = getStringIntMap("select iri, id from pubchem.organization_bases");
        nextOrganizationID = getIntValue("select coalesce(max(id)+1,0) from pubchem.organization_bases");

        new QueryResultProcessor(patternQuery("?organization rdf:type vcard:Organization"))
        {
            @Override
            protected void parse() throws IOException
            {
                String organization = getStringID("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/");
                int organizationID;

                if((organizationID = oldOrganizations.removeKeyIfAbsent(organization, NO_VALUE)) == NO_VALUE)
                    newOrganizations.put(organization, organizationID = nextOrganizationID++);

                usedOrganizations.put(organization, organizationID);
            }
        }.load(model);

        batch("insert into pubchem.organization_bases(iri, id) values (?,?)", newOrganizations);
        newOrganizations.clear();
    }


    private static void loadCountryNames(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet(
                "select organization, name from pubchem.organization_country_names");

        new QueryResultProcessor(patternQuery("?organization vcard:country-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                int organizationID = getOrganizationID(
                        getStringID("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
                String name = getString("name");

                IntObjectPair<String> pair = PrimitiveTuples.pair(organizationID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.organization_country_names where organization = ? and name = ?", oldNames);
        batch("insert into pubchem.organization_country_names(organization, name) values (?,?)", newNames);
    }


    private static void loadFormattedNames(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet(
                "select organization, name from pubchem.organization_formatted_names");

        new QueryResultProcessor(patternQuery("?organization vcard:fn ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                int organizationID = getOrganizationID(
                        getStringID("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
                String name = getString("name");

                IntObjectPair<String> pair = PrimitiveTuples.pair(organizationID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.organization_formatted_names where organization = ? and name = ?", oldNames);
        batch("insert into pubchem.organization_formatted_names(organization, name) values (?,?)", newNames);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet(
                "select organization, crossref from pubchem.organization_crossref_matches");

        new QueryResultProcessor(patternQuery("?organization skos:closeMatch ?crossref"))
        {
            @Override
            protected void parse() throws IOException
            {
                int organizationID = getOrganizationID(
                        getStringID("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
                String crossref = getStringID("crossref", "https://data.crossref.org/fundingdata/funder/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(organizationID, crossref);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.organization_crossref_matches where organization = ? and crossref = ?", oldNames);
        batch("insert into pubchem.organization_crossref_matches(organization, crossref) values (?,?)", newNames);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load organizations ...");

        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/organization", "pc_organization_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/organization/check.sparql");

        loadBases(model);
        loadCountryNames(model);
        loadFormattedNames(model);
        loadCloseMatches(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish organizations ...");

        batch("delete from pubchem.organization_bases where id = ?", oldOrganizations.values());
        batch("insert into pubchem.organization_bases(iri, id) values (?,?)" + " on conflict do nothing",
                newOrganizations);

        usedOrganizations = null;
        newOrganizations = null;
        oldOrganizations = null;

        System.out.println();
    }


    static int getOrganizationID(String organization) throws IOException
    {
        synchronized(newOrganizations)
        {
            int organizationID = usedOrganizations.getIfAbsent(organization, NO_VALUE);

            if(organizationID == NO_VALUE)
            {
                System.out.println("    add missing organization " + organization);

                if((organizationID = oldOrganizations.removeKeyIfAbsent(organization, NO_VALUE)) == NO_VALUE)
                    newOrganizations.put(organization, organizationID = nextOrganizationID++);

                usedOrganizations.put(organization, organizationID);
            }

            return organizationID;
        }
    }
}
