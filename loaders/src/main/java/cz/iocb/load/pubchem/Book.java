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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem books from the files {@code pubchem/RDF/book/pc_book_*.ttl.gz} into the table
 * {@code pubchem.books} and the table of their authors.
 */
public class Book extends Updater
{
    /**
     * IRI prefix of the books.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the books.
     */
    private static final EntityTable<Integer> books = new EntityTable<>("pubchem.books", intKey("id"), null,
            uniqueVarchar("title"), varchar("publisher"), varchar("location"), uniqueVarchar("subtitle"),
            varchar("date"), uniqueVarchar("isbn"));

    /**
     * Books referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingBooks = new MissingEntities<>("book", true);


    /**
     * Not instantiable.
     */
    private Book()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the book files.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", dcterms + "creator", dcterms + "title", dcterms + "publisher",
                dcterms + "date", prism + "location", prism + "isbn", prism + "subtitle", skos + "exactMatch");
        dispatcher.checkTypes(all(), vocab + "Book", fabio + "Book");
        dispatcher.checkLink(all(), skos + "exactMatch", all(), "http://rdf.ncbi.nlm.nih.gov/pubchem/book/",
                "https://www.ncbi.nlm.nih.gov/books/");
    }


    /**
     * Registers the handler that adds the books described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(fabio + "Book", (subject, object) -> {
            Integer bookID = getIntID(subject, prefix);

            books.reference(bookID);
            missingBooks.described(bookID);
        });
    }


    /**
     * Registers the handler that sets the titles of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String title = getString(object);

            books.set(bookID, "title", title);
        });
    }


    /**
     * Registers the handler that sets the publishers of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadPublishers(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "publisher", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String publisher = getString(object);

            books.set(bookID, "publisher", publisher);
        });
    }


    /**
     * Registers the handler that sets the publication places of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadLocations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "location", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String location = getString(object);

            books.set(bookID, "location", location);
        });
    }


    /**
     * Registers the handler that sets the subtitles of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSubtitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "subtitle", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String subtitle = getString(object);

            books.set(bookID, "subtitle", subtitle);
        });
    }


    /**
     * Registers the handler that sets the dates of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadDates(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "date", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String date = getString(object);

            books.set(bookID, "date", date);
        });
    }


    /**
     * Registers the handler that sets the ISBNs of the books.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIsbns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "isbn", (subject, object) -> {
            Integer bookID = getBookID(subject.getURI());
            String isbn = getString(object);

            books.set(bookID, "isbn", isbn);
        });
    }


    /**
     * Registers the handler of the authors of the books and the action that stores their changes in
     * {@code pubchem.book_authors}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Loads the books from the files {@code pubchem/RDF/book/pc_book_*.ttl.gz} into the tables and flushes the table of
     * the books.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Stores the books, including those referenced by the parts loaded later, and deletes the stored books that are
     * neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish books ...");

        books.store();

        System.out.println();
    }


    /**
     * Returns the id of the book with the given IRI; a book not described by the data gets a row and is counted as
     * missing.
     *
     * @param value the IRI of the book
     * @return the id of the book
     * @throws IOException if the IRI is not an IRI of a book
     */
    static Integer getBookID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer bookID = Integer.parseInt(value.substring(prefixLength));

        if(books.reference(bookID))
            missingBooks.referenced(bookID);

        return bookID;
    }
}
