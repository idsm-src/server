package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Organization extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepOrganizations = new StringIntMap();
    private static final StringIntMap newOrganizations = new StringIntMap();
    private static final StringIntMap oldOrganizations = new StringIntMap();
    private static int nextOrganizationID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.organization_bases", oldOrganizations);

        nextOrganizationID = oldOrganizations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?organization rdf:type vcard:Organization"))
        {
            @Override
            protected void parse() throws IOException
            {
                String organization = getStringID("organization", prefix);
                Integer organizationID;

                if((organizationID = oldOrganizations.remove(organization)) == null)
                    newOrganizations.put(organization, nextOrganizationID++);
                else
                    keepOrganizations.put(organization, organizationID);
            }
        }.load(model);
    }


    private static void loadCountryNames(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,name from pubchem.organization_country_names", oldNames);

        new QueryResultProcessor(patternQuery("?organization vcard:country-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer organizationID = getOrganizationID(getIRI("organization"));
                String name = getString("name");

                Pair<Integer, String> pair = Pair.getPair(organizationID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.organization_country_names where organization=? and name=?", oldNames);
        store("insert into pubchem.organization_country_names(organization,name) values(?,?)", newNames);
    }


    private static void loadFormattedNames(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,name from pubchem.organization_formatted_names", oldNames);

        new QueryResultProcessor(patternQuery("?organization vcard:fn ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer organizationID = getOrganizationID(getIRI("organization"));
                String name = getString("name");

                Pair<Integer, String> pair = Pair.getPair(organizationID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.organization_formatted_names where organization=? and name=?", oldNames);
        store("insert into pubchem.organization_formatted_names(organization,name) values(?,?)", newNames);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,crossref from pubchem.organization_crossref_matches", oldNames);

        new QueryResultProcessor(patternQuery("?organization skos:closeMatch ?crossref"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer organizationID = getOrganizationID(getIRI("organization"));
                String crossref = getStringID("crossref", "https://data.crossref.org/fundingdata/funder/");

                Pair<Integer, String> pair = Pair.getPair(organizationID, crossref);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.organization_crossref_matches where organization=? and crossref=?", oldNames);
        store("insert into pubchem.organization_crossref_matches(organization,crossref) values(?,?)", newNames);
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

        store("delete from pubchem.organization_bases where iri=? and id=?", oldOrganizations);
        store("insert into pubchem.organization_bases(iri,id) values(?,?)", newOrganizations);

        System.out.println();
    }


    static Integer getOrganizationID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String organization = value.substring(prefixLength);

        synchronized(newOrganizations)
        {
            Integer organizationID = keepOrganizations.get(organization);

            if(organizationID != null)
                return organizationID;

            organizationID = newOrganizations.get(organization);

            if(organizationID != null)
                return organizationID;

            System.out.println("    add missing organization " + organization);

            if((organizationID = oldOrganizations.remove(organization)) == null)
                newOrganizations.put(organization, organizationID = nextOrganizationID++);
            else
                keepOrganizations.put(organization, organizationID);

            return organizationID;
        }
    }
}
