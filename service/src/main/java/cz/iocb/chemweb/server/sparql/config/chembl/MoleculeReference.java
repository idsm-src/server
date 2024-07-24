package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;



public class MoleculeReference
{
    private static String moleculeReferenceType = schema + ".molecule_reference_type";


    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            Table table = new Table(schema, "molecule_pubchem_references");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("ncbi:pubchem-compound", "compound_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-compound", "compound_id"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PubChem Reference: ' || compound_id)"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-compound", "compound_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemRef"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("pubchem:compound", "compound_id"));
        }

        {
            Table table = new Table(schema, "molecule_pubchem_thom_pharm_references");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("ncbi:pubchem-substance", "substance_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-substance", "substance_id"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PubChem Thomson Pharma Subset Reference: ' || substance_id)"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-substance", "substance_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemThomPharmRef"));
        }

        {
            Table table = new Table(schema, "molecule_pubchem_dotf_references");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("ncbi:pubchem-substance", "substance_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-substance", "substance_id"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PubChem Drugs of the Future Subset Reference: ' || substance_id)"));
            config.addQuadMapping(table, graph, config.createIriMapping("ncbi:pubchem-substance", "substance_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemDotfRef"));
        }

        {
            Table table = new Table(schema, "molecule_chebi_references");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:chebi", "chebi_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "chebi_id"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChEBI Reference: ' || chebi_id)"));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "chebi_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ChebiRef"));

            // extension
            /*
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi_id"));
            */
        }

        {
            Table table = new Table(schema, "molecule_references");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:zinc", "reference"),
                    config.createAreEqualCondition("reference_type", "'ZINC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:zinc", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Zinc Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'ZINC'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:surechembl", "reference"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' SureChEMBL Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:emolecules", "reference"),
                    config.createAreEqualCondition("reference_type", "'EMOLECULES'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:emolecules", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' eMolecules Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'EMOLECULES'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:mcule", "reference"),
                    config.createAreEqualCondition("reference_type", "'MCULE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:mcule", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Mcule Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'MCULE'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:nikkaji", "reference"),
                    config.createAreEqualCondition("reference_type", "'NIKKAJI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nikkaji", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Nikkaji Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'NIKKAJI'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:actor", "reference"),
                    config.createAreEqualCondition("reference_type", "'ACTOR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:actor", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ACToR Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'ACTOR'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pdbe", "reference"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PDBe Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' NMRShiftDB Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:kegg", "reference"),
                    config.createAreEqualCondition("reference_type", "'KEGG LIGAND'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:kegg", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Kegg Ligand Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'KEGG LIGAND'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:drugbank", "reference"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' DrugBank Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:hmdb", "reference"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' The Human Metabolome Database Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:iuphar", "reference"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' IUPHAR Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:selleck", "reference"),
                    config.createAreEqualCondition("reference_type", "'SELLECK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:selleck", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Selleck Chemicals Reference: ' || " + schema
                                    + ".url_decode(reference))"),
                    config.createAreEqualCondition("reference_type", "'SELLECK'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pharmgkb-drug", "reference"),
                    config.createAreEqualCondition("reference_type", "'PHARM GKB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pharmgkb-drug", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PharmGKB Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'PHARM GKB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:expression_atlas", "reference"),
                    config.createAreEqualCondition("reference_type", "'ATLAS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:expression_atlas", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Gene Expression Atlas Reference: ' ||  " + schema
                                    + ".url_decode(reference))"),
                    config.createAreEqualCondition("reference_type", "'ATLAS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:recon", "reference"),
                    config.createAreEqualCondition("reference_type", "'RECON'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:recon", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Recon Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'RECON'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:wikipedia", "reference"),
                    config.createAreEqualCondition("reference_type", "'WIKIPEDIA MOL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:wikipedia", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Wikipedia Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'WIKIPEDIA MOL'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("identifiers:lincs.smallmolecule", "reference"),
                    config.createAreEqualCondition("reference_type", "'LINCS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:lincs.smallmolecule", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Library of Integrated Network-based Cellular Signatures Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'LINCS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:fda_srs", "reference"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fda_srs", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' FDA/USP SRS Reference: ' || reference)"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));
        }

        {
            Table table = new Table(schema, "molecule_reference_types");

            config.addQuadMapping(table, graph, config.createIriMapping("reference:zinc", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ZincRef"),
                    config.createAreEqualCondition("reference_type", "'ZINC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SureChemblRef"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:emolecules", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:EmoleculesRef"),
                    config.createAreEqualCondition("reference_type", "'EMOLECULES'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:mcule", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:MculeRef"),
                    config.createAreEqualCondition("reference_type", "'MCULE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nikkaji", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:NikkajiRef"),
                    config.createAreEqualCondition("reference_type", "'NIKKAJI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:actor", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ActorRef"),
                    config.createAreEqualCondition("reference_type", "'ACTOR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PdbeRef"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:NmrShiftDb2Ref"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:kegg", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:KeggLigandRef"),
                    config.createAreEqualCondition("reference_type", "'KEGG LIGAND'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:DrugbankRef"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:HmdbRef"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:IupharRef"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:selleck", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SelleckRef"),
                    config.createAreEqualCondition("reference_type", "'SELLECK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pharmgkb-drug", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PharmGkbRef"),
                    config.createAreEqualCondition("reference_type", "'PHARM GKB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:expression_atlas", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:AtlasRef"),
                    config.createAreEqualCondition("reference_type", "'ATLAS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:recon", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ReconRef"),
                    config.createAreEqualCondition("reference_type", "'RECON'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:wikipedia", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:WikipediaMolRef"),
                    config.createAreEqualCondition("reference_type", "'WIKIPEDIA MOL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("identifiers:lincs.smallmolecule", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:LincsRef"),
                    config.createAreEqualCondition("reference_type", "'LINCS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fda_srs", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:FdaSrsRef"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));
        }
    }
}
