package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Source extends Updater
{
    private static StringIntMap usedSources;
    private static StringIntMap newSources;
    private static StringIntMap oldSources;
    private static int nextSourceID;

    private static IntStringMap newTitles;
    private static IntStringMap oldTitles;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedSources = new StringIntMap(1000);
        newSources = new StringIntMap(1000);
        oldSources = getStringIntMap("select iri, id from pubchem.source_bases", 1000);
        nextSourceID = getIntValue("select coalesce(max(id)+1,0) from pubchem.source_bases");

        new QueryResultProcessor(patternQuery("?iri rdf:type dcterms:Dataset"))
        {
            @Override
            protected void parse() throws IOException
            {
                String iri = getIRI("iri");
                int sourceID;

                if((sourceID = oldSources.removeKeyIfAbsent(iri, NO_VALUE)) == NO_VALUE)
                    newSources.put(iri, sourceID = nextSourceID++);

                usedSources.put(iri, sourceID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        newTitles = new IntStringMap(1000);
        oldTitles = getIntStringMap("select id, title from pubchem.source_bases where title is not null", 1000);

        new QueryResultProcessor(patternQuery("?source dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int sourceID = usedSources.getOrThrow(getIRI("source"));
                String title = getString("title");

                if(!title.equals(oldTitles.remove(sourceID)))
                    newTitles.put(sourceID, title);
            }
        }.load(model);
    }


    private static void loadSubjects(Model model) throws IOException, SQLException
    {
        IntPairSet newSubjects = new IntPairSet(1000);
        IntPairSet oldSubjects = getIntPairSet("select source, subject from pubchem.source_subjects", 1000);

        new QueryResultProcessor(patternQuery("?source dcterms:subject ?subject"))
        {
            @Override
            protected void parse() throws IOException
            {
                int sourceID = usedSources.getOrThrow(getIRI("source"));
                int conceptID = Concept.getConceptID(getIRI("subject"));

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
        IntStringPairIntMap newAlternatives = new IntStringPairIntMap(1000);
        IntStringPairIntMap oldAlternatives = getIntStringPairIntMap(
                "select source, alternative, __ from pubchem.source_alternatives", 1000);

        new QueryResultProcessor(patternQuery("?source dcterms:alternative ?alternative"))
        {
            int nextAlternativeID = Updater
                    .getIntValue("select coalesce(max(__)+1,0) from pubchem.source_alternatives");

            @Override
            protected void parse() throws IOException
            {
                int sourceID = usedSources.getOrThrow(getIRI("source"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(sourceID, alternative);

                if(oldAlternatives.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newAlternatives.put(pair, nextAlternativeID++);
            }
        }.load(model);

        batch("delete from pubchem.source_alternatives where __ = ?", oldAlternatives.values());
        batch("insert into pubchem.source_alternatives(source, alternative, __) values (?,?,?)", newAlternatives);
    }


    private static String generateSourceTitle(String iri)
    {
        String base = iri.replaceFirst("^http://rdf.ncbi.nlm.nih.gov/pubchem/source/", "");

        if(base.startsWith("ID"))
            return base.substring(2);
        else
            return base.replace('_', ' ');
    }


    static int getSourceID(String iri, String title)
    {
        synchronized(newSources)
        {
            int sourceID = usedSources.getIfAbsent(iri, NO_VALUE);

            if(sourceID == NO_VALUE)
            {
                System.out.println("    add missing source <" + iri + ">");

                if((sourceID = oldSources.removeKeyIfAbsent(iri, NO_VALUE)) == NO_VALUE)
                    newSources.put(iri, sourceID = nextSourceID++);

                if(title == null)
                    title = generateSourceTitle(iri);

                if(!title.equals(oldTitles.remove(sourceID)))
                    newTitles.put(sourceID, title);

                usedSources.put(iri, sourceID);
            }

            return sourceID;
        }
    }


    static int getSourceID(String iri)
    {
        return getSourceID(iri, null);
    }


    static void finish() throws IOException, SQLException
    {
        batch("delete from pubchem.source_bases where id = ?", oldSources.values());
        batch("insert into pubchem.source_bases(iri, id) values (?,?)", newSources);

        batch("update pubchem.source_bases set title = null where id = ?", oldTitles.keySet());
        batch("update pubchem.source_bases set title = ? where id = ?", newTitles, Direction.REVERSE);

        usedSources = null;
        newSources = null;
        oldSources = null;

        newTitles = null;
        oldTitles = null;
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load sources ...");

        Model model = getModel("pubchem/RDF/source/pc_source.ttl.gz");
        check(model, "pubchem/source/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadSubjects(model);
        loadAlternatives(model);

        model.close();
        System.out.println();
    }
}
