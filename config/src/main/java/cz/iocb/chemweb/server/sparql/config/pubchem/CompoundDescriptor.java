package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdShort;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class CompoundDescriptor
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID";

        config.addIriClass(
                new IntegerUserIriClass("pubchem:compound_identifier", "integer", prefix, "_Compound_Identifier"));
        config.addIriClass(new IntegerUserIriClass("pubchem:hydrogen_bond_acceptor_count", "integer", prefix,
                "_Hydrogen_Bond_Acceptor_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:tautomer_count", "integer", prefix, "_Tautomer_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:defined_atom_stereo_count", "integer", prefix,
                "_Defined_Atom_Stereo_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:defined_bond_stereo_count", "integer", prefix,
                "_Defined_Bond_Stereo_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:undefined_bond_stereo_count", "integer", prefix,
                "_Undefined_Bond_Stereo_Count"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:isotope_atom_count", "integer", prefix, "_Isotope_Atom_Count"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:covalent_unit_count", "integer", prefix, "_Covalent_Unit_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:hydrogen_bond_donor_count", "integer", prefix,
                "_Hydrogen_Bond_Donor_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:non_hydrogen_atom_count", "integer", prefix,
                "_Non-hydrogen_Atom_Count"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:rotatable_bond_count", "integer", prefix, "_Rotatable_Bond_Count"));
        config.addIriClass(new IntegerUserIriClass("pubchem:undefined_atom_stereo_count", "integer", prefix,
                "_Undefined_Atom_Stereo_Count"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:total_formal_charge", "integer", prefix, "_Total_Formal_Charge"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:structure_complexity", "integer", prefix, "_Structure_Complexity"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:mono_isotopic_weight", "integer", prefix, "_Mono_Isotopic_Weight"));
        config.addIriClass(new IntegerUserIriClass("pubchem:xlogp3_aa", "integer", prefix, "_XLogP3-AA"));
        config.addIriClass(new IntegerUserIriClass("pubchem:xlogp3", "integer", prefix, "_XLogP3"));
        config.addIriClass(new IntegerUserIriClass("pubchem:exact_mass", "integer", prefix, "_Exact_Mass"));
        config.addIriClass(new IntegerUserIriClass("pubchem:molecular_weight", "integer", prefix, "_Molecular_Weight"));
        config.addIriClass(new IntegerUserIriClass("pubchem:tpsa", "integer", prefix, "_TPSA"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:molecular_formula", "integer", prefix, "_Molecular_Formula"));
        config.addIriClass(new IntegerUserIriClass("pubchem:isomeric_smiles", "integer", prefix, "_Isomeric_SMILES"));
        config.addIriClass(new IntegerUserIriClass("pubchem:canonical_smiles", "integer", prefix, "_Canonical_SMILES"));
        config.addIriClass(new IntegerUserIriClass("pubchem:iupac_inchi", "integer", prefix, "_IUPAC_InChI"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:preferred_iupac_name", "integer", prefix, "_Preferred_IUPAC_Name"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("descriptor:compound");
        ConstantIriMapping type = config.createIriMapping("rdf:type");
        ConstantIriMapping value = config.createIriMapping("sio:SIO_000300");
        ConstantIriMapping unit = config.createIriMapping("sio:SIO_000221");

        ConstantIriMapping oldValue = config.createIriMapping("sio:has-value");
        ConstantIriMapping oldUnit = config.createIriMapping("sio:has-unit");

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("pubchem:compound_identifier", "id");
            String field = "(id::varchar)";

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000140"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "hydrogen_bond_acceptor_count";
            NodeMapping subject = config.createIriMapping("pubchem:hydrogen_bond_acceptor_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000388"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "tautomer_count";
            NodeMapping subject = config.createIriMapping("pubchem:tautomer_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000391"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "defined_atom_stereo_count";
            NodeMapping subject = config.createIriMapping("pubchem:defined_atom_stereo_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000370"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "defined_bond_stereo_count";
            NodeMapping subject = config.createIriMapping("pubchem:defined_bond_stereo_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000371"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "undefined_bond_stereo_count";
            NodeMapping subject = config.createIriMapping("pubchem:undefined_bond_stereo_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000375"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "isotope_atom_count";
            NodeMapping subject = config.createIriMapping("pubchem:isotope_atom_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000372"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "covalent_unit_count";
            NodeMapping subject = config.createIriMapping("pubchem:covalent_unit_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000369"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "hydrogen_bond_donor_count";
            NodeMapping subject = config.createIriMapping("pubchem:hydrogen_bond_donor_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000387"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "non_hydrogen_atom_count";
            NodeMapping subject = config.createIriMapping("pubchem:non_hydrogen_atom_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000373"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "rotatable_bond_count";
            NodeMapping subject = config.createIriMapping("pubchem:rotatable_bond_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000389"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "undefined_atom_stereo_count";
            NodeMapping subject = config.createIriMapping("pubchem:undefined_atom_stereo_count", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000374"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "total_formal_charge";
            NodeMapping subject = config.createIriMapping("pubchem:total_formal_charge", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000336"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdShort, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdShort, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "structure_complexity";
            NodeMapping subject = config.createIriMapping("pubchem:structure_complexity", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000390"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "mono_isotopic_weight";
            NodeMapping subject = config.createIriMapping("pubchem:mono_isotopic_weight", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000337"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, unit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, oldUnit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "xlogp3_aa";
            NodeMapping subject = config.createIriMapping("pubchem:xlogp3_aa", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000395"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "xlogp3";
            NodeMapping subject = config.createIriMapping("pubchem:xlogp3", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000395"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "exact_mass";
            NodeMapping subject = config.createIriMapping("pubchem:exact_mass", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000338"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, unit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, oldUnit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "molecular_weight";
            NodeMapping subject = config.createIriMapping("pubchem:molecular_weight", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000334"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, unit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, oldUnit, config.createIriMapping("obo:UO_0000055"),
                    config.createIsNotNullCondition(field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_bases");
            String field = "tpsa";
            NodeMapping subject = config.createIriMapping("pubchem:tpsa", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000392"),
                    config.createIsNotNullCondition(field));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, unit, config.createIriMapping("obo:UO_0000324"),
                    config.createIsNotNullCondition(field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(xsdFloat, field));
            config.addQuadMapping(table, graph, subject, oldUnit, config.createIriMapping("obo:UO_0000324"),
                    config.createIsNotNullCondition(field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_molecular_formulas");
            String field = "molecular_formula";
            NodeMapping subject = config.createIriMapping("pubchem:molecular_formula", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000335"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_isomeric_smileses");
            String field = "isomeric_smiles";
            NodeMapping subject = config.createIriMapping("pubchem:isomeric_smiles", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000379"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_canonical_smileses");
            String field = "canonical_smiles";
            NodeMapping subject = config.createIriMapping("pubchem:canonical_smiles", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000376"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_iupac_inchis");
            String field = "iupac_inchi";
            NodeMapping subject = config.createIriMapping("pubchem:iupac_inchi", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000396"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }

        {
            Table table = new Table(schema, "descriptor_compound_preferred_iupac_names");
            String field = "preferred_iupac_name";
            NodeMapping subject = config.createIriMapping("pubchem:preferred_iupac_name", "compound");

            config.addQuadMapping(table, graph, subject, type, config.createIriMapping("sio:CHEMINF_000382"));
            config.addQuadMapping(table, graph, subject, value, config.createLiteralMapping(rdfLangStringEn, field));

            // deprecated
            config.addQuadMapping(table, graph, subject, oldValue, config.createLiteralMapping(rdfLangStringEn, field));
        }
    }
}
