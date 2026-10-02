package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class Molecule
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:compound", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
        config.addIriClass(new IntegerUserIriClass("chembl:molfile", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "_Molfile"));
        config.addIriClass(new IntegerUserIriClass("chembl:image", INT4,
                "https://www.ebi.ac.uk/chembl/api/data/image/CHEMBL", ".svg"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_alogp", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#alogp"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_aromatic_rings", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#aromatic_rings"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_hba", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#hba"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_hbd", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#hbd"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_heavy_atoms", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#heavy_atoms"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_num_ro5_violations", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#num_ro5_violations"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_psa", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#psa"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_qed_weighted", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#qed_weighted"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_rtb", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#rtb"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_mw_freebase", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#mw_freebase"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_full_mwt", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#full_mwt"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_ro3_pass", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#ro3_pass"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_full_molformula", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#full_molformula"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_standard_inchi_key", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#standard_inchi_key"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_standard_inchi", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#standard_inchi"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_canonical_smiles", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#canonical_smiles"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_dictionary");
            TermMapping subject = config.createIriMapping("chembl:compound", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:CellTherapy"),
                    config.createAreEqualCondition("molecule_type", "'Cell'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Oligosaccharide"),
                    config.createAreEqualCondition("molecule_type", "'Oligosaccharide'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Enzyme"),
                    config.createAreEqualCondition("molecule_type", "'Enzyme'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Oligonucleotide"),
                    config.createAreEqualCondition("molecule_type", "'Oligonucleotide'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Antibody"),
                    config.createAreEqualCondition("molecule_type", "'Antibody'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnknownSubstance"),
                    config.createAreEqualCondition("molecule_type", "'Unknown'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinMolecule"),
                    config.createAreEqualCondition("molecule_type", "'Protein'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SmallMolecule"),
                    config.createAreEqualCondition("molecule_type", "'Small molecule'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedMolecule"), config.createAreEqualCondition("molecule_type",
                            "'Gene'::varchar", "'Antibody drug conjugate'::varchar", "'Vaccine component'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("foaf:depiction"),
                    config.createIriMapping("chembl:image", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:highestDevelopmentPhase"),
                    config.createLiteralMapping(xsdFloat, "max_phase"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "(coalesce(pref_name, chembl_id))::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "(coalesce(pref_name, chembl_id))::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:substanceType"),
                    config.createLiteralMapping(xsdString, "molecule_type"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("foaf:Image"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('SVG Image Depiction of ' || chembl_id)::varchar"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Substance"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_names");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "biotherapeutics");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isBiotherapeutic"),
                    config.createLiteralMapping(true));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:helmNotation"),
                    config.createLiteralMapping(xsdString, "helm_notation"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_atc_classification");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:atcClassification"),
                    config.createLiteralMapping(xsdString, "level5"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "biotherapeutic_components");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasBioComponent"),
                    config.createIriMapping("chembl:biocomponent", "component_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:biocomponent", "component_id"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_hrac_classification");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:HRACClassification"),
                    config.createLiteralMapping(
                            "https://hracglobal.com/tools/2024-hrac-global-herbicide-moa-classification"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_irac_classification");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:IRACClassification"),
                    config.createLiteralMapping("https://irac-online.org/mode-of-action/"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_frac_classification");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:FRACClassification"),
                    config.createLiteralMapping("https://www.frac.info/knowledge-database/downloads"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_docs");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                    config.createIriMapping("chembl:document", "document_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document_id"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_properties");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000251"),
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ALogP: ' || alogp::varchar)::varchar"),
                    config.createIsNotNullCondition(table, "alogp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000381"),
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Aromatic Rings: ' || aromatic_rings)::varchar"),
                    config.createIsNotNullCondition(table, "aromatic_rings"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000245"),
                    config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Hydrogen Bond Acceptors: ' || hba)::varchar"),
                    config.createIsNotNullCondition(table, "hba"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000244"),
                    config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Hydrogen Bond Donors: ' || hbd)::varchar"),
                    config.createIsNotNullCondition(table, "hbd"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000300"),
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Heavy Atoms: ' || heavy_atoms)::varchar"),
                    config.createIsNotNullCondition(table, "heavy_atoms"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000312"),
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' RO5 Violations: ' || num_ro5_violations)::varchar"),
                    config.createIsNotNullCondition(table, "num_ro5_violations"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000307"),
                    config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Polar Surface Area: ' || psa::varchar)::varchar"),
                    config.createIsNotNullCondition(table, "psa"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000431"),
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' QED Weighted Score: ' || qed_weighted::varchar)::varchar"),
                    config.createIsNotNullCondition(table, "qed_weighted"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000254"),
                    config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Rotatable Bonds: ' || rtb)::varchar"),
                    config.createIsNotNullCondition(table, "rtb"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000350"),
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Freebase Molecular Weight: ' || mw_freebase::varchar)::varchar"),
                    config.createIsNotNullCondition(table, "mw_freebase"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000216"),
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Full Molecular Weight: ' || full_mwt::varchar)::varchar"),
                    config.createIsNotNullCondition(table, "full_mwt"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000315"),
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdString, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' RO3 Pass: ' || ro3_pass)::varchar"),
                    config.createIsNotNullCondition(table, "ro3_pass"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIsNotNullCondition(table, "full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000042"),
                    config.createIsNotNullCondition(table, "full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Molecular Formula: ' || full_molformula)::varchar"),
                    config.createIsNotNullCondition(table, "full_molformula"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "full_molformula"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_structures");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000059"),
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Standard InChi Key: ' || standard_inchi_key)::varchar"),
                    config.createIsNotNullCondition(table, "standard_inchi_key"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000113"),
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' Standard InChi')::varchar"),
                    config.createIsNotNullCondition(table, "standard_inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000018"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' Canonical Smiles')::varchar"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));

            // extension
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "canonical_smiles"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_hierarchy");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasParentMolecule"),
                    config.createIriMapping("chembl:compound", "parent_molecule_id"),
                    config.createAreNotEqualCondition(table, "molecule_id", "parent_molecule_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "parent_molecule_id"),
                    config.createIriMapping("cco:hasChildMolecule"), subject,
                    config.createAreNotEqualCondition(table, "molecule_id", "parent_molecule_id"));
        }

        // extension
        {
            DatabaseTable table = new DatabaseTable("molecules", "chembl");
            TermMapping subject = config.createIriMapping("chembl:molfile", "id");
            LiteralClass molfileLiteral = new StringSubsetLiteralClass("chembl-molfile");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("chembl:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("chembl:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));
        }
    }
}
