package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
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
    private static String moleculeReferenceType = schema + ".molecule_reference_type";


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
            DatabaseTable table = new DatabaseTable(schema, "molecule_bases");
            TermMapping subject = config.createIriMapping("chembl:compound", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:CellTherapy"),
                    config.createAreEqualCondition("type", "'Cell'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Oligosaccharide"),
                    config.createAreEqualCondition("type", "'Oligosaccharide'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Enzyme"), config.createAreEqualCondition("type", "'Enzyme'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Oligonucleotide"),
                    config.createAreEqualCondition("type", "'Oligonucleotide'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Antibody"),
                    config.createAreEqualCondition("type", "'Antibody'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnknownSubstance"),
                    config.createAreEqualCondition("type", "'Unknown'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinMolecule"),
                    config.createAreEqualCondition("type", "'Protein'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:SmallMolecule"),
                    config.createAreEqualCondition("type", "'Small molecule'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:UnclassifiedMolecule"), config.createAreEqualCondition("type",
                            "'Gene'::varchar", "'Antibody drug conjugate'::varchar", "'Vaccine component'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("foaf:depiction"),
                    config.createIriMapping("chembl:image", "id"), config.createIsNotNullCondition(table, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:highestDevelopmentPhase"),
                    config.createLiteralMapping(xsdFloat, "phase"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:substanceType"),
                    config.createLiteralMapping(xsdString, "type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isBiotherapeutic"),
                    config.createLiteralMapping(xsdBoolean, "biotherapeutic"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:helmNotation"),
                    config.createLiteralMapping(xsdString, "helm_notation"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:HRACClassification"),
                    config.createLiteralMapping(xsdString, "hrac_classification"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:IRACClassification"),
                    config.createLiteralMapping(xsdString, "irac_classification"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:FRACClassification"),
                    config.createLiteralMapping(xsdString, "frac_classification"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasParentMolecule"),
                    config.createIriMapping("chembl:compound", "parent"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "parent"),
                    config.createIriMapping("cco:hasChildMolecule"), subject);
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("foaf:Image"),
                    config.createIsNotNullCondition(table, "chembl_id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Substance"), config.createIsNotNullCondition(table, "chembl_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_alternatives");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_atc_classifications");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:atcClassification"),
                    config.createLiteralMapping(xsdString, "classification"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_biocomponents");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasBioComponent"),
                    config.createIriMapping("chembl:biocomponent", "biocomponent"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:biocomponent", "biocomponent"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_documents");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                    config.createIriMapping("chembl:document", "document"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_descriptors");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_alogp", "molecule"),
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000251"),
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "alogp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule"),
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_aromatic_rings", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000381"),
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_aromatic_rings", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "aromatic_rings"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hba", "molecule"),
                    config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000245"),
                    config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hba"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hbd", "molecule"),
                    config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000244"),
                    config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hbd"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_heavy_atoms", "molecule"),
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000300"),
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "heavy_atoms"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule"),
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000312"),
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "num_ro5_violations"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_psa", "molecule"),
                    config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000307"),
                    config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "psa"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_qed_weighted", "molecule"),
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000431"),
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "qed_weighted"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_rtb", "molecule"),
                    config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000254"),
                    config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "rtb"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_mw_freebase", "molecule"),
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000350"),
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "mw_freebase"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_mwt", "molecule"),
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000216"),
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "full_mwt"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_ro3_pass", "molecule"),
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000315"),
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdString, "ro3_pass"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule"),
                    config.createIsNotNullCondition(table, "full_molformula"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_molformula", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000042"),
                    config.createIsNotNullCondition(table, "full_molformula"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_molformula", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "full_molformula"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_aromatic_rings", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "heavy_atoms"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "num_ro5_violations"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_molformula", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "full_molformula"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_structures");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule"),
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000059"),
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi_key"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule"),
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_standard_inchi", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000113"),
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_standard_inchi", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_canonical_smiles", "molecule"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000018"),
                    config.createIsNotNullCondition(table, "canonical_smiles"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_canonical_smiles", "molecule"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "canonical_smiles"));

            // extension
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "standard_inchi_key"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_standard_inchi", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "standard_inchi"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_canonical_smiles", "molecule"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "canonical_smiles"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_labels");

            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_aromatic_rings", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "heavy_atoms"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "num_ro5_violations"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_molformula", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "standard_inchi_key"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_standard_inchi", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "standard_inchi"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_canonical_smiles", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "canonical_smiles"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "molecule"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "image"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:bindingdb", "reference"),
                    config.createAreEqualCondition("type", "'BINDING DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:brenda", "reference"),
                    config.createAreEqualCondition("type", "'BRENDA'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:ccdc", "reference"),
                    config.createAreEqualCondition("type", "'CCDC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:chebi", "reference"),
                    config.createAreEqualCondition("type", "'CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createAreEqualCondition("type", "'CLINICAL TRIALS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:comptox", "reference"),
                    config.createAreEqualCondition("type", "'COMPTOX'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:drugcentral", "reference"),
                    config.createAreEqualCondition("type", "'DRUG CENTRAL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:drugbank", "reference"),
                    config.createAreEqualCondition("type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:fdasrs", "reference"),
                    config.createAreEqualCondition("type", "'FDA SRS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:foodb", "reference"),
                    config.createAreEqualCondition("type", "'FOO DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:hmdb", "reference"),
                    config.createAreEqualCondition("type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:iuphar", "reference"),
                    config.createAreEqualCondition("type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createAreEqualCondition("type", "'LIPID MAPS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:molport", "reference"),
                    config.createAreEqualCondition("type", "'MOLPORT'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createAreEqualCondition("type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pdbe", "reference"),
                    config.createAreEqualCondition("type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createAreEqualCondition("type", "'PROBES AND DRUGS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:pubchem", "reference"),
                    config.createAreEqualCondition("type", "'PUBCHEM'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createAreEqualCondition("type", "'RCSB PDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rheachebi", "reference"),
                    config.createAreEqualCondition("type", "'RHEA CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createAreEqualCondition("type", "'RHEA POLYMER'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:surechembl", "reference"),
                    config.createAreEqualCondition("type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:moleculeXref"),
                    config.createIriMapping("reference:swisslipids", "reference"),
                    config.createAreEqualCondition("type", "'SWISS LIPIDS'::" + moleculeReferenceType));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_references");

            config.addQuadMapping(table, graph, config.createIriMapping("reference:bindingdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:BindingDbRef"),
                    config.createAreEqualCondition("type", "'BINDING DB'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:brenda", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:BrendaRef"),
                    config.createAreEqualCondition("type", "'BRENDA'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:ccdc", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:CcdcRef"),
                    config.createAreEqualCondition("type", "'CCDC'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ChebiRef"),
                    config.createAreEqualCondition("type", "'CHEBI'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ClinicalTrialsRef"),
                    config.createAreEqualCondition("type", "'CLINICAL TRIALS'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:comptox", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:CompToxRef"),
                    config.createAreEqualCondition("type", "'COMPTOX'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugcentral", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:DrugCentralRef"),
                    config.createAreEqualCondition("type", "'DRUG CENTRAL'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:DrugbankRef"),
                    config.createAreEqualCondition("type", "'DRUGBANK'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fdasrs", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:FdaSrsRef"),
                    config.createAreEqualCondition("type", "'FDA SRS'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:foodb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:FooDbRef"),
                    config.createAreEqualCondition("type", "'FOO DB'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:HmdbRef"),
                    config.createAreEqualCondition("type", "'HMDB'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:IupharRef"),
                    config.createAreEqualCondition("type", "'IUPHAR'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:LipidMapsRef"),
                    config.createAreEqualCondition("type", "'LIPID MAPS'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:molport", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:MolportRef"),
                    config.createAreEqualCondition("type", "'MOLPORT'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:NmrShiftDb2Ref"),
                    config.createAreEqualCondition("type", "'NMR SHIFT DB2'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PdbeRef"),
                    config.createAreEqualCondition("type", "'PDBE'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:ProbesAndDrugsRef"),
                    config.createAreEqualCondition("type", "'PROBES AND DRUGS'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:PubchemRef"),
                    config.createAreEqualCondition("type", "'PUBCHEM'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RcsbPdbRef"),
                    config.createAreEqualCondition("type", "'RCSB PDB'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheachebi", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RheaRef"),
                    config.createAreEqualCondition("type", "'RHEA CHEBI'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:RheaRef"),
                    config.createAreEqualCondition("type", "'RHEA POLYMER'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SureChemblRef"),
                    config.createAreEqualCondition("type", "'SURE CHEMBL'::" + moleculeReferenceType), true);
            config.addQuadMapping(table, graph, config.createIriMapping("reference:swisslipids", "reference"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("cco:SwissLipidsRef"),
                    config.createAreEqualCondition("type", "'SWISS LIPIDS'::" + moleculeReferenceType), true);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_reference_labels");

            config.addQuadMapping(table, graph, config.createIriMapping("reference:bindingdb", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'BINDING DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:brenda", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'BRENDA'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:ccdc", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'CCDC'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:chebi", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:clinicaltrials", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'CLINICAL TRIALS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:comptox", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'COMPTOX'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugcentral", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'DRUG CENTRAL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:drugbank", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'DRUGBANK'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:fdasrs", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'FDA SRS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:foodb", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'FOO DB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:hmdb", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'HMDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:iuphar", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'IUPHAR'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:lipidmaps", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'LIPID MAPS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:molport", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'MOLPORT'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:nmrshiftdb2", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'NMR SHIFT DB2'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pdbe", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PDBE'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:probesanddrugs", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PROBES AND DRUGS'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:pubchem", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'PUBCHEM'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rcsbpdb", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'RCSB PDB'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheachebi", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'RHEA CHEBI'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:rheapolymer", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'RHEA POLYMER'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:surechembl", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'SURE CHEMBL'::" + moleculeReferenceType));
            config.addQuadMapping(table, graph, config.createIriMapping("reference:swisslipids", "reference"),
                    config.createIriMapping("rdfs:label"), config.createLiteralMapping(xsdString, "label"),
                    config.createAreEqualCondition("type", "'SWISS LIPIDS'::" + moleculeReferenceType));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_pubchem_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "molecule_chebi_references");
            TermMapping subject = config.createIriMapping("chembl:compound", "molecule");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:chebi", "chebi"));
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
