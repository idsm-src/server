package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Journal extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> journals = new EntityTable<>("pubchem.journal_bases", intKey("id"), null,
            uniqueVarchar("catalogid"), uniqueVarchar("title"), uniqueVarchar("abbreviation"), uniqueVarchar("issn"),
            uniqueVarchar("eissn"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal rdf:type fabio:Journal"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getIntID("journal", prefix);

                journals.reference(journalID);
            }
        }.load(model);
    }


    private static void loadCatalogIds(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal fabio:hasNationalLibraryOfMedicineJournalId ?id"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"));
                String catalogID = getString("id");

                journals.set(journalID, "catalogid", catalogID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"));
                String title = getString("title");

                journals.set(journalID, "title", title);
            }
        }.load(model);
    }


    private static void loadAbbreviations(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal fabio:hasNLMJournalTitleAbbreviation ?abbreviation"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"));
                String abbreviation = getString("abbreviation");

                journals.set(journalID, "abbreviation", abbreviation);
            }
        }.load(model);
    }


    private static void loadIssns(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal prism:issn ?issn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"));
                String issn = getString("issn");

                journals.set(journalID, "issn", issn);
            }
        }.load(model);
    }


    private static void loadEissns(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?journal prism:eissn ?eissn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"));
                String eissn = getString("eissn");

                journals.set(journalID, "eissn", eissn);
            }
        }.load(model);
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
            System.out.println("    add missing journal " + journalID);

        return journalID;
    }
}
