package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.fabio;
import static cz.iocb.load.pubchem.PubChemRDF.prism;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



public class Book extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> books = new EntityTable<>("pubchem.book_bases", intKey("id"), null,
            uniqueVarchar("title"), varchar("publisher"), varchar("location"), uniqueVarchar("subtitle"),
            varchar("date"), uniqueVarchar("isbn"));
    private static final MissingEntities<Integer> missingBooks = new MissingEntities<>("book", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", dcterms + "creator", dcterms + "title", dcterms + "publisher",
                dcterms + "date", prism + "location", prism + "isbn", prism + "subtitle", skos + "exactMatch");
        dispatcher.checkTypes(all(), vocab + "Book", fabio + "Book");
        dispatcher.checkLink(all(), skos + "exactMatch", all(), "http://rdf.ncbi.nlm.nih.gov/pubchem/book/",
                "https://www.ncbi.nlm.nih.gov/books/");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(fabio + "Book", (subject, object) -> {
            Integer bookID = getIntID(subject, prefix);

            books.reference(bookID);
            missingBooks.described(bookID);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String title = getString(object);

            books.set(bookID, "title", title);
        });
    }


    private static void loadPublishers(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "publisher", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String publisher = getString(object);

            books.set(bookID, "publisher", publisher);
        });
    }


    private static void loadLocations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "location", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String location = getString(object);

            books.set(bookID, "location", location);
        });
    }


    private static void loadSubtitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "subtitle", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String subtitle = getString(object);

            books.set(bookID, "subtitle", subtitle);
        });
    }


    private static void loadDates(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "date", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String date = getString(object);

            books.set(bookID, "date", date);
        });
    }


    private static void loadIsbns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "isbn", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String isbn = getString(object);

            books.set(bookID, "isbn", isbn);
        });
    }


    private static void loadAuthors(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepAuthors = new IntPairSet();
        IntPairSet newAuthors = new IntPairSet();
        IntPairSet oldAuthors = new IntPairSet();

        load("select book,author from pubchem.book_authors", oldAuthors);

        dispatcher.on(dcterms + "creator", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            Integer authorID = Author.getAuthorID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(bookID, authorID);

            if(oldAuthors.remove(pair))
                keepAuthors.add(pair);
            else if(!keepAuthors.contains(pair))
                newAuthors.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.book_authors where book=? and author=?", oldAuthors);
            store("insert into pubchem.book_authors(book,author) values(?,?)", newAuthors);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load books ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTitles(dispatcher);
        loadPublishers(dispatcher);
        loadLocations(dispatcher);
        loadSubtitles(dispatcher);
        loadDates(dispatcher);
        loadIsbns(dispatcher);
        loadAuthors(dispatcher);

        dispatcher.load("pubchem/RDF/book", "pc_book_[0-9]+\\.ttl\\.gz");
        missingBooks.settle();
        dispatcher.finish();

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
            missingBooks.referenced(bookID);

        return bookID;
    }
}
