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
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



public class Author extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/author/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepAuthors = new StringIntMap();
    private static final StringIntMap newAuthors = new StringIntMap();
    private static final StringIntMap oldAuthors = new StringIntMap();
    private static int nextAuthorID;
    private static final MissingEntities<String> missingAuthors = new MissingEntities<>("author", false);


    private static void loadBases() throws SQLException
    {
        load("select iri,id from pubchem.author_bases", oldAuthors);

        nextAuthorID = oldAuthors.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
    }


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), vcard + "given-name", vcard + "family-name", vcard + "fn",
                vcard + "organization-name", vcard + "hasUID", dcterms + "source", rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Author");
        dispatcher.checkValues(dcterms + "source", "https://orcid.org");
    }


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


    private static void loadOrganizations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringPairIntMap keepOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap newOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap oldOrganizations = new IntStringPairIntMap();

        load("select author,organization,__ from pubchem.author_organizations", oldOrganizations);

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
            store("delete from pubchem.author_organizations where author=? and organization=? and __=?",
                    oldOrganizations);
            store("insert into pubchem.author_organizations(author,organization,__) values(?,?,?)", newOrganizations);
        });
    }


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


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish authors ...");

        store("delete from pubchem.author_bases where iri=? and id=?", oldAuthors);
        store("insert into pubchem.author_bases(iri,id) values(?,?)", newAuthors);

        System.out.println();
    }


    static Integer getAuthorID(String author) throws IOException
    {
        return getAuthorID(author, true);
    }


    private static Integer getAuthorID(String value, boolean verbose) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

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
