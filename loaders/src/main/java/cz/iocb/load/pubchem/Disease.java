package cz.iocb.load.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.Disease.matchUnits;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the PubChem diseases from {@code pubchem/RDF/disease/pc_disease.ttl.gz} into the table
 * {@code pubchem.diseases} and the tables of their alternative labels and matches.
 */
public class Disease extends Updater
{
    /**
     * IRI prefix of the diseases.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the diseases.
     */
    private static final EntityTable<Integer> diseases = new EntityTable<>("pubchem.diseases", intKey("id"), null,
            varchar("label"));

    /**
     * Diseases referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingDiseases = new MissingEntities<>("disease", true);


    /**
     * Not instantiable.
     */
    private Disease()
    {
    }


    /**
     * Tests whether the node is an incorrect match of a disease, an obo IRI without the prefix of an ontology, which is
     * ignored.
     *
     * @param node the matching node
     * @return whether the match is incorrect
     */
    private static boolean isIncorrect(Node node)
    {
        return node.isURI() && node.getURI().matches("http://purl\\.obolibrary\\.org/obo/[0-9]*");
    }


    /**
     * Registers the checks of the predicates, types and values of the disease file.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", skos + "prefLabel", skos + "altLabel", skos + "closeMatch",
                skos + "relatedMatch");
        dispatcher.checkTypes(all(), vocab + "Disease", sio + "SIO_010299");
        dispatcher.checkPrefixes(all(), skos + "relatedMatch", node -> !isIncorrect(node),
                "https://uts.nlm.nih.gov/uts/umls/concept/C", "http://purl.obolibrary.org/obo/MONDO_",
                "http://purl.obolibrary.org/obo/HP_", "https://omim.org/entry/",
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C", "https://www.ncbi.nlm.nih.gov/medgen/C",
                "https://www.ncbi.nlm.nih.gov/medgen/CN", "https://rarediseases.info.nih.gov/diseases/",
                "https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=",
                "https://www.kegg.jp/entry/H", "http://nanbyodata.jp/ontology/NANDO_", "http://identifiers.org/DOID:",
                "http://identifiers.org/kegg.disease:H", "http://identifiers.org/medgen:CN",
                "http://identifiers.org/medgen:C", "http://identifiers.org/umls:C",
                "http://identifiers.org/pharmgkb.disease:PA", "http://identifiers.org/orphanet:",
                "http://identifiers.org/NANDO:", "http://identifiers.org/ncit:C", "http://identifiers.org/mim:",
                "http://identifiers.org/HP:", "https://www.pharmgkb.org/disease/PA",
                "https://hpo.jax.org/app/browse/term/HP:", "https://www.orpha.net/en/disease/detail/",
                "http://purl.obolibrary.org/obo/DOID:", "https://www.disease-ontology.org/?id=DOID:",
                "https://monarchinitiative.org/disease/MONDO:", "https://glycosmos.org/diseases/DOID:");
        dispatcher.checkPrefixes(all(), skos + "closeMatch", node -> !isIncorrect(node), "http://id.nlm.nih.gov/mesh/",
                "http://identifiers.org/mesh:", "https://uts.nlm.nih.gov/uts/umls/concept/C",
                "http://purl.obolibrary.org/obo/MONDO_", "http://purl.obolibrary.org/obo/HP_",
                "https://omim.org/entry/", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "https://www.ncbi.nlm.nih.gov/medgen/C", "https://www.ncbi.nlm.nih.gov/medgen/CN",
                "https://rarediseases.info.nih.gov/diseases/",
                "https://www.guidetopharmacology.org/GRAC/DiseaseDisplayForward?diseaseId=",
                "https://www.kegg.jp/entry/H", "http://nanbyodata.jp/ontology/NANDO_", "http://identifiers.org/DOID:",
                "http://identifiers.org/kegg.disease:H", "http://identifiers.org/medgen:CN",
                "http://identifiers.org/medgen:C", "http://identifiers.org/umls:C",
                "http://identifiers.org/pharmgkb.disease:PA", "http://identifiers.org/orphanet:",
                "http://identifiers.org/NANDO:", "http://identifiers.org/ncit:C", "http://identifiers.org/mim:",
                "http://identifiers.org/HP:", "https://www.pharmgkb.org/disease/PA",
                "https://hpo.jax.org/app/browse/term/HP:", "https://www.orpha.net/en/disease/detail/",
                "http://purl.obolibrary.org/obo/DOID:", "https://www.disease-ontology.org/?id=DOID:",
                "https://monarchinitiative.org/disease/MONDO:", "https://glycosmos.org/diseases/DOID:");
        dispatcher.checkPaired(skos + "closeMatch", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
        dispatcher.checkPaired(skos + "closeMatch", "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:");
    }


    /**
     * Registers the handler that adds the diseases described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadBases(TripleDispatcher dispatcher)
    {
        dispatcher.onType(sio + "SIO_010299", (subject, object) -> {
            Integer diseaseID = getIntID(subject, prefix);

            diseases.reference(diseaseID);
            missingDiseases.described(diseaseID);
        });
    }


    /**
     * Registers the handler that sets the labels of the diseases.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadLabels(TripleDispatcher dispatcher)
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            diseases.set(getDiseaseID(subject.getURI()), "label", getString(object));
        });
    }


    /**
     * Registers the handler of the alternative labels of the diseases and the action that stores their changes in
     * {@code pubchem.disease_alternatives}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws SQLException if a database error occurs
     */
    private static void loadAlternatives(TripleDispatcher dispatcher) throws SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select disease,alternative from pubchem.disease_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer diseaseID = getDiseaseID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(diseaseID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_alternatives where disease=? and alternative=?", oldAlternatives);
            store("insert into pubchem.disease_alternatives(disease,alternative) values(?,?)", newAlternatives);
        });
    }


    /**
     * Registers the handler of the resources linked to the diseases by {@code skos:closeMatch} (except the MeSH
     * descriptors) and the action that stores their changes in {@code pubchem.disease_matches}; an incorrect match is
     * skipped and a Genetic and Rare Diseases IRI with leading zeros is repaired, both with a warning.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws SQLException if a database error occurs
     */
    private static void loadCloseMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_matches", oldMatches);

        dispatcher.on(skos + "closeMatch", (subject, object) -> {
            String iri = object.getURI();

            if(iri.startsWith("http://id.nlm.nih.gov/mesh/") || iri.startsWith("http://identifiers.org/mesh:"))
                return;

            // workaround
            if(isIncorrect(object))
            {
                Problems.warning("incorrect value of " + skos + "closeMatch", text(subject) + " " + iri);
                return;
            }

            // workaround
            if(iri.matches("https://rarediseases\\.info\\.nih\\.gov/diseases/0+[0-9]*/index"))
            {
                Problems.warning("repaired value of " + skos + "closeMatch", text(subject) + " " + iri);
                iri = iri.replaceFirst("^(https://rarediseases.info.nih.gov/diseases/)0*([0-9]*/index)$", "$1$2");
            }

            Integer diseaseID = getDiseaseID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getResourceId(iri, matchUnits);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_matches where disease=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.disease_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    /**
     * Registers the handler of the MeSH descriptors linked to the diseases by {@code skos:closeMatch} and the action
     * that stores their changes in {@code pubchem.disease_mesh_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws SQLException if a database error occurs
     */
    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select disease,match from pubchem.disease_mesh_matches", oldMatches);

        dispatcher.on(skos + "closeMatch", (subject, object) -> {
            if(!object.getURI().startsWith("http://id.nlm.nih.gov/mesh/"))
                return;

            Integer diseaseID = getDiseaseID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_mesh_matches where disease=? and match=?", oldMatches);
            store("insert into pubchem.disease_mesh_matches(disease,match) values(?,?)", newMatches);
        });
    }


    /**
     * Registers the handler of the resources linked to the diseases by {@code skos:relatedMatch} and the action that
     * stores their changes in {@code pubchem.disease_related_matches}; an incorrect match is skipped with a warning.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws SQLException if a database error occurs
     */
    private static void loadRelatedMatches(TripleDispatcher dispatcher) throws SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_related_matches", oldMatches);

        dispatcher.on(skos + "relatedMatch", (subject, object) -> {
            // workaround
            if(isIncorrect(object))
            {
                Problems.warning("incorrect value of " + skos + "relatedMatch", text(subject) + " " + text(object));
                return;
            }

            Integer diseaseID = getDiseaseID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getResourceId(object, matchUnits);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.disease_related_matches where disease=? and match_unit=? and match_id=?",
                    oldMatches);
            store("insert into pubchem.disease_related_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    /**
     * Loads the diseases from {@code pubchem/RDF/disease/pc_disease.ttl.gz} into the tables and flushes the table of
     * the diseases.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load diseases ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadAlternatives(dispatcher);
        loadCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadRelatedMatches(dispatcher);

        dispatcher.load("pubchem/RDF/disease/pc_disease.ttl.gz");
        missingDiseases.settle();
        dispatcher.finish();

        diseases.flush();

        System.out.println();
    }


    /**
     * Stores the diseases, including those referenced by the parts loaded later, and deletes the stored diseases that
     * are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish diseases ...");

        diseases.store();

        System.out.println();
    }


    /**
     * Returns the id of the disease with the given IRI; a disease not described by the data gets a row and is counted
     * as missing.
     *
     * @param value the IRI of the disease
     * @return the id of the disease
     * @throws IOException if the IRI is not an IRI of a disease
     */
    static Integer getDiseaseID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer diseaseID = Integer.parseInt(value.substring(prefixLength));

        if(diseases.reference(diseaseID))
            missingDiseases.referenced(diseaseID);

        return diseaseID;
    }
}
