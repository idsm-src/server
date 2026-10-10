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



public class ChEMBL extends Updater
{
    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String dcterms = "http://purl.org/dc/terms/";
    static final String bibo = "http://purl.org/ontology/bibo/";
    static final String skos = "http://www.w3.org/2004/02/skos/core#";
    static final String foaf = "http://xmlns.com/foaf/0.1/";
    static final String sio = "http://semanticscience.org/resource/";
    static final String bao = "http://www.bioassayontology.org/bao#";
    static final String cco = "http://rdf.ebi.ac.uk/terms/chembl#";
    static final String chembl = "http://rdf.ebi.ac.uk/resource/chembl/";
    static final String pav = "http://purl.org/pav/";

    static final String rdfType = rdf + "type";
    static final String rdfsLabel = rdfs + "label";
    static final String chemblId = cco + "chemblId";
    static final String voidInDataset = "http://rdfs.org/ns/void#inDataset";

    private static String version;


    /*
     * Returns the current version of the dataset, as its VoID description states it.
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


    /*
     * Returns the path of an RDF file of the release.
     */
    private static String rdfFile(String name)
    {
        return "chembl/rdf/" + name;
    }


    static String file(String part)
    {
        return rdfFile("chembl_" + version + "_" + part + ".ttl.gz");
    }


    static void checkValue(Node subject, Node predicate, Node object, String value) throws IOException
    {
        if(!TripleStreamProcessor.getString(object).equals(value))
            Problems.error("unexpected value of " + predicate.getURI(),
                    text(subject) + " " + text(object) + " instead of '" + value + "'");
    }


    static String getChemblId(Node subject, Node predicate, Node object, String value) throws IOException
    {
        checkValue(subject, predicate, object, value);
        return TripleStreamProcessor.getString(object);
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init();

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
