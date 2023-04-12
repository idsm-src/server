package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Book extends Updater
{
    private static IntHashSet usedBooks;
    private static IntHashSet newBooks;
    private static IntHashSet oldBooks;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedBooks = new IntHashSet();
        newBooks = new IntHashSet();
        oldBooks = getIntSet("select id from pubchem.book_bases");

        new QueryResultProcessor(patternQuery("?book rdf:type fabio:Book"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");

                if(!oldBooks.remove(bookID))
                    newBooks.add(bookID);

                usedBooks.add(bookID);
            }
        }.load(model);

        batch("insert into pubchem.book_bases(id) values (?)", newBooks);
        newBooks.clear();
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.book_bases where title is not null");

        new QueryResultProcessor(patternQuery("?book dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String title = getString("title");

                addBookID(bookID);

                if(!title.equals(oldTitles.remove(bookID)))
                    newTitles.put(bookID, title);
            }
        }.load(model);

        batch("update pubchem.book_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.book_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadPublishers(Model model) throws IOException, SQLException
    {
        IntStringMap newPublishers = new IntStringMap();
        IntStringMap oldPublishers = getIntStringMap(
                "select id, publisher from pubchem.book_bases where publisher is not null");

        new QueryResultProcessor(patternQuery("?book dcterms:publisher ?publisher"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String publisher = getString("publisher");

                addBookID(bookID);

                if(!publisher.equals(oldPublishers.remove(bookID)))
                    newPublishers.put(bookID, publisher);
            }
        }.load(model);

        batch("update pubchem.book_bases set publisher = null where id = ?", oldPublishers.keySet());
        batch("insert into pubchem.book_bases(id, publisher) values (?,?) "
                + "on conflict (id) do update set publisher=EXCLUDED.publisher", newPublishers);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        IntStringMap newLocations = new IntStringMap();
        IntStringMap oldLocations = getIntStringMap(
                "select id, location from pubchem.book_bases where location is not null");

        new QueryResultProcessor(patternQuery("?book prism:location ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String location = getString("location");

                addBookID(bookID);

                if(!location.equals(oldLocations.remove(bookID)))
                    newLocations.put(bookID, location);
            }
        }.load(model);

        batch("update pubchem.book_bases set location = null where id = ?", oldLocations.keySet());
        batch("insert into pubchem.book_bases(id, location) values (?,?) "
                + "on conflict (id) do update set location=EXCLUDED.location", newLocations);
    }


    private static void loadSubtitles(Model model) throws IOException, SQLException
    {
        IntStringMap newSubtitles = new IntStringMap();
        IntStringMap oldSubtitles = getIntStringMap(
                "select id, subtitle from pubchem.book_bases where subtitle is not null");

        new QueryResultProcessor(patternQuery("?book dcterms:publisher ?subtitle"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String subtitle = getString("subtitle");

                addBookID(bookID);

                if(!subtitle.equals(oldSubtitles.remove(bookID)))
                    newSubtitles.put(bookID, subtitle);
            }
        }.load(model);

        batch("update pubchem.book_bases set subtitle = null where id = ?", oldSubtitles.keySet());
        batch("insert into pubchem.book_bases(id, subtitle) values (?,?) "
                + "on conflict (id) do update set subtitle=EXCLUDED.subtitle", newSubtitles);
    }


    private static void loadDates(Model model) throws IOException, SQLException
    {
        IntStringMap newDates = new IntStringMap();
        IntStringMap oldDates = getIntStringMap("select id, date from pubchem.book_bases where date is not null");

        new QueryResultProcessor(patternQuery("?book dcterms:date ?date"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String date = getString("date");

                addBookID(bookID);

                if(!date.equals(oldDates.remove(bookID)))
                    newDates.put(bookID, date);
            }
        }.load(model);

        batch("update pubchem.book_bases set date = null where id = ?", oldDates.keySet());
        batch("insert into pubchem.book_bases(id, date) values (?,?) "
                + "on conflict (id) do update set date=EXCLUDED.date", newDates);
    }


    private static void loadIsbns(Model model) throws IOException, SQLException
    {
        IntStringMap newIsbns = new IntStringMap();
        IntStringMap oldIsbns = getIntStringMap("select id, isbn from pubchem.book_bases where isbn is not null");

        new QueryResultProcessor(patternQuery("?book prism:isbn ?isbn"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                String isbn = getString("isbn");

                addBookID(bookID);

                if(!isbn.equals(oldIsbns.remove(bookID)))
                    newIsbns.put(bookID, isbn);
            }
        }.load(model);

        batch("update pubchem.book_bases set isbn = null where id = ?", oldIsbns.keySet());
        batch("insert into pubchem.book_bases(id, isbn) values (?,?) "
                + "on conflict (id) do update set isbn=EXCLUDED.isbn", newIsbns);
    }


    private static void loadAuthors(Model model) throws IOException, SQLException
    {
        IntPairSet newAuthors = new IntPairSet();
        IntPairSet oldAuthors = getIntPairSet("select book, author from pubchem.book_authors");

        new QueryResultProcessor(patternQuery("?book dcterms:creator ?author"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bookID = getIntID("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK");
                int authorID = Author.getAuthorID(getStringID("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"));
                IntIntPair pair = PrimitiveTuples.pair(bookID, authorID);

                addBookID(bookID);

                if(!oldAuthors.remove(pair))
                    newAuthors.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.book_authors where book = ? and author = ?", oldAuthors);
        batch("insert into pubchem.book_authors(book, author) values (?,?)", newAuthors);
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
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish books ...");

        batch("delete from pubchem.book_bases where id = ?", oldBooks);
        batch("insert into pubchem.book_bases(id) values (?)" + " on conflict do nothing", newBooks);

        usedBooks = null;
        newBooks = null;
        oldBooks = null;

        System.out.println();
    }


    static void addBookID(int bookID)
    {
        synchronized(newBooks)
        {
            if(usedBooks.add(bookID))
            {
                System.out.println("    add missing book NBK" + bookID);

                if(!oldBooks.remove(bookID))
                    newBooks.add(bookID);
            }
        }
    }
}
