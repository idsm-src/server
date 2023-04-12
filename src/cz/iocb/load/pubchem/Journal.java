package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Journal extends Updater
{
    private static IntHashSet usedJournals;
    private static IntHashSet newJournals;
    private static IntHashSet oldJournals;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedJournals = new IntHashSet();
        newJournals = new IntHashSet();
        oldJournals = getIntSet("select id from pubchem.journal_bases");

        new QueryResultProcessor(patternQuery("?journal rdf:type fabio:Journal"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");

                if(!oldJournals.remove(journalID))
                    newJournals.add(journalID);

                usedJournals.add(journalID);
            }
        }.load(model);

        batch("insert into pubchem.journal_bases(id) values (?)", newJournals);
        newJournals.clear();
    }


    private static void loadCatalogIds(Model model) throws IOException, SQLException
    {
        IntStringMap newCatalogIds = new IntStringMap();
        IntStringMap oldCatalogIds = getIntStringMap(
                "select id, catalogid from pubchem.journal_bases where catalogid is not null");

        new QueryResultProcessor(patternQuery("?journal fabio:hasNationalLibraryOfMedicineJournalId ?id"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                String catalogId = getString("id");

                addJournalID(journalID);

                if(!catalogId.equals(oldCatalogIds.remove(journalID)))
                    newCatalogIds.put(journalID, catalogId);
            }
        }.load(model);

        batch("update pubchem.journal_bases set catalogid = null where id = ?", oldCatalogIds.keySet());
        batch("insert into pubchem.journal_bases(id, catalogid) values (?,?) "
                + "on conflict (id) do update set catalogid=EXCLUDED.catalogid", newCatalogIds);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.journal_bases where title is not null");

        new QueryResultProcessor(patternQuery("?journal dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                String title = getString("title");

                addJournalID(journalID);

                if(!title.equals(oldTitles.remove(journalID)))
                    newTitles.put(journalID, title);
            }
        }.load(model);

        batch("update pubchem.journal_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.journal_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbbreviations(Model model) throws IOException, SQLException
    {
        IntStringMap newAbbreviations = new IntStringMap();
        IntStringMap oldAbbreviations = getIntStringMap(
                "select id, abbreviation from pubchem.journal_bases where abbreviation is not null");

        new QueryResultProcessor(patternQuery("?journal fabio:hasNLMJournalTitleAbbreviation ?abbreviation"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                String abbreviation = getString("abbreviation");

                addJournalID(journalID);

                if(!abbreviation.equals(oldAbbreviations.remove(journalID)))
                    newAbbreviations.put(journalID, abbreviation);
            }
        }.load(model);

        batch("update pubchem.journal_bases set abbreviation = null where id = ?", oldAbbreviations.keySet());
        batch("insert into pubchem.journal_bases(id, abbreviation) values (?,?) "
                + "on conflict (id) do update set abbreviation=EXCLUDED.abbreviation", newAbbreviations);
    }


    private static void loadIssns(Model model) throws IOException, SQLException
    {
        IntStringMap newIssns = new IntStringMap();
        IntStringMap oldIssns = getIntStringMap("select id, issn from pubchem.journal_bases where issn is not null");

        new QueryResultProcessor(patternQuery("?journal prism:issn ?issn"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                String issn = getString("issn");

                addJournalID(journalID);

                if(!issn.equals(oldIssns.remove(journalID)))
                    newIssns.put(journalID, issn);
            }
        }.load(model);

        batch("update pubchem.journal_bases set issn = null where id = ?", oldIssns.keySet());
        batch("insert into pubchem.journal_bases(id, issn) values (?,?) "
                + "on conflict (id) do update set issn=EXCLUDED.issn", newIssns);
    }


    private static void loadEissns(Model model) throws IOException, SQLException
    {
        IntStringMap newEissns = new IntStringMap();
        IntStringMap oldEissns = getIntStringMap("select id, eissn from pubchem.journal_bases where eissn is not null");

        new QueryResultProcessor(patternQuery("?journal prism:eissn ?eissn"))
        {
            @Override
            protected void parse() throws IOException
            {
                int journalID = getIntID("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
                String eissn = getString("eissn");

                addJournalID(journalID);

                if(!eissn.equals(oldEissns.remove(journalID)))
                    newEissns.put(journalID, eissn);
            }
        }.load(model);

        batch("update pubchem.journal_bases set eissn = null where id = ?", oldEissns.keySet());
        batch("insert into pubchem.journal_bases(id, eissn) values (?,?) "
                + "on conflict (id) do update set eissn=EXCLUDED.eissn", newEissns);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load journals ...");

        Model model = ModelFactory.createDefaultModel();

        processFiles("pubchem/RDF/journal", "pc_journal_[0-9]+\\.ttl\\.gz", file -> {
            Model submodel = getModel(file);

            synchronized(model)
            {
                model.add(submodel);
            }

            submodel.close();
        });

        check(model, "pubchem/journal/check.sparql");

        loadBases(model);
        loadCatalogIds(model);
        loadTitles(model);
        loadAbbreviations(model);
        loadIssns(model);
        loadEissns(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish journals ...");

        batch("delete from pubchem.journal_bases where id = ?", oldJournals);
        batch("insert into pubchem.journal_bases(id) values (?)" + " on conflict do nothing", newJournals);

        usedJournals = null;
        newJournals = null;
        oldJournals = null;

        System.out.println();
    }


    static void addJournalID(int journalID)
    {
        synchronized(newJournals)
        {
            if(usedJournals.add(journalID))
            {
                System.out.println("    add missing journal " + journalID);

                if(!oldJournals.remove(journalID))
                    newJournals.add(journalID);
            }
        }
    }
}
