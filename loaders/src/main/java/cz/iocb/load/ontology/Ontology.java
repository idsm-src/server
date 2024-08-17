package cz.iocb.load.ontology;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
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

        public abstract String getVersion(Model model);
    }


    static private class OwlSource extends Source
    {
        public final String iri;

        public OwlSource(String name, String iri)
        {
            super(name);
            this.iri = iri;
        }


        public String getVersionFromInfo(Model model)
        {
            String version = null;

            try(QueryExecution qexec = QueryExecutionFactory
                    .create(patternQuery("<" + iri + "> <http://www.w3.org/2002/07/owl#versionInfo> | "
                            + "<http://usefulinc.com/ns/doap#Version> ?version"), model))
            {
                org.apache.jena.query.ResultSet results = qexec.execSelect();

                while(results.hasNext())
                {
                    RDFNode node = results.nextSolution().get("version");

                    if(node.isLiteral())
                    {
                        String tmp = node.asLiteral().getLexicalForm();

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
            }

            return version;
        }


        public String getVersionFromVersionIRI(Model model)
        {
            String version = null;

            try(QueryExecution qexec = QueryExecutionFactory
                    .create(patternQuery("<" + iri + "> <http://www.w3.org/2002/07/owl#versionIRI> ?version"), model))
            {
                org.apache.jena.query.ResultSet results = qexec.execSelect();

                if(results.hasNext())
                {
                    RDFNode node = results.nextSolution().get("version");

                    if(node.isLiteral())
                        version = node.asLiteral().getLexicalForm();
                    else
                        version = node.asResource().getURI();

                    if(version.matches(".*-20[0-9]{6}$"))
                        version = version.replaceFirst(".*-(20[0-9]{2})([0-9]{2})([0-9]{2})$", "$1-$2-$3");
                    else
                        version = version.replaceFirst(".*/([0-9.-]+)(/|$).*", "$1");
                }

                if(results.hasNext())
                    version = null;
            }

            return version;
        }


        @Override
        public String getVersion(Model model)
        {
            String version = getVersionFromInfo(model);

            if(version == null)
                version = getVersionFromVersionIRI(model);

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
        public String getVersion(Model model)
        {
            return version;
        }
    }


    private static class Unit
    {
        int id;
        int valueOffset;
        String pattern;
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


    protected static abstract class OntologyQueryResultProcessor extends QueryResultProcessor
    {
        protected OntologyQueryResultProcessor(String sparql)
        {
            super(sparql);
        }

        protected Pair<Integer, Integer> getId(String name)
        {
            return Ontology.getId(solution.getResource(name));
        }

        protected String getBlankNode(String name)
        {
            return solution.getResource(name).getId().getLabelString();
        }
    }


    private static final List<Source> sources = new ArrayList<Source>();

    private static final List<Unit> units = new ArrayList<Unit>();
    private static final HashMap<String, Integer> blankNodes = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> builtinResources = new HashMap<String, Integer>();

    private static int nextResourceID;
    private static int maxBuiltinResourceID;

    private static final StringIntMap keepResources = new StringIntMap();
    private static final StringIntMap newResources = new StringIntMap();
    private static final StringIntMap oldResources = new StringIntMap();

    public static final int unitUncategorized = 0;
    public static final int unitBlank = 1;
    public static final int unitSIO = 2;
    public static final int unitCHEMINF = 3;
    public static final int unitBAO = 4;
    public static final int unitGO = 5;
    public static final int unitPR = 6;
    public static final int unitThesaurus = 10;
    public static final int unitPR0 = 31;
    public static final int unitPR1 = 32;
    public static final int unitPR2 = 33;
    public static final int unitAT = 34;
    public static final int unitZDBGENE = 35;
    public static final int unitStar = 95;
    public static final int unitRareDiseases = 180;


    private static void initSourceList()
    {
        sources.add(new OwlSource("BioAssay Ontology (BAO)", "http://www.bioassayontology.org/bao/bao_complete.owl"));
        sources.add(new OwlSource("Protein Ontology (PRO)", "http://purl.obolibrary.org/obo/pr.owl"));
        sources.add(new OwlSource("Gene Ontology (GO)", "http://purl.obolibrary.org/obo/go.owl"));
        sources.add(new OwlSource("Sequence Ontology (SO)", "http://purl.obolibrary.org/obo/so.owl"));
        sources.add(new OwlSource("Cell Line Ontology (CLO)", "http://purl.obolibrary.org/obo/clo_merged.owl"));
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
        sources.add(new StaticSource("Disease Drivers Ontology (DISDRIV)", "2023-12-15"));
        sources.add(new OwlSource("Genotype Ontology (GENO)", "http://purl.obolibrary.org/obo/geno.owl"));
        sources.add(
                new OwlSource("Common Anatomy Reference Ontology (CARO)", "http://purl.obolibrary.org/obo/caro.owl"));
        sources.add(new OwlSource("Environment Ontology (ENVO)", "http://purl.obolibrary.org/obo/envo.owl"));
        sources.add(new OwlSource("Ontology for General Medical Science (OGMS)",
                "http://purl.obolibrary.org/obo/ogms.owl"));
        sources.add(new StaticSource("Unified phenotype ontology (uPheno)", "2.0"));
        sources.add(new OwlSource("OBO Metadata Ontology", "http://purl.obolibrary.org/obo/omo.owl"));
        sources.add(new StaticSource("Biological Pathway Exchange (BioPAX)", "1.0"));
        sources.add(new OwlSource("UniProt RDF schema ontology", "http://purl.uniprot.org/core/"));
        sources.add(new OwlSource("PDBx ontology", "https://rdf.wwpdb.org/schema/pdbx-v50.owl"));
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
        sources.add(new StaticSource("OWL 2 Schema (OWL 2)", "2009-10-16"));
        sources.add(new StaticSource("RDF Schema (RDFS)", "1.1"));
        sources.add(new StaticSource("RDF Vocabulary Terms", "1.1"));
    }


    public static void loadCategories() throws SQLException
    {
        try(Statement statement = connection.createStatement())
        {
            try(ResultSet result = statement.executeQuery(
                    "select unit_id, value_offset - 1, pattern from ontology.resource_categories__reftable"))
            {
                while(result.next())
                {
                    Unit unit = new Unit();
                    unit.id = result.getShort(1);
                    unit.valueOffset = result.getInt(2);
                    unit.pattern = result.getString(3);

                    units.add(unit);
                }
            }
        }

        // PubChem
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active", 0);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive", 1);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive", 2);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified", 3);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe", 4);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs", 7);

        // MESH
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#AllowedDescriptorQualifierPair", 32);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Concept", 33);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#DisallowedDescriptorQualifierPair", 34);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#GeographicalDescriptor", 35);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#CheckTag", 36);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#PublicationType", 37);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Qualifier", 38);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Disease", 39);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Chemical", 40);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Organism", 41);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Population", 42);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Protocol", 43);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#Term", 44);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#TopicalDescriptor", 45);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#TreeNumber", 46);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#SCR_Anatomy", 47);

        // ChEBI
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#has_functional_parent", 64);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#is_conjugate_base_of", 65);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#is_conjugate_acid_of", 66);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#is_enantiomer_of", 67);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#is_tautomer_of", 68);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#has_parent_hydride", 69);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#is_substituent_group_from", 70);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#BRAND_NAME", 71);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#IUPAC_NAME", 72);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi#INN", 73);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasDbXref", 74);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasExactSynonym", 75);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym", 76);
    }


    public static Pair<Integer, Integer> getId(String iri)
    {
        if(iri == null)
            return null;

        for(Unit unit : units)
        {
            if(iri.matches(unit.pattern))
            {
                String tail = iri.substring(unit.valueOffset);
                int id = 0;

                if(unit.id == unitPR0)
                {
                    // [0-9][A-Z0-9][0-9][A-Z0-9]{3}[0-9]
                    id = tail.charAt(0) - '0';
                    id = id * 36 + code(tail.charAt(1));
                    id = id * 10 + tail.charAt(2) - '0';
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 36 + code(tail.charAt(5));
                    id = id * 10 + tail.charAt(6) - '0';
                }
                else if(unit.id == unitPR1 || unit.id == unitPR2)
                {
                    // [A-Z][0-9][A-Z0-9]{3}[0-9](-([12])?[0-9])?
                    id = tail.charAt(0) - 'A';
                    id = id * 10 + tail.charAt(1) - '0';
                    id = id * 36 + code(tail.charAt(2));
                    id = id * 36 + code(tail.charAt(3));
                    id = id * 36 + code(tail.charAt(4));
                    id = id * 10 + tail.charAt(5) - '0';

                    if(unit.id == unitPR1)
                        id = id * 30 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitAT)
                {
                    // [A-Z0-9]G[0-9]{5}
                    id = code(tail.charAt(0)) * 100000 + Integer.parseInt(tail.substring(2));
                }
                else if(unit.id == unitZDBGENE)
                {
                    // [0-9]{6}-([1-3])?[0-9]{1,3}$
                    id = Integer.parseInt(tail.substring(0, 6));
                    id = id * 4000 + Integer.parseInt(tail.substring(7));
                }
                else if(unit.id == unitStar)
                {
                    id = tail.charAt(0) - '0';
                }
                else if(unit.id == unitRareDiseases)
                {
                    id = Integer.parseInt(tail.substring(0, tail.length() - 6));
                }
                else
                {
                    id = Integer.parseInt(tail);
                }

                return Pair.getPair(unit.id, id);
            }
        }

        Integer resourceID = builtinResources.get(iri);

        if(resourceID == null)
            return null;

        return Pair.getPair(unitUncategorized, resourceID);
    }


    private static int code(char value)
    {
        return value > '9' ? 10 + value - 'A' : value - '0';
    }


    private static Pair<Integer, Integer> getId(Resource resource)
    {
        if(resource.isAnon())
        {
            String blanknode = resource.getId().getLabelString();
            Integer blanknodeID = blankNodes.get(blanknode);

            if(blanknodeID == null)
                blankNodes.put(blanknode, blanknodeID = blankNodes.size());

            return Pair.getPair(unitBlank, blanknodeID);
        }
        else
        {
            String iri = resource.getURI();
            Pair<Integer, Integer> result = getId(iri);

            if(result != null)
                return result;

            Integer resourceID = keepResources.get(iri);

            if(resourceID != null)
                return Pair.getPair(unitUncategorized, resourceID);

            resourceID = newResources.get(iri);

            if(resourceID != null)
                return Pair.getPair(unitUncategorized, resourceID);

            resourceID = oldResources.get(iri);

            if(resourceID == null || resourceID <= maxBuiltinResourceID)
                newResources.put(iri, resourceID = nextResourceID++);
            else
                keepResources.put(iri, oldResources.remove(iri));

            return Pair.getPair(unitUncategorized, resourceID);
        }
    }


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select iri,resource_id from ontology.resources__reftable", oldResources);

        maxBuiltinResourceID = Math.max(builtinResources.values().stream().max(Integer::compare).orElse(-1).intValue(),
                127);
        nextResourceID = Math.max(oldResources.values().stream().max(Integer::compare).orElse(-1).intValue(),
                maxBuiltinResourceID) + 1;

        builtinResources.forEach((iri, id) -> {
            Integer old = oldResources.get(iri);

            if(old == null || !old.equals(id))
                newResources.put(iri, id);
            else
                keepResources.put(iri, oldResources.remove(iri));
        });
    }


    private static void loadClasses(Model model) throws IOException, SQLException
    {
        IntPairSet newClasses = new IntPairSet();
        IntPairSet oldClasses = new IntPairSet();

        load("select class_unit,class_id from ontology.classes", oldClasses);

        new OntologyQueryResultProcessor(loadQuery("ontology/classes.sparql"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> classID = getId("iri");

                if(!oldClasses.remove(classID))
                    newClasses.add(classID);
            }
        }.load(model);

        store("delete from ontology.classes where class_unit=? and class_id=?", oldClasses);
        store("insert into ontology.classes(class_unit,class_id) values(?,?)", newClasses);
    }


    private static void loadProperties(Model model) throws IOException, SQLException
    {
        IntPairSet newProperties = new IntPairSet();
        IntPairSet oldProperties = new IntPairSet();

        load("select property_unit,property_id from ontology.properties", oldProperties);

        new OntologyQueryResultProcessor(loadQuery("ontology/properties.sparql"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> propertyID = getId("iri");

                if(!oldProperties.remove(propertyID))
                    newProperties.add(propertyID);
            }
        }.load(model);

        store("delete from ontology.properties where property_unit=? and property_id=?", oldProperties);
        store("insert into ontology.properties(property_unit,property_id) values(?,?)", newProperties);
    }


    private static void loadIndividuals(Model model) throws IOException, SQLException
    {
        IntPairSet newIndividuals = new IntPairSet();
        IntPairSet oldIndividuals = new IntPairSet();

        load("select individual_unit,individual_id from ontology.individuals", oldIndividuals);

        new OntologyQueryResultProcessor(patternQuery("?iri rdf:type owl:NamedIndividual"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> individualID = getId("iri");

                if(!oldIndividuals.remove(individualID))
                    newIndividuals.add(individualID);
            }
        }.load(model);

        store("delete from ontology.individuals where individual_unit=? and individual_id=?", oldIndividuals);
        store("insert into ontology.individuals(individual_unit,individual_id) values(?,?)", newIndividuals);
    }


    private static void loadResourceLabels(Model model) throws IOException, SQLException
    {
        IntPairStringMap keepLabels = new IntPairStringMap();
        IntPairStringMap newLabels = new IntPairStringMap();
        IntPairStringMap oldLabels = new IntPairStringMap();

        load("select resource_unit,resource_id,label from ontology.resource_labels", oldLabels);

        new OntologyQueryResultProcessor(loadQuery("ontology/labels.sparql"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> resourceID = getId("iri");
                String label = getString("label");

                if(label.equals(oldLabels.remove(resourceID)))
                {
                    keepLabels.put(resourceID, label);
                }
                else
                {
                    String keep = keepLabels.get(resourceID);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLabels.put(resourceID, label);

                    if(put != null && !label.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.resource_labels where resource_unit=? and resource_id=? and label=?", oldLabels);
        store("insert into ontology.resource_labels(resource_unit,resource_id,label) values(?,?,?) "
                + "on conflict(resource_unit,resource_id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadSuperClasses(Model model) throws IOException, SQLException
    {
        IntPairIntPairSet oldSuperClasses = new IntPairIntPairSet();
        IntPairIntPairSet newSuperClasses = new IntPairIntPairSet();

        load("select class_unit,class_id,superclass_unit,superclass_id from ontology.superclasses", oldSuperClasses);

        new OntologyQueryResultProcessor(
                patternQuery("?class rdfs:subClassOf ?superclass. filter(?superclass != owl:Thing)"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> classID = getId("class");
                Pair<Integer, Integer> superclassID = getId("superclass");

                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = Pair.getPair(classID, superclassID);

                if(!oldSuperClasses.remove(pair))
                    newSuperClasses.add(pair);
            }
        }.load(model);


        new OntologyQueryResultProcessor(loadQuery("ontology/superclasses.sparql"))
        {
            Pair<Integer, Integer> thingID = Ontology
                    .getId(ResourceFactory.createResource("http://www.w3.org/2002/07/owl#Thing"));

            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> classID = getId("class");

                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = Pair.getPair(classID, thingID);

                if(!oldSuperClasses.remove(pair))
                    newSuperClasses.add(pair);
            }
        }.load(model);

        store("delete from ontology.superclasses "
                + "where class_unit=? and class_id=? and superclass_unit=? and superclass_id=?", oldSuperClasses);
        store("insert into ontology.superclasses(class_unit,class_id,superclass_unit,superclass_id) values(?,?,?,?)",
                newSuperClasses);
    }


    private static void loadSuperProperties(Model model) throws IOException, SQLException
    {
        IntPairIntPairSet oldSuperProperties = new IntPairIntPairSet();
        IntPairIntPairSet newSuperProperties = new IntPairIntPairSet();

        load("select property_unit,property_id,superproperty_unit,superproperty_id from ontology.superproperties",
                oldSuperProperties);

        new OntologyQueryResultProcessor(patternQuery("?property rdfs:subPropertyOf ?superproperty"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> propertyID = getId("property");
                Pair<Integer, Integer> superpropertyID = getId("superproperty");

                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = Pair.getPair(propertyID, superpropertyID);

                if(!oldSuperProperties.remove(pair))
                    newSuperProperties.add(pair);
            }
        }.load(model);

        store("delete from ontology.superproperties "
                + "where property_unit=? and property_id=? and superproperty_unit=? and superproperty_id=?",
                oldSuperProperties);
        store("insert into ontology.superproperties(property_unit,property_id,superproperty_unit,superproperty_id) "
                + "values(?,?,?,?)", newSuperProperties);
    }


    private static void loadDomains(Model model) throws IOException, SQLException
    {
        IntPairIntPairSet oldDomains = new IntPairIntPairSet();
        IntPairIntPairSet newDomains = new IntPairIntPairSet();

        load("select property_unit,property_id,domain_unit,domain_id from ontology.property_domains", oldDomains);

        new OntologyQueryResultProcessor(patternQuery("?property rdfs:domain ?domain"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> propertyID = getId("property");
                Pair<Integer, Integer> domainID = getId("domain");

                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = Pair.getPair(propertyID, domainID);

                if(!oldDomains.remove(pair))
                    newDomains.add(pair);
            }
        }.load(model);

        store("delete from ontology.property_domains "
                + "where property_unit=? and property_id=? and domain_unit=? and domain_id=?", oldDomains);
        store("insert into ontology.property_domains(property_unit,property_id,domain_unit,domain_id) values(?,?,?,?)",
                newDomains);
    }


    private static void loadRanges(Model model) throws IOException, SQLException
    {
        IntPairIntPairSet oldRanges = new IntPairIntPairSet();
        IntPairIntPairSet newRanges = new IntPairIntPairSet();

        load("select property_unit,property_id,range_unit,range_id from ontology.property_ranges", oldRanges);

        new OntologyQueryResultProcessor(patternQuery("?property rdfs:range ?range"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> propertyID = getId("property");
                Pair<Integer, Integer> rangeID = getId("range");

                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> pair = Pair.getPair(propertyID, rangeID);

                if(!oldRanges.remove(pair))
                    newRanges.add(pair);
            }
        }.load(model);

        store("delete from ontology.property_ranges "
                + "where property_unit=? and property_id=? and range_unit=? and range_id=?", oldRanges);
        store("insert into ontology.property_ranges(property_unit,property_id,range_unit,range_id) values(?,?,?,?)",
                newRanges);
    }


    private static void loadSomeValuesFromRestriction(Model model) throws SQLException, IOException
    {
        IntValueRestrictionMap keepRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap oldRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap newRestrictions = new IntValueRestrictionMap();

        load("select restriction_id,property_unit,property_id,class_unit,class_id "
                + "from ontology.somevaluesfrom_restrictions", oldRestrictions);

        new OntologyQueryResultProcessor(patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:someValuesFrom ?class"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> restrictionID = getId("restriction");
                Pair<Integer, Integer> propertyID = getId("property");
                Pair<Integer, Integer> classID = getId("class");

                ValueRestriction restriction = new ValueRestriction(propertyID, classID);

                if(restrictionID.getOne() != Ontology.unitBlank)
                    throw new IOException();

                if(restriction.equals(oldRestrictions.remove(restrictionID.getTwo())))
                {
                    keepRestrictions.put(restrictionID.getTwo(), restriction);
                }
                else
                {
                    ValueRestriction keep = keepRestrictions.get(restrictionID.getTwo());

                    if(restriction.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    ValueRestriction put = newRestrictions.put(restrictionID.getTwo(), restriction);

                    if(put != null && !restriction.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.somevaluesfrom_restrictions "
                + "where restriction_id=? and property_unit=? and property_id=? and class_unit=? and class_id=?",
                oldRestrictions);
        store("insert into ontology.somevaluesfrom_restrictions"
                + "(restriction_id,property_unit,property_id,class_unit,class_id) values(?,?,?,?,?)"
                + "on conflict(restriction_id) do update set property_unit=EXCLUDED.property_unit, "
                + "property_id=EXCLUDED.property_id, class_unit=EXCLUDED.class_unit, class_id=EXCLUDED.class_id",
                newRestrictions);
    }


    private static void loadAllValuesFromRestriction(Model model) throws SQLException, IOException
    {
        IntValueRestrictionMap keepRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap oldRestrictions = new IntValueRestrictionMap();
        IntValueRestrictionMap newRestrictions = new IntValueRestrictionMap();

        load("select restriction_id,property_unit,property_id,class_unit,class_id "
                + "from ontology.allvaluesfrom_restrictions", oldRestrictions);

        new OntologyQueryResultProcessor(patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:allValuesFrom ?class"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> restrictionID = getId("restriction");
                Pair<Integer, Integer> propertyID = getId("property");
                Pair<Integer, Integer> classID = getId("class");

                ValueRestriction restriction = new ValueRestriction(propertyID, classID);

                if(restrictionID.getOne() != Ontology.unitBlank)
                    throw new IOException();

                if(restriction.equals(oldRestrictions.remove(restrictionID.getTwo())))
                {
                    keepRestrictions.put(restrictionID.getTwo(), restriction);
                }
                else
                {
                    ValueRestriction keep = keepRestrictions.get(restrictionID.getTwo());

                    if(restriction.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    ValueRestriction put = newRestrictions.put(restrictionID.getTwo(), restriction);

                    if(put != null && !restriction.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.allvaluesfrom_restrictions "
                + "where restriction_id=? and property_unit=? and property_id=? and class_unit=? and class_id=?",
                oldRestrictions);
        store("insert into ontology.allvaluesfrom_restrictions "
                + "(restriction_id,property_unit,property_id,class_unit,class_id) values(?,?,?,?,?)"
                + "on conflict(restriction_id) do update set property_unit=EXCLUDED.property_unit, "
                + "property_id=EXCLUDED.property_id, class_unit=EXCLUDED.class_unit, class_id=EXCLUDED.class_id",
                newRestrictions);
    }


    private static void loadCardinalityRestriction(Model model) throws SQLException, IOException
    {
        IntCardinalityRestrictionMap keepRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap oldRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap newRestrictions = new IntCardinalityRestrictionMap();

        load("select restriction_id,property_unit,property_id,cardinality from ontology.cardinality_restrictions",
                oldRestrictions);

        new OntologyQueryResultProcessor(patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:cardinality ?cardinality"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> restrictionID = getId("restriction");
                Pair<Integer, Integer> propertyID = getId("property");
                Integer classID = getInt("cardinality");

                CardinalityRestriction restriction = new CardinalityRestriction(propertyID, classID);

                if(restrictionID.getOne() != Ontology.unitBlank)
                    throw new IOException();

                if(restriction.equals(oldRestrictions.remove(restrictionID.getTwo())))
                {
                    keepRestrictions.put(restrictionID.getTwo(), restriction);
                }
                else
                {
                    CardinalityRestriction keep = keepRestrictions.get(restrictionID.getTwo());

                    if(restriction.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    CardinalityRestriction put = newRestrictions.put(restrictionID.getTwo(), restriction);

                    if(put != null && !restriction.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.cardinality_restrictions "
                + "where restriction_id=? and property_unit=? and property_id=? and cardinality=?", oldRestrictions);
        store("insert into ontology.cardinality_restrictions (restriction_id,property_unit,property_id,cardinality) "
                + "values(?,?,?,?) on conflict(restriction_id) do update set property_unit=EXCLUDED.property_unit, "
                + "property_id=EXCLUDED.property_id, cardinality=EXCLUDED.cardinality", newRestrictions);
    }


    private static void loadMinCardinalityRestriction(Model model) throws SQLException, IOException
    {
        IntCardinalityRestrictionMap keepRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap oldRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap newRestrictions = new IntCardinalityRestrictionMap();

        load("select restriction_id,property_unit,property_id,cardinality from ontology.mincardinality_restrictions",
                oldRestrictions);

        new OntologyQueryResultProcessor(patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:minCardinality ?cardinality"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> restrictionID = getId("restriction");
                Pair<Integer, Integer> propertyID = getId("property");
                Integer classID = getInt("cardinality");

                CardinalityRestriction restriction = new CardinalityRestriction(propertyID, classID);

                if(restrictionID.getOne() != Ontology.unitBlank)
                    throw new IOException();

                if(restriction.equals(oldRestrictions.remove(restrictionID.getTwo())))
                {
                    keepRestrictions.put(restrictionID.getTwo(), restriction);
                }
                else
                {
                    CardinalityRestriction keep = keepRestrictions.get(restrictionID.getTwo());

                    if(restriction.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    CardinalityRestriction put = newRestrictions.put(restrictionID.getTwo(), restriction);

                    if(put != null && !restriction.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.mincardinality_restrictions "
                + "where restriction_id=? and property_unit=? and property_id=? and cardinality=?", oldRestrictions);
        store("insert into ontology.mincardinality_restrictions(restriction_id,property_unit,property_id,cardinality) "
                + "values(?,?,?,?) on conflict(restriction_id) do update set property_unit=EXCLUDED.property_unit, "
                + "property_id=EXCLUDED.property_id, cardinality=EXCLUDED.cardinality", newRestrictions);
    }


    private static void loadMaxCardinalityRestriction(Model model) throws SQLException, IOException
    {
        IntCardinalityRestrictionMap keepRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap oldRestrictions = new IntCardinalityRestrictionMap();
        IntCardinalityRestrictionMap newRestrictions = new IntCardinalityRestrictionMap();

        load("select restriction_id,property_unit,property_id,cardinality from ontology.maxcardinality_restrictions",
                oldRestrictions);

        new OntologyQueryResultProcessor(patternQuery(
                "?restriction rdf:type owl:Restriction; owl:onProperty ?property; owl:maxCardinality ?cardinality"))
        {
            @Override
            protected void parse() throws IOException
            {
                Pair<Integer, Integer> restrictionID = getId("restriction");
                Pair<Integer, Integer> propertyID = getId("property");
                Integer classID = getInt("cardinality");

                CardinalityRestriction restriction = new CardinalityRestriction(propertyID, classID);

                if(restrictionID.getOne() != Ontology.unitBlank)
                    throw new IOException();

                if(restriction.equals(oldRestrictions.remove(restrictionID.getTwo())))
                {
                    keepRestrictions.put(restrictionID.getTwo(), restriction);
                }
                else
                {
                    CardinalityRestriction keep = keepRestrictions.get(restrictionID.getTwo());

                    if(restriction.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    CardinalityRestriction put = newRestrictions.put(restrictionID.getTwo(), restriction);

                    if(put != null && !restriction.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from ontology.maxcardinality_restrictions "
                + "where restriction_id=? and property_unit=? and property_id=? and cardinality=?", oldRestrictions);
        store("insert into ontology.maxcardinality_restrictions(restriction_id,property_unit,property_id,cardinality) "
                + "values(?,?,?,?) on conflict(restriction_id) do update set property_unit=EXCLUDED.property_unit, "
                + "property_id=EXCLUDED.property_id, cardinality=EXCLUDED.cardinality", newRestrictions);
    }


    static void finish() throws SQLException, IOException
    {
        store("delete from ontology.resources__reftable where iri=? and resource_id=?", oldResources);
        store("insert into ontology.resources__reftable(iri,resource_id) values(?,?)", newResources);
    }


    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();
            loadCategories();

            initSourceList();

            Model model = ModelFactory.createDefaultModel();

            processFiles("ontology", ".*", file -> {
                Lang lang = file.endsWith(".ttl") ? Lang.TTL : Lang.RDFXML;
                Model submodel = getModel(file, lang);

                synchronized(model)
                {
                    model.add(submodel);
                }

                submodel.close();
            });

            System.out.println("=== load ontologies ===");

            for(Source source : sources)
                System.out.println(source.getName() + ": " + source.getVersion(model));

            loadBases(model);
            loadClasses(model);
            loadProperties(model);
            loadIndividuals(model);
            loadResourceLabels(model);

            loadSuperClasses(model);

            loadSuperProperties(model);
            loadDomains(model);
            loadRanges(model);

            loadSomeValuesFromRestriction(model);
            loadAllValuesFromRestriction(model);
            loadCardinalityRestriction(model);
            loadMinCardinalityRestriction(model);
            loadMaxCardinalityRestriction(model);

            finish();

            for(Source source : sources)
                setVersion(source.getName(), source.getVersion(model));

            model.close();
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
