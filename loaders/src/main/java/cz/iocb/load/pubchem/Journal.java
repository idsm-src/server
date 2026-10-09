package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.str;
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
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



public class Journal extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> journals = new EntityTable<>("pubchem.journals", intKey("id"), null,
            uniqueVarchar("catalog_id"), uniqueVarchar("title"), uniqueVarchar("abbreviation"), uniqueVarchar("issn"),
            uniqueVarchar("eissn"));
    private static final MissingEntities<Integer> missingJournals = new MissingEntities<>("journal", true);


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
                            dispatcher.missing(match);
        });
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(fabio + "Journal", (subject, object) -> {
            Integer journalID = getIntID(subject, prefix);

            journals.reference(journalID);
            missingJournals.described(journalID);
        });
    }


    private static void loadCatalogIds(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(fabio + "hasNationalLibraryOfMedicineJournalId", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String catalogID = getString(object);

            journals.set(journalID, "catalog_id", catalogID);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String title = getString(object);

            journals.set(journalID, "title", title);
        });
    }


    private static void loadAbbreviations(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(fabio + "hasNLMJournalTitleAbbreviation", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String abbreviation = getString(object);

            journals.set(journalID, "abbreviation", abbreviation);
        });
    }


    private static void loadIssns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "issn", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String issn = getString(object);

            journals.set(journalID, "issn", issn);
        });
    }


    private static void loadEissns(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(prism + "eissn", (subject, object) -> {
            Integer journalID = getJournalID(subject.getURI());
            String eissn = getString(object);

            journals.set(journalID, "eissn", eissn);
        });
    }


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


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish journals ...");

        journals.store();

        System.out.println();
    }


    static Integer getJournalID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer journalID = Integer.parseInt(value.substring(prefixLength));

        if(journals.reference(journalID))
            missingJournals.referenced(journalID);

        return journalID;
    }
}
