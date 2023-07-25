package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Book extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK";
    static final int prefixLength = prefix.length();

    private static final IntSet keepBooks = new IntSet();
    private static final IntSet newBooks = new IntSet();
    private static final IntSet oldBooks = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.book_bases", oldBooks);

        new QueryResultProcessor(patternQuery("?book rdf:type fabio:Book"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getIntID("book", prefix);

                if(oldBooks.remove(bookID))
                    keepBooks.add(bookID);
                else
                    newBooks.add(bookID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.book_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?book dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(bookID)))
                {
                    keepTitles.put(bookID, title);
                }
                else
                {
                    String keep = keepTitles.get(bookID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(bookID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.book_bases(id,title) values(?,?) on conflict(id) do update set title=EXCLUDED.title",
                newTitles);
    }


    private static void loadPublishers(Model model) throws IOException, SQLException
    {
        IntStringMap keepPublishers = new IntStringMap();
        IntStringMap newPublishers = new IntStringMap();
        IntStringMap oldPublishers = new IntStringMap();

        load("select id,publisher from pubchem.book_bases where publisher is not null", oldPublishers);

        new QueryResultProcessor(patternQuery("?book dcterms:publisher ?publisher"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String publisher = getString("publisher");

                if(publisher.equals(oldPublishers.remove(bookID)))
                {
                    keepPublishers.put(bookID, publisher);
                }
                else
                {
                    String keep = keepPublishers.get(bookID);

                    if(publisher.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newPublishers.put(bookID, publisher);

                    if(put != null && !publisher.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set publisher=null where id=? and publisher=?", oldPublishers);
        store("insert into pubchem.book_bases(id,publisher) values(?,?) "
                + "on conflict(id) do update set publisher=EXCLUDED.publisher", newPublishers);
    }


    private static void loadLocations(Model model) throws IOException, SQLException
    {
        IntStringMap keepLocations = new IntStringMap();
        IntStringMap newLocations = new IntStringMap();
        IntStringMap oldLocations = new IntStringMap();

        load("select id,location from pubchem.book_bases where location is not null", oldLocations);

        new QueryResultProcessor(patternQuery("?book prism:location ?location"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String location = getString("location");

                if(location.equals(oldLocations.remove(bookID)))
                {
                    keepLocations.put(bookID, location);
                }
                else
                {
                    String keep = keepLocations.get(bookID);

                    if(location.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLocations.put(bookID, location);

                    if(put != null && !location.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set location=null where id=? and location=?", oldLocations);
        store("insert into pubchem.book_bases(id,location) values(?,?) "
                + "on conflict(id) do update set location=EXCLUDED.location", newLocations);
    }


    private static void loadSubtitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepSubtitles = new IntStringMap();
        IntStringMap newSubtitles = new IntStringMap();
        IntStringMap oldSubtitles = new IntStringMap();

        load("select id,subtitle from pubchem.book_bases where subtitle is not null", oldSubtitles);

        new QueryResultProcessor(patternQuery("?book dcterms:publisher ?subtitle"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String subtitle = getString("subtitle");

                if(subtitle.equals(oldSubtitles.remove(bookID)))
                {
                    keepSubtitles.put(bookID, subtitle);
                }
                else
                {
                    String keep = keepSubtitles.get(bookID);

                    if(subtitle.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newSubtitles.put(bookID, subtitle);

                    if(put != null && !subtitle.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set subtitle=null where id=? and subtitle=?", oldSubtitles);
        store("insert into pubchem.book_bases(id,subtitle) values(?,?) "
                + "on conflict(id) do update set subtitle=EXCLUDED.subtitle", newSubtitles);
    }


    private static void loadDates(Model model) throws IOException, SQLException
    {
        IntStringMap keepDates = new IntStringMap();
        IntStringMap newDates = new IntStringMap();
        IntStringMap oldDates = new IntStringMap();

        load("select id,date from pubchem.book_bases where date is not null", oldDates);

        new QueryResultProcessor(patternQuery("?book dcterms:date ?date"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String date = getString("date");

                if(date.equals(oldDates.remove(bookID)))
                {
                    keepDates.put(bookID, date);
                }
                else
                {
                    String keep = keepDates.get(bookID);

                    if(date.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newDates.put(bookID, date);

                    if(put != null && !date.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set date=null where id=? and date=?", oldDates);
        store("insert into pubchem.book_bases(id,date) values(?,?) on conflict(id) do update set date=EXCLUDED.date",
                newDates);
    }


    private static void loadIsbns(Model model) throws IOException, SQLException
    {
        IntStringMap keepIsbns = new IntStringMap();
        IntStringMap newIsbns = new IntStringMap();
        IntStringMap oldIsbns = new IntStringMap();

        load("select id,isbn from pubchem.book_bases where isbn is not null", oldIsbns);

        new QueryResultProcessor(patternQuery("?book prism:isbn ?isbn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bookID = getBookID(getIRI("book"), true);
                String isbn = getString("isbn");

                if(isbn.equals(oldIsbns.remove(bookID)))
                {
                    keepIsbns.put(bookID, isbn);
                }
                else
                {
                    String keep = keepIsbns.get(bookID);

                    if(isbn.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newIsbns.put(bookID, isbn);

                    if(put != null && !isbn.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.book_bases set isbn=null where id=? and isbn=?", oldIsbns);
        store("insert into pubchem.book_bases(id,isbn) values(?,?) on conflict(id) do update set isbn=EXCLUDED.isbn",
                newIsbns);
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
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish books ...");

        store("delete from pubchem.book_bases where id=?", oldBooks);
        store("insert into pubchem.book_bases(id) values(?)", newBooks);

        System.out.println();
    }


    static Integer getBookID(String value) throws IOException
    {
        return getBookID(value, false);
    }


    static Integer getBookID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer bookID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newBooks)
        {
            if(newBooks.contains(bookID))
            {
                if(forceKeep)
                {
                    newBooks.remove(bookID);
                    keepBooks.add(bookID);
                }
            }
            else if(!keepBooks.contains(bookID))
            {
                System.out.println("    add missing book NBK" + bookID);

                if(!oldBooks.remove(bookID) && !forceKeep)
                    newBooks.add(bookID);
                else
                    keepBooks.add(bookID);
            }
        }

        return bookID;
    }
}
