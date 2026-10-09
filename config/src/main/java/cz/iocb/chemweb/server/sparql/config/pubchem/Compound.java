package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdShort;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class Compound
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID";

        config.addIriClass(new IntegerUserIriClass("pubchem:compound", INT4, prefix));
        config.addIriClass(new IntegerUserIriClass("pubchem:molfile", INT4, prefix, "_Molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:compound");

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_bases");
            TermMapping subject = config.createIriMapping("pubchem:compound", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Compound"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:compound_identifier", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:compound_identifier", "id"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:compound_identifier", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010004"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_molfiles");
            TermMapping subject = config.createIriMapping("pubchem:molfile", "compound");
            LiteralClass molfileLiteral = new StringSubsetLiteralClass("pubchem-molfile");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:compound", "compound"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:compound", "compound"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:compound", "compound"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_labels");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_components");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000480"),
                    config.createIriMapping("pubchem:compound", "component"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_isotopologues");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000455"),
                    config.createIriMapping("pubchem:compound", "isotopologue"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_parents");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:has_parent"),
                    config.createIriMapping("pubchem:compound", "parent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_stereoisomers");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000461"),
                    config.createIriMapping("pubchem:compound", "isomer"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_same_connectivities");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000462"),
                    config.createIriMapping("pubchem:compound", "isomer"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_roles");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000087"),
                    config.createIriMapping("ontology:uncategorized", "role_id"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:has-role"),
                    config.createIriMapping("ontology:uncategorized", "role_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_types");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", "type_unit", "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_active_ingredients");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:is_active_ingredient_of"),
                    config.createIriMapping("ontology:resource", "ingredient_unit", "ingredient_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_bases");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:hydrogen_bond_acceptor_count", "compound"),
                    config.createIsNotNullCondition(table, "hydrogen_bond_acceptor_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:defined_atom_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "defined_atom_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:defined_bond_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "defined_bond_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:undefined_bond_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "undefined_bond_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:isotope_atom_count", "compound"),
                    config.createIsNotNullCondition(table, "isotope_atom_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:covalent_unit_count", "compound"),
                    config.createIsNotNullCondition(table, "covalent_unit_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:hydrogen_bond_donor_count", "compound"),
                    config.createIsNotNullCondition(table, "hydrogen_bond_donor_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:non_hydrogen_atom_count", "compound"),
                    config.createIsNotNullCondition(table, "non_hydrogen_atom_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:rotatable_bond_count", "compound"),
                    config.createIsNotNullCondition(table, "rotatable_bond_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:undefined_atom_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "undefined_atom_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:total_formal_charge", "compound"),
                    config.createIsNotNullCondition(table, "total_formal_charge"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:structure_complexity", "compound"),
                    config.createIsNotNullCondition(table, "structure_complexity"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:mono_isotopic_weight", "compound"),
                    config.createIsNotNullCondition(table, "mono_isotopic_weight"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:xlogp3_aa", "compound"),
                    config.createIsNotNullCondition(table, "xlogp3_aa"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:xlogp3", "compound"),
                    config.createIsNotNullCondition(table, "xlogp3"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:exact_mass", "compound"),
                    config.createIsNotNullCondition(table, "exact_mass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:molecular_weight", "compound"),
                    config.createIsNotNullCondition(table, "molecular_weight"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:tpsa", "compound"),
                    config.createIsNotNullCondition(table, "tpsa"));

            // extension
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:hydrogen_bond_acceptor_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "hydrogen_bond_acceptor_count"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:defined_atom_stereo_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "defined_atom_stereo_count"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:defined_bond_stereo_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "defined_bond_stereo_count"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:undefined_bond_stereo_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "undefined_bond_stereo_count"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:isotope_atom_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "isotope_atom_count"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:covalent_unit_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "covalent_unit_count"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:hydrogen_bond_donor_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "hydrogen_bond_donor_count"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:non_hydrogen_atom_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "non_hydrogen_atom_count"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:rotatable_bond_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "rotatable_bond_count"));
            config.addQuadMapping(table, graph,
                    config.createIriMapping("pubchem:undefined_atom_stereo_count", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "undefined_atom_stereo_count"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:total_formal_charge", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "total_formal_charge"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:structure_complexity", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "structure_complexity"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:mono_isotopic_weight", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "mono_isotopic_weight"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:xlogp3_aa", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "xlogp3_aa"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:xlogp3", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "xlogp3"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:exact_mass", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "exact_mass"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:molecular_weight", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject,
                    config.createIsNotNullCondition(table, "molecular_weight"));
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:tpsa", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject, config.createIsNotNullCondition(table, "tpsa"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:hydrogen_bond_acceptor_count", "compound"),
                    config.createIsNotNullCondition(table, "hydrogen_bond_acceptor_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:defined_atom_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "defined_atom_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:defined_bond_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "defined_bond_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:undefined_bond_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "undefined_bond_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:isotope_atom_count", "compound"),
                    config.createIsNotNullCondition(table, "isotope_atom_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:covalent_unit_count", "compound"),
                    config.createIsNotNullCondition(table, "covalent_unit_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:hydrogen_bond_donor_count", "compound"),
                    config.createIsNotNullCondition(table, "hydrogen_bond_donor_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:non_hydrogen_atom_count", "compound"),
                    config.createIsNotNullCondition(table, "non_hydrogen_atom_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:rotatable_bond_count", "compound"),
                    config.createIsNotNullCondition(table, "rotatable_bond_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:undefined_atom_stereo_count", "compound"),
                    config.createIsNotNullCondition(table, "undefined_atom_stereo_count"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:total_formal_charge", "compound"),
                    config.createIsNotNullCondition(table, "total_formal_charge"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:structure_complexity", "compound"),
                    config.createIsNotNullCondition(table, "structure_complexity"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:mono_isotopic_weight", "compound"),
                    config.createIsNotNullCondition(table, "mono_isotopic_weight"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:xlogp3_aa", "compound"),
                    config.createIsNotNullCondition(table, "xlogp3_aa"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:xlogp3", "compound"),
                    config.createIsNotNullCondition(table, "xlogp3"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:exact_mass", "compound"),
                    config.createIsNotNullCondition(table, "exact_mass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:molecular_weight", "compound"),
                    config.createIsNotNullCondition(table, "molecular_weight"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:tpsa", "compound"),
                    config.createIsNotNullCondition(table, "tpsa"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_molecular_formulas");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:molecular_formula", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:molecular_formula", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:molecular_formula", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_smileses");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:smiles", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:smiles", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:smiles", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_connectivity_smileses");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:connectivity_smiles", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:connectivity_smiles", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:connectivity_smiles", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_iupac_inchis");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:iupac_inchi", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:iupac_inchi", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:iupac_inchi", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_preferred_iupac_names");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:preferred_iupac_name", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:preferred_iupac_name", "compound"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:preferred_iupac_name", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_matches");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "compound_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_bases");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:hydrogen_bond_acceptor_count"),
                    config.createLiteralMapping(xsdShort, "hydrogen_bond_acceptor_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:defined_atom_stereo_count"),
                    config.createLiteralMapping(xsdShort, "defined_atom_stereo_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:defined_bond_stereo_count"),
                    config.createLiteralMapping(xsdShort, "defined_bond_stereo_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:undefined_bond_stereo_count"),
                    config.createLiteralMapping(xsdShort, "undefined_bond_stereo_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:isotope_atom_count"),
                    config.createLiteralMapping(xsdShort, "isotope_atom_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:covalent_unit_count"),
                    config.createLiteralMapping(xsdShort, "covalent_unit_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:hydrogen_bond_donor_count"),
                    config.createLiteralMapping(xsdShort, "hydrogen_bond_donor_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:non_hydrogen_atom_count"),
                    config.createLiteralMapping(xsdShort, "non_hydrogen_atom_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:rotatable_bond_count"),
                    config.createLiteralMapping(xsdShort, "rotatable_bond_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:undefined_atom_stereo_count"),
                    config.createLiteralMapping(xsdShort, "undefined_atom_stereo_count"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:total_formal_charge"),
                    config.createLiteralMapping(xsdShort, "total_formal_charge"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:structure_complexity"),
                    config.createLiteralMapping(xsdFloat, "structure_complexity"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:mono_isotopic_weight"),
                    config.createLiteralMapping(xsdFloat, "mono_isotopic_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:xlogp3"),
                    config.createLiteralMapping(xsdFloat, "xlogp3_aa"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:xlogp3"),
                    config.createLiteralMapping(xsdFloat, "xlogp3"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:exact_mass"),
                    config.createLiteralMapping(xsdFloat, "exact_mass"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:molecular_weight"),
                    config.createLiteralMapping(xsdFloat, "molecular_weight"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:tpsa"),
                    config.createLiteralMapping(xsdFloat, "tpsa"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_molecular_formulas");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:molecular_formula"),
                    config.createLiteralMapping(xsdString, "molecular_formula"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_connectivity_smileses");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:connectivity_smiles"),
                    config.createLiteralMapping(xsdString, "connectivity_smiles"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_iupac_inchis");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:iupac_inchi"),
                    config.createLiteralMapping(xsdString, "iupac_inchi"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "descriptor_compound_preferred_iupac_names");
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:preferred_iupac_name"),
                    config.createLiteralMapping(xsdString, "preferred_iupac_name"));
        }

        {
            TermMapping subject = config.createIriMapping("pubchem:compound", "compound");

            config.addQuadMapping(new DatabaseTable(schema, "inchikey_compounds"),
                    new DatabaseTable(schema, "inchikey_bases"), "inchikey", "id", graph, subject,
                    config.createIriMapping("vocab:inchikey"), config.createLiteralMapping(xsdString, "inchikey"));
        }
    }
}
