package cz.iocb.load.pubchem;

import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.frapo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vcard;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



public class Organization extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepOrganizations = new StringIntMap();
    private static final StringIntMap newOrganizations = new StringIntMap();
    private static final StringIntMap oldOrganizations = new StringIntMap();
    private static final MissingEntities<String> missingOrganizations = new MissingEntities<>("organization", true);
    private static int nextOrganizationID;


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", vcard + "country-name", vcard + "fn", skos + "closeMatch");
        dispatcher.checkTypes(all(), vocab + "Organization", frapo + "FundingAgency", vcard + "Organization");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.organization_bases", oldOrganizations);

        nextOrganizationID = oldOrganizations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(vcard + "Organization", (subject, object) -> {
            String organization = getStringID(subject, prefix);

            addOrganization(organization);
            missingOrganizations.described(organization);
        });
    }


    private static void loadCountryNames(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,name from pubchem.organization_country_names", oldNames);

        dispatcher.on(vcard + "country-name", (subject, object) -> {
            Integer organizationID = getOrganizationID(subject.getURI());
            String name = getString(object);

            Pair<Integer, String> pair = Pair.getPair(organizationID, name);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.organization_country_names where organization=? and name=?", oldNames);
            store("insert into pubchem.organization_country_names(organization,name) values(?,?)", newNames);
        });
    }


    private static void loadFormattedNames(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,name from pubchem.organization_formatted_names", oldNames);

        dispatcher.on(vcard + "fn", (subject, object) -> {
            Integer organizationID = getOrganizationID(subject.getURI());
            String name = getString(object);

            Pair<Integer, String> pair = Pair.getPair(organizationID, name);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.organization_formatted_names where organization=? and name=?", oldNames);
            store("insert into pubchem.organization_formatted_names(organization,name) values(?,?)", newNames);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,crossref from pubchem.organization_crossref_matches", oldNames);

        dispatcher.on(skos + "closeMatch", (subject, object) -> {
            Integer organizationID = getOrganizationID(subject.getURI());
            String crossref = getStringID(object, "https://data.crossref.org/fundingdata/funder/");

            Pair<Integer, String> pair = Pair.getPair(organizationID, crossref);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.organization_crossref_matches where organization=? and crossref=?", oldNames);
            store("insert into pubchem.organization_crossref_matches(organization,crossref) values(?,?)", newNames);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load organizations ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadCountryNames(dispatcher);
        loadFormattedNames(dispatcher);
        loadCloseMatches(dispatcher);

        dispatcher.load("pubchem/RDF/organization", "pc_organization_[0-9]+\\.ttl\\.gz");
        missingOrganizations.settle();
        dispatcher.finish();

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

            missingOrganizations.referenced(organization);

            return addOrganization(organization);
        }
    }


    /*
     * Classifies an organization as kept or new unless it has been classified already.
     */
    private static Integer addOrganization(String organization)
    {
        synchronized(newOrganizations)
        {
            Integer organizationID = keepOrganizations.get(organization);

            if(organizationID == null)
                organizationID = newOrganizations.get(organization);

            if(organizationID != null)
                return organizationID;

            if((organizationID = oldOrganizations.remove(organization)) == null)
                newOrganizations.put(organization, organizationID = nextOrganizationID++);
            else
                keepOrganizations.put(organization, organizationID);

            return organizationID;
        }
    }
}
