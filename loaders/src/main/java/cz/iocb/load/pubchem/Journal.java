package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.str;
import static cz.iocb.load.common.TripleDispatcher.text;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem journals ({@code pubchem/RDF/journal}) into the table {@code pubchem.journals}.
 */
public class Journal extends Updater
{
    /**
     * IRI prefix of the journals.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the journals.
     */
    private static final EntityTable<Integer> journals = new EntityTable<>("pubchem.journals", intKey("id"), null,
            uniqueVarchar("catalog_id"), uniqueVarchar("title"), uniqueVarchar("abbreviation"), uniqueVarchar("issn"),
            uniqueVarchar("eissn"));

    /**
     * Journals referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingJournals = new MissingEntities<>("journal", true);


    /**
     * Not instantiable.
     */
    private Journal()
    {
    }


    /**
     * Registers the checks of the predicates and types of the journal files and the check that the NLM catalog matches
     * of the journals agree with their catalog ids.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", dcterms + "title", prism + "issn", prism + "eissn",
                skos + "exactMatch", fabio + "hasNationalLibraryOfMedicineJournalId",
                fabio + "hasNLMJournalTitleAbbreviation");
        dispatcher.checkTypes(all(), vocab + "Journal", fabio + "Journal");

        HashMap<Node, HashSet<String>> matches = new HashMap<>();
        HashMap<Node, HashSet<String>> catalogIds = new HashMap<>();

        dispatcher.on(skos + "exactMatch", (subject, object) -> {
            if(str(object) != null)
                matches.computeIfAbsent(subject, k -> new HashSet<>()).add(str(object));
        });

        dispatcher.on(fabio + "hasNationalLibraryOfMedicineJournalId", (subject, object) -> {
            if(str(object) != null)
                catalogIds.computeIfAbsent(subject, k -> new HashSet<>()).add(str(object));
        });

        dispatcher.after(() -> {
            for(Entry<Node, HashSet<String>> entry : matches.entrySet())
                for(String id : catalogIds.getOrDefault(entry.getKey(), new HashSet<>()))
                    for(String match : entry.getValue())
                        if(!id.equals(match.replaceAll("https://www.ncbi.nlm.nih.gov/nlmcatalog/", "")))
                            Problems.error("value of " + skos + "exactMatch not matching the NLM catalog id",
                                    text(entry.getKey()) + " " + match);
        });
    }


    /**
     * Registers the handler that adds the journals described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(fabio + "Journal", (subject, object) -> {
            Integer journalID = getIntID(subject, prefix);

            journals.reference(journalID);
            missingJournals.described(journalID);
        });
    }


    /**
     * Registers the handler that sets the NLM catalog ids of the journals.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCatalogIds(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(fabio + "hasNationalLibraryOfMedicineJournalId", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String catalogID = getString(object);

            journals.set(journalID, "catalog_id", catalogID);
        });
    }


    /**
     * Registers the handler that sets the titles of the journals.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String title = getString(object);

            journals.set(journalID, "title", title);
        });
    }


    /**
     * Registers the handler that sets the NLM title abbreviations of the journals.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadAbbreviations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(fabio + "hasNLMJournalTitleAbbreviation", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String abbreviation = getString(object);

            journals.set(journalID, "abbreviation", abbreviation);
        });
    }


    /**
     * Registers the handler that sets the ISSNs of the journals.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIssns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "issn", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String issn = getString(object);

            journals.set(journalID, "issn", issn);
        });
    }


    /**
     * Registers the handler that sets the electronic ISSNs of the journals.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadEissns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "eissn", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String eissn = getString(object);

            journals.set(journalID, "eissn", eissn);
        });
    }


    /**
     * Loads the journals from the PubChemRDF files and flushes their table.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load journals ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadCatalogIds(dispatcher);
        loadTitles(dispatcher);
        loadAbbreviations(dispatcher);
        loadIssns(dispatcher);
        loadEissns(dispatcher);

        dispatcher.load("pubchem/RDF/journal", "pc_journal_[0-9]+\\.ttl\\.gz");
        missingJournals.settle();
        dispatcher.finish();

        journals.flush();

        System.out.println();
    }


    /**
     * Stores the journals, including those referenced by the parts loaded later, and deletes the stored journals that
     * are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish journals ...");

        journals.store();

        System.out.println();
    }


    /**
     * Returns the id of the journal with the given IRI; a journal not described by the data gets a row and is counted
     * as missing.
     *
     * @param value the IRI of the journal
     * @return the id of the journal
     * @throws IOException if the IRI is not an IRI of a journal
     */
    static Integer getJournalID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer journalID = Integer.parseInt(value.substring(prefixLength));

        if(journals.reference(journalID))
            missingJournals.referenced(journalID);

        return journalID;
    }
}
