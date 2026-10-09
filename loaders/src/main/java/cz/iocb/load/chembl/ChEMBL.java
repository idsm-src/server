package cz.iocb.load.chembl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
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

    private static final int maxWarnings = 10;

    private static final LinkedHashMap<String, Integer> warnings = new LinkedHashMap<>();
    private static final LinkedHashMap<String, Integer> allWarnings = new LinkedHashMap<>();
    private static final HashSet<String> files = new HashSet<>();
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
     * Returns the path of an RDF file of the release and records that the loader reads it.
     */
    private static String rdfFile(String name)
    {
        files.add(name);
        return "chembl/rdf/" + name;
    }


    static String file(String part)
    {
        return rdfFile("chembl_" + version + "_" + part + ".ttl.gz");
    }


    /*
     * Warns about the RDF files of the release that the loader has not read, such as a part that a new release adds.
     * The ChEMBL ontology, which comes with the release, is loaded by the ontology loader.
     */
    private static void checkFiles() throws IOException
    {
        System.out.println("check files ...");

        List<String> names;

        try(Stream<Path> paths = Files.list(Path.of(baseDirectory, "chembl", "rdf")))
        {
            names = paths.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".ttl.gz")).sorted()
                    .toList();
        }

        for(String name : names)
            if(!name.equals("cco.ttl.gz") && !files.contains(name))
                warning("unknown file", name);

        finishLoad();
    }


    /*
     * Reports a problem of the data that the loader can deal with, typically a triple that the mapping does not
     * reproduce. Only the first occurrences of each kind are printed, the remaining ones are counted.
     */
    static void warning(String kind, String detail)
    {
        int count = warnings.merge(kind, 1, Integer::sum);
        allWarnings.merge(kind, 1, Integer::sum);

        if(count <= maxWarnings)
            System.out.println("    warning: " + kind + ": " + detail);
    }


    static void unexpected(Node subject, Node predicate, Node object)
    {
        warning("unexpected predicate " + predicate.getURI(), subject + " " + object);
    }


    static void checkType(Node subject, Node object, String type)
    {
        if(!object.isURI() || !object.getURI().equals(type))
            warning("unexpected rdf:type " + object, subject.getURI());
    }


    static void checkValue(Node subject, Node predicate, Node object, String value) throws IOException
    {
        if(!TripleStreamProcessor.getString(object).equals(value))
            warning("unexpected value of " + predicate.getURI(),
                    subject.getURI() + " " + object + " instead of '" + value + "'");
    }


    static String getChemblId(Node subject, Node predicate, Node object, String value) throws IOException
    {
        checkValue(subject, predicate, object, value);
        return TripleStreamProcessor.getString(object);
    }


    /*
     * Returns the (unit, id) of an ontology resource; an IRI that is not of the given unit (if any) is reported and
     * ignored.
     */
    static Pair<Integer, Integer> getOntologyId(Node subject, Node predicate, Node object, Short unit)
    {
        Pair<Integer, Integer> id = Ontology.getId(object.getURI());

        if(id == null || unit != null && id.getOne().intValue() != unit.intValue())
        {
            warning("unexpected ontology resource of " + predicate.getURI(), subject.getURI() + " " + object);
            return null;
        }

        return id;
    }


    private static void printWarningCounts()
    {
        for(Entry<String, Integer> entry : warnings.entrySet())
            if(entry.getValue() > maxWarnings)
                System.out.println("    warning: " + entry.getKey() + ": " + entry.getValue() + " occurrences");

        warnings.clear();
    }


    static void finishLoad()
    {
        printWarningCounts();
        System.out.println();
    }


    /*
     * Prints the numbers of the warnings of the whole load, so that none of them is missed among the progress output.
     */
    private static void printWarningSummary()
    {
        System.out.println(allWarnings.isEmpty() ? "no warnings" : "warnings:");

        for(Entry<String, Integer> entry : allWarnings.entrySet())
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());

        System.out.println();
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

            checkFiles();
            printWarningSummary();
            MissingEntities.printSummary();

            syncIndex("chembl", true);

            setCount("ChEMBL Substances", Molecule.size());
            setCount("ChEMBL Assays", Assay.size());

            setVersion("ChEMBL", version);

            updateVersion();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
