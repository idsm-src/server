package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem concepts (the ATC and substance categorization schemes and their concepts) from
 * {@code pubchem/RDF/concept/pc_concept.ttl.gz} into the table {@code pubchem.concepts}.
 */
class Concept extends Updater
{
    /**
     * IRI prefix of the concepts.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the concepts.
     */
    private static final EntityTable<Integer> concepts = new EntityTable<>("pubchem.concepts", intKey("id"), null,
            uniqueVarchar("iri").determinedByKey(), varchar("label"), integer("scheme"), integer("broader"));

    /**
     * Ids of the concepts by their IRIs without the prefix.
     */
    private static final StringIntMap conceptIDs = new StringIntMap();

    /**
     * Concepts referenced but not described by the data.
     */
    private static final MissingEntities<String> missingConcepts = new MissingEntities<>("concept", true);

    /**
     * Id of the next new concept.
     */
    private static int nextConceptID;


    /**
     * Not instantiable.
     */
    private Concept()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the concept file; the only concept schemes expected
     * are ATC and the substance categorization.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), "http://purl.org/pav/importedFrom", skos + "broader", skos + "inScheme",
                skos + "prefLabel", rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Concept", skos + "ConceptScheme", skos + "Concept", skos + "concept");
        dispatcher.checkValues("http://purl.org/pav/importedFrom", Source.prefix + "ID11950");

        Set<String> schemes = Set.of(prefix + "ATC", prefix + "SubstanceCategorization");

        dispatcher.onType(skos + "ConceptScheme", (subject, object) -> {
            if(!(subject.isURI() && schemes.contains(subject.getURI())))
                Problems.error("unexpected concept scheme", text(subject));
        });
    }


    /**
     * Loads the ids of the stored concepts and registers the handler that adds the concepts described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws SQLException
    {
        load("select iri,id from pubchem.concepts", conceptIDs);

        nextConceptID = conceptIDs.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        dispatcher.onType(vocab + "Concept", (subject, object) -> {
            String concept = getStringID(subject, prefix);

            addConcept(concept);
            missingConcepts.described(concept);
        });
    }


    /**
     * Registers the handler that sets the labels of the concepts.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadLabels(TripleDispatcher dispatcher)
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            Integer conceptID = getConceptID(subject.getURI());
            String label = getString(object);

            concepts.set(conceptID, "label", label);
        });
    }


    /**
     * Registers the handler that sets the schemes of the concepts.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadScheme(TripleDispatcher dispatcher)
    {
        dispatcher.on(skos + "inScheme", (subject, object) -> {
            Integer conceptID = getConceptID(subject.getURI());
            Integer schemeID = getConceptID(object.getURI());

            concepts.set(conceptID, "scheme", schemeID);
        });
    }


    /**
     * Registers the handler that sets the broader concepts of the concepts; a concept stated as its own broader concept
     * is skipped with a warning.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadBroader(TripleDispatcher dispatcher)
    {
        dispatcher.on(skos + "broader", (subject, object) -> {
            Integer conceptID = getConceptID(subject.getURI());
            Integer broaderID = getConceptID(object.getURI());

            // workaround
            if(conceptID == broaderID)
            {
                Problems.warning("concept as its own skos:broader", text(subject));
                return;
            }

            concepts.set(conceptID, "broader", broaderID);
        });
    }


    /**
     * Loads the concepts from {@code pubchem/RDF/concept/pc_concept.ttl.gz} into the table and flushes it.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load concepts ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadScheme(dispatcher);
        loadBroader(dispatcher);

        dispatcher.load("pubchem/RDF/concept/pc_concept.ttl.gz");
        missingConcepts.settle();
        dispatcher.finish();

        concepts.flush();

        System.out.println();
    }


    /**
     * Stores the concepts, including those referenced by the parts loaded later, and deletes the stored concepts that
     * are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish concepts ...");

        concepts.store();

        System.out.println();
    }


    /**
     * Returns the id of the concept with the given IRI; a concept not described by the data gets a row and is counted
     * as missing.
     *
     * @param value the IRI of the concept
     * @return the id of the concept
     * @throws IOException if the IRI is not an IRI of a concept
     */
    static Integer getConceptID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        String concept = value.substring(prefixLength);

        synchronized(conceptIDs)
        {
            Integer conceptID = conceptIDs.get(concept);

            if(conceptID != null && concepts.contains(conceptID))
                return conceptID;

            missingConcepts.referenced(concept);

            return addConcept(concept);
        }
    }


    /**
     * Adds the row of a concept, which keeps its id if it has one.
     *
     * @param concept the IRI of the concept without the prefix
     * @return the id of the concept
     * @throws IOException if the row of the concept holds another IRI
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
