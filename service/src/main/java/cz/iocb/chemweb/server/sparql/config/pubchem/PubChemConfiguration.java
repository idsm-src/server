package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.chebi.Chebi;
import cz.iocb.chemweb.server.sparql.config.chembl.Assay;
import cz.iocb.chemweb.server.sparql.config.chembl.Mechanism;
import cz.iocb.chemweb.server.sparql.config.chembl.Molecule;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.mesh.Mesh;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Function;
import cz.iocb.sparql.engine.mapping.classes.DateConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.LangStringConstantTagClass;
import cz.iocb.sparql.engine.mapping.classes.UserIriClass;
import cz.iocb.sparql.engine.mapping.extension.ParameterDefinition;
import cz.iocb.sparql.engine.mapping.extension.ProcedureDefinition;
import cz.iocb.sparql.engine.mapping.extension.ResultDefinition;



public class PubChemConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "pubchem";

    public static final LangStringConstantTagClass rdfLangStringEn = LangStringConstantTagClass.get("en");
    public static final DateConstantZoneClass xsdDateM4 = DateConstantZoneClass.get(-4 * 60 * 60);


    public PubChemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addFunctions();
        addProcedures();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        // PubChem
        addPrefix("anatomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/");
        addPrefix("author", "http://rdf.ncbi.nlm.nih.gov/pubchem/author/");
        addPrefix("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/");
        addPrefix("book", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/");
        addPrefix("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/");
        addPrefix("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/");
        addPrefix("concept", "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/");
        addPrefix("conserveddomain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/");
        addPrefix("cooccurrence", "http://rdf.ncbi.nlm.nih.gov/pubchem/cooccurrence/");
        addPrefix("descriptor", "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/");
        addPrefix("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/");
        addPrefix("endpoint", "http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/");
        addPrefix("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/");
        addPrefix("grant", "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/");
        addPrefix("inchikey", "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");
        addPrefix("journal", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/");
        addPrefix("measuregroup", "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/");
        addPrefix("organization", "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/");
        addPrefix("patent", "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/");
        addPrefix("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/");
        addPrefix("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/");
        addPrefix("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/");
        addPrefix("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/");
        addPrefix("substance", "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/");
        addPrefix("synonym", "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/");
        addPrefix("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/");
        addPrefix("vocab", "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#");
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("sio", "http://semanticscience.org/resource/");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        addPrefix("bao", "http://www.bioassayontology.org/bao#");
        addPrefix("bp", "http://www.biopax.org/release/biopax-level3.owl#");
        addPrefix("ndfrt", "http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#");
        addPrefix("ncit", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#");
        addPrefix("wikidata", "http://www.wikidata.org/entity/");
        addPrefix("ops", "http://www.openphacts.org/units/");
        addPrefix("cito", "http://purl.org/spar/cito/");
        addPrefix("fabio", "http://purl.org/spar/fabio/");
        addPrefix("uniprot", "http://purl.uniprot.org/uniprot/");
        addPrefix("up", "http://purl.uniprot.org/core/");
        addPrefix("pdbo", "http://rdf.wwpdb.org/schema/pdbx-v50.owl#");
        addPrefix("pdbo40", "http://rdf.wwpdb.org/schema/pdbx-v40.owl#");
        addPrefix("pdbr", "http://rdf.wwpdb.org/pdb/");
        //addPrefix("taxonomy", "http://identifiers.org/taxonomy/");
        addPrefix("reactome", "http://identifiers.org/reactome/");
        //addPrefix("chembl", "http://rdf.ebi.ac.uk/resource/chembl/molecule/");
        addPrefix("chemblchembl", "http://linkedchemistry.info/chembl/chemblid/");
        addPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        addPrefix("void", "http://rdfs.org/ns/void#");
        addPrefix("dcterms", "http://purl.org/dc/terms/");

        // PubChem extension
        addPrefix("pubchem", "http://rdf.ncbi.nlm.nih.gov/pubchem/");
        addPrefix("ensembl", "http://rdf.ebi.ac.uk/resource/ensembl/");
        addPrefix("cheminf", "http://semanticscience.org/resource/");
        addPrefix("vcard", "http://www.w3.org/2006/vcard/ns#");
        addPrefix("prism", "http://prismstandard.org/namespaces/basic/3.0/");
        addPrefix("frapo", "http://purl.org/cerif/frapo/");
        addPrefix("epo", "http://data.epo.org/linked-data/def/patent/");
        addPrefix("edam", "http://edamontology.org/");

        // ChEMBL
        addPrefix("bibo", "http://purl.org/ontology/bibo/");
        addPrefix("cco", "http://rdf.ebi.ac.uk/terms/chembl#");
        addPrefix("chembl", "http://rdf.ebi.ac.uk/resource/chembl/");

        // ChEBI
        addPrefix("chebi", "http://purl.obolibrary.org/obo/chebi/");
        addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");

        // MeSH
        addPrefix("mesh", "http://id.nlm.nih.gov/mesh/");
        addPrefix("meshv", "http://id.nlm.nih.gov/mesh/vocab#");

        // extensions
        addPrefix("sachem", "http://bioinfo.uochb.cas.cz/rdf/v1.0/sachem#");
        addPrefix("template", "http://bioinfo.iocb.cz/0.9/template#");
        addPrefix("fulltext", "http://bioinfo.uochb.cas.cz/rdf/v1.0/fulltext#");
    }


    private void addResourceClasses() throws SQLException
    {
        Common.addResourceClasses(this);
        Chebi.addResourceClasses(this);
        Molecule.addResourceClasses(this);
        Mesh.addResourceClasses(this);
        Ontology.addResourceClasses(this);
        Assay.addResourceClasses(this);
        Mechanism.addResourceClasses(this);

        Anatomy.addResourceClasses(this);
        Author.addResourceClasses(this);
        Bioassay.addResourceClasses(this);
        Book.addResourceClasses(this);
        Cell.addResourceClasses(this);
        Compound.addResourceClasses(this);
        CompoundDescriptor.addResourceClasses(this);
        Concept.addResourceClasses(this);
        ConservedDomain.addResourceClasses(this);
        Cooccurrence.addResourceClasses(this);
        Disease.addResourceClasses(this);
        Endpoint.addResourceClasses(this);
        Gene.addResourceClasses(this);
        Grant.addResourceClasses(this);
        InchiKey.addResourceClasses(this);
        Journal.addResourceClasses(this);
        Measuregroup.addResourceClasses(this);
        Organization.addResourceClasses(this);
        Patent.addResourceClasses(this);
        Pathway.addResourceClasses(this);
        Protein.addResourceClasses(this);
        Reference.addResourceClasses(this);
        Source.addResourceClasses(this);
        Substance.addResourceClasses(this);
        SubstanceDescriptor.addResourceClasses(this);
        Synonym.addResourceClasses(this);
        Taxonomy.addResourceClasses(this);

        Sachem.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Anatomy.addQuadMappings(this);
        Author.addQuadMappings(this);
        Bioassay.addQuadMappings(this);
        Book.addQuadMappings(this);
        Cell.addQuadMappings(this);
        Compound.addQuadMappings(this);
        CompoundDescriptor.addQuadMappings(this);
        Concept.addQuadMappings(this);
        ConservedDomain.addQuadMappings(this);
        Cooccurrence.addQuadMappings(this);
        Disease.addQuadMappings(this);
        Endpoint.addQuadMappings(this);
        Gene.addQuadMappings(this);
        Grant.addQuadMappings(this);
        InchiKey.addQuadMappings(this);
        Journal.addQuadMappings(this);
        Measuregroup.addQuadMappings(this);
        Organization.addQuadMappings(this);
        Patent.addQuadMappings(this);
        Pathway.addQuadMappings(this);
        Protein.addQuadMappings(this);
        Reference.addQuadMappings(this);
        Source.addQuadMappings(this);
        Substance.addQuadMappings(this);
        SubstanceDescriptor.addQuadMappings(this);
        Synonym.addQuadMappings(this);
        Taxonomy.addQuadMappings(this);
    }


    private void addFunctions()
    {
        Common.addFunctions(this);
    }


    private void addProcedures()
    {
        String fulltext = getPrefixes().get("fulltext");
        UserIriClass compound = getIriClass("pubchem:compound");


        /* fulltext:bioassaySearch */
        ProcedureDefinition bioassay = new ProcedureDefinition(fulltext + "bioassaySearch",
                new Function(schema, "bioassay"));
        bioassay.addParameter(new ParameterDefinition(fulltext + "query", xsdString, null));
        bioassay.addResult(new ResultDefinition(getIriClass("pubchem:bioassay")));
        addProcedure(bioassay);


        /* fulltext:compoundSearch */
        ProcedureDefinition compoundSearch = new ProcedureDefinition(fulltext + "compoundSearch",
                new Function(schema, "compound_fulltext"));
        compoundSearch.addParameter(new ParameterDefinition(fulltext + "query", xsdString, null));
        compoundSearch.addResult(new ResultDefinition(fulltext + "compound", compound, "compound"));
        compoundSearch.addResult(new ResultDefinition(fulltext + "score", xsdFloat, "score"));
        compoundSearch.addResult(new ResultDefinition(fulltext + "name", rdfLangStringEn, "name"));
        addProcedure(compoundSearch);
    }
}
