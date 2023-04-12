package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.Tuples;
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
        usedConcepts = new StringIntMap();
        newConcepts = new StringIntMap();
        oldConcepts = getStringIntMap("select iri, id from pubchem.concept_bases");
        nextConceptID = getIntValue("select coalesce(max(id)+1,0) from pubchem.concept_bases");

        new QueryResultProcessor(patternQuery("?concept rdf:type ?type"))
        {
            @Override
            protected void parse() throws IOException
            {
                String concept = getStringID("concept", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/");
                int conceptID = oldConcepts.removeKeyIfAbsent(concept, NO_VALUE);

                if(conceptID == NO_VALUE)
                    newConcepts.put(concept, conceptID = nextConceptID++);

                usedConcepts.put(concept, conceptID);
            }
        }.load(model);

        batch("insert into pubchem.concept_bases(iri, id) values (?,?)", newConcepts);
        newConcepts.clear();
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringPairMap newLabels = new IntStringPairMap();
        IntStringMap oldLabels = getIntStringMap("select id, label from pubchem.concept_bases where label is not null");

        new QueryResultProcessor(patternQuery("?concept skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                String concept = getStringID("concept", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/");
                int conceptID = getConceptID(concept);
                String label = getString("label");

                if(!label.equals(oldLabels.remove(conceptID)))
                    newLabels.put(conceptID, Tuples.pair(concept, label));
            }
        }.load(model);

        batch("update pubchem.concept_bases set label = null where id = ?", oldLabels.keySet());
        batch("insert into pubchem.concept_bases(id, iri, label) values (?,?,?) "
                + "on conflict (id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadScheme(Model model) throws IOException, SQLException
    {
        IntStringIntPairMap newSchemes = new IntStringIntPairMap();
        IntIntHashMap oldSchemes = getIntIntMap(
                "select id, scheme from pubchem.concept_bases where scheme is not null");

        new QueryResultProcessor(patternQuery("?concept skos:inScheme ?scheme"))
        {
            @Override
            protected void parse() throws IOException
            {
                String concept = getStringID("concept", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/");
                int conceptID = getConceptID(concept);
                int schemeID = getConceptID(getStringID("scheme", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"));

                if(schemeID != oldSchemes.removeKeyIfAbsent(conceptID, NO_VALUE))
                    newSchemes.put(conceptID, Tuples.pair(concept, schemeID));
            }
        }.load(model);

        batch("update pubchem.concept_bases set scheme = null where id = ?", oldSchemes.keySet());
        batch("insert into pubchem.concept_bases(id, iri, scheme) values (?,?,?) "
                + "on conflict (id) do update set scheme=EXCLUDED.scheme", newSchemes);
    }


    private static void loadBroader(Model model) throws IOException, SQLException
    {
        IntStringIntPairMap newBroaders = new IntStringIntPairMap();
        IntIntHashMap oldBroaders = getIntIntMap(
                "select id, broader from pubchem.concept_bases where broader is not null");

        new QueryResultProcessor(patternQuery("?concept skos:broader ?broader"))
        {
            @Override
            protected void parse() throws IOException
            {
                String concept = getStringID("concept", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/");
                int conceptID = getConceptID(concept);
                int broaderID = getConceptID(getStringID("broader", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"));

                if(conceptID != broaderID && broaderID != oldBroaders.removeKeyIfAbsent(conceptID, NO_VALUE))
                    newBroaders.put(conceptID, Tuples.pair(concept, broaderID));
            }
        }.load(model);

        batch("update pubchem.concept_bases set broader = null where id = ?", oldBroaders.keySet());
        batch("insert into pubchem.concept_bases(id, iri, broader) values (?,?,?) "
                + "on conflict (id) do update set broader=EXCLUDED.broader", newBroaders);
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


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish concepts ...");

        batch("delete from pubchem.concept_bases where id = ?", oldConcepts.values());
        batch("insert into pubchem.concept_bases(iri, id) values (?,?)" + " on conflict do nothing", newConcepts);

        usedConcepts = null;
        newConcepts = null;
        oldConcepts = null;

        System.out.println();
    }


    static int getConceptID(String concept)
    {
        synchronized(newConcepts)
        {
            int conceptID = usedConcepts.getIfAbsent(concept, NO_VALUE);

            if(conceptID == NO_VALUE)
            {
                System.out.println("    add missing concept " + concept);

                if((conceptID = oldConcepts.removeKeyIfAbsent(concept, NO_VALUE)) == NO_VALUE)
                    newConcepts.put(concept, conceptID = nextConceptID++);

                usedConcepts.put(concept, conceptID);
            }

            return conceptID;
        }
    }
}
