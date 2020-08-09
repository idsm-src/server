package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Concept extends Updater
{
    private static StringIntMap usedConcepts;
    private static StringIntMap newConcepts;
    private static StringIntMap oldConcepts;
    private static int nextConceptID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedConcepts = new StringIntMap(10000);
        newConcepts = new StringIntMap(10000);
        oldConcepts = getStringIntMap("select iri, id from pubchem.concept_bases", 10000);
        nextConceptID = getIntValue("select coalesce(max(id)+1,0) from pubchem.concept_bases");

        new QueryResultProcessor(patternQuery("?iri rdf:type ?type"))
        {
            @Override
            protected void parse() throws IOException
            {
                String iri = getIRI("iri");
                int conceptID;

                if((conceptID = oldConcepts.removeKeyIfAbsent(iri, NO_VALUE)) == NO_VALUE)
                    newConcepts.put(iri, conceptID = nextConceptID++);

                usedConcepts.put(iri, conceptID);
            }
        }.load(model);

        batch("insert into pubchem.concept_bases(iri, id) values (?,?)", newConcepts);
        newConcepts.clear();
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap newLabels = new IntStringMap(10000);
        IntStringMap oldLabels = getIntStringMap("select id, label from pubchem.concept_bases where label is not null",
                10000);

        new QueryResultProcessor(patternQuery("?concept skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                int conceptID = usedConcepts.getOrThrow(getIRI("concept"));
                String label = getString("label");

                if(!label.equals(oldLabels.remove(conceptID)))
                    newLabels.put(conceptID, label);
            }
        }.load(model);

        batch("update pubchem.concept_bases set label = null where id = ?", oldLabels.keySet());
        batch("update pubchem.concept_bases set label = ? where id = ?", newLabels, Direction.REVERSE);
    }


    private static void loadScheme(Model model) throws IOException, SQLException
    {
        IntIntHashMap newSchemes = new IntIntHashMap(10000);
        IntIntHashMap oldSchemes = getIntIntMap("select id, scheme from pubchem.concept_bases where scheme is not null",
                10000);

        new QueryResultProcessor(patternQuery("?concept skos:inScheme ?scheme"))
        {
            @Override
            protected void parse() throws IOException
            {
                int conceptID = usedConcepts.getOrThrow(getIRI("concept"));
                int schemeID = usedConcepts.getOrThrow(getIRI("scheme"));

                if(schemeID != oldSchemes.removeKeyIfAbsent(conceptID, NO_VALUE))
                    newSchemes.put(conceptID, schemeID);
            }
        }.load(model);

        batch("update pubchem.concept_bases set scheme = null where id = ?", oldSchemes.keySet());
        batch("update pubchem.concept_bases set scheme = ? where id = ?", newSchemes, Direction.REVERSE);
    }


    private static void loadBroader(Model model) throws IOException, SQLException
    {
        IntIntHashMap newBroaders = new IntIntHashMap(10000);
        IntIntHashMap oldBroaders = getIntIntMap(
                "select id, broader from pubchem.concept_bases where broader is not null", 10000);

        new QueryResultProcessor(patternQuery("?concept skos:broader ?broader"))
        {
            @Override
            protected void parse() throws IOException
            {
                int conceptID = usedConcepts.getOrThrow(getIRI("concept"));
                int broaderID = usedConcepts.getOrThrow(getIRI("broader"));

                if(conceptID != broaderID && broaderID != oldBroaders.removeKeyIfAbsent(conceptID, NO_VALUE))
                    newBroaders.put(conceptID, broaderID);
            }
        }.load(model);

        batch("update pubchem.concept_bases set broader = null where id = ?", oldBroaders.keySet());
        batch("update pubchem.concept_bases set broader = ? where id = ?", newBroaders, Direction.REVERSE);
    }


    static int getConceptID(String iri)
    {
        synchronized(newConcepts)
        {
            int conceptID = usedConcepts.getIfAbsent(iri, NO_VALUE);

            if(conceptID == NO_VALUE)
            {
                System.out.println("    add missing concept <" + iri + ">");

                if((conceptID = oldConcepts.removeKeyIfAbsent(iri, NO_VALUE)) == NO_VALUE)
                    newConcepts.put(iri, conceptID = nextConceptID++);

                usedConcepts.put(iri, conceptID);
            }

            return conceptID;
        }
    }


    static void finish() throws IOException, SQLException
    {
        batch("delete from pubchem.concept_bases where id = ?", oldConcepts.values());
        batch("insert into pubchem.concept_bases(iri, id) values (?,?)", newConcepts);

        usedConcepts = null;
        newConcepts = null;
        oldConcepts = null;
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load concepts ...");

        Model model = getModel("pubchem/RDF/concept/pc_concept.ttl.gz");
        check(model, "pubchem/concept/check.sparql");

        loadBases(model);
        loadLabels(model);
        loadScheme(model);
        loadBroader(model);

        model.close();
        System.out.println();
    }
}
