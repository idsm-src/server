package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Concept extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/";
    static final int prefixLength = prefix.length();

    private static final StringIntMap keepConcepts = new StringIntMap();
    private static final StringIntMap newConcepts = new StringIntMap();
    private static final StringIntMap oldConcepts = new StringIntMap();
    private static int nextConceptID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.concept_bases", oldConcepts);

        nextConceptID = oldConcepts.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?concept rdf:type ?type"))
        {
            @Override
            protected void parse() throws IOException
            {
                String concept = getStringID("concept", prefix);
                Integer conceptID = oldConcepts.remove(concept);

                if(conceptID == null)
                    newConcepts.put(concept, nextConceptID++);
                else
                    keepConcepts.put(concept, conceptID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringPairMap newLabels = new IntStringPairMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select id,label from pubchem.concept_bases where label is not null", oldLabels);

        new QueryResultProcessor(patternQuery("?concept skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"), true);
                String label = getString("label");

                if(label.equals(oldLabels.remove(conceptID)))
                {
                    keepLabels.put(conceptID, label);
                }
                else
                {
                    String keep = keepLabels.get(conceptID);

                    Pair<String, String> pair = Pair.getPair(getStringID("concept", prefix), label);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newLabels.put(conceptID, pair);

                    if(put != null && !label.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.concept_bases set label=null where id=? and and label=?", oldLabels);
        store("insert into pubchem.concept_bases(id,iri,label) values(?,?,?) "
                + "on conflict(id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadScheme(Model model) throws IOException, SQLException
    {
        IntIntMap keepSchemes = new IntIntMap();
        IntStringIntPairMap newSchemes = new IntStringIntPairMap();
        IntIntMap oldSchemes = new IntIntMap();

        load("select id,scheme from pubchem.concept_bases where scheme is not null", oldSchemes);

        new QueryResultProcessor(patternQuery("?concept skos:inScheme ?scheme"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"), true);
                Integer schemeID = getConceptID(getIRI("scheme"));

                if(schemeID.equals(oldSchemes.remove(conceptID)))
                {
                    keepSchemes.put(conceptID, schemeID);
                }
                else
                {
                    Integer keep = keepSchemes.get(conceptID);

                    Pair<String, Integer> pair = Pair.getPair(getStringID("concept", prefix), schemeID);

                    if(schemeID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, Integer> put = newSchemes.put(conceptID, pair);

                    if(put != null && !schemeID.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.concept_bases set scheme=null where id=? and scheme=?", oldSchemes);
        store("insert into pubchem.concept_bases(id,iri,scheme) values(?,?,?) "
                + "on conflict(id) do update set scheme=EXCLUDED.scheme", newSchemes);
    }


    private static void loadBroader(Model model) throws IOException, SQLException
    {
        IntIntMap keepBroaders = new IntIntMap();
        IntStringIntPairMap newBroaders = new IntStringIntPairMap();
        IntIntMap oldBroaders = new IntIntMap();

        load("select id,broader from pubchem.concept_bases where broader is not null", oldBroaders);

        new QueryResultProcessor(patternQuery("?concept skos:broader ?broader"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"), true);
                Integer broaderID = getConceptID(getIRI("broader"));

                // workaround
                if(conceptID == broaderID)
                {
                    System.out.println("    ignore " + getStringID("concept", prefix) + " for skos:broader");
                    return;
                }

                if(broaderID.equals(oldBroaders.remove(conceptID)))
                {
                    keepBroaders.put(conceptID, broaderID);
                }
                else
                {
                    Integer keep = keepBroaders.get(conceptID);

                    Pair<String, Integer> pair = Pair.getPair(getStringID("concept", prefix), broaderID);

                    if(broaderID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, Integer> put = newBroaders.put(conceptID, pair);

                    if(put != null && !broaderID.equals(put.getTwo()))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.concept_bases set broader=null where id=? and broader=?", oldBroaders);
        store("insert into pubchem.concept_bases(id,iri,broader) values(?,?,?) "
                + "on conflict(id) do update set broader=EXCLUDED.broader", newBroaders);
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

        store("delete from pubchem.concept_bases where iri=? and id=?", oldConcepts);
        store("insert into pubchem.concept_bases(iri,id) values(?,?)", newConcepts);

        System.out.println();
    }


    static Integer getConceptID(String value) throws IOException
    {
        return getConceptID(value, false);
    }


    static Integer getConceptID(String value, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String concept = value.substring(prefixLength);

        synchronized(newConcepts)
        {
            Integer conceptID = keepConcepts.get(concept);

            if(conceptID != null)
                return conceptID;

            conceptID = newConcepts.get(concept);

            if(conceptID != null)
            {
                if(keepForce)
                {
                    newConcepts.remove(concept);
                    keepConcepts.put(concept, conceptID);
                }

                return conceptID;
            }

            System.out.println("    add missing concept " + concept);

            if((conceptID = oldConcepts.remove(concept)) != null)
                keepConcepts.put(concept, conceptID);
            else if(keepForce)
                keepConcepts.put(concept, conceptID = nextConceptID++);
            else
                newConcepts.put(concept, conceptID = nextConceptID++);

            return conceptID;
        }
    }
}
