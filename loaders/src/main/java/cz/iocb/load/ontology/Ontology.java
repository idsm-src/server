package cz.iocb.load.ontology;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.builtinResourceLimit;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBlank;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitCHEBI;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.load.common.TripleDispatcher.is;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.BlankNodes;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



public class Ontology extends Updater
{
    static private abstract class Source
    {
        private final String name;

        public Source(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public abstract String getVersion();
    }


    static private class OwlSource extends Source
    {
        public final String iri;

        // the values of owl:versionInfo, doap:Version and owl:versionIRI of the ontology
        private final LinkedHashSet<Node> infos = new LinkedHashSet<>();
        private final LinkedHashSet<Node> releases = new LinkedHashSet<>();
        private final LinkedHashSet<Node> versionIRIs = new LinkedHashSet<>();

        public OwlSource(String name, String iri)
        {
            super(name);
            this.iri = iri;
        }


        public String getVersionFromInfo()
        {
            String version = null;

            List<Node> nodes = new ArrayList<>(infos);
            nodes.addAll(releases);

            for(Node node : nodes)
            {
                if(node.isLiteral())
                {
                    String tmp = node.getLiteralLexicalForm();

                    if(tmp.startsWith("http"))
                        tmp = tmp.replaceFirst(".*/([0-9.-]+)(/|$).*", "$1");
                    else if(!tmp.matches(".*[0-9].*"))
                        continue;

                    if(version == null)
                        version = tmp;
                    else if(!tmp.matches("[0-9]{4}(-[0-9]{2}){2}") || !version.matches("[0-9]{4}(-[0-9]{2}){2}"))
                        return null;
                    else if(tmp.compareTo(version) > 0)
                        version = tmp;
                }
            }

            return version;
        }


        public String getVersionFromVersionIRI()
        {
            if(versionIRIs.size() != 1)
                return null;

            Node node = versionIRIs.iterator().next();
            String version = node.isLiteral() ? node.getLiteralLexicalForm() : node.getURI();

            if(version.matches(".*-20[0-9]{6}$"))
                version = version.replaceFirst(".*-(20[0-9]{2})([0-9]{2})([0-9]{2})$", "$1-$2-$3");
            else
                version = version.replaceFirst(".*/([0-9.-]+)(/|$).*", "$1");

            return version;
        }


        @Override
        public String getVersion()
        {
            String version = getVersionFromInfo();

            if(version == null)
                version = getVersionFromVersionIRI();

            return version;
        }
    }


    static private class StaticSource extends Source
    {
        public final String version;

        public StaticSource(String name, String version)
        {
            super(name);
            this.version = version;
        }

        @Override
        public String getVersion()
        {
            return version;
        }
    }


    /*
     * The rdfs:subClassOf triples as pairs of the (unit, id) of their subject and object, encoded as longs.
     */
    private static class SubclassPairs
    {
        private long[] subclasses = new long[1024];
        private long[] superclasses = new long[1024];
        private int size = 0;


        void add(Pair<Integer, Integer> subclass, Pair<Integer, Integer> superclass)
        {
            if(size == subclasses.length)
            {
                subclasses = Arrays.copyOf(subclasses, 2 * size);
                superclasses = Arrays.copyOf(superclasses, 2 * size);
            }

            subclasses[size] = encode(subclass);
            superclasses[size] = encode(superclass);
            size++;
        }


        /*
         * Returns the class with its direct and indirect subclasses.
         */
        HashSet<Long> getSubclasses(Pair<Integer, Integer> root)
        {
            HashSet<Long> result = new HashSet<>();
            result.add(encode(root));

            for(boolean changed = true; changed;)
            {
                changed = false;

                for(int i = 0; i < size; i++)
                    if(result.contains(superclasses[i]) && result.add(subclasses[i]))
                        changed = true;
            }

            return result;
        }


        static long encode(Pair<Integer, Integer> id)
        {
            return (long) id.getOne() << 32 | id.getTwo() & 0xffffffffL;
        }
    }


    private static class Unit
    {
        int id;
        int valueOffset;
        String suffix;
        Pattern pattern;
    }


    private static class ValueRestriction
    {
        public final Pair<Integer, Integer> propertyID;
        public final Pair<Integer, Integer> classID;

        public ValueRestriction(Pair<Integer, Integer> propertyID, Pair<Integer, Integer> classID)
        {
            this.propertyID = propertyID;
            this.classID = classID;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;

            if(obj == null || obj.getClass() != this.getClass())
                return false;

            ValueRestriction other = (ValueRestriction) obj;

            return propertyID.equals(other.propertyID) && classID.equals(other.classID);
        }

        @Override
        public int hashCode()
        {
            return propertyID.hashCode() + classID.hashCode();
        }
    }


    private static class CardinalityRestriction
    {
        public final Pair<Integer, Integer> propertyID;
        public final Integer cardinality;

        public CardinalityRestriction(Pair<Integer, Integer> propertyID, Integer cardinality)
        {
            this.propertyID = propertyID;
            this.cardinality = cardinality;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;

            if(obj == null || obj.getClass() != this.getClass())
                return false;

            CardinalityRestriction other = (CardinalityRestriction) obj;

            return propertyID.equals(other.propertyID) && cardinality.equals(other.cardinality);
        }

        @Override
        public int hashCode()
        {
            return propertyID.hashCode() + cardinality.hashCode();
        }
    }


    @SuppressWarnings("serial")
    public static class IntValueRestrictionMap extends SqlMap<Integer, ValueRestriction>
    {
        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public ValueRestriction getValue(ResultSet result) throws SQLException
        {
            return new ValueRestriction(Pair.getPair(result.getInt(2), result.getInt(3)),
                    Pair.getPair(result.getInt(4), result.getInt(5)));
        }

        @Override
        public void set(PreparedStatement statement, Integer key, ValueRestriction value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setInt(2, value.propertyID.getOne());
            statement.setInt(3, value.propertyID.getTwo());
            statement.setInt(4, value.classID.getOne());
            statement.setInt(5, value.classID.getTwo());
        }
    }


    @SuppressWarnings("serial")
    public static class IntCardinalityRestrictionMap extends SqlMap<Integer, CardinalityRestriction>
    {
        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public CardinalityRestriction getValue(ResultSet result) throws SQLException
        {
            return new CardinalityRestriction(Pair.getPair(result.getInt(2), result.getInt(3)), result.getInt(4));
        }

        @Override
        public void set(PreparedStatement statement, Integer key, CardinalityRestriction value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setInt(2, value.propertyID.getOne());
            statement.setInt(3, value.propertyID.getTwo());
            statement.setInt(4, value.cardinality);
        }
    }


    static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
    static final String owl = "http://www.w3.org/2002/07/owl#";
    static final String doap = "http://usefulinc.com/ns/doap#";

    private static final List<Source> sources = new ArrayList<>();

    private static final List<Unit> units = new ArrayList<>();
    private static final HashMap<Node, Integer> blankNodes = new HashMap<>();
    private static final HashMap<String, Integer> builtinResources = new HashMap<>();

    // the subjects of the rdf:type triples by their types, and the rdfs:subClassOf triples
    private static final HashMap<Node, List<Node>> instances = new HashMap<>();
    private static final SubclassPairs subclassPairs = new SubclassPairs();

    private static int nextResourceID;

    private static final StringIntMap keepResources = new StringIntMap();
    private static final StringIntMap newResources = new StringIntMap();
    private static final StringIntMap oldResources = new StringIntMap();


    private static void initSourceList()
    {
        sources.add(new OwlSource("BioAssay Ontology (BAO)", "http://www.bioassayontology.org/bao/bao_complete.owl"));
        sources.add(new OwlSource("Protein Ontology (PRO)", "http://purl.obolibrary.org/obo/pr.owl"));
        sources.add(new OwlSource("Gene Ontology (GO)", "http://purl.obolibrary.org/obo/go.owl"));
        sources.add(new OwlSource("Sequence Ontology (SO)", "http://purl.obolibrary.org/obo/so.owl"));
        sources.add(new OwlSource("Cell Line Ontology (CLO)", "http://purl.obolibrary.org/obo/clo/clo_merged.owl"));
        sources.add(new OwlSource("Cell Ontology (CL)", "http://purl.obolibrary.org/obo/cl.owl"));
        sources.add(new OwlSource("The BRENDA Tissue Ontology (BTO)", "http://purl.obolibrary.org/obo/bto.owl"));
        sources.add(new OwlSource("Human Disease Ontology (DO)", "http://purl.obolibrary.org/obo/doid.owl"));
        sources.add(new OwlSource("Mondo Disease Ontology (MONDO)", "http://purl.obolibrary.org/obo/mondo.owl"));
        sources.add(new OwlSource("Symptom Ontology (SYMP)", "http://purl.obolibrary.org/obo/symp.owl"));
        sources.add(
                new OwlSource("Pathogen Transmission Ontology (TRANS)", "http://purl.obolibrary.org/obo/trans.owl"));
        sources.add(new OwlSource("The Human Phenotype Ontology (HP)", "http://purl.obolibrary.org/obo/hp.owl"));
        sources.add(new OwlSource("Phenotype And Trait Ontology (PATO)", "http://purl.obolibrary.org/obo/pato.owl"));
        sources.add(new OwlSource("Units of Measurement Ontology (UO)", "http://purl.obolibrary.org/obo/uo.owl"));
        sources.add(new OwlSource("Ontology for Biomedical Investigations (OBI)",
                "http://purl.obolibrary.org/obo/obi.owl"));
        sources.add(new OwlSource("Information Artifact Ontology (IAO)", "http://purl.obolibrary.org/obo/iao.owl"));
        sources.add(new OwlSource("Uber-anatomy Ontology (UBERON)", "http://purl.obolibrary.org/obo/uberon.owl"));
        sources.add(new OwlSource("NCBI Taxonomy Database", "http://purl.obolibrary.org/obo/ncbitaxon.owl"));
        sources.add(new OwlSource("National Center Institute Thesaurus (OBO Edition)",
                "http://purl.obolibrary.org/obo/ncit.owl"));
        sources.add(new OwlSource("OBO Relations Ontology", "http://purl.obolibrary.org/obo/ro.owl"));
        sources.add(new OwlSource("Basic Formal Ontology (BFO)", "http://purl.obolibrary.org/obo/bfo.owl"));
        sources.add(new OwlSource("Food Ontology (FOODON)", "http://purl.obolibrary.org/obo/foodon.owl"));
        sources.add(new OwlSource("Evidence and Conclusion Ontology (ECO)", "http://purl.obolibrary.org/obo/eco.owl"));
        sources.add(new OwlSource("Disease Drivers Ontology (DISDRIV)", "http://purl.obolibrary.org/obo/disdriv.owl"));
        sources.add(new OwlSource("Genotype Ontology (GENO)", "http://purl.obolibrary.org/obo/geno.owl"));
        sources.add(
                new OwlSource("Common Anatomy Reference Ontology (CARO)", "http://purl.obolibrary.org/obo/caro.owl"));
        sources.add(new OwlSource("Environment Ontology (ENVO)", "http://purl.obolibrary.org/obo/envo.owl"));
        sources.add(new OwlSource("Ontology for General Medical Science (OGMS)",
                "http://purl.obolibrary.org/obo/ogms.owl"));
        sources.add(new OwlSource("Unified phenotype ontology (uPheno)", "http://purl.obolibrary.org/obo/upheno.owl"));
        sources.add(new OwlSource("OBO Metadata Ontology", "http://purl.obolibrary.org/obo/omo.owl"));
        sources.add(new StaticSource("Biological Pathway Exchange (BioPAX)", "1.0"));
        sources.add(new OwlSource("UniProt RDF schema ontology", "http://purl.uniprot.org/core/"));
        sources.add(new OwlSource("PDBx ontology", "http://rdf.wwpdb.org/schema/pdbx-v50.owl"));
        sources.add(new OwlSource("Quantities, Units, Dimensions and Types Ontology (QUDT)",
                "http://qudt.org/2.1/schema/qudt"));
        sources.add(new StaticSource("Open PHACTS Units extending QUDT", "2013-09-18"));
        sources.add(new StaticSource("Shapes Constraint Language (SHACL)", "2017-07-20"));
        sources.add(new OwlSource("Linked Models: Datatype Ontology (DTYPE)",
                "http://www.linkedmodel.org/1.1/schema/dtype"));
        sources.add(new OwlSource("Linked Models: Vocabulary for Attaching Essential Metadata (VAEM)",
                "http://www.linkedmodel.org/2.0/schema/vaem"));
        sources.add(new OwlSource("Chemical Information Ontology (CHEMINF)",
                "http://semanticchemistry.github.io/semanticchemistry/ontology/cheminf.owl"));
        sources.add(new OwlSource("Semanticscience integrated ontology (SIO)",
                "http://semanticscience.org/ontology/sio.owl"));
        sources.add(new OwlSource("Ontology of Bioscientific Data Analysis and Data Management (EDAM)",
                "http://edamontology.org"));
        sources.add(new StaticSource("National Drug File-Reference Terminology (NDF-RT)", "2011-10-14"));
        sources.add(new OwlSource("National Center Institute Thesaurus (NCIt)",
                "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl"));
        sources.add(new OwlSource("Experimental Factor Ontology (EFO)", "http://www.ebi.ac.uk/efo/efo.owl"));
        sources.add(new StaticSource("Eagle-i Resource Ontology (ERO)", "2015-06-30"));
        sources.add(new OwlSource("Funding, Research Administration and Projects Ontology (FRAPO)",
                "http://purl.org/cerif/frapo"));
        sources.add(new OwlSource("Patent Ontology (EPO)", "http://data.epo.org/linked-data/def/patent/"));
        sources.add(new OwlSource("W3C PROVenance Interchange", "http://www.w3.org/ns/prov#"));
        sources.add(new OwlSource("Metadata Authority Description Schema in RDF (MADS/RDF)",
                "http://www.loc.gov/mads/rdf/v1"));
        sources.add(new OwlSource("Citation Typing Ontology (CiTO)", "http://purl.org/spar/cito"));
        sources.add(new StaticSource("Ontology for vCard", "2014-05-22"));
        sources.add(new StaticSource("Feature Annotation Location Description Ontology (FALDO)", "2013"));
        sources.add(new OwlSource("FRBR-aligned Bibliographic Ontology (FaBiO)", "http://purl.org/spar/fabio"));
        sources.add(new OwlSource("Essential FRBR in OWL2 DL Ontology (FRBR)", "http://purl.org/spar/frbr"));
        sources.add(new StaticSource("Dublin Core Metadata Initiative Terms (DCMI)", "2020-01-20"));
        sources.add(new OwlSource("Bibliographic Ontology (BIBO)", "http://purl.org/ontology/bibo/"));
        sources.add(new StaticSource("Simple Knowledge Organization System (SKOS)", "2009-08-18"));
        sources.add(new StaticSource("Description of a Project Vocabulary (DOAP)", "2022-03-13"));
        sources.add(new StaticSource("FOAF Vocabulary", "0.1"));
        sources.add(new OwlSource("Provenance, Authoring and Versioning (PAV)", "http://purl.org/pav/"));
        sources.add(new StaticSource("SemWeb Vocab Status Ontology", "2011-12-12"));
        sources.add(new StaticSource("Vocabulary of Interlinked Datasets (VoID)", "2011-03-06"));
        sources.add(new StaticSource("Situation Ontology", "1.1"));
        sources.add(new OwlSource("Mass Spectrometry Ontology (MS)", "http://purl.obolibrary.org/obo/ms.owl"));
        sources.add(new OwlSource("ClassyFire Ontology", "http://purl.obolibrary.org/obo/ChemOnt.owl"));
        sources.add(new StaticSource("Chemical Entity Materials and Reactions Ontological Framework (ChEMROF)",
                "2026-10-03"));
        sources.add(new StaticSource("OWL 2 Schema (OWL 2)", "2009-10-16"));
        sources.add(new StaticSource("RDF Schema (RDFS)", "1.1"));
        sources.add(new StaticSource("RDF Vocabulary Terms", "1.1"));
    }


    public static void loadCategories() throws SQLException
    {
        try(Statement statement = connection.createStatement())
        {
            try(ResultSet result = statement
                    .executeQuery("select id, value_offset - 1, suffix, pattern from ontology.units"))
            {
                while(result.next())
                {
                    Unit unit = new Unit();
                    unit.id = result.getShort(1);
                    unit.valueOffset = result.getInt(2);
                    unit.suffix = result.getString(3);
                    unit.pattern = Pattern.compile(result.getString(4));

                    units.add(unit);
                }
            }
        }



        /*
         * sparql engine resources
         */

        // predicates
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 0);

        // datatypes
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#dirLangString", 400);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", 401);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#boolean", 402);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#byte", 403);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#date", 404);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#dateTime", 405);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", 406);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#decimal", 407);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#double", 408);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#float", 409);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#int", 410);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#integer", 411);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#long", 412);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#negativeInteger", 413);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", 414);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#nonPositiveInteger", 415);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#positiveInteger", 416);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#short", 417);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#string", 418);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#unsignedByte", 419);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#unsignedInt", 420);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#unsignedLong", 421);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#unsignedShort", 422);



        /*
         * service description resources
         */

        // classes
        builtinResources.put("http://rdfs.org/ns/void#Dataset", 2000);
        builtinResources.put("http://rdfs.org/ns/void#Linkset", 2001);
        builtinResources.put("http://www.w3.org/ns/shacl#PrefixDeclaration", 2002);
        builtinResources.put("http://www.w3.org/ns/shacl#SPARQLAskExecutable", 2003);
        builtinResources.put("http://www.w3.org/ns/shacl#SPARQLConstructExecutable", 2004);
        builtinResources.put("http://www.w3.org/ns/shacl#SPARQLDescribeExecutable", 2005);
        builtinResources.put("http://www.w3.org/ns/shacl#SPARQLExecutable", 2006);
        builtinResources.put("http://www.w3.org/ns/shacl#SPARQLSelectExecutable", 2007);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Dataset", 2008);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Feature", 2009);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Function", 2010);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Graph", 2011);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#GraphCollection", 2012);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Service", 2013);

        // predicates
        builtinResources.put("http://ldf.fi/void-ext#datatype", 2400);
        builtinResources.put("http://ldf.fi/void-ext#datatypePartition", 2401);
        builtinResources.put("http://ldf.fi/void-ext#distinctIRIReferenceObjects", 2402);
        builtinResources.put("http://ldf.fi/void-ext#distinctIRIReferenceSubjects", 2403);
        builtinResources.put("http://ldf.fi/void-ext#distinctLiterals", 2404);
        builtinResources.put("http://purl.org/dc/terms/issued", 2405);
        builtinResources.put("http://rdfs.org/ns/void#class", 2406);
        builtinResources.put("http://rdfs.org/ns/void#classes", 2407);
        builtinResources.put("http://rdfs.org/ns/void#classPartition", 2408);
        builtinResources.put("http://rdfs.org/ns/void#distinctObjects", 2409);
        builtinResources.put("http://rdfs.org/ns/void#distinctSubjects", 2410);
        builtinResources.put("http://rdfs.org/ns/void#linkPredicate", 2411);
        builtinResources.put("http://rdfs.org/ns/void#objectsTarget", 2412);
        builtinResources.put("http://rdfs.org/ns/void#properties", 2413);
        builtinResources.put("http://rdfs.org/ns/void#property", 2414);
        builtinResources.put("http://rdfs.org/ns/void#propertyPartition", 2415);
        builtinResources.put("http://rdfs.org/ns/void#subjectsTarget", 2416);
        builtinResources.put("http://rdfs.org/ns/void#subset", 2417);
        builtinResources.put("http://rdfs.org/ns/void#target", 2418);
        builtinResources.put("http://rdfs.org/ns/void#triples", 2419);
        builtinResources.put("http://www.w3.org/ns/shacl#ask", 2420);
        builtinResources.put("http://www.w3.org/ns/shacl#construct", 2421);
        builtinResources.put("http://www.w3.org/ns/shacl#declare", 2422);
        builtinResources.put("http://www.w3.org/ns/shacl#describe", 2423);
        builtinResources.put("http://www.w3.org/ns/shacl#namespace", 2424);
        builtinResources.put("http://www.w3.org/ns/shacl#prefix", 2425);
        builtinResources.put("http://www.w3.org/ns/shacl#prefixes", 2426);
        builtinResources.put("http://www.w3.org/ns/shacl#select", 2427);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#availableGraphs", 2428);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultDataset", 2429);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime", 2430);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultGraph", 2431);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#endpoint", 2432);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#extensionFunction", 2433);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#feature", 2434);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#graph", 2435);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#name", 2436);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#namedGraph", 2437);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#propertyFeature", 2438);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#resultFormat", 2439);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#supportedLanguage", 2440);
        builtinResources.put("https://purl.expasy.org/sparql-examples/ontology#federatesWith", 2441);
        builtinResources.put("https://schema.org/target", 2442);



        /*
         * shared resources
         */

        // predicates
        builtinResources.put("http://purl.org/dc/terms/bibliographicCitation", 4000);
        builtinResources.put("http://purl.org/dc/terms/date", 4001);
        builtinResources.put("http://purl.org/dc/terms/description", 4002);
        builtinResources.put("http://purl.org/dc/terms/modified", 4003);
        builtinResources.put("http://purl.org/dc/terms/subject", 4004);
        builtinResources.put("http://purl.org/dc/terms/title", 4005);
        builtinResources.put("http://purl.org/ontology/bibo/doi", 4006);
        builtinResources.put("http://purl.org/ontology/bibo/pmid", 4007);
        builtinResources.put("http://rdf.wwpdb.org/schema/pdbx-v50.owl#link_to_pdb", 4008);
        builtinResources.put("http://semanticscience.org/resource/has-value", 4009);
        builtinResources.put("http://semanticscience.org/resource/is-attribute-of", 4010);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#altLabel", 4011);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#broader", 4012);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#closeMatch", 4013);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#exactMatch", 4014);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#prefLabel", 4015);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#relatedMatch", 4016);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#family-name", 4017);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#given-name", 4018);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#organization-name", 4019);



        /*
         * ontology resources
         */

        // classes
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property", 6000);
        builtinResources.put("http://www.w3.org/2002/07/owl#Class", 6001);
        builtinResources.put("http://www.w3.org/2002/07/owl#NamedIndividual", 6002);
        builtinResources.put("http://www.w3.org/2002/07/owl#Restriction", 6003);

        // predicates
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#domain", 6400);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#label", 6401);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#range", 6402);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#seeAlso", 6403);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#subClassOf", 6404);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 6405);
        builtinResources.put("http://www.w3.org/2002/07/owl#allValuesFrom", 6406);
        builtinResources.put("http://www.w3.org/2002/07/owl#cardinality", 6407);
        builtinResources.put("http://www.w3.org/2002/07/owl#maxCardinality", 6408);
        builtinResources.put("http://www.w3.org/2002/07/owl#minCardinality", 6409);
        builtinResources.put("http://www.w3.org/2002/07/owl#onProperty", 6410);
        builtinResources.put("http://www.w3.org/2002/07/owl#someValuesFrom", 6411);



        /*
         * Wikidata resources
         */

        // predicates
        builtinResources.put("http://www.wikidata.org/prop/direct/P2017", 8000);
        builtinResources.put("http://www.wikidata.org/prop/direct/P233", 8001);



        /*
         * MoNA and ISDB resources
         */

        // classes
        builtinResources.put("http://www.w3.org/2006/vcard/ns#Individual", 10000);

        // predicates
        builtinResources.put("http://purl.org/dc/terms/created", 10400);
        builtinResources.put("http://purl.org/dc/terms/dateAccepted", 10401);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#hasEmail", 10402);

        // datatypes
        builtinResources.put("http://bioinfo.uochb.cas.cz/rdf/v1.0/ms#spectrum", 10800);



        /*
         * MeSH resources
         */

        // classes
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#AllowedDescriptorQualifierPair", 12000);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#CheckTag", 12001);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Concept", 12002);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#DisallowedDescriptorQualifierPair", 12003);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#GeographicalDescriptor", 12004);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#PublicationType", 12005);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Qualifier", 12006);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Anatomy", 12007);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Chemical", 12008);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Disease", 12009);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Organism", 12010);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Population", 12011);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Protocol", 12012);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Term", 12013);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#TopicalDescriptor", 12014);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#TreeNumber", 12015);

        // predicates
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#abbreviation", 12400);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#active", 12401);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#allowableQualifier", 12402);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#altLabel", 12403);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#annotation", 12404);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderConcept", 12405);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderDescriptor", 12406);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderQualifier", 12407);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#casn1_label", 12408);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#concept", 12409);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#considerAlso", 12410);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateCreated", 12411);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateEstablished", 12412);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateRevised", 12413);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#entryVersion", 12414);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#frequency", 12415);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#hasDescriptor", 12416);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#hasQualifier", 12417);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#historyNote", 12418);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#identifier", 12419);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#indexerConsiderAlso", 12420);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#lastActiveYear", 12421);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#lexicalTag", 12422);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#mappedTo", 12423);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#narrowerConcept", 12424);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#nlmClassificationNumber", 12425);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#note", 12426);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#onlineNote", 12427);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#parentTreeNumber", 12428);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#pharmacologicalAction", 12429);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredConcept", 12430);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredMappedTo", 12431);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredTerm", 12432);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#prefLabel", 12433);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#previousIndexing", 12434);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#publicMeSHNote", 12435);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#registryNumber", 12436);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#relatedConcept", 12437);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#relatedRegistryNumber", 12438);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#scopeNote", 12439);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#seeAlso", 12440);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#sortVersion", 12441);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#source", 12442);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#term", 12443);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#thesaurusID", 12444);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#treeNumber", 12445);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#useInstead", 12446);



        /*
         * MolMeDB resources
         */

        // classes
        builtinResources.put("http://purl.org/dc/terms/BibliographicResource", 14000);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#AbsorptionWavelength", 14001);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#AtomisticSimulation", 14002);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#BiologyBasedMembraneModel", 14003);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#BrainMembraneModel", 14004);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#CellMembraneModel", 14005);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#CoarseGrainedSimulation", 14006);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#ContactAngle", 14007);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#DepthOfMinima", 14008);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#EyeMembraneModel", 14009);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#FluorescenceLifetime", 14010);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#FluorescenceWavelength", 14011);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#GenericMembraneModel", 14012);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#HybridResolutionSimulation", 14013);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#InhibitionAssay", 14014);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#IntestineMembraneModel", 14015);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#LogK", 14016);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#LogPerm", 14017);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#MembraneModel", 14018);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#MembranePermeabilityMethod", 14019);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#MembranePositionMethod", 14020);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#OralMembraneModel", 14021);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#PenetrationBarrier", 14022);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#PKm", 14023);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#PositionOfMinima", 14024);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#ProtonationMaxPH", 14025);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#ProtonationMinPH", 14026);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#QuantumYield", 14027);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#SkinMembraneModel", 14028);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#SubstanceBasedMembraneModel", 14029);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#SubstrateBindingAssay", 14030);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#UnitedAtomsSimulation", 14031);
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#WaterMembranePartitioningMethod", 14032);

        // predicates
        builtinResources.put("https://rdf.molmedb.upol.cz/vocabulary#hasStDev", 14400);
        builtinResources.put("https://w3id.org/reproduceme#hasExperimentalCondition", 14401);



        /*
         * ChEBI resources
         */

        // classes
        builtinResources.put("http://www.w3.org/2002/07/owl#Axiom", 16000);

        // predicates
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/has_functional_parent", 16400);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/has_parent_hydride", 16401);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/is_conjugate_acid_of", 16402);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/is_conjugate_base_of", 16403);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/is_enantiomer_of", 16404);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/is_substituent_group_from", 16405);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/is_tautomer_of", 16406);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", 16407);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasDbXref", 16408);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym", 16409);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", 16410);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym", 16411);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasSynonymType", 16412);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#id", 16413);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#inSubset", 16414);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#source", 16415);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedProperty", 16416);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedSource", 16417);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedTarget", 16418);
        builtinResources.put("http://www.w3.org/2002/07/owl#deprecated", 16419);
        builtinResources.put("https://w3id.org/chemrof/charge", 16420);
        builtinResources.put("https://w3id.org/chemrof/generalized_empirical_formula", 16421);
        builtinResources.put("https://w3id.org/chemrof/inchi_key_string", 16422);
        builtinResources.put("https://w3id.org/chemrof/inchi_string", 16423);
        builtinResources.put("https://w3id.org/chemrof/mass", 16424);
        builtinResources.put("https://w3id.org/chemrof/monoisotopic_mass", 16425);
        builtinResources.put("https://w3id.org/chemrof/smiles_string", 16426);
        builtinResources.put("https://w3id.org/chemrof/wurcs_representation", 16427);

        // individuals
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/BRAND_NAME", 16800);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/INN", 16801);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/IUPAC_NAME", 16802);



        /*
         * ChEMBL resources
         */

        // classes
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Activity", 18000);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ADMET", 18001);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Antibody", 18002);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Assay", 18003);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#BindingSite", 18004);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#BioComponent", 18005);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellLine", 18006);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellLineTarget", 18007);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellTherapy", 18008);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CGDRef", 18009);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ChebiRef", 18010);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ChimericProtein", 18011);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Document", 18012);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#DrugbankRef", 18013);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#DrugIndication", 18014);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Enzyme", 18015);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#EnzymeClassRef", 18016);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#FdaSrsRef", 18017);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoComponentRef", 18018);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoFunctionRef", 18019);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoProcessRef", 18020);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#HmdbRef", 18021);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#IntactRef", 18022);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#InterproRef", 18023);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#IupharRef", 18024);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Journal", 18025);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Macromolecule", 18026);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Mechanism", 18027);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Metal", 18028);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NmrShiftDb2Ref", 18029);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NonMolecular", 18030);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NucleicAcid", 18031);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Oligonucleotide", 18032);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Oligosaccharide", 18033);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#OligosaccharideTarget", 18034);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Organism", 18035);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PdbeRef", 18036);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PfamRef", 18037);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PharmgkbRef", 18038);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Phenotype", 18039);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinClassification", 18040);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinComplex", 18041);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinComplexGroup", 18042);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinDataBankRef", 18043);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinFamily", 18044);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinMolecule", 18045);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinNucleicAcidComplex", 18046);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinProteinInteraction", 18047);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinSelectivityGroup", 18048);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemBioassayRef", 18049);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemRef", 18050);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ReactomeRef", 18051);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SingleProtein", 18052);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SmallMolecule", 18053);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SmallMoleculeTarget", 18054);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Source", 18055);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SubCellular", 18056);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Substance", 18057);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SureChemblRef", 18058);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Target", 18059);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#TargetComponent", 18060);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#TimbalRef", 18061);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Tissue", 18062);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedMolecule", 18063);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedTarget", 18064);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UniprotRef", 18065);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnknownSubstance", 18066);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnknownTarget", 18067);
        builtinResources.put("http://xmlns.com/foaf/0.1/Image", 18068);

        // predicates
        builtinResources.put("http://purl.org/dc/elements/1.1/identifier", 18400);
        builtinResources.put("http://purl.org/ontology/bibo/eissn", 18401);
        builtinResources.put("http://purl.org/ontology/bibo/issn", 18402);
        builtinResources.put("http://purl.org/ontology/bibo/issue", 18403);
        builtinResources.put("http://purl.org/ontology/bibo/pageEnd", 18404);
        builtinResources.put("http://purl.org/ontology/bibo/pageStart", 18405);
        builtinResources.put("http://purl.org/ontology/bibo/shortTitle", 18406);
        builtinResources.put("http://purl.org/ontology/bibo/volume", 18407);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#activityComment", 18408);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayCategory", 18409);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayCellType", 18410);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayStrain", 18411);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assaySubCellFrac", 18412);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayTestType", 18413);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayTissue", 18414);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayType", 18415);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayXref", 18416);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#atcClassification", 18417);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#bindingSiteName", 18418);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#cellosaurusId", 18419);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#chemblId", 18420);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#classLevel", 18421);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#classPath", 18422);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#componentType", 18423);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#dataValidityComment", 18424);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#dataValidityIssue", 18425);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#documentType", 18426);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#FRACClassification", 18427);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasActivity", 18428);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasAssay", 18429);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasBindingSite", 18430);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasBioComponent", 18431);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasCellLine", 18432);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasChildMolecule", 18433);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasCLO", 18434);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasDocument", 18435);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasDrugIndication", 18436);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasEFO", 18437);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasEFOName", 18438);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasJournal", 18439);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMechanism", 18440);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMesh", 18441);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMeshHeading", 18442);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMolecule", 18443);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasParentMolecule", 18444);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasProteinClassification", 18445);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasQUDT", 18446);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasSource", 18447);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTarget", 18448);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetComponent", 18449);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetComponentDescendant", 18450);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetDescendant", 18451);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasUnitOnto", 18452);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#helmNotation", 18453);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#highestDevelopmentPhase", 18454);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#HRACClassification", 18455);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#IRACClassification", 18456);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isBindingSiteForMechanism", 18457);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isBiotherapeutic", 18458);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isCellLineForAssay", 18459);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isCellLineForTarget", 18460);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isSpeciesGroup", 18461);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isTargetForCellLine", 18462);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isTargetForMechanism", 18463);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#mechanismActionType", 18464);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#mechanismDescription", 18465);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#moleculeXref", 18466);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#organismName", 18467);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#pChembl", 18468);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#potentialDuplicate", 18469);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#proteinSequence", 18470);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relation", 18471);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relEquivalentTo", 18472);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relHasSubset", 18473);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relOverlapsWith", 18474);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relSubsetOf", 18475);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardRelation", 18476);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardType", 18477);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardUnits", 18478);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardValue", 18479);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#substanceType", 18480);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetCmptXref", 18481);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetConfDesc", 18482);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetConfScore", 18483);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetRelDesc", 18484);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetRelType", 18485);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetType", 18486);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#taxonomy", 18487);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#type", 18488);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#units", 18489);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#value", 18490);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#narrower", 18491);
        builtinResources.put("http://xmlns.com/foaf/0.1/depiction", 18492);

        // individuals
        builtinResources.put("http://qudt.org/vocab/unit#Centimeter", 18800);
        builtinResources.put("http://qudt.org/vocab/unit#Day", 18801);
        builtinResources.put("http://qudt.org/vocab/unit#DegreeCelsius", 18802);
        builtinResources.put("http://qudt.org/vocab/unit#Gram", 18803);
        builtinResources.put("http://qudt.org/vocab/unit#Hour", 18804);
        builtinResources.put("http://qudt.org/vocab/unit#InternationalUnitPerLiter", 18805);
        builtinResources.put("http://qudt.org/vocab/unit#Kilogram", 18806);
        builtinResources.put("http://qudt.org/vocab/unit#Liter", 18807);
        builtinResources.put("http://qudt.org/vocab/unit#Micrometer", 18808);
        builtinResources.put("http://qudt.org/vocab/unit#Millimeter", 18809);
        builtinResources.put("http://qudt.org/vocab/unit#MilliSecond", 18810);
        builtinResources.put("http://qudt.org/vocab/unit#MinuteTime", 18811);
        builtinResources.put("http://qudt.org/vocab/unit#Percent", 18812);
        builtinResources.put("http://qudt.org/vocab/unit#SecondTime", 18813);
        builtinResources.put("http://www.openphacts.org/units/GramPerLiter", 18814);
        builtinResources.put("http://www.openphacts.org/units/MicrogramPerMilliliter", 18815);
        builtinResources.put("http://www.openphacts.org/units/Micromolar", 18816);
        builtinResources.put("http://www.openphacts.org/units/MilligramPerDeciliter", 18817);
        builtinResources.put("http://www.openphacts.org/units/MilligramPerMilliliter", 18818);
        builtinResources.put("http://www.openphacts.org/units/Millimolar", 18819);
        builtinResources.put("http://www.openphacts.org/units/Molar", 18820);
        builtinResources.put("http://www.openphacts.org/units/NanogramPerMilliliter", 18821);
        builtinResources.put("http://www.openphacts.org/units/Nanomolar", 18822);
        builtinResources.put("http://www.openphacts.org/units/PicogramPerMilliliter", 18823);
        builtinResources.put("http://www.openphacts.org/units/Picomolar", 18824);



        /*
         * PubChem resources
         */

        // classes
        builtinResources.put("http://data.epo.org/linked-data/def/patent/Publication", 20000);
        builtinResources.put("http://purl.org/cerif/frapo/FundingAgency", 20001);
        builtinResources.put("http://purl.org/cerif/frapo/Grant", 20002);
        builtinResources.put("http://purl.org/dc/terms/Dataset", 20003);
        builtinResources.put("http://purl.org/spar/fabio/Book", 20004);
        builtinResources.put("http://purl.org/spar/fabio/Journal", 20005);
        builtinResources.put("http://purl.uniprot.org/core/Enzyme", 20006);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Anatomy", 20007);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Author", 20008);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#BioAssay", 20009);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Book", 20010);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Cell", 20011);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Compound", 20012);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#CompoundIdentifier", 20013);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Concept", 20014);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#ConnectivitySMILES", 20015);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#ConservedDomain", 20016);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Cooccurrence", 20017);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#CovalentUnitCount", 20018);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#DefinedAtomStereoCount", 20019);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#DefinedBondStereoCount", 20020);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Disease", 20021);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Endpoint", 20022);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#ExactMass", 20023);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Gene", 20024);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#GeneSymbol", 20025);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Grant", 20026);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#HydrogenBondAcceptorCount", 20027);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#HydrogenBondDonorCount", 20028);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#InChIKey", 20029);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#IsotopeAtomCount", 20030);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#IUPACInChI", 20031);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Journal", 20032);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#MeasureGroup", 20033);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#MolecularFormula", 20034);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#MolecularWeight", 20035);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#MonoIsotopicWeight", 20036);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#NonHydrogenAtomCount", 20037);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Organization", 20038);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Patent", 20039);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PatentAssignee", 20040);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PatentInventor", 20041);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Pathway", 20042);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PreferredIUPACName", 20043);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Protein", 20044);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Reference", 20045);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#RotatableBondCount", 20046);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#SMILES", 20047);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Source", 20048);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#StructureComplexity", 20049);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Substance", 20050);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#SubstanceVersion", 20051);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Synonym", 20052);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Taxonomy", 20053);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#TotalFormalCharge", 20054);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#TPSA", 20055);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#UndefinedAtomStereoCount", 20056);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#UndefinedBondStereoCount", 20057);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#XLogP3", 20058);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Gene", 20059);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Pathway", 20060);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Protein", 20061);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#Concept", 20062);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#ConceptScheme", 20063);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#Organization", 20064);

        // predicates
        builtinResources.put("http://data.epo.org/linked-data/def/patent/applicantVC", 20400);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationCPCAdditional", 20401);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationCPCInventive", 20402);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationIPCAdditional", 20403);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationIPCInventive", 20404);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/filingDate", 20405);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/grantDate", 20406);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/inventorVC", 20407);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/publicationDate", 20408);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/publicationNumber", 20409);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/titleOfInvention", 20410);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/contentType", 20411);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/eissn", 20412);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/endingPage", 20413);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/isbn", 20414);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/issn", 20415);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/issueIdentifier", 20416);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/location", 20417);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/pageRange", 20418);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/publicationName", 20419);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/startingPage", 20420);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/subtitle", 20421);
        builtinResources.put("http://purl.obolibrary.org/obo/has-role", 20422);
        builtinResources.put("http://purl.org/cerif/frapo/hasFundingAgency", 20423);
        builtinResources.put("http://purl.org/cerif/frapo/hasGrantNumber", 20424);
        builtinResources.put("http://purl.org/cerif/frapo/isSupportedBy", 20425);
        builtinResources.put("http://purl.org/dc/terms/abstract", 20426);
        builtinResources.put("http://purl.org/dc/terms/alternative", 20427);
        builtinResources.put("http://purl.org/dc/terms/available", 20428);
        builtinResources.put("http://purl.org/dc/terms/creator", 20429);
        builtinResources.put("http://purl.org/dc/terms/identifier", 20430);
        builtinResources.put("http://purl.org/dc/terms/isPartOf", 20431);
        builtinResources.put("http://purl.org/dc/terms/language", 20432);
        builtinResources.put("http://purl.org/dc/terms/license", 20433);
        builtinResources.put("http://purl.org/dc/terms/publisher", 20434);
        builtinResources.put("http://purl.org/dc/terms/rights", 20435);
        builtinResources.put("http://purl.org/dc/terms/source", 20436);
        builtinResources.put("http://purl.org/pav/importedFrom", 20437);
        builtinResources.put("http://purl.org/spar/cito/citesAsDataSource", 20438);
        builtinResources.put("http://purl.org/spar/cito/discusses", 20439);
        builtinResources.put("http://purl.org/spar/cito/isCitedBy", 20440);
        builtinResources.put("http://purl.org/spar/cito/isDiscussedBy", 20441);
        builtinResources.put("http://purl.org/spar/fabio/hasNationalLibraryOfMedicineJournalId", 20442);
        builtinResources.put("http://purl.org/spar/fabio/hasNLMJournalTitleAbbreviation", 20443);
        builtinResources.put("http://purl.org/spar/fabio/hasPrimarySubjectTerm", 20444);
        builtinResources.put("http://purl.org/spar/fabio/hasSubjectTerm", 20445);
        builtinResources.put("http://purl.uniprot.org/core/encodedBy", 20446);
        builtinResources.put("http://purl.uniprot.org/core/enzyme", 20447);
        builtinResources.put("http://purl.uniprot.org/core/organism", 20448);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#connectivity_smiles", 20449);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#covalent_unit_count", 20450);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#defined_atom_stereo_count", 20451);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#defined_bond_stereo_count", 20452);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#discussesAsDerivedByTextMining", 20453);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#exact_mass", 20454);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent", 20455);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasQualifier", 20456);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasSimilarProtein", 20457);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hydrogen_bond_acceptor_count", 20458);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hydrogen_bond_donor_count", 20459);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inchikey", 20460);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of", 20461);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#isotope_atom_count", 20462);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#iupac_inchi", 20463);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#molecular_formula", 20464);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#molecular_weight", 20465);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#mono_isotopic_weight", 20466);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#non_hydrogen_atom_count", 20467);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#preferred_iupac_name", 20468);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#priorityDate", 20469);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome", 20470);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#rotatable_bond_count", 20471);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#structure_complexity", 20472);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#substance_version", 20473);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#total_formal_charge", 20474);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#tpsa", 20475);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#undefined_atom_stereo_count", 20476);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#undefined_bond_stereo_count", 20477);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#xlogp3", 20478);
        builtinResources.put("http://semanticscience.org/resource/gene-symbol", 20479);
        builtinResources.put("http://semanticscience.org/resource/has-attribute", 20480);
        builtinResources.put("http://semanticscience.org/resource/has-unit", 20481);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#organism", 20482);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#pathwayComponent", 20483);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#object", 20484);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject", 20485);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#inScheme", 20486);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#related", 20487);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#country-name", 20488);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#fn", 20489);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#hasUID", 20490);
        builtinResources.put("http://xmlns.com/foaf/0.1/homepage", 20491);

        // individuals
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active", 20800);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs", 20801);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive", 20802);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive", 20803);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe", 20804);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified", 20805);
    }


    public static Pair<Integer, Integer> getId(String iri)
    {
        if(iri == null)
            return null;

        for(Unit unit : units)
            if(unit.pattern.matcher(iri).matches())
                return Pair.getPair(unit.id, OntologyResource.parseId(unit.id,
                        iri.substring(unit.valueOffset, iri.length() - unit.suffix.length())));

        Integer resourceID = builtinResources.get(iri);

        if(resourceID == null)
            return null;

        return Pair.getPair((int) unitUncategorized, resourceID);
    }


    /*
     * Returns the (unit, id) of a node; a blank node, as well as an IRI that is neither of a unit nor a built-in
     * resource, gets a new id when it is first seen.
     */
    private static Pair<Integer, Integer> getId(Node node)
    {
        if(node.isBlank())
        {
            Integer blanknodeID = blankNodes.get(node);

            if(blanknodeID == null)
                blankNodes.put(node, blanknodeID = blankNodes.size());

            return Pair.getPair((int) unitBlank, blanknodeID);
        }
        else
        {
            String iri = node.getURI();
            Pair<Integer, Integer> result = getId(iri);

            if(result != null)
                return result;

            Integer resourceID = keepResources.get(iri);

            if(resourceID != null)
                return Pair.getPair((int) unitUncategorized, resourceID);

            resourceID = newResources.get(iri);

            if(resourceID != null)
                return Pair.getPair((int) unitUncategorized, resourceID);

            resourceID = oldResources.get(iri);

            if(resourceID == null || resourceID <= builtinResourceLimit - 1)
                newResources.put(iri, resourceID = nextResourceID++);
            else
                keepResources.put(iri, oldResources.remove(iri));

            return Pair.getPair((int) unitUncategorized, resourceID);
        }
    }


    /*
     * Returns the (unit, id) of a node if it has one already, without giving it a new id, or null otherwise.
     */
    private static Pair<Integer, Integer> findId(Node node)
    {
        if(node.isBlank())
        {
            Integer blanknodeID = blankNodes.get(node);

            return blanknodeID == null ? null : Pair.getPair((int) unitBlank, blanknodeID);
        }

        if(!node.isURI())
            return null;

        String iri = node.getURI();
        Pair<Integer, Integer> result = getId(iri);

        if(result != null)
            return result;

        Integer resourceID = keepResources.get(iri);

        if(resourceID == null)
            resourceID = newResources.get(iri);

        return resourceID == null ? null : Pair.getPair((int) unitUncategorized, resourceID);
    }


    /*
     * Records a value of the data: a value stored in the database is kept, any other one is new.
     */
    private static <T> void add(T value, SqlSet<T> keepValues, SqlSet<T> newValues, SqlSet<T> oldValues)
    {
        if(oldValues.remove(value))
            keepValues.add(value);
        else if(!keepValues.contains(value))
            newValues.add(value);
    }


    /*
     * Collects the values of owl:versionInfo, doap:Version and owl:versionIRI of the ontologies of the sources.
     */
    private static void loadVersions(TripleDispatcher dispatcher)
    {
        HashMap<String, OwlSource> owlSources = new HashMap<>();

        for(Source source : sources)
            if(source instanceof OwlSource owlSource)
                owlSources.put(owlSource.iri, owlSource);

        dispatcher.on(owl + "versionInfo", (subject, object) -> {
            if(subject.isURI() && owlSources.containsKey(subject.getURI()))
                owlSources.get(subject.getURI()).infos.add(object);
        });

        dispatcher.on(doap + "Version", (subject, object) -> {
            if(subject.isURI() && owlSources.containsKey(subject.getURI()))
                owlSources.get(subject.getURI()).releases.add(object);
        });

        dispatcher.on(owl + "versionIRI", (subject, object) -> {
            if(subject.isURI() && owlSources.containsKey(subject.getURI()))
                owlSources.get(subject.getURI()).versionIRIs.add(object);
        });
    }


    /*
     * Collects the subjects of the rdf:type triples by their types and the rdfs:subClassOf triples, as some resources
     * can be classified only once all the files have been read.
     */
    private static void loadTypes(TripleDispatcher dispatcher)
    {
        dispatcher.on(rdf + "type", (subject, object) -> {
            instances.computeIfAbsent(object, k -> new ArrayList<>()).add(subject);
        });

        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            subclassPairs.add(getId(subject), getId(object));
        });
    }


    /*
     * Returns the instances of the type.
     */
    private static List<Node> getInstances(String type)
    {
        return instances.getOrDefault(NodeFactory.createURI(type), List.of());
    }


    /*
     * Returns the types whose instances are classes, which are rdfs:Class and its direct and indirect subclasses.
     */
    private static List<Node> getClassTypes()
    {
        Pair<Integer, Integer> classID = findId(NodeFactory.createURI(rdfs + "Class"));
        HashSet<Long> metaclasses = classID == null ? new HashSet<>() : subclassPairs.getSubclasses(classID);

        List<Node> types = new ArrayList<>();

        for(Node type : instances.keySet())
        {
            Pair<Integer, Integer> typeID = findId(type);

            if(is(type, rdfs + "Class") || typeID != null && metaclasses.contains(SubclassPairs.encode(typeID)))
                types.add(type);
        }

        return types;
    }


    private static void loadBases() throws IOException, SQLException
    {
        load("select iri,id from ontology.uncategorized_resources", oldResources);

        nextResourceID = Math.max(oldResources.values().stream().max(Integer::compare).orElse(-1).intValue() + 1,
                builtinResourceLimit);

        builtinResources.forEach((iri, id) -> {
            Integer old = oldResources.get(iri);

            if(old == null || !old.equals(id))
                newResources.put(iri, id);
            else
                keepResources.put(iri, oldResources.remove(iri));
        });
    }


    /*
     * Loads the classes: the instances of the classes of classes, the classes in the class hierarchy, the domains and
     * the ranges of the properties and the classes of the values of the value restrictions.
     */
    private static void loadClasses(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepClasses = new IntPairSet();
        IntPairSet newClasses = new IntPairSet();
        IntPairSet oldClasses = new IntPairSet();

        load("select unit,id from ontology.classes", oldClasses);

        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            add(getId(subject), keepClasses, newClasses, oldClasses);
            add(getId(object), keepClasses, newClasses, oldClasses);
        });

        for(String predicate : List.of(rdfs + "domain", rdfs + "range", owl + "someValuesFrom", owl + "allValuesFrom"))
            dispatcher.on(predicate, (subject, object) -> {
                add(getId(object), keepClasses, newClasses, oldClasses);
            });

        dispatcher.after(() -> {
            for(Node type : getClassTypes())
                for(Node instance : instances.get(type))
                    add(getId(instance), keepClasses, newClasses, oldClasses);

            store("delete from ontology.classes where unit=? and id=?", oldClasses);
            store("insert into ontology.classes(unit,id) values(?,?)", newClasses);
        });
    }


    /*
     * Loads the properties: the instances of the property types, the properties in the property hierarchy, the
     * properties with a domain or a range and the properties of the restrictions.
     */
    private static void loadProperties(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepProperties = new IntPairSet();
        IntPairSet newProperties = new IntPairSet();
        IntPairSet oldProperties = new IntPairSet();

        load("select unit,id from ontology.properties", oldProperties);

        for(String type : List.of(owl + "ObjectProperty", owl + "DatatypeProperty", owl + "AnnotationProperty",
                rdf + "Property"))
            dispatcher.onType(type, (subject, object) -> {
                add(getId(subject), keepProperties, newProperties, oldProperties);
            });

        dispatcher.on(rdfs + "subPropertyOf", (subject, object) -> {
            add(getId(subject), keepProperties, newProperties, oldProperties);
            add(getId(object), keepProperties, newProperties, oldProperties);
        });

        for(String predicate : List.of(rdfs + "domain", rdfs + "range"))
            dispatcher.on(predicate, (subject, object) -> {
                add(getId(subject), keepProperties, newProperties, oldProperties);
            });

        dispatcher.on(owl + "onProperty", (subject, object) -> {
            add(getId(object), keepProperties, newProperties, oldProperties);
        });

        dispatcher.after(() -> {
            store("delete from ontology.properties where unit=? and id=?", oldProperties);
            store("insert into ontology.properties(unit,id) values(?,?)", newProperties);
        });
    }


    private static void loadIndividuals(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepIndividuals = new IntPairSet();
        IntPairSet newIndividuals = new IntPairSet();
        IntPairSet oldIndividuals = new IntPairSet();

        load("select unit,id from ontology.individuals", oldIndividuals);

        dispatcher.onType(owl + "NamedIndividual", (subject, object) -> {
            add(getId(subject), keepIndividuals, newIndividuals, oldIndividuals);
        });

        dispatcher.after(() -> {
            store("delete from ontology.individuals where unit=? and id=?", oldIndividuals);
            store("insert into ontology.individuals(unit,id) values(?,?)", newIndividuals);
        });
    }


    /*
     * Loads the labels of the resources: of the English labels and the labels without a language, the greatest one
     * in the order of SPARQL, which prefers a label with a language and then the greater lexical form.
     */
    private static void loadResourceLabels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairStringMap keepLabels = new IntPairStringMap();
        IntPairStringMap newLabels = new IntPairStringMap();
        IntPairStringMap oldLabels = new IntPairStringMap();

        load("select resource_unit,resource_id,label from ontology.resource_labels", oldLabels);

        HashMap<Pair<Integer, Integer>, Node> labels = new HashMap<>();

        dispatcher.on(rdfs + "label", (subject, object) -> {
            if(!object.isLiteral())
                return;

            String language = object.getLiteralLanguage();

            if(language.isEmpty() || language.equals("en"))
                labels.merge(getId(subject), object, (label, other) -> isGreater(other, label) ? other : label);
        });

        dispatcher.after(() -> {
            for(Entry<Pair<Integer, Integer>, Node> entry : labels.entrySet())
            {
                Pair<Integer, Integer> resourceID = entry.getKey();
                String label = entry.getValue().getLiteralLexicalForm();

                if(label.equals(oldLabels.remove(resourceID)))
                    keepLabels.put(resourceID, label);
                else
                    newLabels.put(resourceID, label);
            }

            store("delete from ontology.resource_labels where resource_unit=? and resource_id=? and label=?",
                    oldLabels);
            store("insert into ontology.resource_labels(resource_unit,resource_id,label) values(?,?,?) "
                    + "on conflict(resource_unit,resource_id) do update set label=EXCLUDED.label", newLabels);
        });
    }


    /*
     * Tests whether a label is greater than another one: a label with a language is greater than one without it,
     * otherwise the label with the greater lexical form is.
     */
    private static boolean isGreater(Node label, Node other)
    {
        boolean tagged = !label.getLiteralLanguage().isEmpty();

        if(tagged != !other.getLiteralLanguage().isEmpty())
            return tagged;

        return label.getLiteralLexicalForm().compareTo(other.getLiteralLexicalForm()) > 0;
    }


    /*
     * Loads the superclasses: the superclasses of the class hierarchy other than owl:Thing, except those between two
     * ChEBI classes, and owl:Thing as the superclass of the classes without a superclass (except blank nodes,
     * owl:Thing and rdfs:Resource).
     */
    private static void loadSuperClasses(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairIntPairSet keepSuperClasses = new IntPairIntPairSet();
        IntPairIntPairSet newSuperClasses = new IntPairIntPairSet();
        IntPairIntPairSet oldSuperClasses = new IntPairIntPairSet();

        load("select class_unit,class_id,superclass_unit,superclass_id from ontology.superclasses", oldSuperClasses);

        HashSet<Pair<Integer, Integer>> subclasses = new HashSet<>();
        HashSet<Pair<Integer, Integer>> classes = new HashSet<>();

        dispatcher.on(rdfs + "subClassOf", (subject, object) -> {
            Pair<Integer, Integer> classID = getId(subject);
            Pair<Integer, Integer> superclassID = getId(object);

            subclasses.add(classID);
            classes.add(superclassID);

            if(is(object, owl + "Thing") || classID.getOne() == unitCHEBI && superclassID.getOne() == unitCHEBI)
                return;

            add(Pair.getPair(classID, superclassID), keepSuperClasses, newSuperClasses, oldSuperClasses);
        });

        for(String predicate : List.of(rdfs + "domain", rdfs + "range"))
            dispatcher.on(predicate, (subject, object) -> {
                classes.add(getId(object));
            });

        dispatcher.after(() -> {
            Pair<Integer, Integer> thingID = getId(NodeFactory.createURI(owl + "Thing"));
            Pair<Integer, Integer> resourceID = findId(NodeFactory.createURI(rdfs + "Resource"));

            for(Node instance : getInstances(owl + "Class"))
                if(!instance.isBlank() && !is(instance, owl + "Thing") && !is(instance, rdfs + "Resource")
                        && !subclasses.contains(findId(instance)))
                    classes.add(getId(instance));

            for(Pair<Integer, Integer> classID : classes)
                if(classID.getOne() != unitBlank && !classID.equals(thingID) && !classID.equals(resourceID)
                        && !subclasses.contains(classID))
                    add(Pair.getPair(classID, thingID), keepSuperClasses, newSuperClasses, oldSuperClasses);

            store("delete from ontology.superclasses "
                    + "where class_unit=? and class_id=? and superclass_unit=? and superclass_id=?", oldSuperClasses);
            store("insert into ontology.superclasses(class_unit,class_id,superclass_unit,superclass_id) "
                    + "values(?,?,?,?)", newSuperClasses);
        });
    }


    private static void loadSuperProperties(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairIntPairSet keepSuperProperties = new IntPairIntPairSet();
        IntPairIntPairSet newSuperProperties = new IntPairIntPairSet();
        IntPairIntPairSet oldSuperProperties = new IntPairIntPairSet();

        load("select property_unit,property_id,superproperty_unit,superproperty_id from ontology.superproperties",
                oldSuperProperties);

        dispatcher.on(rdfs + "subPropertyOf", (subject, object) -> {
            add(Pair.getPair(getId(subject), getId(object)), keepSuperProperties, newSuperProperties,
                    oldSuperProperties);
        });

        dispatcher.after(() -> {
            store("delete from ontology.superproperties "
                    + "where property_unit=? and property_id=? and superproperty_unit=? and superproperty_id=?",
                    oldSuperProperties);
            store("insert into ontology.superproperties(property_unit,property_id,superproperty_unit,superproperty_id) "
                    + "values(?,?,?,?)", newSuperProperties);
        });
    }


    private static void loadDomains(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairIntPairSet keepDomains = new IntPairIntPairSet();
        IntPairIntPairSet newDomains = new IntPairIntPairSet();
        IntPairIntPairSet oldDomains = new IntPairIntPairSet();

        load("select property_unit,property_id,domain_unit,domain_id from ontology.property_domains", oldDomains);

        dispatcher.on(rdfs + "domain", (subject, object) -> {
            add(Pair.getPair(getId(subject), getId(object)), keepDomains, newDomains, oldDomains);
        });

        dispatcher.after(() -> {
            store("delete from ontology.property_domains "
                    + "where property_unit=? and property_id=? and domain_unit=? and domain_id=?", oldDomains);
            store("insert into ontology.property_domains(property_unit,property_id,domain_unit,domain_id) "
                    + "values(?,?,?,?)", newDomains);
        });
    }


    private static void loadRanges(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairIntPairSet keepRanges = new IntPairIntPairSet();
        IntPairIntPairSet newRanges = new IntPairIntPairSet();
        IntPairIntPairSet oldRanges = new IntPairIntPairSet();

        load("select property_unit,property_id,range_unit,range_id from ontology.property_ranges", oldRanges);

        dispatcher.on(rdfs + "range", (subject, object) -> {
            add(Pair.getPair(getId(subject), getId(object)), keepRanges, newRanges, oldRanges);
        });

        dispatcher.after(() -> {
            store("delete from ontology.property_ranges "
                    + "where property_unit=? and property_id=? and range_unit=? and range_id=?", oldRanges);
            store("insert into ontology.property_ranges(property_unit,property_id,range_unit,range_id) "
                    + "values(?,?,?,?)", newRanges);
        });
    }


    /*
     * Loads the restrictions. A restriction is a blank node, so its triples are collected and the restrictions are
     * assembled once all the files have been read.
     */
    private static void loadRestrictions(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        BlankNodes restrictions = new BlankNodes(dispatcher, owl + "onProperty", owl + "someValuesFrom",
                owl + "allValuesFrom", owl + "cardinality", owl + "minCardinality", owl + "maxCardinality");

        dispatcher.onType(owl + "Restriction", (subject, object) -> {
            if(!subject.isBlank())
                throw new IOException("unexpected restriction " + subject);
        });

        loadValueRestrictions(dispatcher, restrictions, owl + "someValuesFrom", "somevaluesfrom_restrictions");
        loadValueRestrictions(dispatcher, restrictions, owl + "allValuesFrom", "allvaluesfrom_restrictions");
        loadCardinalityRestrictions(dispatcher, restrictions, owl + "cardinality", "cardinality_restrictions");
        loadCardinalityRestrictions(dispatcher, restrictions, owl + "minCardinality", "mincardinality_restrictions");
        loadCardinalityRestrictions(dispatcher, restrictions, owl + "maxCardinality", "maxcardinality_restrictions");
    }


    /*
     * Loads the restrictions whose class of values is given by the predicate.
     */
    private static void loadValueRestrictions(TripleDispatcher dispatcher, BlankNodes restrictions, String predicate,
            String table) throws IOException, SQLException
    {
        IntValueRestrictionMap keepRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap oldRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap newRestrictions = new IntValueRestrictionMap();

        load("select id,property_unit,property_id,class_unit,class_id from ontology." + table, oldRestrictions);

        dispatcher.after(() -> {
            for(Node node : getInstances(owl + "Restriction"))
            {
                for(Node property : restrictions.values(node, owl + "onProperty"))
                {
                    for(Node value : restrictions.values(node, predicate))
                    {
                        Integer restrictionID = getId(node).getTwo();
                        ValueRestriction restriction = new ValueRestriction(getId(property), getId(value));

                        if(restriction.equals(oldRestrictions.remove(restrictionID)))
                        {
                            keepRestrictions.put(restrictionID, restriction);
                        }
                        else
                        {
                            ValueRestriction keep = keepRestrictions.get(restrictionID);

                            if(restriction.equals(keep))
                                continue;
                            else if(keep != null)
                                throw new IOException();

                            ValueRestriction put = newRestrictions.put(restrictionID, restriction);

                            if(put != null && !restriction.equals(put))
                                throw new IOException();
                        }
                    }
                }
            }

            store("delete from ontology." + table
                    + " where id=? and property_unit=? and property_id=? and class_unit=? and class_id=?",
                    oldRestrictions);
            store("insert into ontology." + table
                    + "(id,property_unit,property_id,class_unit,class_id) values(?,?,?,?,?) "
                    + "on conflict(id) do update set property_unit=EXCLUDED.property_unit, "
                    + "property_id=EXCLUDED.property_id, class_unit=EXCLUDED.class_unit, class_id=EXCLUDED.class_id",
                    newRestrictions);
        });
    }


    /*
     * Loads the restrictions whose cardinality is given by the predicate.
     */
    private static void loadCardinalityRestrictions(TripleDispatcher dispatcher, BlankNodes restrictions,
            String predicate, String table) throws IOException, SQLException
    {
        IntCardinalityRestrictionMap keepRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap oldRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap newRestrictions = new IntCardinalityRestrictionMap();

        load("select id,property_unit,property_id,cardinality from ontology." + table, oldRestrictions);

        dispatcher.after(() -> {
            for(Node node : getInstances(owl + "Restriction"))
            {
                for(Node property : restrictions.values(node, owl + "onProperty"))
                {
                    for(Node value : restrictions.values(node, predicate))
                    {
                        Integer restrictionID = getId(node).getTwo();
                        CardinalityRestriction restriction = new CardinalityRestriction(getId(property),
                                getCardinality(value));

                        if(restriction.equals(oldRestrictions.remove(restrictionID)))
                        {
                            keepRestrictions.put(restrictionID, restriction);
                        }
                        else
                        {
                            CardinalityRestriction keep = keepRestrictions.get(restrictionID);

                            if(restriction.equals(keep))
                                continue;
                            else if(keep != null)
                                throw new IOException();

                            CardinalityRestriction put = newRestrictions.put(restrictionID, restriction);

                            if(put != null && !restriction.equals(put))
                                throw new IOException();
                        }
                    }
                }
            }

            store("delete from ontology." + table
                    + " where id=? and property_unit=? and property_id=? and cardinality=?", oldRestrictions);
            store("insert into ontology." + table + "(id,property_unit,property_id,cardinality) "
                    + "values(?,?,?,?) on conflict(id) do update set property_unit=EXCLUDED.property_unit, "
                    + "property_id=EXCLUDED.property_id, cardinality=EXCLUDED.cardinality", newRestrictions);
        });
    }


    /*
     * Returns the value of a cardinality, a literal of a numeric datatype.
     */
    private static int getCardinality(Node node) throws IOException
    {
        if(!node.isLiteral() || !(node.getLiteralValue() instanceof Number number))
            throw new IOException("unexpected cardinality " + node);

        return number.intValue();
    }


    static void finish() throws SQLException, IOException
    {
        store("delete from ontology.uncategorized_resources where iri=? and id=?", oldResources);
        store("insert into ontology.uncategorized_resources(iri,id) values(?,?)", newResources);
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            loadCategories();

            initSourceList();

            TripleDispatcher dispatcher = new TripleDispatcher();

            loadVersions(dispatcher);
            loadTypes(dispatcher);

            loadBases();
            loadClasses(dispatcher);
            loadProperties(dispatcher);
            loadIndividuals(dispatcher);
            loadResourceLabels(dispatcher);

            loadSuperClasses(dispatcher);

            loadSuperProperties(dispatcher);
            loadDomains(dispatcher);
            loadRanges(dispatcher);

            loadRestrictions(dispatcher);

            dispatcher.load("ontology", ".*");

            System.out.println("=== load ontologies ===");

            for(Source source : sources)
                System.out.println(source.getName() + ": " + source.getVersion());

            dispatcher.finish();
            finish();

            for(Source source : sources)
                setVersion(source.getName(), source.getVersion());

            updateVersion();

            checkProblems();

            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
