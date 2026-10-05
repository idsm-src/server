package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;



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
            DatabaseTable table = new DatabaseTable(schema, "molecule_pubchem_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("pubchem:compound", "compound_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_chebi_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:chebi", "chebi_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:bindingdb", "reference"),
                    config.createAreEqualCondition("reference_type", "'BINDING DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:bindingdb", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' BindingDB Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'BINDING DB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:brenda", "reference"),
                    config.createAreEqualCondition("reference_type", "'BRENDA'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:brenda", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Brenda Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'BRENDA'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:ccdc", "reference"),
                    config.createAreEqualCondition("reference_type", "'CCDC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:ccdc", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' CCDC Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'CCDC'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:chebi", "reference"),
                    config.createAreEqualCondition("reference_type", "'CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChEBI Reference: CHEBI:' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'CHEBI'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createAreEqualCondition("reference_type", "'CLINICAL TRIALS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ClinicalTrials.gov Reference: NCT' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'CLINICAL TRIALS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:comptox", "reference"),
                    config.createAreEqualCondition("reference_type", "'COMPTOX'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:comptox", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' EPA CompTox Dashboard Reference: DTXSID' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'COMPTOX'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:drugcentral", "reference"),
                    config.createAreEqualCondition("reference_type", "'DRUG CENTRAL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugcentral", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' DrugCentral Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'DRUG CENTRAL'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:drugbank", "reference"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' DrugBank Reference: DB' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:fdasrs", "reference"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fdasrs", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' FDA/USP SRS Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:foodb", "reference"),
                    config.createAreEqualCondition("reference_type", "'FOO DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:foodb", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' FooDB Reference: FDB' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'FOO DB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:hmdb", "reference"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' The Human Metabolome Database Reference: HMDB' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:iuphar", "reference"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' IUPHAR Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createAreEqualCondition("reference_type", "'LIPID MAPS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Lipid Maps Reference: LM' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'LIPID MAPS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:molport", "reference"),
                    config.createAreEqualCondition("reference_type", "'MOLPORT'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:molport", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' MolPort Reference: Molport-' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'MOLPORT'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' NMRShiftDB Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pdbe", "reference"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PDBe Reference: ' || reference || ' - Ideal conformer')::varchar"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createAreEqualCondition("reference_type", "'PROBES AND DRUGS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Probes&Drugs Reference: PD' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'PROBES AND DRUGS'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pubchem", "reference"),
                    config.createAreEqualCondition("reference_type", "'PUBCHEM'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' PubChem Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'PUBCHEM'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createAreEqualCondition("reference_type", "'RCSB PDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' RCSB PDB Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'RCSB PDB'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rheachebi", "reference"),
                    config.createAreEqualCondition("reference_type", "'RHEA CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheachebi", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Rhea Reference: CHEBI:' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'RHEA CHEBI'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createAreEqualCondition("reference_type", "'RHEA POLYMER'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Rhea Reference: POLYMER:' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'RHEA POLYMER'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:surechembl", "reference"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' SureChEMBL Reference: ' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:swisslipids", "reference"),
                    config.createAreEqualCondition("reference_type", "'SWISS LIPIDS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:swisslipids", "reference"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' SwissLipids Reference: SLM:' || reference)::varchar"),
                    config.createAreEqualCondition("reference_type", "'SWISS LIPIDS'::" + moleculeReferenceType));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_reference_types");

            config.addQuadMapping(table, graph, config.createIriMapping("reference:bindingdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:BindingDbRef"),
                    config.createAreEqualCondition("reference_type", "'BINDING DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:brenda", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:BrendaRef"),
                    config.createAreEqualCondition("reference_type", "'BRENDA'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:ccdc", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:CcdcRef"),
                    config.createAreEqualCondition("reference_type", "'CCDC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ChebiRef"),
                    config.createAreEqualCondition("reference_type", "'CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ClinicalTrialsRef"),
                    config.createAreEqualCondition("reference_type", "'CLINICAL TRIALS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:comptox", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:CompToxRef"),
                    config.createAreEqualCondition("reference_type", "'COMPTOX'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugcentral", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:DrugCentralRef"),
                    config.createAreEqualCondition("reference_type", "'DRUG CENTRAL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:DrugbankRef"),
                    config.createAreEqualCondition("reference_type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fdasrs", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:FdaSrsRef"),
                    config.createAreEqualCondition("reference_type", "'FDA SRS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:foodb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:FooDbRef"),
                    config.createAreEqualCondition("reference_type", "'FOO DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:HmdbRef"),
                    config.createAreEqualCondition("reference_type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:IupharRef"),
                    config.createAreEqualCondition("reference_type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:LipidMapsRef"),
                    config.createAreEqualCondition("reference_type", "'LIPID MAPS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:molport", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:MolportRef"),
                    config.createAreEqualCondition("reference_type", "'MOLPORT'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:NmrShiftDb2Ref"),
                    config.createAreEqualCondition("reference_type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PdbeRef"),
                    config.createAreEqualCondition("reference_type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ProbesAndDrugsRef"),
                    config.createAreEqualCondition("reference_type", "'PROBES AND DRUGS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemRef"),
                    config.createAreEqualCondition("reference_type", "'PUBCHEM'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RcsbPdbRef"),
                    config.createAreEqualCondition("reference_type", "'RCSB PDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheachebi", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RheaRef"),
                    config.createAreEqualCondition("reference_type", "'RHEA CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RheaRef"),
                    config.createAreEqualCondition("reference_type", "'RHEA POLYMER'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SureChemblRef"),
                    config.createAreEqualCondition("reference_type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:swisslipids", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SwissLipidsRef"),
                    config.createAreEqualCondition("reference_type", "'SWISS LIPIDS'::" + moleculeReferenceType));
        }
    }
}
