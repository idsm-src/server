package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Source extends Updater
{
    private static StringIntMap usedSources;
    private static StringIntMap newSources;
    private static StringIntMap oldSources;
    private static int nextSourceID;

    private static IntStringPairMap newTitles;
    private static IntStringMap oldTitles;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedSources = new StringIntMap();
        newSources = new StringIntMap();
        oldSources = getStringIntMap("select iri, id from pubchem.source_bases");
        nextSourceID = getIntValue("select coalesce(max(id)+1,0) from pubchem.source_bases");

        new QueryResultProcessor(patternQuery("?source rdf:type dcterms:Dataset"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
                int sourceID = oldSources.removeKeyIfAbsent(source, NO_VALUE);

                if(sourceID == NO_VALUE)
                    newSources.put(source, sourceID = nextSourceID++);

                usedSources.put(source, sourceID);
            }
        }.load(model);

        batch("insert into pubchem.source_bases(iri, id) values (?,?)", newSources);
        newSources.clear();
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        newTitles = new IntStringPairMap();
        oldTitles = getIntStringMap("select id, title from pubchem.source_bases where title is not null");

        new QueryResultProcessor(patternQuery("?source dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
                int sourceID = getSourceID(source);

                String title = getString("title");

                if(!title.equals(oldTitles.remove(sourceID)))
                    newTitles.put(sourceID, Tuples.pair(source, title));
            }
        }.load(model);
    }


    private static void loadHomepages(Model model) throws IOException, SQLException
    {
        IntStringPairMap newHomepages = new IntStringPairMap();
        IntStringMap oldHomepages = getIntStringMap(
                "select id, homepage from pubchem.source_bases where homepage is not null");

        new QueryResultProcessor(patternQuery("?source foaf:homepage ?homepage"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
                int sourceID = getSourceID(source);
                String homepage = getIRI("homepage");

                if(!homepage.equals(oldHomepages.remove(sourceID)))
                    newHomepages.put(sourceID, Tuples.pair(source, homepage));
            }
        }.load(model);

        batch("update pubchem.source_bases set homepage = null where id = ?", oldHomepages.keySet());
        batch("insert into pubchem.source_bases(id, iri, homepage) values (?,?,?) "
                + "on conflict (id) do update set homepage=EXCLUDED.homepage", newHomepages);
    }


    private static void loadLicenses(Model model) throws IOException, SQLException
    {
        IntStringPairMap newLicenses = new IntStringPairMap();
        IntStringMap oldLicenses = getIntStringMap(
                "select id, license from pubchem.source_bases where license is not null");

        new QueryResultProcessor(patternQuery("?source dcterms:license ?license"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
                int sourceID = getSourceID(source);
                String license = getIRI("license");

                if(!license.equals(oldLicenses.remove(sourceID)))
                    newLicenses.put(sourceID, Tuples.pair(source, license));
            }
        }.load(model);

        batch("update pubchem.source_bases set license = null where id = ?", oldLicenses.keySet());
        batch("insert into pubchem.source_bases(id, iri, license) values (?,?,?) "
                + "on conflict (id) do update set license=EXCLUDED.license", newLicenses);
    }


    private static void loadRights(Model model) throws IOException, SQLException
    {
        IntStringPairMap newRights = new IntStringPairMap();
        IntStringMap oldRights = getIntStringMap(
                "select id, rights from pubchem.source_bases where rights is not null");

        new QueryResultProcessor(patternQuery("?source dcterms:rights ?rights"))
        {
            @Override
            protected void parse() throws IOException
            {
                String source = getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
                int sourceID = getSourceID(source);
                String rights = getString("rights");

                if(!rights.equals(oldRights.remove(sourceID)))
                    newRights.put(sourceID, Tuples.pair(source, rights));
            }
        }.load(model);

        batch("update pubchem.source_bases set rights = null where id = ?", oldRights.keySet());
        batch("insert into pubchem.source_bases(id, iri, rights) values (?,?,?) "
                + "on conflict (id) do update set rights=EXCLUDED.rights", newRights);
    }


    private static void loadSubjects(Model model) throws IOException, SQLException
    {
        IntPairSet newSubjects = new IntPairSet();
        IntPairSet oldSubjects = getIntPairSet("select source, subject from pubchem.source_subjects");

        new QueryResultProcessor(patternQuery("?source dcterms:subject ?subject"))
        {
            @Override
            protected void parse() throws IOException
            {
                int sourceID = getSourceID(getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"));
                int conceptID = Concept
                        .getConceptID(getStringID("subject", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"));

                IntIntPair pair = PrimitiveTuples.pair(sourceID, conceptID);

                if(!oldSubjects.remove(pair))
                    newSubjects.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.source_subjects where source = ? and subject = ?", oldSubjects);
        batch("insert into pubchem.source_subjects(source, subject) values (?,?)", newSubjects);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select source, alternative from pubchem.source_alternatives");

        new QueryResultProcessor(patternQuery("?source dcterms:alternative ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                int sourceID = getSourceID(getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(sourceID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.source_alternatives where source = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.source_alternatives(source, alternative) values (?,?)", newAlternatives);
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

        batch("delete from pubchem.source_bases where id = ?", oldSources.values());
        batch("insert into pubchem.source_bases(iri, id) values (?,?)" + " on conflict do nothing", newSources);

        batch("update pubchem.source_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.source_bases(id, iri, title) values (?,?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);

        usedSources = null;
        newSources = null;
        oldSources = null;

        newTitles = null;
        oldTitles = null;

        System.out.println();
    }


    static int getSourceID(String source, String title)
    {
        synchronized(newSources)
        {
            int sourceID = usedSources.getIfAbsent(source, NO_VALUE);

            if(sourceID == NO_VALUE)
            {
                System.out.println("    add missing source " + source);

                if((sourceID = oldSources.removeKeyIfAbsent(source, NO_VALUE)) == NO_VALUE)
                    newSources.put(source, sourceID = nextSourceID++);

                if(title == null)
                    title = generateSourceTitle(source);
                System.err.println("T: " + title);
                if(!title.equals(oldTitles.remove(sourceID)))
                    newTitles.put(sourceID, Tuples.pair(source, title));

                usedSources.put(source, sourceID);
            }

            return sourceID;
        }
    }


    static int getSourceID(String source)
    {
        return getSourceID(source, null);
    }


    private static String generateSourceTitle(String source)
    {
        if(source.startsWith("ID"))
            return source.substring(2);
        else
            return source.replace('_', ' ');
    }
}
