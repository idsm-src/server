package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Author extends Updater
{
    private static StringIntMap usedAuthors;
    private static StringIntMap newAuthors;
    private static StringIntMap oldAuthors;
    private static int nextAuthorID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedAuthors = new StringIntMap();
        newAuthors = new StringIntMap();
        oldAuthors = getStringIntMap("select iri, id from pubchem.author_bases");
        nextAuthorID = getIntValue("select coalesce(max(id)+1,0) from pubchem.author_bases");
    }


    private static void loadGivenNames(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet("select author, name from pubchem.author_given_names");

        new QueryResultProcessor(patternQuery("?author vcard:given-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                int authorID = getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"), false);
                String name = getString("name");

                IntObjectPair<String> pair = PrimitiveTuples.pair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.author_given_names where author = ? and name = ?", oldNames);
        batch("insert into pubchem.author_given_names(author, name) values (?,?)", newNames);
    }


    private static void loadFamilyNames(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet("select author, name from pubchem.author_family_names");

        new QueryResultProcessor(patternQuery("?author vcard:family-name ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                int authorID = getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"), false);
                String name = getString("name");

                IntObjectPair<String> pair = PrimitiveTuples.pair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.author_family_names where author = ? and name = ?", oldNames);
        batch("insert into pubchem.author_family_names(author, name) values (?,?)", newNames);
    }


    private static void loadFormattedNames(Model model) throws IOException, SQLException
    {
        IntStringPairSet newNames = new IntStringPairSet();
        IntStringPairSet oldNames = getIntStringPairSet("select author, name from pubchem.author_formatted_names");

        new QueryResultProcessor(patternQuery("?author vcard:fn ?name"))
        {
            @Override
            protected void parse() throws IOException
            {
                int authorID = getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"), false);
                String name = getString("name");

                IntObjectPair<String> pair = PrimitiveTuples.pair(authorID, name);

                if(!oldNames.remove(pair))
                    newNames.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.author_formatted_names where author = ? and name = ?", oldNames);
        batch("insert into pubchem.author_formatted_names(author, name) values (?,?)", newNames);
    }


    private static void loadOrganizations(Model model) throws IOException, SQLException
    {
        IntStringPairIntMap newOrganizations = new IntStringPairIntMap();
        IntStringPairIntMap oldOrganizations = getIntStringPairIntMap(
                "select author, organization, __ from pubchem.author_organizations");

        new QueryResultProcessor(patternQuery("?author vcard:organization-name ?organization"))
        {
            int nextValueID = getIntValue("select coalesce(max(__)+1,0) from pubchem.author_organizations");

            @Override
            protected void parse() throws IOException
            {
                int authorID = getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"), false);
                String organization = getString("organization");

                IntObjectPair<String> pair = PrimitiveTuples.pair(authorID, organization);

                if(oldOrganizations.removeKeyIfAbsent(pair, Integer.MIN_VALUE) == Integer.MIN_VALUE)
                    newOrganizations.put(pair, nextValueID++);
            }
        }.load(model);

        batch("delete from pubchem.author_organizations where __ = ?", oldOrganizations.values());
        batch("insert into pubchem.author_organizations(author, organization, __) values (?,?,?)", newOrganizations);
    }


    private static void loadOrcids(Model model) throws IOException, SQLException
    {
        IntStringPairSet newOrcids = new IntStringPairSet();
        IntStringPairSet oldOrcids = getIntStringPairSet("select author, orcid from pubchem.author_orcids");

        new QueryResultProcessor(patternQuery("?author vcard:hasUID ?orcid"))
        {
            @Override
            protected void parse() throws IOException
            {
                int authorID = getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"), false);
                String orcid = getStringID("orcid", "https://orcid.org/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(authorID, orcid);

                if(!oldOrcids.remove(pair))
                    newOrcids.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.author_orcids where author = ? and orcid = ?", oldOrcids);
        batch("insert into pubchem.author_orcids(author, orcid) values (?,?)", newOrcids);
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

        batch("delete from pubchem.author_bases where id = ?", oldAuthors.values());
        batch("insert into pubchem.author_bases(iri, id) values (?,?)" + " on conflict do nothing", newAuthors);

        usedAuthors = null;
        newAuthors = null;
        oldAuthors = null;

        System.out.println();
    }


    static int getAuthorID(String author) throws IOException
    {
        return getAuthorID(author, true);
    }


    private static int getAuthorID(String author, boolean verbose) throws IOException
    {
        synchronized(newAuthors)
        {
            int authorID = usedAuthors.getIfAbsent(author, NO_VALUE);

            if(authorID == NO_VALUE)
            {
                if(verbose)
                    System.out.println("    add missing author " + author);

                if((authorID = oldAuthors.removeKeyIfAbsent(author, NO_VALUE)) == NO_VALUE)
                    newAuthors.put(author, authorID = nextAuthorID++);

                usedAuthors.put(author, authorID);
            }

            return authorID;
        }
    }
}
