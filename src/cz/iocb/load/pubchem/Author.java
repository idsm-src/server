package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Author extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/author/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepAuthors = new StringIntMap();
    private static final StringIntMap newAuthors = new StringIntMap();
    private static final StringIntMap oldAuthors = new StringIntMap();
    private static int nextAuthorID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.author_bases", oldAuthors);

        nextAuthorID = oldAuthors.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
    }


    private static void loadGivenNames(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select author,name from pubchem.author_given_names", oldNames);

        new QueryResultProcessor(patternQuery("?author vcard:given-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer authorID = getAuthorID(getIRI("author"), false);
                String name = getString("name");

                Pair<Integer, String> pair = Pair.getPair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.author_given_names where author=? and name=?", oldNames);
        store("insert into pubchem.author_given_names(author,name) values(?,?)", newNames);
    }


    private static void loadFamilyNames(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select author,name from pubchem.author_family_names", oldNames);

        new QueryResultProcessor(patternQuery("?author vcard:family-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer authorID = getAuthorID(getIRI("author"), false);
                String name = getString("name");

                Pair<Integer, String> pair = Pair.getPair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.author_family_names where author=? and name=?", oldNames);
        store("insert into pubchem.author_family_names(author,name) values(?,?)", newNames);
    }


    private static void loadFormattedNames(Model model) throws IOException, SQLException
    {
        IntStringSet newNames = new IntStringSet();
        IntStringSet oldNames = new IntStringSet();

        load("select author,name from pubchem.author_formatted_names", oldNames);

        new QueryResultProcessor(patternQuery("?author vcard:fn ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer authorID = getAuthorID(getIRI("author"), false);
                String name = getString("name");

                Pair<Integer, String> pair = Pair.getPair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        store("delete from pubchem.author_formatted_names where author=? and name=?", oldNames);
        store("insert into pubchem.author_formatted_names(author,name) values(?,?)", newNames);
    }


    private static void loadOrganizations(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap oldOrganizations = new IntStringPairIntMap();

        load("select author,organization,__ from pubchem.author_organizations", oldOrganizations);

        new QueryResultProcessor(patternQuery("?author vcard:organization-name ?organization"))
        {
            int nextValueID = oldOrganizations.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            @Override
            protected void parse() throws IOException
            {
                Integer authorID = getAuthorID(getIRI("author"), false);
                String organization = getString("organization");

                Pair<Integer, String> pair = Pair.getPair(authorID, organization);

                if(oldOrganizations.remove(pair) == null)
                    newOrganizations.put(pair, nextValueID++);
            }
        }.load(model);

        store("delete from pubchem.author_organizations where author=? and organization=? and __=?", oldOrganizations);
        store("insert into pubchem.author_organizations(author,organization,__) values(?,?,?)", newOrganizations);
    }


    private static void loadOrcids(Model model) throws IOException, SQLException
    {
        IntStringSet newOrcids = new IntStringSet();
        IntStringSet oldOrcids = new IntStringSet();

        load("select author,orcid from pubchem.author_orcids", oldOrcids);

        new QueryResultProcessor(patternQuery("?author vcard:hasUID ?orcid"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer authorID = getAuthorID(getIRI("author"), false);
                String orcid = getStringID("orcid", "https://orcid.org/");

                Pair<Integer, String> pair = Pair.getPair(authorID, orcid);

                if(!oldOrcids.remove(pair))
                    newOrcids.add(pair);
            }
        }.load(model);

        store("delete from pubchem.author_orcids where author=? and orcid=?", oldOrcids);
        store("insert into pubchem.author_orcids(author,orcid) values(?,?)", newOrcids);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load authors ...");

        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/author", "pc_author_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/author/check.sparql");

        loadBases(model);
        loadGivenNames(model);
        loadFamilyNames(model);
        loadFormattedNames(model);
        loadOrganizations(model);
        loadOrcids(model);

        model.close();
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
                System.out.println("    add missing author " + author);

            if((authorID = oldAuthors.remove(author)) == null)
                newAuthors.put(author, authorID = nextAuthorID++);
            else
                keepAuthors.put(author, authorID);

            return authorID;
        }
    }
}
