package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Journal extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/";
    static final int prefixLength = prefix.length();

    private static final IntSet keepJournals = new IntSet();
    private static final IntSet newJournals = new IntSet();
    private static final IntSet oldJournals = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.journal_bases", oldJournals);

        new QueryResultProcessor(patternQuery("?journal rdf:type fabio:Journal"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getIntID("journal", prefix);

                if(oldJournals.remove(journalID))
                    keepJournals.add(journalID);
                else
                    newJournals.add(journalID);
            }
        }.load(model);
    }


    private static void loadCatalogIds(Model model) throws IOException, SQLException
    {
        IntStringMap keepCatalogIds = new IntStringMap();
        IntStringMap newCatalogIds = new IntStringMap();
        IntStringMap oldCatalogIds = new IntStringMap();

        load("select id,catalogid from pubchem.journal_bases where catalogid is not null", oldCatalogIds);

        new QueryResultProcessor(patternQuery("?journal fabio:hasNationalLibraryOfMedicineJournalId ?id"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"), true);
                String catalogID = getString("id");

                if(catalogID.equals(oldCatalogIds.remove(journalID)))
                {
                    keepCatalogIds.put(journalID, catalogID);
                }
                else
                {
                    String keep = keepCatalogIds.get(journalID);

                    if(catalogID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newCatalogIds.put(journalID, catalogID);

                    if(put != null && !catalogID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.journal_bases set catalogid=null where id=? and catalogid=?", oldCatalogIds);
        store("insert into pubchem.journal_bases(id,catalogid) values(?,?) "
                + "on conflict(id) do update set catalogid=EXCLUDED.catalogid", newCatalogIds);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.journal_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?journal dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(journalID)))
                {
                    keepTitles.put(journalID, title);
                }
                else
                {
                    String keep = keepTitles.get(journalID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(journalID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.journal_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.journal_bases(id,title) values(?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadAbbreviations(Model model) throws IOException, SQLException
    {
        IntStringMap keepAbbreviations = new IntStringMap();
        IntStringMap newAbbreviations = new IntStringMap();
        IntStringMap oldAbbreviations = new IntStringMap();

        load("select id,abbreviation from pubchem.journal_bases where abbreviation is not null", oldAbbreviations);

        new QueryResultProcessor(patternQuery("?journal fabio:hasNLMJournalTitleAbbreviation ?abbreviation"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"), true);
                String abbreviation = getString("abbreviation");

                if(abbreviation.equals(oldAbbreviations.remove(journalID)))
                {
                    keepAbbreviations.put(journalID, abbreviation);
                }
                else
                {
                    String keep = keepAbbreviations.get(journalID);

                    if(abbreviation.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newAbbreviations.put(journalID, abbreviation);

                    if(put != null && !abbreviation.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.journal_bases set abbreviation=null where id=? and abbreviation=?", oldAbbreviations);
        store("insert into pubchem.journal_bases(id,abbreviation) values(?,?) "
                + "on conflict(id) do update set abbreviation=EXCLUDED.abbreviation", newAbbreviations);
    }


    private static void loadIssns(Model model) throws IOException, SQLException
    {
        IntStringMap keepIssns = new IntStringMap();
        IntStringMap newIssns = new IntStringMap();
        IntStringMap oldIssns = new IntStringMap();

        load("select id,issn from pubchem.journal_bases where issn is not null", oldIssns);

        new QueryResultProcessor(patternQuery("?journal prism:issn ?issn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"), true);
                String issn = getString("issn");

                if(issn.equals(oldIssns.remove(journalID)))
                {
                    keepIssns.put(journalID, issn);
                }
                else
                {
                    String keep = keepIssns.get(journalID);

                    if(issn.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newIssns.put(journalID, issn);

                    if(put != null && !issn.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.journal_bases set issn=null where id=? and issn=?", oldIssns);
        store("insert into pubchem.journal_bases(id,issn) values(?,?) on conflict(id) do update set issn=EXCLUDED.issn",
                newIssns);
    }


    private static void loadEissns(Model model) throws IOException, SQLException
    {
        IntStringMap keepEissns = new IntStringMap();
        IntStringMap newEissns = new IntStringMap();
        IntStringMap oldEissns = new IntStringMap();

        load("select id,eissn from pubchem.journal_bases where eissn is not null", oldEissns);

        new QueryResultProcessor(patternQuery("?journal prism:eissn ?eissn"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer journalID = getJournalID(getIRI("journal"), true);
                String eissn = getString("eissn");

                if(eissn.equals(oldEissns.remove(journalID)))
                {
                    keepEissns.put(journalID, eissn);
                }
                else
                {
                    String keep = keepEissns.get(journalID);

                    if(eissn.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newEissns.put(journalID, eissn);

                    if(put != null && !eissn.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.journal_bases set eissn=null where id=? and eissn=?", oldEissns);
        store("insert into pubchem.journal_bases(id,eissn) values(?,?) "
                + "on conflict(id) do update set eissn=EXCLUDED.eissn", newEissns);
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

        store("delete from pubchem.journal_bases where id=?", oldJournals);
        store("insert into pubchem.journal_bases(id) values(?)", newJournals);

        System.out.println();
    }


    static Integer getJournalID(String value) throws IOException
    {
        return getJournalID(value, false);
    }


    static Integer getJournalID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer journalID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newJournals)
        {
            if(newJournals.contains(journalID))
            {
                if(forceKeep)
                {
                    newJournals.remove(journalID);
                    keepJournals.add(journalID);
                }
            }
            else if(!keepJournals.contains(journalID))
            {
                System.out.println("    add missing journal " + journalID);

                if(!oldJournals.remove(journalID) && !forceKeep)
                    newJournals.add(journalID);
                else
                    keepJournals.add(journalID);
            }
        }

        return journalID;
    }
}
