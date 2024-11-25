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
    public static final int unitWormbaseGene = 244;


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

        // predicates
        builtinResources.put("http://data.epo.org/linked-data/def/patent/applicantVC", 130);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationCPCAdditional", 131);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationCPCInventive", 132);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationIPCAdditional", 133);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/classificationIPCInventive", 134);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/filingDate", 135);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/grantDate", 136);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/inventorVC", 137);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/publicationDate", 138);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/publicationNumber", 139);
        builtinResources.put("http://data.epo.org/linked-data/def/patent/titleOfInvention", 140);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#abbreviation", 141);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#active", 142);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#allowableQualifier", 143);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#altLabel", 144);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#annotation", 145);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderConcept", 146);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderDescriptor", 147);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#broaderQualifier", 148);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#casn1_label", 149);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#concept", 150);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#considerAlso", 151);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateCreated", 152);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateEstablished", 153);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#dateRevised", 154);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#entryVersion", 155);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#frequency", 156);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#hasDescriptor", 157);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#hasQualifier", 158);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#historyNote", 159);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#identifier", 160);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#indexerConsiderAlso", 161);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#lastActiveYear", 162);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#lexicalTag", 163);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#mappedTo", 164);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#narrowerConcept", 165);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#nlmClassificationNumber", 166);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#note", 167);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#onlineNote", 168);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#parentTreeNumber", 169);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#pharmacologicalAction", 170);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredConcept", 171);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredMappedTo", 172);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#preferredTerm", 173);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#prefLabel", 174);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#previousIndexing", 175);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#publicMeSHNote", 176);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#registryNumber", 177);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#relatedConcept", 178);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#relatedRegistryNumber", 179);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#scopeNote", 180);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#seeAlso", 181);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#sortVersion", 182);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#source", 183);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#term", 184);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#thesaurusID", 185);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#treeNumber", 186);
        builtinResources.put("http://id.nlm.nih.gov/mesh/vocab#useInstead", 187);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/contentType", 188);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/eissn", 189);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/endingPage", 190);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/isbn", 191);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/issn", 192);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/issueIdentifier", 193);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/location", 194);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/pageRange", 195);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/publicationName", 196);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/startingPage", 197);
        builtinResources.put("http://prismstandard.org/namespaces/basic/3.0/subtitle", 198);
        builtinResources.put("http://purl.obolibrary.org/obo/has-role", 199);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/formula", 200);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/charge", 201);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/inchi", 202);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/inchikey", 203);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/mass", 204);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/monoisotopicmass", 205);
        builtinResources.put("http://purl.obolibrary.org/obo/chebi/smiles", 206);
        builtinResources.put("http://purl.org/cerif/frapo/hasFundingAgency", 207);
        builtinResources.put("http://purl.org/cerif/frapo/hasGrantNumber", 208);
        builtinResources.put("http://purl.org/cerif/frapo/isSupportedBy", 209);
        builtinResources.put("http://purl.org/dc/elements/1.1/identifier", 210);
        builtinResources.put("http://purl.org/dc/terms/abstract", 211);
        builtinResources.put("http://purl.org/dc/terms/alternative", 212);
        builtinResources.put("http://purl.org/dc/terms/available", 213);
        builtinResources.put("http://purl.org/dc/terms/bibliographicCitation", 214);
        builtinResources.put("http://purl.org/dc/terms/created", 215);
        builtinResources.put("http://purl.org/dc/terms/creator", 216);
        builtinResources.put("http://purl.org/dc/terms/date", 217);
        builtinResources.put("http://purl.org/dc/terms/dateAccepted", 218);
        builtinResources.put("http://purl.org/dc/terms/description", 219);
        builtinResources.put("http://purl.org/dc/terms/identifier", 220);
        builtinResources.put("http://purl.org/dc/terms/isPartOf", 221);
        builtinResources.put("http://purl.org/dc/terms/issued", 222);
        builtinResources.put("http://purl.org/dc/terms/language", 223);
        builtinResources.put("http://purl.org/dc/terms/license", 224);
        builtinResources.put("http://purl.org/dc/terms/modified", 225);
        builtinResources.put("http://purl.org/dc/terms/publisher", 226);
        builtinResources.put("http://purl.org/dc/terms/rights", 227);
        builtinResources.put("http://purl.org/dc/terms/source", 228);
        builtinResources.put("http://purl.org/dc/terms/subject", 229);
        builtinResources.put("http://purl.org/dc/terms/title", 230);
        builtinResources.put("http://purl.org/ontology/bibo/doi", 231);
        builtinResources.put("http://purl.org/ontology/bibo/eissn", 232);
        builtinResources.put("http://purl.org/ontology/bibo/issn", 233);
        builtinResources.put("http://purl.org/ontology/bibo/issue", 234);
        builtinResources.put("http://purl.org/ontology/bibo/pageEnd", 235);
        builtinResources.put("http://purl.org/ontology/bibo/pageStart", 236);
        builtinResources.put("http://purl.org/ontology/bibo/pmid", 237);
        builtinResources.put("http://purl.org/ontology/bibo/shortTitle", 238);
        builtinResources.put("http://purl.org/ontology/bibo/volume", 239);
        builtinResources.put("http://purl.org/pav/importedFrom", 240);
        builtinResources.put("http://purl.org/spar/cito/citesAsDataSource", 241);
        builtinResources.put("http://purl.org/spar/cito/discusses", 242);
        builtinResources.put("http://purl.org/spar/cito/isCitedBy", 243);
        builtinResources.put("http://purl.org/spar/cito/isDiscussedBy", 244);
        builtinResources.put("http://purl.org/spar/fabio/hasNationalLibraryOfMedicineJournalId", 245);
        builtinResources.put("http://purl.org/spar/fabio/hasNLMJournalTitleAbbreviation", 246);
        builtinResources.put("http://purl.org/spar/fabio/hasPrimarySubjectTerm", 247);
        builtinResources.put("http://purl.org/spar/fabio/hasSubjectTerm", 248);
        builtinResources.put("http://purl.uniprot.org/core/encodedBy", 249);
        builtinResources.put("http://purl.uniprot.org/core/enzyme", 250);
        builtinResources.put("http://purl.uniprot.org/core/organism", 251);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#activityComment", 252);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayCategory", 253);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayCellType", 254);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayStrain", 255);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assaySubCellFrac", 256);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayTestType", 257);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayTissue", 258);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayType", 259);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#assayXref", 260);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#atcClassification", 261);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#bindingSiteName", 262);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#cellosaurusId", 263);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#cellXref", 264);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#classLevel", 265);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#classPath", 266);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#componentType", 267);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#dataValidityComment", 268);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#dataValidityIssue", 269);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#documentType", 270);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#fracClassification", 271);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasActivity", 272);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasAssay", 273);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasBindingSite", 274);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasBioComponent", 275);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasCellLine", 276);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasCLO", 277);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasDocument", 278);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasDrugIndication", 279);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasEFO", 280);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasEFOName", 281);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasChildMolecule", 282);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasJournal", 283);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMechanism", 284);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMesh", 285);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMeshHeading", 286);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasMolecule", 287);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasParentMolecule", 288);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasProteinClassification", 289);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasQUDT", 290);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasSource", 291);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTarget", 292);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetComponent", 293);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetComponentDescendant", 294);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasTargetDescendant", 295);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hasUnitOnto", 296);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#helmNotation", 297);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#highestDevelopmentPhase", 298);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#hracClassification", 299);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#chemblId", 300);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#iracClassification", 301);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isBindingSiteForMechanism", 302);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isBiotherapeutic", 303);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isCellLineForAssay", 304);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isCellLineForTarget", 305);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isSpeciesGroup", 306);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isTargetForCellLine", 307);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#isTargetForMechanism", 308);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#mechanismActionType", 309);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#mechanismDescription", 310);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#moleculeXref", 311);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#organismName", 312);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#pChembl", 313);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#potentialDuplicate", 314);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#proteinSequence", 315);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relation", 316);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relEquivalentTo", 317);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relHasSubset", 318);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relOverlapsWith", 319);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#relSubsetOf", 320);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardRelation", 321);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardType", 322);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardUnits", 323);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#standardValue", 324);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#substanceType", 325);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetCmptXref", 326);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetConfDesc", 327);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetConfScore", 328);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetRelDesc", 329);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetRelType", 330);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#targetType", 331);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#taxonomy", 332);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#type", 333);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#units", 334);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#value", 335);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#discussesAsDerivedByTextMining", 336);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent", 337);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#hasSimilarProtein", 338);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of", 339);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#priorityDate", 340);
        builtinResources.put("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#PubChemAssayOutcome", 341);
        builtinResources.put("http://rdf.wwpdb.org/schema/pdbx-v40.owl#link_to_pdb", 342);
        builtinResources.put("http://rdf.wwpdb.org/schema/pdbx-v50.owl#link_to_pdb", 343);
        builtinResources.put("http://semanticscience.org/resource/gene-symbol", 344);
        builtinResources.put("http://semanticscience.org/resource/has-attribute", 345);
        builtinResources.put("http://semanticscience.org/resource/has-unit", 346);
        builtinResources.put("http://semanticscience.org/resource/has-value", 347);
        builtinResources.put("http://semanticscience.org/resource/is-attribute-of", 348);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#organism", 349);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#pathwayComponent", 350);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasAlternativeId", 351);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasOBONamespace", 352);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#hasSynonymType", 353);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#id", 354);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#inSubset", 355);
        builtinResources.put("http://www.geneontology.org/formats/oboInOwl#source", 356);
        builtinResources.put("http://www.wikidata.org/prop/direct/P2017", 357);
        builtinResources.put("http://www.wikidata.org/prop/direct/P233", 358);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultDataset", 359);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime", 360);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#defaultGraph", 361);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#endpoint", 362);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#entailmentRegime", 363);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#extensionFunction", 364);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#feature", 365);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#graph", 366);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#namedGraph", 367);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#propertyFeature", 368);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#resultFormat", 369);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#supportedLanguage", 370);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#name", 371);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#object", 372);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject", 373);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 374);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#domain", 375);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#label", 376);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#range", 377);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#subClassOf", 378);
        builtinResources.put("http://www.w3.org/2000/01/rdf-schema#subPropertyOf", 379);
        builtinResources.put("http://www.w3.org/2002/07/owl#allValuesFrom", 380);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedProperty", 381);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedSource", 382);
        builtinResources.put("http://www.w3.org/2002/07/owl#annotatedTarget", 383);
        builtinResources.put("http://www.w3.org/2002/07/owl#cardinality", 384);
        builtinResources.put("http://www.w3.org/2002/07/owl#deprecated", 385);
        builtinResources.put("http://www.w3.org/2002/07/owl#maxCardinality", 386);
        builtinResources.put("http://www.w3.org/2002/07/owl#minCardinality", 387);
        builtinResources.put("http://www.w3.org/2002/07/owl#onProperty", 388);
        builtinResources.put("http://www.w3.org/2002/07/owl#sameAs", 389);
        builtinResources.put("http://www.w3.org/2002/07/owl#someValuesFrom", 390);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#altLabel", 391);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#broader", 392);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#closeMatch", 393);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#exactMatch", 394);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#inScheme", 395);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#narrower", 396);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#prefLabel", 397);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#related", 398);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#relatedMatch", 399);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#sameAs", 400);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#country-name", 401);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#family-name", 402);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#fn", 403);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#given-name", 404);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#hasEmail", 405);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#hasUID", 406);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#organization-name", 407);
        builtinResources.put("http://xmlns.com/foaf/0.1/depiction", 408);
        builtinResources.put("http://xmlns.com/foaf/0.1/homepage", 409);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#name", 410);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#availableGraphs", 411);
        builtinResources.put("http://rdfs.org/ns/void#triples", 412);
        builtinResources.put("http://rdfs.org/ns/void#classes", 413);
        builtinResources.put("http://rdfs.org/ns/void#properties", 414);
        builtinResources.put("http://rdfs.org/ns/void#distinctSubjects", 415);
        builtinResources.put("http://rdfs.org/ns/void#distinctObjects", 416);
        builtinResources.put("http://rdfs.org/ns/void#subset", 417);
        builtinResources.put("http://rdfs.org/ns/void#classPartition", 418);
        builtinResources.put("http://rdfs.org/ns/void#class", 419);
        builtinResources.put("http://rdfs.org/ns/void#propertyPartition", 420);
        builtinResources.put("http://rdfs.org/ns/void#property", 421);
        builtinResources.put("http://rdfs.org/ns/void#target", 422);
        builtinResources.put("http://rdfs.org/ns/void#subjectsTarget", 423);
        builtinResources.put("http://rdfs.org/ns/void#objectsTarget", 424);

        // classes
        builtinResources.put("http://data.epo.org/linked-data/def/patent/Publication", 512);
        builtinResources.put("http://purl.org/cerif/frapo/FundingAgency", 513);
        builtinResources.put("http://purl.org/cerif/frapo/Grant", 514);
        builtinResources.put("http://purl.org/dc/terms/Dataset", 515);
        builtinResources.put("http://purl.org/spar/fabio/Book", 516);
        builtinResources.put("http://purl.org/spar/fabio/Journal", 517);
        builtinResources.put("http://purl.uniprot.org/core/Enzyme", 518);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Activity", 519);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ActorRef", 520);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ADMET", 521);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Antibody", 522);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Assay", 523);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#AtlasRef", 524);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#BindingSite", 525);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#BioComponent", 526);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellLine", 527);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellLineTarget", 528);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CellTherapy", 529);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#CGDRef", 530);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Document", 531);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#DrugbankRef", 532);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#DrugIndication", 533);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#EmoleculesRef", 534);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Enzyme", 535);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#EnzymeClassRef", 536);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#FdaSrsRef", 537);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoComponentRef", 538);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoFunctionRef", 539);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#GoProcessRef", 540);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#HmdbRef", 541);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ChebiRef", 542);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ChimericProtein", 543);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#IntactRef", 544);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#InterproRef", 545);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#IupharRef", 546);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Journal", 547);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#KeggLigandRef", 548);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#LincsCellRef", 549);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#LincsRef", 550);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Macromolecule", 551);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#MculeRef", 552);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Mechanism", 553);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Metal", 554);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NikkajiRef", 555);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NmrShiftDb2Ref", 556);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NonMolecular", 557);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#NucleicAcid", 558);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Oligonucleotide", 559);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Oligosaccharide", 560);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#OligosaccharideTarget", 561);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Organism", 562);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PdbeRef", 563);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PfamRef", 564);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PharmgkbRef", 565);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PharmGkbRef", 566);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Phenotype", 567);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinClassification", 568);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinComplex", 569);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinComplexGroup", 570);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinDataBankRef", 571);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinFamily", 572);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinMolecule", 573);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinNucleicAcidComplex", 574);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinProteinInteraction", 575);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ProteinSelectivityGroup", 576);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemBioassayRef", 577);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemDotfRef", 578);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemRef", 579);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#PubchemThomPharmRef", 580);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ReactomeRef", 581);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ReconRef", 582);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SelleckRef", 583);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SingleProtein", 584);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SmallMolecule", 585);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SmallMoleculeTarget", 586);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Source", 587);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SubCellular", 588);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Substance", 589);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#SureChemblRef", 590);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Target", 591);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#TargetComponent", 592);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#TimbalRef", 593);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#Tissue", 594);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedMolecule", 595);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnclassifiedTarget", 596);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UniprotRef", 597);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnknownSubstance", 598);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#UnknownTarget", 599);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#WikipediaMolRef", 600);
        builtinResources.put("http://rdf.ebi.ac.uk/terms/chembl#ZincRef", 601);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Gene", 602);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Pathway", 603);
        builtinResources.put("http://www.biopax.org/release/biopax-level3.owl#Protein", 604);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Dataset", 605);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Feature", 606);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Function", 607);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Graph", 608);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#NamedGraph", 609);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#Service", 610);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property", 611);
        builtinResources.put("http://www.w3.org/2002/07/owl#Axiom", 612);
        builtinResources.put("http://www.w3.org/2002/07/owl#Class", 613);
        builtinResources.put("http://www.w3.org/2002/07/owl#NamedIndividual", 614);
        builtinResources.put("http://www.w3.org/2002/07/owl#Restriction", 615);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#Concept", 616);
        builtinResources.put("http://www.w3.org/2004/02/skos/core#ConceptScheme", 617);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#Individual", 618);
        builtinResources.put("http://www.w3.org/2006/vcard/ns#Organization", 619);
        builtinResources.put("http://xmlns.com/foaf/0.1/Image", 620);
        builtinResources.put("http://www.w3.org/ns/sparql-service-description#GraphCollection", 621);
        builtinResources.put("http://rdfs.org/ns/void#Dataset", 622);

        // datatypes
        builtinResources.put("http://www.w3.org/2001/XMLSchema#boolean", 900);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#short", 901);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#int", 902);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#long", 903);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#integer", 904);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#decimal", 905);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#float", 906);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#double", 907);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#date", 908);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#dateTime", 909);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#dayTimeDuration", 910);
        builtinResources.put("http://www.w3.org/2001/XMLSchema#string", 911);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", 912);
        builtinResources.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML", 913);
        builtinResources.put("http://bioinfo.uochb.cas.cz/rdf/v1.0/ms#spectrum", 914);
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

                else if(unit.id == unitWormbaseGene)
                {
                    id = Integer.parseInt(tail.substring(0, 8));
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
                999);
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
