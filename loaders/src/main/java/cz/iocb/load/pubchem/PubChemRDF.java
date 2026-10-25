package cz.iocb.load.pubchem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of PubChem: reads the PubChemRDF dump, together with the XML files of the bioassays and the SDF files of the
 * compounds, into the {@code pubchem} schema. The parts are read in the order of their dependencies and stored when all
 * of them have been read; the whole load is committed at once or rolled back.
 */
public class PubChemRDF extends Updater
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
     * Namespace of OWL.
     */
    static final String owl = "http://www.w3.org/2002/07/owl#";

    /**
     * Namespace of the XML Schema datatypes.
     */
    static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    /**
     * Namespace of SKOS.
     */
    static final String skos = "http://www.w3.org/2004/02/skos/core#";

    /**
     * Namespace of vCard.
     */
    static final String vcard = "http://www.w3.org/2006/vcard/ns#";

    /**
     * Namespace of the Dublin Core terms.
     */
    static final String dcterms = "http://purl.org/dc/terms/";

    /**
     * Namespace of FOAF.
     */
    static final String foaf = "http://xmlns.com/foaf/0.1/";

    /**
     * Namespace of PRISM.
     */
    static final String prism = "http://prismstandard.org/namespaces/basic/3.0/";

    /**
     * Namespace of CiTO.
     */
    static final String cito = "http://purl.org/spar/cito/";

    /**
     * Namespace of FaBiO.
     */
    static final String fabio = "http://purl.org/spar/fabio/";

    /**
     * Namespace of FRAPO.
     */
    static final String frapo = "http://purl.org/cerif/frapo/";

    /**
     * Namespace of the Semanticscience Integrated Ontology.
     */
    static final String sio = "http://semanticscience.org/resource/";

    /**
     * Namespace of the OBO ontologies.
     */
    static final String obo = "http://purl.obolibrary.org/obo/";

    /**
     * Namespace of the BioAssay Ontology.
     */
    static final String bao = "http://www.bioassayontology.org/bao#";

    /**
     * Namespace of BioPAX level 3.
     */
    static final String bp = "http://www.biopax.org/release/biopax-level3.owl#";

    /**
     * Namespace of the UniProt core ontology.
     */
    static final String up = "http://purl.uniprot.org/core/";

    /**
     * Namespace of the PDBx ontology.
     */
    static final String pdbo = "http://rdf.wwpdb.org/schema/pdbx-v50.owl#";

    /**
     * Namespace of the EDAM ontology.
     */
    static final String edam = "http://edamontology.org/";

    /**
     * Namespace of the PubChem vocabulary.
     */
    static final String vocab = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#";


    /**
     * Not instantiable.
     */
    private PubChemRDF()
    {
    }


    /**
     * Returns the date of the last modification of the PubChemRDF dataset, as its VoID description states it.
     *
     * @return the date of the last modification, or null for an unknown version
     * @throws IOException if an I/O error occurs
     */
    private static String getVersion() throws IOException
    {
        String dataset = "http://rdf.ncbi.nlm.nih.gov/pubchem/void.ttl#PubChemRDF";
        String modified = "http://purl.org/dc/terms/modified";
        StringBuilder version = new StringBuilder();

        try(InputStream in = openFile("pubchem/RDF/void.ttl"))
        {
            if(in != null)
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(subject.isURI() && subject.getURI().equals(dataset) && predicate.getURI().equals(modified))
                            version.append(getLexicalForm(object));
                    }
                }.load(in);
            }
        }

        return version.isEmpty() ? null : version.toString();
    }


    /**
     * Returns the date of the download of the given part of the PubChem dump, which stands for its version, as the
     * download script has recorded it.
     *
     * @param part the directory of the part, such as {@code Bioassay} or {@code Compound}
     * @return the date of the download, or null for an unknown date
     * @throws IOException if an I/O error occurs
     */
    private static String getDownloadDate(String part) throws IOException
    {
        try(BufferedReader reader = getReader("pubchem/" + part + "/version.txt.gz"))
        {
            return reader.readLine();
        }
    }


    /**
     * Runs the load of PubChem. It records the counts of the substances, compounds and bioassays and the versions of
     * the parts, checks that no file of the dump has been left out, synchronizes the Sachem index of the compounds and
     * commits; any problem fails the load with exit status 1.
     *
     * @param args the command-line arguments (ignored)
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init("pubchem");

            String version = getVersion();
            String bioassayVersion = getDownloadDate("Bioassay");
            String molfileVersion = getDownloadDate("Compound");
            System.out.println("=== load pubchem version " + version + " (bioassays " + bioassayVersion + ", molfiles "
                    + molfileVersion + ") ===");
            System.out.println();


            Ontology.loadCategories();

            // required by Cell, ConservedDomain, Endpoint, Gene, Pathway, Protein, Substance, Taxonomy
            Reference.preload();

            Concept.load();
            Source.load(); // require Concept
            Compound.load();
            InchiKey.load(); // require Compound
            Synonym.load(); // require Compound, Concept

            PatentIpc.load();
            PatentCpc.load(); // require PatentIpc
            Patent.load(); // require Compound, PatentCpc, PatentIpc

            Substance.load(); // require Compound, Patent, Source, Synonym

            Author.load();
            Book.load(); // require Author
            Journal.load();
            Organization.load();
            Grant.load(); // require Organization

            Disease.load();
            Taxonomy.load(); // require Patent
            ConservedDomain.load();
            Anatomy.load(); // require Patent
            Cell.load(); // require Anatomy, Taxonomy
            Gene.load(); // require Patent, Taxonomy
            Protein.load(); // require ConservedDomain, Gene, Patent, Taxonomy
            Pathway.load(); // require Compound, Gene, Protein, Source, Taxonomy

            Cooccurrence.load(); // require Compound, Disease, Gene, Protein
            Reference.load(); // require Author, Book, Compound, Disease, Journal, Gene, Grant, Organization, Protein

            Bioassay.load(); // require Patent, Source
            Measuregroup.load(); // require Anatomy, Bioassay, Cell, Gene, Protein, Source, Taxonomy
            Endpoint.load(); // require Bioassay, Measuregroup, Substance
            Measuregroup.loadSubstances(); // require Endpoint

            CompoundDescriptor.load(); // require Compound
            SubstanceDescriptor.load(); // require Substance


            Concept.finish();
            Source.finish();
            Compound.finish();
            Substance.finish();

            Author.finish();
            Book.finish();
            Journal.finish();
            Organization.finish();
            Grant.finish();

            Disease.finish();
            Taxonomy.finish();
            ConservedDomain.finish();
            Anatomy.finish();
            Cell.finish();
            Gene.finish();
            Protein.finish();
            Pathway.finish();

            PatentIpc.finish();
            PatentCpc.finish();
            Patent.finish();
            Reference.finish();

            Bioassay.finish();
            Measuregroup.finish();

            CompoundDescriptor.finish();

            setCount("PubChem Substances", Substance.size());
            setCount("PubChem Compounds", Compound.size());
            setCount("PubChem BioAssays", Bioassay.size());

            setVersion("PubChemRDF", version);
            setVersion("PubChem BioAssays (XML)", bioassayVersion);
            setVersion("PubChem Compounds (SDF)", molfileVersion);

            updateVersion();

            // the documentation and the checksums of the dump
            checkFiles("pubchem", "RDF/README", "RDF/schema/.*", "Bioassay/XML/README",
                    "Compound/CURRENT-Full/SDF/README-Compound-SDF", "Compound/CURRENT-Full/SDF/.*\\.md5");
            MissingEntities.printSummary();
            checkProblems();

            syncIndex("pubchem", false);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
