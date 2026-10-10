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



public class PubChemRDF extends Updater
{
    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String owl = "http://www.w3.org/2002/07/owl#";
    static final String xsd = "http://www.w3.org/2001/XMLSchema#";
    static final String skos = "http://www.w3.org/2004/02/skos/core#";
    static final String vcard = "http://www.w3.org/2006/vcard/ns#";
    static final String dcterms = "http://purl.org/dc/terms/";
    static final String foaf = "http://xmlns.com/foaf/0.1/";
    static final String prism = "http://prismstandard.org/namespaces/basic/3.0/";
    static final String cito = "http://purl.org/spar/cito/";
    static final String fabio = "http://purl.org/spar/fabio/";
    static final String frapo = "http://purl.org/cerif/frapo/";
    static final String sio = "http://semanticscience.org/resource/";
    static final String obo = "http://purl.obolibrary.org/obo/";
    static final String bao = "http://www.bioassayontology.org/bao#";
    static final String bp = "http://www.biopax.org/release/biopax-level3.owl#";
    static final String up = "http://purl.uniprot.org/core/";
    static final String pdbo = "http://rdf.wwpdb.org/schema/pdbx-v50.owl#";
    static final String edam = "http://edamontology.org/";
    static final String vocab = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#";


    /*
     * Returns the date of the last modification of the PubChemRDF dataset, as its VoID description states it; null
     * stands for an unknown version.
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


    /*
     * Returns the date of the download of the given part of the PubChem dump, which stands for its version, as the
     * download script has recorded it; null stands for an unknown date.
     */
    private static String getDownloadDate(String part) throws IOException
    {
        try(BufferedReader reader = getReader("pubchem/" + part + "/version.txt.gz"))
        {
            return reader.readLine();
        }
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();

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
