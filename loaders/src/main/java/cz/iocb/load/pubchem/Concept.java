package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Concept extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> concepts = new EntityTable<>("pubchem.concept_bases", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("label"), integer("scheme"), integer("broader"));
    private static final StringIntMap conceptIDs = new StringIntMap();
    private static int nextConceptID;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,id from pubchem.concept_bases", conceptIDs);

        nextConceptID = conceptIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        new QueryResultProcessor(patternQuery("?concept rdf:type vocab:Concept"))
        {
            @Override
            protected void parse() throws IOException
            {
                addConcept(getStringID("concept", prefix));
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?concept skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"));
                String label = getString("label");

                concepts.set(conceptID, "label", label);
            }
        }.load(model);
    }


    private static void loadScheme(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?concept skos:inScheme ?scheme"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"));
                Integer schemeID = getConceptID(getIRI("scheme"));

                concepts.set(conceptID, "scheme", schemeID);
            }
        }.load(model);
    }


    private static void loadBroader(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?concept skos:broader ?broader"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer conceptID = getConceptID(getIRI("concept"));
                Integer broaderID = getConceptID(getIRI("broader"));

                // workaround
                if(conceptID == broaderID)
                {
                    System.out.println("    ignore " + getStringID("concept", prefix) + " for skos:broader");
                    return;
                }

                concepts.set(conceptID, "broader", broaderID);
            }
        }.load(model);
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

        concepts.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish concepts ...");

        concepts.store();

        System.out.println();
    }


    static Integer getConceptID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        String concept = value.substring(prefixLength);

        synchronized(conceptIDs)
        {
            Integer conceptID = conceptIDs.get(concept);

            if(conceptID != null && concepts.contains(conceptID))
                return conceptID;

            System.out.println("    add missing concept " + concept);

            return addConcept(concept);
        }
    }


    /*
     * Adds the row of a concept, which keeps its id if it has one.
     */
    private static Integer addConcept(String concept) throws IOException
    {
        synchronized(conceptIDs)
        {
            Integer conceptID = conceptIDs.get(concept);

            if(conceptID == null)
                conceptIDs.put(concept, conceptID = nextConceptID++);

            concepts.set(conceptID, "iri", concept);

            return conceptID;
        }
    }
}
