package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.foaf;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



class Source extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> sources = new EntityTable<>("pubchem.sources", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("title"), varchar("homepage"), varchar("license"),
            varchar("rights"));
    private static final StringIntMap sourceIDs = new StringIntMap();
    private static final MissingEntities<String> missingSources = new MissingEntities<>("source", true);
    private static int nextSourceID;


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "title", dcterms + "alternative", dcterms + "subject",
                dcterms + "license", dcterms + "rights", foaf + "homepage", rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Source", dcterms + "Dataset");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws SQLException
    {
        load("select iri,id from pubchem.sources", sourceIDs);

        nextSourceID = sourceIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(dcterms + "Dataset", (subject, object) -> {
            String source = getStringID(subject, prefix);

            addSource(source);
            missingSources.described(source);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher)
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            String title = getString(object);

            sources.set(sourceID, "title", title);
        });
    }


    private static void loadHomepages(TripleDispatcher dispatcher)
    {
        dispatcher.on(foaf + "homepage", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            String homepage = object.getURI();

            sources.set(sourceID, "homepage", homepage);
        });
    }


    private static void loadLicenses(TripleDispatcher dispatcher)
    {
        dispatcher.on(dcterms + "license", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            String license = object.getURI();

            sources.set(sourceID, "license", license);
        });
    }


    private static void loadRights(TripleDispatcher dispatcher)
    {
        dispatcher.on(dcterms + "rights", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            String rights = getString(object);

            sources.set(sourceID, "rights", rights);
        });
    }


    private static void loadSubjects(TripleDispatcher dispatcher) throws SQLException
    {
        IntPairSet keepSubjects = new IntPairSet();
        IntPairSet newSubjects = new IntPairSet();
        IntPairSet oldSubjects = new IntPairSet();

        load("select source,subject from pubchem.source_subjects", oldSubjects);

        dispatcher.on(dcterms + "subject", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            Integer conceptID = Concept.getConceptID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(sourceID, conceptID);

            if(oldSubjects.remove(pair))
                keepSubjects.add(pair);
            else if(!keepSubjects.contains(pair))
                newSubjects.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.source_subjects where source=? and subject=?", oldSubjects);
            store("insert into pubchem.source_subjects(source,subject) values(?,?)", newSubjects);
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select source,alternative from pubchem.source_alternatives", oldAlternatives);

        dispatcher.on(dcterms + "alternative", (subject, object) -> {
            Integer sourceID = getSourceID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(sourceID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.source_alternatives where source=? and alternative=?", oldAlternatives);
            store("insert into pubchem.source_alternatives(source,alternative) values(?,?)", newAlternatives);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load sources ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTitles(dispatcher);
        loadHomepages(dispatcher);
        loadLicenses(dispatcher);
        loadRights(dispatcher);
        loadSubjects(dispatcher);
        loadAlternatives(dispatcher);

        dispatcher.load("pubchem/RDF/source/pc_source.ttl.gz");
        missingSources.settle();
        dispatcher.finish();

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

            missingSources.referenced(source);

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

            missingSources.referenced(source);

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
