package cz.iocb.chemweb.server.sparql.config.chembl;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.mesh.Mesh;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.config.pubchem.Bioassay;
import cz.iocb.chemweb.server.sparql.config.pubchem.Compound;
import cz.iocb.chemweb.server.sparql.config.pubchem.Reference;
import cz.iocb.chemweb.server.sparql.config.pubchem.Substance;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class ChemblConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "chembl";


    public ChemblConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("ebi", "http://rdf.ebi.ac.uk/dataset/");

        addPrefix("bao", "http://www.bioassayontology.org/bao#");
        addPrefix("bibo", "http://purl.org/ontology/bibo/");
        addPrefix("cco", "http://rdf.ebi.ac.uk/terms/chembl#");
        addPrefix("chembl", "http://rdf.ebi.ac.uk/resource/chembl/");
        addPrefix("chembl_activity", "http://rdf.ebi.ac.uk/resource/chembl/activity/");
        addPrefix("chembl_assay", "http://rdf.ebi.ac.uk/resource/chembl/assay/");
        addPrefix("chembl_binding_site", "http://rdf.ebi.ac.uk/resource/chembl/binding_site/");
        addPrefix("chembl_bio_cmpt", "http://rdf.ebi.ac.uk/resource/chembl/biocomponent/");
        addPrefix("chembl_cell_line", "http://rdf.ebi.ac.uk/resource/chembl/cell_line/");
        addPrefix("chembl_document", "http://rdf.ebi.ac.uk/resource/chembl/document/");
        addPrefix("chembl_indication", "http://rdf.ebi.ac.uk/resource/chembl/drug_indication/");
        addPrefix("chembl_journal", "http://rdf.ebi.ac.uk/resource/chembl/journal/");
        addPrefix("chembl_moa", "http://rdf.ebi.ac.uk/resource/chembl/drug_mechanism/");
        addPrefix("chembl_molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/");
        addPrefix("chembl_protclass", "http://rdf.ebi.ac.uk/resource/chembl/protclass/");
        addPrefix("chembl_source", "http://rdf.ebi.ac.uk/resource/chembl/source/");
        addPrefix("chembl_target", "http://rdf.ebi.ac.uk/resource/chembl/target/");
        addPrefix("chembl_target_cmpt", "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/");
        addPrefix("cheminf", "http://semanticscience.org/resource/");
        addPrefix("cito", "http://purl.org/spar/cito/");
        addPrefix("dc", "http://purl.org/dc/elements/1.1/");
        addPrefix("dcterms", "http://purl.org/dc/terms/");
        addPrefix("doi", "http://dx.doi.org/");
        addPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        addPrefix("freq", "http://purl.org/cld/freq/");
        addPrefix("mio", "http://www.ebi.ac.uk/ontology-lookup/?termId=");
        addPrefix("oborel", "http://purl.obolibrary.org/obo#");
        addPrefix("ops", "http://www.openphacts.org/units/");
        addPrefix("pav", "http://purl.org/pav/");
        addPrefix("qudt", "http://qudt.org/vocab/unit#");
        addPrefix("sd", "http://www.w3.org/ns/sparql-service-description#");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
        addPrefix("uniprot", "http://purl.uniprot.org/uniprot/");
        addPrefix("voag", "http://voag.linkedmodel.org/voag#");
        addPrefix("void", "http://rdfs.org/ns/void#");
        //addPrefix("uo", "http://purl.obolibrary.org/obo/");

        // extension
        addPrefix("pdbo", "http://rdf.wwpdb.org/schema/pdbx-v50.owl#");
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Common.addResourceClasses(this);
        Ontology.addResourceClasses(this);
        Mesh.addResourceClasses(this);
        Compound.addResourceClasses(this);
        Substance.addResourceClasses(this);
        Bioassay.addResourceClasses(this);
        Reference.addResourceClasses(this);

        Activity.addResourceClasses(this);
        Assay.addResourceClasses(this);
        BindingSite.addResourceClasses(this);
        BioComponent.addResourceClasses(this);
        CellLine.addResourceClasses(this);
        Document.addResourceClasses(this);
        DrugIndication.addResourceClasses(this);
        Journal.addResourceClasses(this);
        Mechanism.addResourceClasses(this);
        Molecule.addResourceClasses(this);
        MoleculeReference.addResourceClasses(this);
        ProteinClassification.addResourceClasses(this);
        Source.addResourceClasses(this);
        TargetComponent.addResourceClasses(this);
        TargetComponentReference.addResourceClasses(this);
        Target.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Activity.addQuadMappings(this);
        Assay.addQuadMappings(this);
        BindingSite.addQuadMappings(this);
        BioComponent.addQuadMappings(this);
        CellLine.addQuadMappings(this);
        Document.addQuadMappings(this);
        DrugIndication.addQuadMappings(this);
        Journal.addQuadMappings(this);
        Mechanism.addQuadMappings(this);
        Molecule.addQuadMappings(this);
        MoleculeReference.addQuadMappings(this);
        ProteinClassification.addQuadMappings(this);
        Source.addQuadMappings(this);
        TargetComponent.addQuadMappings(this);
        TargetComponentReference.addQuadMappings(this);
        Target.addQuadMappings(this);
        Taxonomy.addQuadMappings(this);
    }
}
