package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Book extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> books = new EntityTable<>("pubchem.book_bases", intKey("id"), null,
            uniqueVarchar("title"), varchar("publisher"), varchar("location"), uniqueVarchar("subtitle"),
            varchar("date"), uniqueVarchar("isbn"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book rdf:type fabio:Book"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getIntID("book", prefix);

                books.reference(bookID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String title = getString("title");

                books.set(bookID, "title", title);
            }
        }.load(model);
    }


    private static void loadPublishers(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book dcterms:publisher ?publisher"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String publisher = getString("publisher");

                books.set(bookID, "publisher", publisher);
            }
        }.load(model);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book prism:location ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String location = getString("location");

                books.set(bookID, "location", location);
            }
        }.load(model);
    }


    private static void loadSubtitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book prism:subtitle ?subtitle"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String subtitle = getString("subtitle");

                books.set(bookID, "subtitle", subtitle);
            }
        }.load(model);
    }


    private static void loadDates(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book dcterms:date ?date"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String date = getString("date");

                books.set(bookID, "date", date);
            }
        }.load(model);
    }


    private static void loadIsbns(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?book prism:isbn ?isbn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                String isbn = getString("isbn");

                books.set(bookID, "isbn", isbn);
            }
        }.load(model);
    }


    private static void loadAuthors(Model model) throws IOException, SQLException
    {
        IntPairSet newAuthors = new IntPairSet();
        IntPairSet oldAuthors = new IntPairSet();

        load("select book,author from pubchem.book_authors", oldAuthors);

        new QueryResultProcessor(patternQuery("?book dcterms:creator ?author"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"));
                Integer authorID = Author.getAuthorID(getIRI("author"));

                Pair<Integer, Integer> pair = Pair.getPair(bookID, authorID);

                if(!oldAuthors.remove(pair))
                    newAuthors.add(pair);
            }
        }.load(model);

        store("delete from pubchem.book_authors where book=? and author=?", oldAuthors);
        store("insert into pubchem.book_authors(book,author) values(?,?)", newAuthors);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load books ...");

        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/book", "pc_book_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/book/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadPublishers(model);
        loadLocations(model);
        loadSubtitles(model);
        loadDates(model);
        loadIsbns(model);
        loadAuthors(model);

        model.close();

        books.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish books ...");

        books.store();

        System.out.println();
    }


    static Integer getBookID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer bookID = Integer.parseInt(value.substring(prefixLength));

        if(books.reference(bookID))
            System.out.println("    add missing book NBK" + bookID);

        return bookID;
    }
}
