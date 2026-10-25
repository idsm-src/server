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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem organizations ({@code pubchem/RDF/organization}) into the table {@code pubchem.organizations}
 * and the tables of their names and Crossref funder matches.
 */
public class Organization extends Updater
{
    /**
     * IRI prefix of the organizations.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Stored organizations met in the data, by their IRIs without the prefix.
     */
    private static final StringIntMap keepOrganizations = new StringIntMap();

    /**
     * Organizations met in the data that are not stored yet, by their IRIs without the prefix.
     */
    private static final StringIntMap newOrganizations = new StringIntMap();

    /**
     * Stored organizations not met in the data so far, by their IRIs without the prefix; those left are deleted.
     */
    private static final StringIntMap oldOrganizations = new StringIntMap();

    /**
     * Organizations referenced but not described by the data.
     */
    private static final MissingEntities<String> missingOrganizations = new MissingEntities<>("organization", true);

    /**
     * Id of the next new organization.
     */
    private static int nextOrganizationID;


    /**
     * Not instantiable.
     */
    private Organization()
    {
    }


    /**
     * Registers the checks of the predicates and types of the organization files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", vcard + "country-name", vcard + "fn", skos + "closeMatch");
        dispatcher.checkTypes(all(), vocab + "Organization", frapo + "FundingAgency", vcard + "Organization");
    }


    /**
     * Loads the ids of the stored organizations and registers the handler that adds the organizations described by the
     * data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        load("select iri,id from pubchem.organizations", oldOrganizations);

        nextOrganizationID = oldOrganizations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(vcard + "Organization", (subject, object) -> {
            String organization = getStringID(subject, prefix);

            addOrganization(organization);
            missingOrganizations.described(organization);
        });
    }


    /**
     * Registers the handler of the country names of the organizations and the action that stores their changes in
     * {@code pubchem.organization_country_names}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the formatted names of the organizations and the action that stores their changes in
     * {@code pubchem.organization_formatted_names}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the Crossref funders matching the organizations and the action that stores their changes
     * in {@code pubchem.organization_crossref_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select organization,match from pubchem.organization_crossref_matches", oldNames);

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
            store("delete from pubchem.organization_crossref_matches where organization=? and match=?", oldNames);
            store("insert into pubchem.organization_crossref_matches(organization,match) values(?,?)", newNames);
        });
    }


    /**
     * Loads the organizations from the PubChemRDF files.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Stores the new organizations, including those referenced by the parts loaded later, and deletes the stored
     * organizations that are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish organizations ...");

        store("delete from pubchem.organizations where iri=? and id=?", oldOrganizations);
        store("insert into pubchem.organizations(iri,id) values(?,?)", newOrganizations);

        System.out.println();
    }


    /**
     * Returns the id of the organization with the given IRI; an organization not described by the data is added and
     * counted as missing.
     *
     * @param value the IRI of the organization
     * @return the id of the organization
     * @throws IOException if the IRI is not an IRI of an organization
     */
    static Integer getOrganizationID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

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


    /**
     * Classifies an organization as kept or new unless it has been classified already.
     *
     * @param organization the IRI of the organization without the prefix
     * @return the id of the organization
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
