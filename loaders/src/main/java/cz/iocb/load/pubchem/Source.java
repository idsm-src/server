package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Source extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> sources = new EntityTable<>("pubchem.source_bases", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("title"), varchar("homepage"), varchar("license"),
            varchar("rights"));
    private static final StringIntMap sourceIDs = new StringIntMap();
    private static int nextSourceID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.source_bases", sourceIDs);

        nextSourceID = sourceIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?source rdf:type dcterms:Dataset"))
        {
            @Override
            protected void parse() throws IOException
            {
                addSource(getStringID("source", prefix));
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?source dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                String title = getString("title");

                sources.set(sourceID, "title", title);
            }
        }.load(model);
    }


    private static void loadHomepages(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?source foaf:homepage ?homepage"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                String homepage = getIRI("homepage");

                sources.set(sourceID, "homepage", homepage);
            }
        }.load(model);
    }


    private static void loadLicenses(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?source dcterms:license ?license"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                String license = getIRI("license");

                sources.set(sourceID, "license", license);
            }
        }.load(model);
    }


    private static void loadRights(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?source dcterms:rights ?rights"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                String rights = getString("rights");

                sources.set(sourceID, "rights", rights);
            }
        }.load(model);
    }


    private static void loadSubjects(Model model) throws IOException, SQLException
    {
        IntPairSet newSubjects = new IntPairSet();
        IntPairSet oldSubjects = new IntPairSet();

        load("select source,subject from pubchem.source_subjects", oldSubjects);

        new QueryResultProcessor(patternQuery("?source dcterms:subject ?subject"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                Integer conceptID = Concept.getConceptID(getIRI("subject"));

                Pair<Integer, Integer> pair = Pair.getPair(sourceID, conceptID);

                if(!oldSubjects.remove(pair))
                    newSubjects.add(pair);
            }
        }.load(model);

        store("delete from pubchem.source_subjects where source=? and subject=?", oldSubjects);
        store("insert into pubchem.source_subjects(source,subject) values(?,?)", newSubjects);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select source,alternative from pubchem.source_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?source dcterms:alternative ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(sourceID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.source_alternatives where source=? and alternative=?", oldAlternatives);
        store("insert into pubchem.source_alternatives(source,alternative) values(?,?)", newAlternatives);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load sources ...");

        Model model = getModel("pubchem/RDF/source/pc_source.ttl.gz");

        check(model, "pubchem/source/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadHomepages(model);
        loadLicenses(model);
        loadRights(model);
        loadSubjects(model);
        loadAlternatives(model);

        model.close();

        sources.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish sources ...");

        sources.store();

        System.out.println();
    }


    static Integer registerSourceID(String source, String title) throws IOException
    {
        synchronized(sourceIDs)
        {
            Integer sourceID = sourceIDs.get(source);

            if(sourceID != null && sources.contains(sourceID))
                return sourceID;

            System.out.println("    add missing source " + source);

            sourceID = addSource(source);
            sources.set(sourceID, "title", title);

            return sourceID;
        }
    }


    static Integer getSourceID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String source = value.substring(prefixLength);

        synchronized(sourceIDs)
        {
            Integer sourceID = sourceIDs.get(source);

            if(sourceID != null && sources.contains(sourceID))
                return sourceID;

            System.out.println("    add missing source " + source);

            return addSource(source);
        }
    }


    /*
     * Adds the row of a source, which keeps its id if it has one.
     */
    private static Integer addSource(String source) throws IOException
    {
        synchronized(sourceIDs)
        {
            Integer sourceID = sourceIDs.get(source);

            if(sourceID == null)
                sourceIDs.put(source, sourceID = nextSourceID++);

            sources.set(sourceID, "iri", source);

            return sourceID;
        }
    }
}
