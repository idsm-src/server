package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Source extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepSources = new StringIntMap();
    private static final StringIntMap newSources = new StringIntMap();
    private static final StringIntMap oldSources = new StringIntMap();
    private static int nextSourceID;

    private static final IntStringPairMap newTitles = new IntStringPairMap();
    private static final IntStringMap oldTitles = new IntStringMap();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.source_bases", oldSources);

        nextSourceID = oldSources.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?source rdf:type dcterms:Dataset"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", prefix);
                Integer sourceID = oldSources.remove(source);

                if(sourceID == null)
                    newSources.put(source, nextSourceID++);
                else
                    keepSources.put(source, sourceID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();

        load("select id,title from pubchem.source_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?source dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(sourceID)))
                {
                    keepTitles.put(sourceID, title);
                }
                else
                {
                    String keep = keepTitles.get(sourceID);

                    Pair<String, String> pair = Pair.getPair(getStringID("source", prefix), title);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newTitles.put(sourceID, pair);

                    if(put != null && !title.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);
    }


    private static void loadHomepages(Model model) throws IOException, SQLException
    {
        IntStringMap keepHomepages = new IntStringMap();
        IntStringPairMap newHomepages = new IntStringPairMap();
        IntStringMap oldHomepages = new IntStringMap();

        load("select id,homepage from pubchem.source_bases where homepage is not null", oldHomepages);

        new QueryResultProcessor(patternQuery("?source foaf:homepage ?homepage"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"), true);
                String homepage = getIRI("homepage");

                if(homepage.equals(oldHomepages.remove(sourceID)))
                {
                    keepHomepages.put(sourceID, homepage);
                }
                else
                {
                    String keep = keepHomepages.get(sourceID);

                    Pair<String, String> pair = Pair.getPair(getStringID("source", prefix), homepage);

                    if(homepage.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newHomepages.put(sourceID, pair);

                    if(put != null && !homepage.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.source_bases set homepage=null where id=? and homepage=?", oldHomepages);
        store("insert into pubchem.source_bases(id,iri,homepage) values(?,?,?) "
                + "on conflict(id) do update set homepage=EXCLUDED.homepage", newHomepages);
    }


    private static void loadLicenses(Model model) throws IOException, SQLException
    {
        IntStringMap keepLicenses = new IntStringMap();
        IntStringPairMap newLicenses = new IntStringPairMap();
        IntStringMap oldLicenses = new IntStringMap();

        load("select id,license from pubchem.source_bases where license is not null", oldLicenses);

        new QueryResultProcessor(patternQuery("?source dcterms:license ?license"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"), true);
                String license = getIRI("license");

                if(license.equals(oldLicenses.remove(sourceID)))
                {
                    keepLicenses.put(sourceID, license);
                }
                else
                {
                    String keep = keepLicenses.get(sourceID);

                    Pair<String, String> pair = Pair.getPair(getStringID("source", prefix), license);

                    if(license.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newLicenses.put(sourceID, pair);

                    if(put != null && !license.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.source_bases set license=null where id=? and license=?", oldLicenses);
        store("insert into pubchem.source_bases(id,iri,license) values(?,?,?) "
                + "on conflict(id) do update set license=EXCLUDED.license", newLicenses);
    }


    private static void loadRights(Model model) throws IOException, SQLException
    {
        IntStringMap keepRights = new IntStringMap();
        IntStringPairMap newRights = new IntStringPairMap();
        IntStringMap oldRights = new IntStringMap();

        load("select id,rights from pubchem.source_bases where rights is not null", oldRights);

        new QueryResultProcessor(patternQuery("?source dcterms:rights ?rights"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer sourceID = getSourceID(getIRI("source"), true);
                String rights = getString("rights");

                if(rights.equals(oldRights.remove(sourceID)))
                {
                    keepRights.put(sourceID, rights);
                }
                else
                {
                    String keep = keepRights.get(sourceID);

                    Pair<String, String> pair = Pair.getPair(getStringID("source", prefix), rights);

                    if(rights.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newRights.put(sourceID, pair);

                    if(put != null && !rights.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.source_bases set rights=null where id=? and rights=?", oldRights);
        store("insert into pubchem.source_bases(id,iri,rights) values(?,?,?) "
                + "on conflict(id) do update set rights=EXCLUDED.rights", newRights);
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
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish sources ...");

        store("update pubchem.source_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.source_bases(id,iri,title) values(?,?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);

        store("delete from pubchem.source_bases where iri=? and id=?", oldSources);
        store("insert into pubchem.source_bases(iri,id) values(?,?)", newSources);

        System.out.println();
    }


    static Integer registerSourceID(String source, String title) throws IOException
    {
        synchronized(newSources)
        {
            Integer sourceID = keepSources.get(source);

            if(sourceID != null)
                return sourceID;

            sourceID = newSources.get(source);

            if(sourceID != null)
                return sourceID;

            System.out.println("    add missing source " + source);

            if((sourceID = oldSources.remove(source)) == null)
                keepSources.put(source, sourceID = nextSourceID++);
            else
                keepSources.put(source, sourceID);

            if(!title.equals(oldTitles.remove(sourceID)))
                newTitles.put(sourceID, Pair.getPair(source, title));

            return sourceID;
        }
    }


    static Integer getSourceID(String value) throws IOException
    {
        return getSourceID(value, false);
    }


    static Integer getSourceID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String source = value.substring(prefixLength);

        synchronized(newSources)
        {
            Integer sourceID = keepSources.get(source);

            if(sourceID != null)
                return sourceID;

            sourceID = newSources.get(source);

            if(sourceID != null)
            {
                if(keepForce)
                {
                    newSources.remove(source);
                    keepSources.put(source, sourceID);
                }

                return sourceID;
            }

            System.out.println("    add missing source " + source);

            if((sourceID = oldSources.remove(source)) != null)
                keepSources.put(source, sourceID);
            else if(keepForce)
                keepSources.put(source, sourceID = nextSourceID++);
            else
                newSources.put(source, sourceID = nextSourceID++);

            return sourceID;
        }
    }
}
