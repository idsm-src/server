package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Molecule
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:compound", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
        config.addIriClass(new IntegerUserIriClass("chembl:molfile", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "_Molfile"));
        config.addIriClass(new IntegerUserIriClass("chembl:image", "integer",
                "https://www.ebi.ac.uk/chembl/api/data/image/CHEMBL", ".svg"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_cx_most_apka", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#cx_most_apka"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_cx_most_bpka", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#cx_most_bpka"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_cx_logd", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#cx_logd"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_cx_logp", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#cx_logp"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_alogp", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#alogp"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_aromatic_rings", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#aromatic_rings"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_hba", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#hba"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_hbd", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#hbd"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_heavy_atoms", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#heavy_atoms"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_num_ro5_violations", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#num_ro5_violations"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_psa", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#psa"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_qed_weighted", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#qed_weighted"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_rtb", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#rtb"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_mw_freebase", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#mw_freebase"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_mw_monoisotopic", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#mw_monoisotopic"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_full_mwt", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#full_mwt"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_molecular_species", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#molecular_species"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_ro3_pass", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#ro3_pass"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_full_molformula", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#full_molformula"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_standard_inchi_key", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#standard_inchi_key"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_standard_inchi", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#standard_inchi"));
        config.addIriClass(new IntegerUserIriClass("chembl:molecule_canonical_smiles", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL", "#canonical_smiles"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            Table table = new Table(schema, "molecule_dictionary");
            NodeMapping subject = config.createIriMapping("chembl:compound", "id");

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
                    config.createIriMapping("cco:UnclassifiedMolecule"),
                    config.createAreEqualCondition("molecule_type", "'Gene'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("foaf:depiction"),
                    config.createIriMapping("chembl:image", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi_par_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:highestDevelopmentPhase"),
                    config.createLiteralMapping(xsdFloat, "max_phase"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "(coalesce(pref_name, chembl_id))"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "(coalesce(pref_name, chembl_id))"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:substanceType"),
                    config.createLiteralMapping(xsdString, "molecule_type"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("foaf:Image"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:image", "id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('SVG Image Depiction of ' || chembl_id)"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:Substance"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("chembl/Substance.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:pageTemplate"),
                    config.createLiteralMapping("chembl/Substance.vm"));
        }

        {
            Table table = new Table(schema, "molecule_names");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "biotherapeutics");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:isBiotherapeutic"),
                    config.createLiteralMapping(true));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:helmNotation"),
                    config.createLiteralMapping(xsdString, "helm_notation"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
        }

        {
            Table table = new Table(schema, "molecule_atc_classification");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:atcClassification"),
                    config.createLiteralMapping(xsdString, "level5"));
        }

        {
            Table table = new Table(schema, "biotherapeutic_components");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasBioComponent"),
                    config.createIriMapping("chembl:biocomponent", "component_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:biocomponent", "component_id"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            Table table = new Table(schema, "molecule_hrac_classification");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, new Table(schema, "hrac_classification"), "hrac_class_id", "hrac_class_id",
                    graph, subject, config.createIriMapping("cco:hracClassification"),
                    config.createLiteralMapping(xsdString, "hrac_code"));
        }

        {
            Table table = new Table(schema, "molecule_irac_classification");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, new Table(schema, "irac_classification"), "irac_class_id", "irac_class_id",
                    graph, subject, config.createIriMapping("cco:iracClassification"),
                    config.createLiteralMapping(xsdString, "level2"));
        }

        {
            Table table = new Table(schema, "molecule_frac_classification");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, new Table(schema, "frac_classification"), "frac_class_id", "frac_class_id",
                    graph, subject, config.createIriMapping("cco:fracClassification"),
                    config.createLiteralMapping(xsdString, "level2"));
        }

        {
            Table table = new Table(schema, "molecule_docs");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                    config.createIriMapping("chembl:document", "document_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document_id"),
                    config.createIriMapping("cco:hasMolecule"), subject);
        }

        {
            Table table = new Table(schema, "compound_properties");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_cx_most_apka", "molecule_id"),
                    config.createIsNotNullCondition("cx_most_apka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_apka", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000195"),
                    config.createIsNotNullCondition("cx_most_apka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_apka", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "cx_most_apka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_apka", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChemAxon Most Acidic pKa: ' || cx_most_apka::varchar)"),
                    config.createIsNotNullCondition("cx_most_apka"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_cx_most_bpka", "molecule_id"),
                    config.createIsNotNullCondition("cx_most_bpka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_bpka", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000195"),
                    config.createIsNotNullCondition("cx_most_bpka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_bpka", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "cx_most_bpka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_bpka", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChemAxon Most Basic pKa: ' || cx_most_bpka::varchar)"),
                    config.createIsNotNullCondition("cx_most_bpka"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_cx_logd", "molecule_id"),
                    config.createIsNotNullCondition("cx_logd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logd", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000322"),
                    config.createIsNotNullCondition("cx_logd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "cx_logd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logd", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChemAxon LogD: ' || cx_logd::varchar)"),
                    config.createIsNotNullCondition("cx_logd"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_cx_logp", "molecule_id"),
                    config.createIsNotNullCondition("cx_logp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logp", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000251"),
                    config.createIsNotNullCondition("cx_logp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "cx_logp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logp", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' ChemAxon LogP: ' || cx_logp::varchar)"),
                    config.createIsNotNullCondition("cx_logp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIsNotNullCondition("alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000251"),
                    config.createIsNotNullCondition("alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "alogp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' ALogP: ' || alogp::varchar)"),
                    config.createIsNotNullCondition("alogp"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIsNotNullCondition("aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000381"),
                    config.createIsNotNullCondition("aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "aromatic_rings"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Aromatic Rings: ' || aromatic_rings)"),
                    config.createIsNotNullCondition("aromatic_rings"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIsNotNullCondition("hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000245"),
                    config.createIsNotNullCondition("hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Hydrogen Bond Acceptors: ' || hba)"),
                    config.createIsNotNullCondition("hba"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIsNotNullCondition("hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000244"),
                    config.createIsNotNullCondition("hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Hydrogen Bond Donors: ' || hbd)"),
                    config.createIsNotNullCondition("hbd"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIsNotNullCondition("heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000300"),
                    config.createIsNotNullCondition("heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "heavy_atoms"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Heavy Atoms: ' || heavy_atoms)"),
                    config.createIsNotNullCondition("heavy_atoms"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIsNotNullCondition("num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000312"),
                    config.createIsNotNullCondition("num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "num_ro5_violations"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' RO5 Violations: ' || num_ro5_violations)"),
                    config.createIsNotNullCondition("num_ro5_violations"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIsNotNullCondition("psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000307"),
                    config.createIsNotNullCondition("psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Polar Surface Area: ' || psa::varchar)"),
                    config.createIsNotNullCondition("psa"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIsNotNullCondition("qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000431"),
                    config.createIsNotNullCondition("qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' QED Weighted Score: ' || qed_weighted::varchar)"),
                    config.createIsNotNullCondition("qed_weighted"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIsNotNullCondition("rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000254"),
                    config.createIsNotNullCondition("rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' Rotatable Bonds: ' || rtb)"),
                    config.createIsNotNullCondition("rtb"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIsNotNullCondition("mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000350"),
                    config.createIsNotNullCondition("mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "mw_freebase"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Freebase Molecular Weight: ' || mw_freebase::varchar)"),
                    config.createIsNotNullCondition("mw_freebase"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_mw_monoisotopic", "molecule_id"),
                    config.createIsNotNullCondition("mw_monoisotopic"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_mw_monoisotopic", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000218"),
                    config.createIsNotNullCondition("mw_monoisotopic"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_mw_monoisotopic", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdDouble, "mw_monoisotopic"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_mw_monoisotopic", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Monoisotopic Mass: ' || mw_monoisotopic::varchar)"),
                    config.createIsNotNullCondition("mw_monoisotopic"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIsNotNullCondition("full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000216"),
                    config.createIsNotNullCondition("full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdDouble, "full_mwt"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Full Molecular Weight: ' || full_mwt::varchar)"),
                    config.createIsNotNullCondition("full_mwt"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_molecular_species", "molecule_id"),
                    config.createIsNotNullCondition("molecular_species"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_molecular_species", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000326"),
                    config.createIsNotNullCondition("molecular_species"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_molecular_species", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "molecular_species"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_molecular_species", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Molecular Species: ' || molecular_species)"),
                    config.createIsNotNullCondition("molecular_species"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIsNotNullCondition("ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000315"),
                    config.createIsNotNullCondition("ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"), config.createLiteralMapping(xsdString, "ro3_pass"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' RO3 Pass: ' || ro3_pass)"),
                    config.createIsNotNullCondition("ro3_pass"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIsNotNullCondition("full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000042"),
                    config.createIsNotNullCondition("full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "full_molformula"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Molecular Formula: ' || full_molformula)"),
                    config.createIsNotNullCondition("full_molformula"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_apka", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("cx_most_apka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_most_bpka", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("cx_most_bpka"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("cx_logd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_cx_logp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("cx_logp"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_alogp", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("alogp"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_aromatic_rings", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("aromatic_rings"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hba", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("hba"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_hbd", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("hbd"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_heavy_atoms", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("heavy_atoms"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_num_ro5_violations", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("num_ro5_violations"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_psa", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("psa"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_qed_weighted", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("qed_weighted"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_rtb", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("rtb"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_mw_freebase", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("mw_freebase"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_mw_monoisotopic", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("mw_monoisotopic"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_full_mwt", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("full_mwt"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_molecular_species", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("molecular_species"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:molecule_ro3_pass", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition("ro3_pass"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_full_molformula", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("full_molformula"));
        }

        {
            Table table = new Table(schema, "compound_structures");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIsNotNullCondition("standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000059"),
                    config.createIsNotNullCondition("standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString,
                            "('CHEMBL' || molecule_id || ' Standard InChi Key: ' || standard_inchi_key)"),
                    config.createIsNotNullCondition("standard_inchi_key"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIsNotNullCondition("standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000113"),
                    config.createIsNotNullCondition("standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' Standard InChi')"),
                    config.createIsNotNullCondition("standard_inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIsNotNullCondition("canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("sio:CHEMINF_000018"),
                    config.createIsNotNullCondition("canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "canonical_smiles"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "('CHEMBL' || molecule_id || ' Canonical Smiles')"),
                    config.createIsNotNullCondition("canonical_smiles"));

            // extension
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi_key", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("standard_inchi_key"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_standard_inchi", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("standard_inchi"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("chembl:molecule_canonical_smiles", "molecule_id"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition("canonical_smiles"));
        }

        {
            Table table = new Table(schema, "molecule_hierarchy");
            NodeMapping subject = config.createIriMapping("chembl:compound", "molecule_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasParentMolecule"),
                    config.createIriMapping("chembl:compound", "parent_molecule_id"),
                    config.createAreNotEqualCondition("molecule_id", "parent_molecule_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "parent_molecule_id"),
                    config.createIriMapping("cco:hasChildMolecule"), subject,
                    config.createAreNotEqualCondition("molecule_id", "parent_molecule_id"));
        }

        // extension
        {
            Table table = new Table("molecules", "chembl");
            NodeMapping subject = config.createIriMapping("chembl:molfile", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("chembl:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("chembl:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "molfile"));
        }
    }
}
