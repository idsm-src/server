package cz.iocb.load.chembl;

import static cz.iocb.load.common.TripleStreamProcessor.text;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of ChEMBL from the RDF files of its release into the tables of the schema {@code chembl}. It reads the
 * version of the release from its VoID description, loads the entities in the order of their references, stores them
 * and records the version and the counts of the substances and assays.
 */
public class ChEMBL extends Updater
{
    /**
     * Namespace of RDF.
     */
    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
     * Namespace of RDF Schema.
     */
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * Namespace of the DCMI Metadata Terms.
     */
    static final String dcterms = "http://purl.org/dc/terms/";

    /**
     * Namespace of the Bibliographic Ontology.
     */
    static final String bibo = "http://purl.org/ontology/bibo/";

    /**
     * Namespace of SKOS.
     */
    static final String skos = "http://www.w3.org/2004/02/skos/core#";

    /**
     * Namespace of FOAF.
     */
    static final String foaf = "http://xmlns.com/foaf/0.1/";

    /**
     * Namespace of the Semanticscience Integrated Ontology.
     */
    static final String sio = "http://semanticscience.org/resource/";

    /**
     * Namespace of the BioAssay Ontology.
     */
    static final String bao = "http://www.bioassayontology.org/bao#";

    /**
     * Namespace of the ChEMBL core ontology.
     */
    static final String cco = "http://rdf.ebi.ac.uk/terms/chembl#";

    /**
     * Namespace of the ChEMBL resources.
     */
    static final String chembl = "http://rdf.ebi.ac.uk/resource/chembl/";

    /**
     * Namespace of the Provenance, Authoring and Versioning ontology.
     */
    static final String pav = "http://purl.org/pav/";

    /**
     * IRI of {@code rdf:type}.
     */
    static final String rdfType = rdf + "type";

    /**
     * IRI of {@code rdfs:label}.
     */
    static final String rdfsLabel = rdfs + "label";

    /**
     * IRI of {@code cco:chemblId}.
     */
    static final String chemblId = cco + "chemblId";

    /**
     * IRI of {@code void:inDataset}.
     */
    static final String voidInDataset = "http://rdfs.org/ns/void#inDataset";

    /**
     * The version of the loaded release.
     */
    private static String version;


    /**
     * Not instantiable.
     */
    private ChEMBL()
    {
    }


    /**
     * Returns the current version of the dataset, as its VoID description states it.
     *
     * @return the current version of the dataset
     * @throws IOException if the VoID file cannot be read or does not state exactly one current version
     * @throws SQLException if a database error occurs
     */
    private static String getVersion() throws IOException, SQLException
    {
        String dataset = "http://rdf.ebi.ac.uk/dataset/chembl";
        HashSet<Node> currents = new HashSet<>();
        HashMap<Node, HashSet<Node>> versions = new HashMap<>();

        TripleDispatcher dispatcher = new TripleDispatcher();

        dispatcher.on(pav + "hasCurrentVersion", (subject, object) -> {
            if(TripleDispatcher.is(subject, dataset))
                currents.add(object);
        });

        dispatcher.on(pav + "version", (subject, object) -> {
            versions.computeIfAbsent(subject, k -> new HashSet<>()).add(object);
        });

        dispatcher.load(rdfFile("void.ttl.gz"));

        List<Node> values = new ArrayList<>();

        for(Node current : currents)
            values.addAll(versions.getOrDefault(current, new HashSet<>()));

        if(values.isEmpty())
            throw new IOException("unknown version of the dataset");

        if(values.size() > 1)
            throw new IOException("ambiguous version of the dataset");

        return TripleStreamProcessor.getLexicalForm(values.get(0));
    }


    /**
     * Returns the path of an RDF file of the release.
     *
     * @param name the name of the file
     * @return the path of the file
     */
    private static String rdfFile(String name)
    {
        return "chembl/rdf/" + name;
    }


    /**
     * Returns the path of the RDF file of a part of the release, whose name carries the version.
     *
     * @param part the name of the part, such as {@code molecule}
     * @return the path of the file
     */
    static String file(String part)
    {
        return rdfFile("chembl_" + version + "_" + part + ".ttl.gz");
    }


    /**
     * Reports an error if the string value of a triple is not the expected one.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @param value the expected value
     * @throws IOException if the object is not a string literal
     */
    static void checkValue(Node subject, Node predicate, Node object, String value) throws IOException
    {
        if(!TripleStreamProcessor.getString(object).equals(value))
            Problems.error("unexpected value of " + predicate.getURI(),
                    text(subject) + " " + text(object) + " instead of '" + value + "'");
    }


    /**
     * Returns the ChEMBL id of a {@code cco:chemblId} triple and reports an error if it is not the id the IRI of the
     * subject implies.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @param value the expected id
     * @return the ChEMBL id
     * @throws IOException if the object is not a string literal
     */
    static String getChemblId(Node subject, Node predicate, Node object, String value) throws IOException
    {
        checkValue(subject, predicate, object, value);
        return TripleStreamProcessor.getString(object);
    }


    /**
     * Runs the loader.
     *
     * @param args the command-line arguments (ignored)
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init("chembl");

            version = getVersion();
            System.out.println("=== load chembl version " + version + " ===");
            System.out.println();


            Ontology.loadCategories();

            // a load references the entities named in its comment, so their finish, which stores their rows, has
            // to follow it
            Source.load();
            Journal.load();
            Document.load(); // require Journal
            CellLine.load();
            Target.load(); // require CellLine, TargetComponent
            TargetComponent.load(); // require Taxonomy
            BioComponent.load(); // require Taxonomy
            ProteinClassification.load(); // require Target, TargetComponent
            BindingSite.load(); // require Target
            Mechanism.load(); // require BindingSite, Molecule, Target
            DrugIndication.load(); // require Molecule

            // the largest entities are stored as soon as possible to free the memory
            Assay.load(); // require Activity, CellLine, Document, Source, Target
            Assay.finish();

            Activity.load(); // require Document, Molecule
            Activity.finish();

            Molecule.load(); // require BioComponent, Document
            Molecule.finish();


            Mechanism.finish();
            DrugIndication.finish();
            BindingSite.finish();
            ProteinClassification.finish();
            TargetComponent.finish();
            Target.finish();
            BioComponent.finish();
            Taxonomy.finish();
            CellLine.finish();
            Document.finish();
            Journal.finish();
            Source.finish();

            setCount("ChEMBL Substances", Molecule.size());
            setCount("ChEMBL Assays", Assay.size());

            setVersion("ChEMBL", version);

            updateVersion();

            // the ChEMBL ontology, which comes with the release, is loaded by the ontology loader, and the checksums of
            // the SDF file are checked by the download script
            checkFiles("chembl", "rdf/cco\\.ttl\\.gz", "sdf/checksums\\.txt");
            MissingEntities.printSummary();
            checkProblems();

            syncIndex("chembl", true);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
