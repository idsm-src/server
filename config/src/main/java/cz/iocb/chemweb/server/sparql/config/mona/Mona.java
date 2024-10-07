package cz.iocb.chemweb.server.sparql.config.mona;

import static cz.iocb.chemweb.server.sparql.config.mona.MonaConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.matchms.Matchms;
import cz.iocb.chemweb.server.sparql.config.mesh.Mesh;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.chemweb.server.sparql.config.pubchem.Compound;
import cz.iocb.chemweb.server.sparql.config.pubchem.Substance;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.DateConstantZoneClass;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Mona
{
    public static final String mona = "https://idsm.elixir-czech.cz/rdf/mona/";
    public static final String bnmona = "https://idsm.elixir-czech.cz/rdf/mona/bn";

    public static final DateConstantZoneClass xsdDateZ = DateConstantZoneClass.get(0);


    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        Common.addPrefixes(config);
        Matchms.addPrefixes(config);
        Sachem.addPrefixes(config);

        config.addPrefix("sio", "http://semanticscience.org/resource/");
        config.addPrefix("obo", "http://purl.obolibrary.org/obo/");
        config.addPrefix("dcterms", "http://purl.org/dc/terms/");
        config.addPrefix("vcard", "http://www.w3.org/2006/vcard/ns#");
        config.addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        config.addPrefix("mona", mona);
        config.addPrefix("bnmona", bnmona);
    }


    public static void addResourceClasses(SparqlDatabaseConfiguration config) throws SQLException
    {
        Ontology.addResourceClasses(config);
        Mesh.addResourceClasses(config);
        Compound.addResourceClasses(config);
        Substance.addResourceClasses(config);

        Matchms.addResourceClasses(config);
        Sachem.addResourceClasses(config);

        config.addIriClass(new MapUserIriClass("mona:experiment", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), mona, ".*", "_EXP"));
        config.addIriClass(new MapUserIriClass("mona:compound", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), mona, ".*", "_CMPD"));
        config.addIriClass(new MapUserIriClass("mona:spectrum", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), mona, ".*", "_MS"));
        config.addIriClass(new IntegerUserIriClass("mona:library", "integer", bnmona + "id", "_library"));
        config.addIriClass(new IntegerUserIriClass("mona:submitter", "integer", bnmona + "id", "_submitter"));

        config.addIriClass(new IntegerUserIriClass("mona:splash", "integer", bnmona + "id", "_splash"));
        config.addIriClass(new IntegerUserIriClass("mona:level", "integer", bnmona + "id", "_level"));
        config.addIriClass(new IntegerUserIriClass("mona:scan", "integer", bnmona + "id", "_scan"));
        config.addIriClass(new IntegerUserIriClass("mona:ionization", "integer", bnmona + "id", "_ionization"));
        config.addIriClass(new IntegerUserIriClass("mona:molfile", "integer", bnmona + "id", "_molfile"));
        config.addIriClass(new IntegerUserIriClass("mona:name", "integer", bnmona + "id", "_name"));
        config.addIriClass(new IntegerUserIriClass("mona:inchi", "integer", bnmona + "id", "_inchi"));
        config.addIriClass(new IntegerUserIriClass("mona:inchikey", "integer", bnmona + "id", "_inchikey"));
        config.addIriClass(new IntegerUserIriClass("mona:formula", "integer", bnmona + "id", "_formula"));
        config.addIriClass(new IntegerUserIriClass("mona:smiles", "integer", bnmona + "id", "_smiles"));
        config.addIriClass(new IntegerUserIriClass("mona:exact_mass", "integer", bnmona + "id", "_exact_mass"));
        config.addIriClass(
                new IntegerUserIriClass("mona:monoisotopic_mass", "integer", bnmona + "id", "_monoisotopic_mass"));
        config.addIriClass(new IntegerUserIriClass("mona:cas_number", "integer", bnmona + "id", "_cas_number"));
        config.addIriClass(new IntegerUserIriClass("mona:hmdb_id", "integer", bnmona + "id", "_hmdb_id"));
        config.addIriClass(new IntegerUserIriClass("mona:chebi_id", "integer", bnmona + "id", "_chebi_id"));
        config.addIriClass(new IntegerUserIriClass("mona:chemspider_id", "integer", bnmona + "id", "_chemspider_id"));
        config.addIriClass(new IntegerUserIriClass("mona:kegg_id", "integer", bnmona + "id", "_kegg_id"));
        config.addIriClass(new IntegerUserIriClass("mona:lipidmaps_id", "integer", bnmona + "id", "_lipidmaps_id"));
        config.addIriClass(
                new IntegerUserIriClass("mona:pubchem_compound_id", "integer", bnmona + "id", "_pubchem_compound_id"));
        config.addIriClass(new IntegerUserIriClass("mona:pubchem_substance_id", "integer", bnmona + "id",
                "_pubchem_substance_id"));
        config.addIriClass(new IntegerUserIriClass("mona:peak", "integer", bnmona + "id", "_peak"));
        config.addIriClass(new IntegerUserIriClass("mona:annotation", "integer", bnmona + "id", "_annotation"));
        config.addIriClass(new IntegerUserIriClass("mona:tag", "integer", bnmona + "id", "_tag"));
        config.addIriClass(new IntegerUserIriClass("mona:retention_time", "integer", bnmona + "id", "_retention_time"));
        config.addIriClass(
                new IntegerUserIriClass("mona:collision_energy", "integer", bnmona + "id", "_collision_energy"));
        config.addIriClass(new IntegerUserIriClass("mona:collision_energy_ramp_start", "integer", bnmona + "id",
                "_collision_energy_ramp_start"));
        config.addIriClass(new IntegerUserIriClass("mona:collision_energy_ramp_end", "integer", bnmona + "id",
                "_collision_energy_ramp_end"));
        config.addIriClass(
                new IntegerUserIriClass("mona:instrument_type", "integer", bnmona + "id", "_instrument_type"));
        config.addIriClass(new IntegerUserIriClass("mona:instrument", "integer", bnmona + "id", "_instrument"));
        config.addIriClass(new IntegerUserIriClass("mona:precursor_type", "integer", bnmona + "id", "_precursor_type"));
        config.addIriClass(new IntegerUserIriClass("mona:precursor_mz", "integer", bnmona + "id", "_precursor_mz"));

        config.addIriClass(new StringUserIriClass("common:email", "mailto:"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("mona:");

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "id");
            NodeMapping spectrum = config.createIriMapping("mona:spectrum", "id");
            NodeMapping submitter = config.createIriMapping("mona:submitter", "submitter");
            NodeMapping library = config.createIriMapping("mona:library", "library");

            config.addQuadMapping(table, graph, compound, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011125")); // molecule
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001180")); // mass spectrometry experiment
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000294")); // mass spectrum
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000579"), // MS1 spectrum
                    config.createAreEqualCondition("level", "'1'::integer"));
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000580"), // MSn spectrum
                    config.createAreNotEqualCondition("level", "'1'::integer"));

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("dcterms:created"),
                    config.createLiteralMapping(xsdDateZ, "created"));
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("dcterms:dateAccepted"),
                    config.createLiteralMapping(xsdDateZ, "curated"));
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("dcterms:modified"),
                    config.createLiteralMapping(xsdDateZ, "updated"));

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000230"), // has input
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000231"), // is input in
                    experiment);

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000229"), // has output
                    spectrum);
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("sio:SIO_000232"), // is output of
                    experiment);

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000066"), // has provider
                    submitter);
            config.addQuadMapping(table, graph, submitter, config.createIriMapping("sio:SIO_000064"), // is provider of
                    experiment);

            config.addQuadMapping(table, graph, library, config.createIriMapping("sio:SIO_001277"), // has data item
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_001278"), // is data item in
                    library);
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("mona:spectrum", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(Matchms.spectrum, "spectrum"));
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("mona:splash", "id");
            NodeMapping spectrum = config.createIriMapping("mona:spectrum", "id");
            Conditions condition = config.createIsNotNullCondition("splash");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002599"), // splash key
                    condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "splash"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    spectrum, condition);
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject, condition);
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("mona:level", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "id");
            Conditions condition = config.createIsNotNullCondition("level");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000511"), // ms level
                    condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdInt, "level"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment, condition);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject, condition);
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("mona:scan", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "id");
            Conditions condition = config.createIsNotNullCondition("ionization_mode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000130"), // positive scan
                    config.createAreEqualCondition("ionization_mode", "'P'::char"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000129"), // negative scan
                    config.createAreEqualCondition("ionization_mode", "'N'::char"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment, condition);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject, condition);
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("mona:ionization", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "id");
            Conditions condition = config.createIsNotNullCondition("ionization_type");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000070"), // atmospheric pressure chemical ionization
                    config.createAreEqualCondition("ionization_type", "'1000070'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000071"), // chemical ionization
                    config.createAreEqualCondition("ionization_type", "'1000071'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000073"), // electrospray ionization
                    config.createAreEqualCondition("ionization_type", "'1000073'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000074"), // fast atom bombardment ionization
                    config.createAreEqualCondition("ionization_type", "'1000074'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000075"), // matrix-assisted laser desorption ionization
                    config.createAreEqualCondition("ionization_type", "'1000075'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000258"), // field ionization
                    config.createAreEqualCondition("ionization_type", "'1000258'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000389"), // electron ionization
                    config.createAreEqualCondition("ionization_type", "'1000389'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000398"), // nanoelectrospray
                    config.createAreEqualCondition("ionization_type", "'1000398'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment, condition);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject, condition);
        }

        /* TODO:
             compound_bases:
               accession
               link
        */

        {
            Table table = new Table(schema, "compound_structures");
            NodeMapping subject = config.createIriMapping("mona:molfile", "compound");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120")); // molecular structure file
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "structure"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_names");
            NodeMapping subject = config.createIriMapping("mona:name", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000043")); // molecular entity name
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "name"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_classyfires");
            NodeMapping subject = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitClassyFire, "class"));
        }

        {
            Table table = new Table(schema, "compound_chebi_classes");
            NodeMapping subject = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi"));
        }

        {
            Table table = new Table(schema, "compound_mesh_classes");
            NodeMapping subject = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:heading", "mesh"));
        }

        {
            Table table = new Table(schema, "compound_inchis");
            NodeMapping subject = config.createIriMapping("mona:inchi", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000113")); // InChI descriptor
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "inchi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_inchikeys");
            NodeMapping subject = config.createIriMapping("mona:inchikey", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000059")); // InChIKey
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "inchikey"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_formulas");
            NodeMapping subject = config.createIriMapping("mona:formula", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000042")); // molecular formula
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "formula"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_smileses");
            NodeMapping subject = config.createIriMapping("mona:smiles", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000018")); // SMILES descriptor
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "smiles"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_exact_masses");
            NodeMapping subject = config.createIriMapping("mona:exact_mass", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000217")); // exact mass descriptor
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "mass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000055")); // molar mass unit

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_monoisotopic_masses");
            NodeMapping subject = config.createIriMapping("mona:monoisotopic_mass", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000218")); // monoisotopic mass descriptor
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "mass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000055")); // molar mass unit

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "compound_cas_numbers");
            NodeMapping subject = config.createIriMapping("mona:cas_number", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000446")); // CAS registry number
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "cas"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_hmdb_ids");
            NodeMapping subject = config.createIriMapping("mona:hmdb_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000408")); // HMDB identifier
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "('HMDB' || hmdb)"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_chebi_ids");
            NodeMapping subject = config.createIriMapping("mona:chebi_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000407")); // ChEBI identifier
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "('CHEBI:' || chebi)"));

            config.addQuadMapping(table, graph, compound, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_chemspider_ids");
            NodeMapping subject = config.createIriMapping("mona:chemspider_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000405")); // ChemSpider identifier
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "('' || chemspider)"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_kegg_ids");
            NodeMapping subject = config.createIriMapping("mona:kegg_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000409")); // KEGG identifier
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "kegg"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        /* TODO:
             compound_knapsack_ids
             compound_lipidbank_ids
        */

        {
            Table table = new Table(schema, "compound_lipidmaps_ids");
            NodeMapping subject = config.createIriMapping("mona:lipidmaps_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000564")); // LipidMaps identifier
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "lipidmaps"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_pubchem_compound_ids");
            NodeMapping subject = config.createIriMapping("mona:pubchem_compound_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000140")); // PubChem compound identifier (CID)
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "('CID' || cid)"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "compound_pubchem_compounds");
            NodeMapping subject = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pubchem:compound", "cid"));
        }

        {
            Table table = new Table(schema, "compound_pubchem_substance_ids");
            NodeMapping subject = config.createIriMapping("mona:pubchem_substance_id", "id");
            NodeMapping compound = config.createIriMapping("mona:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000141")); // PubChem substance identifier (SID)
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "('SID' || sid)"));

            config.addQuadMapping(table, graph, compound, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pubchem:substance", "sid"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000672"), // is identifier for
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000671"), // has identifier
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_annotations");
            NodeMapping subject = config.createIriMapping("mona:peak", "id");
            NodeMapping spectrum = config.createIriMapping("mona:spectrum", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000231")); // peak
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000056"), // position
                    config.createLiteralMapping(xsdFloat, "peak"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000313"), // is component part of
                    spectrum);
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("sio:SIO_000369"), // has component part
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_annotations");
            NodeMapping subject = config.createIriMapping("mona:annotation", "id");
            NodeMapping peak = config.createIriMapping("mona:peak", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001166")); // annotation
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "value"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000254"), // is annotation of
                    peak);
            config.addQuadMapping(table, graph, peak, config.createIriMapping("sio:SIO_000255"), // has annotation
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_tags");
            NodeMapping subject = config.createIriMapping("mona:tag", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001166")); // annotation
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "tag"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000254"), // is annotation of
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000255"), // has annotation
                    subject);
        }

        /* TODO:
             spectrum_normalized_entropies
             spectrum_spectral_entropies
        */

        {
            Table table = new Table(schema, "spectrum_retention_times");
            NodeMapping subject = config.createIriMapping("mona:retention_time", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000894")); // retention time
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "time"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000010"), // second
                    config.createAreEqualCondition("unit", "'10'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000031"), // minute
                    config.createAreEqualCondition("unit", "'31'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_collision_energies");
            NodeMapping subject = config.createIriMapping("mona:collision_energy", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000045"), // collision energy
                    config.createAreNotEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000138"), // normalized collision energy
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "energy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000190"), // ratio unit
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000218"), // volt
                    config.createAreEqualCondition("unit", "'218'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000248"), // kilovolt
                    config.createAreEqualCondition("unit", "'248'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000266"), // electronvolt
                    config.createAreEqualCondition("unit", "'266'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_collision_energy_ramps");
            NodeMapping subject = config.createIriMapping("mona:collision_energy_ramp_start", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002013"), // collision energy ramp start
                    config.createAreNotEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002218"), // percent collision energy ramp start
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ramp_start"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000190"), // ratio unit
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000218"), // volt
                    config.createAreEqualCondition("unit", "'218'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000266"), // electronvolt
                    config.createAreEqualCondition("unit", "'266'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_collision_energy_ramps");
            NodeMapping subject = config.createIriMapping("mona:collision_energy_ramp_end", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002014"), // collision energy ramp end
                    config.createAreNotEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002219"), // percent collision energy ramp end
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ramp_end"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000190"), // ratio unit
                    config.createAreEqualCondition("unit", "'190'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000218"), // volt
                    config.createAreEqualCondition("unit", "'218'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000266"), // electronvolt
                    config.createAreEqualCondition("unit", "'266'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_instrument_types");
            NodeMapping subject = config.createIriMapping("mona:instrument_type", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000463")); // instrument
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "type"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_instruments");
            NodeMapping subject = config.createIriMapping("mona:instrument", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000031")); // instrument model
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "instrument"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_precursor_types");
            NodeMapping subject = config.createIriMapping("mona:precursor_type", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002813")); // adduct ion formula
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdString, "type"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_precursor_mzs");
            NodeMapping subject = config.createIriMapping("mona:precursor_mz", "id");
            NodeMapping experiment = config.createIriMapping("mona:experiment", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000744")); // selected ion m/z
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "mz"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:MS_1000040")); // mz

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "library_bases");
            NodeMapping subject = config.createIriMapping("mona:library", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_000089")); // dataset

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping(xsdString, "description"));
        }

        {
            Table table = new Table(schema, "submitter_bases");
            NodeMapping subject = config.createIriMapping("mona:submitter", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vcard:Individual"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:given-name"),
                    config.createLiteralMapping(xsdString, "first_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:family-name"),
                    config.createLiteralMapping(xsdString, "last_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:hasEmail"),
                    config.createIriMapping("common:email", "email"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:organization-name"),
                    config.createLiteralMapping(xsdString, "institution"));
        }
    }


    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        Matchms.addFunctions(config);
    }
}
