package cz.iocb.load.pubchem;

import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.vcard;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem authors from the files {@code pubchem/RDF/author/pc_author_*.ttl.gz} into the table
 * {@code pubchem.authors} and the tables of their names, organizations and ORCID identifiers.
 */
public class Author extends Updater
{
    /**
     * IRI prefix of the authors.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/author/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Stored authors met in the data, by their IRIs without the prefix.
     */
    private static final StringIntMap keepAuthors = new StringIntMap();

    /**
     * Authors met in the data that are not stored yet, by their IRIs without the prefix.
     */
    private static final StringIntMap newAuthors = new StringIntMap();

    /**
     * Stored authors not met in the data so far, by their IRIs without the prefix; those left are deleted.
     */
    private static final StringIntMap oldAuthors = new StringIntMap();

    /**
     * Id of the next new author.
     */
    private static int nextAuthorID;

    /**
     * Authors referenced but not described by the data.
     */
    private static final MissingEntities<String> missingAuthors = new MissingEntities<>("author", false);


    /**
     * Not instantiable.
     */
    private Author()
    {
    }


    /**
     * Loads the ids of the stored authors.
     *
     * @throws SQLException if a database error occurs
     */
    private static void loadBases() throws SQLException
    {
        load("select iri,id from pubchem.authors", oldAuthors);

        nextAuthorID = oldAuthors.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
    }


    /**
     * Registers the checks of the predicates, types and values of the author files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), vcard + "given-name", vcard + "family-name", vcard + "fn",
                vcard + "organization-name", vcard + "hasUID", dcterms + "source", rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Author");
        dispatcher.checkValues(dcterms + "source", "https://orcid.org");
    }


    /**
     * Registers the handler of the given names of the authors and the action that stores their changes in
     * {@code pubchem.author_given_names}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGivenNames(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select author,name from pubchem.author_given_names", oldNames);

        dispatcher.on(vcard + "given-name", (subject, object) -> {
            Integer authorID = getAuthorID(subject.getURI(), false);
            String name = getString(object);

            Pair<Integer, String> pair = Pair.getPair(authorID, name);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.author_given_names where author=? and name=?", oldNames);
            store("insert into pubchem.author_given_names(author,name) values(?,?)", newNames);
        });
    }


    /**
     * Registers the handler of the family names of the authors and the action that stores their changes in
     * {@code pubchem.author_family_names}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadFamilyNames(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepNames = new IntStringSet();
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select author,name from pubchem.author_family_names", oldNames);

        dispatcher.on(vcard + "family-name", (subject, object) -> {
            Integer authorID = getAuthorID(subject.getURI(), false);
            String name = getString(object);

            Pair<Integer, String> pair = Pair.getPair(authorID, name);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.author_family_names where author=? and name=?", oldNames);
            store("insert into pubchem.author_family_names(author,name) values(?,?)", newNames);
        });
    }


    /**
     * Registers the handler of the formatted names of the authors and the action that stores their changes in
     * {@code pubchem.author_formatted_names}.
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

        load("select author,name from pubchem.author_formatted_names", oldNames);

        dispatcher.on(vcard + "fn", (subject, object) -> {
            Integer authorID = getAuthorID(subject.getURI(), false);
            String name = getString(object);

            Pair<Integer, String> pair = Pair.getPair(authorID, name);

            if(oldNames.remove(pair))
                keepNames.add(pair);
            else if(!keepNames.contains(pair))
                newNames.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.author_formatted_names where author=? and name=?", oldNames);
            store("insert into pubchem.author_formatted_names(author,name) values(?,?)", newNames);
        });
    }


    /**
     * Registers the handler of the organization names of the authors, each with an id of its own, and the action that
     * stores their changes in {@code pubchem.author_organizations}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadOrganizations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringPairIntMap keepOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap newOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap oldOrganizations = new IntStringPairIntMap();

        load("select author,organization,id from pubchem.author_organizations", oldOrganizations);

        AtomicInteger nextValueID = new AtomicInteger(
                oldOrganizations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1);

        dispatcher.on(vcard + "organization-name", (subject, object) -> {
            Integer authorID = getAuthorID(subject.getURI(), false);
            String organization = getString(object);

            Pair<Integer, String> pair = Pair.getPair(authorID, organization);
            Integer valueID = oldOrganizations.remove(pair);

            if(valueID != null)
                keepOrganizations.put(pair, valueID);
            else if(!keepOrganizations.containsKey(pair) && !newOrganizations.containsKey(pair))
                newOrganizations.put(pair, nextValueID.getAndIncrement());
        });

        dispatcher.after(() -> {
            store("delete from pubchem.author_organizations where author=? and organization=? and id=?",
                    oldOrganizations);
            store("insert into pubchem.author_organizations(author,organization,id) values(?,?,?)", newOrganizations);
        });
    }


    /**
     * Registers the handler of the ORCID identifiers of the authors and the action that stores their changes in
     * {@code pubchem.author_orcids}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadOrcids(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepOrcids = new IntStringSet();
        IntStringSet newOrcids = new IntStringSet();
        IntStringSet oldOrcids = new IntStringSet();

        load("select author,orcid from pubchem.author_orcids", oldOrcids);

        dispatcher.on(vcard + "hasUID", (subject, object) -> {
            Integer authorID = getAuthorID(subject.getURI(), false);
            String orcid = getStringID(object, "https://orcid.org/");

            Pair<Integer, String> pair = Pair.getPair(authorID, orcid);

            if(oldOrcids.remove(pair))
                keepOrcids.add(pair);
            else if(!keepOrcids.contains(pair))
                newOrcids.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.author_orcids where author=? and orcid=?", oldOrcids);
            store("insert into pubchem.author_orcids(author,orcid) values(?,?)", newOrcids);
        });
    }


    /**
     * Loads the authors from the files {@code pubchem/RDF/author/pc_author_*.ttl.gz} into the tables.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load authors ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases();
        loadGivenNames(dispatcher);
        loadFamilyNames(dispatcher);
        loadFormattedNames(dispatcher);
        loadOrganizations(dispatcher);
        loadOrcids(dispatcher);

        dispatcher.load("pubchem/RDF/author", "pc_author_[0-9]+\\.ttl\\.gz");
        dispatcher.finish();

        System.out.println();
    }


    /**
     * Stores the authors, including those referenced by the parts loaded later, and deletes the stored authors that are
     * neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish authors ...");

        store("delete from pubchem.authors where iri=? and id=?", oldAuthors);
        store("insert into pubchem.authors(iri,id) values(?,?)", newAuthors);

        System.out.println();
    }


    /**
     * Returns the id of the author of an IRI referenced by another loader; an author not described by the author files
     * is counted as missing.
     *
     * @param author the IRI of the author
     * @return the id of the author
     * @throws IOException if the IRI is not an IRI of an author
     */
    static Integer getAuthorID(String author) throws IOException
    {
        return getAuthorID(author, true);
    }


    /**
     * Returns the id of the author of an IRI; an author unknown so far gets a new id unless it is stored already.
     *
     * @param value the IRI of the author
     * @param verbose whether an author unknown so far is counted as missing
     * @return the id of the author
     * @throws IOException if the IRI is not an IRI of an author
     */
    private static Integer getAuthorID(String value, boolean verbose) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String author = value.substring(prefixLength);

        synchronized(newAuthors)
        {
            Integer authorID = keepAuthors.get(author);

            if(authorID != null)
                return authorID;

            authorID = newAuthors.get(author);

            if(authorID != null)
                return authorID;

            if(verbose)
                missingAuthors.referenced(author);

            if((authorID = oldAuthors.remove(author)) == null)
                newAuthors.put(author, authorID = nextAuthorID++);
            else
                keepAuthors.put(author, authorID);

            return authorID;
        }
    }
}
