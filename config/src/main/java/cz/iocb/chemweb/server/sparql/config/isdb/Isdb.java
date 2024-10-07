package cz.iocb.chemweb.server.sparql.config.isdb;

import static cz.iocb.chemweb.server.sparql.config.isdb.IsdbConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.matchms.Matchms;
import cz.iocb.chemweb.server.sparql.config.pubchem.Compound;
import cz.iocb.chemweb.server.sparql.config.sachem.Sachem;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Isdb
{
    public static final String isdb = "https://idsm.elixir-czech.cz/rdf/isdb/";
    public static final String bnisdb = "https://idsm.elixir-czech.cz/rdf/isdb/bn";
    public static final String isdbLibrary = "<https://idsm.elixir-czech.cz/rdf/isdb/library/isdb>";


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

        config.addPrefix("isdb", isdb);
        config.addPrefix("bnisdb", bnisdb);
    }


    public static void addResourceClasses(SparqlDatabaseConfiguration config) throws SQLException
    {
        Compound.addResourceClasses(config);
        Matchms.addResourceClasses(config);
        Sachem.addResourceClasses(config);

        config.addIriClass(new IntegerUserIriClass("wikidata:entity", "integer", "http://www.wikidata.org/entity/Q"));

        config.addIriClass(new IsdbUserIriClass("isdb:experiment", isdb, "_EXP"));
        config.addIriClass(new MapUserIriClass("isdb:compound", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), isdb, 14, "[A-Z]{14}", "_CMPD"));
        config.addIriClass(new IsdbUserIriClass("isdb:spectrum", isdb, "_MS"));

        config.addIriClass(new MapUserIriClass("isdb:inchi", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), bnisdb, 14, "[A-Z]{14}", "_inchi"));
        config.addIriClass(new MapUserIriClass("isdb:formula", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), bnisdb, 14, "[A-Z]{14}", "_formula"));
        config.addIriClass(new MapUserIriClass("isdb:smiles", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), bnisdb, 14, "[A-Z]{14}", "_smiles"));
        config.addIriClass(new MapUserIriClass("isdb:exact_mass", "integer", new Table(schema, "compound_bases"),
                new TableColumn("id"), new TableColumn("accession"), bnisdb, 14, "[A-Z]{14}", "_exact_mass"));

        config.addIriClass(new IsdbUserIriClass("isdb:scan", bnisdb, "_scan"));
        config.addIriClass(new IsdbUserIriClass("isdb:ionization", bnisdb, "_ionization"));
        config.addIriClass(new IsdbUserIriClass("isdb:level", bnisdb, "_level"));
        config.addIriClass(new IsdbUserIriClass("isdb:instrument_type", bnisdb, "_instrument_type"));
        config.addIriClass(new IsdbUserIriClass("isdb:instrument", bnisdb, "_instrument"));
        config.addIriClass(new IsdbUserIriClass("isdb:precursor_type", bnisdb, "_precursor_type"));
        config.addIriClass(new IsdbUserIriClass("isdb:charge_state", bnisdb, "_charge_state"));
        config.addIriClass(new IsdbUserIriClass("isdb:precursor_mz", bnisdb, "_precursor_mz"));
        config.addIriClass(new IsdbUserIriClass("isdb:tag", bnisdb, "_tag"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("isdb:");

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");

            config.addQuadMapping(table, graph, compound, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011125")); // molecule
        }

        {
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("isdb:inchi", "id");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");

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
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("isdb:formula", "id");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");

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
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("isdb:smiles", "id");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");

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
            Table table = new Table(schema, "compound_bases");
            NodeMapping subject = config.createIriMapping("isdb:exact_mass", "id");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000217")); // exact mass descriptor
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "exact_mass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000055")); // molar mass unit

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"), // is attribute of
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000008"), // has attribute
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping compound = config.createIriMapping("isdb:compound", "id");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");
            NodeMapping spectrum = config.createIriMapping("isdb:spectrum", "id", "ionmode");
            NodeMapping library = config.createIriMapping(isdbLibrary);

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001180")); // mass spectrometry experiment
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000294")); // mass spectrum
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000580")); // MSn spectrum

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000230"), // has input
                    compound);
            config.addQuadMapping(table, graph, compound, config.createIriMapping("sio:SIO_000231"), // is input in
                    experiment);

            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000229"), // has output
                    spectrum);
            config.addQuadMapping(table, graph, spectrum, config.createIriMapping("sio:SIO_000232"), // is output of
                    experiment);

            config.addQuadMapping(table, graph, library, config.createIriMapping("sio:SIO_001277"), // has data item
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_001278"), // is data item in
                    library);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:spectrum", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(Matchms.spectrum, "spectrum"));
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:scan", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000130"), // positive scan
                    config.createAreEqualCondition("ionmode", "'P'::char"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000129"), // negative scan
                    config.createAreEqualCondition("ionmode", "'N'::char"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:ionization", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000073")); // electrospray ionization

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:level", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000511")); // ms level
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(2));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:instrument_type", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000463")); // instrument
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping("in-silico ESI-MS/MS"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        /*
        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:instrument", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000031")); // instrument model
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping("")); //TODO:

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }
        */

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:precursor_type", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1002813")); // adduct ion formula
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping("[M-H]-"), config.createAreEqualCondition("ionmode", "'N'::char"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping("[M+H]+"), config.createAreEqualCondition("ionmode", "'P'::char"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:charge_state", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000041")); // charge state
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(-1), config.createAreEqualCondition("ionmode", "'N'::char"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(1), config.createAreEqualCondition("ionmode", "'P'::char"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:precursor_mz", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MS_1000744")); // selected ion m/z
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "pepmass"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:MS_1000040")); // mz

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000553"), // is parameter in
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000552"), // has parameter
                    subject);
        }

        {
            Table table = new Table(schema, "spectrum_bases");
            NodeMapping subject = config.createIriMapping("isdb:tag", "id", "ionmode");
            NodeMapping experiment = config.createIriMapping("isdb:experiment", "id", "ionmode");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001166")); // annotation
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping("In-Silico"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000254"), // is annotation of
                    experiment);
            config.addQuadMapping(table, graph, experiment, config.createIriMapping("sio:SIO_000255"), // has annotation
                    subject);
        }

        {
            NodeMapping subject = config.createIriMapping(isdbLibrary);

            config.addQuadMapping(null, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_000089")); // dataset

            config.addQuadMapping(null, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping("In Silico Spectral Databases of Natural Products"));
            config.addQuadMapping(null, graph, subject, config.createIriMapping("dcterms:description"),
                    config.createLiteralMapping("An In Silico spectral DataBase (ISDB) of natural products "
                            + "calculated from structures aggregated in the frame of the LOTUS Initiative."));
        }

        {
            Table table = new Table(schema, "compound_pubchem_compounds");
            NodeMapping subject = config.createIriMapping("isdb:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("pubchem:compound", "cid"));
        }

        {
            Table table = new Table(schema, "compound_wikidata_compounds");
            NodeMapping subject = config.createIriMapping("isdb:compound", "compound");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("wikidata:entity", "wikidata"));
        }
    }


    public static void addFunctions(SparqlDatabaseConfiguration config)
    {
        Matchms.addFunctions(config);
    }
}
