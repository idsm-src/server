package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Bioassay
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:bioassay", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_description", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Description"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_protocol", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Protocol"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_comment", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Comment"));
        config.addIriClass(new IntegerUserIriClass("pubchem:chembl_mechanism", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/assay/drug_mech_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:bioassay");

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_bases");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:BioAssay"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000015"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_stages");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000210"),
                    config.createIriMapping("ontology:resource", Ontology.unitBAO, "stage"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_confirmatory_assays");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "confirmatory_assay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000540"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_primary_assays");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "primary_assay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001067"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_summary_assays");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "summary_assay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001094"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"));
        }

        // extensions
        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_chembl_assays");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("chembl:assay", "chembl_assay"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_chembl_mechanisms");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("pubchem:chembl_mechanism", "chembl_mechanism"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_data");
            TermMapping subject = config.createIriMapping("pubchem:bioassay_description", "bioassay");
            Conditions conditions = config.createAreEqualCondition("type_id", "'136'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_000136"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), conditions);

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:bioassay", "bioassay"),
                    config.createIriMapping("sio:SIO_000008"), subject, conditions);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "value"), conditions);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_data");
            TermMapping subject = config.createIriMapping("pubchem:bioassay_protocol", "bioassay");
            Conditions conditions = config.createAreEqualCondition("type_id", "'1041'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001041"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), conditions);

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:bioassay", "bioassay"),
                    config.createIriMapping("sio:SIO_000008"), subject, conditions);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "value"), conditions);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "bioassay_data");
            TermMapping subject = config.createIriMapping("pubchem:bioassay_comment", "bioassay");
            Conditions conditions = config.createAreEqualCondition("type_id", "'1167'::smallint");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001167"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"), conditions);

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:bioassay", "bioassay"),
                    config.createIriMapping("sio:SIO_000008"), subject, conditions);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:bioassay", "bioassay"), conditions);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "value"), conditions);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_bases");
            TermMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000209"),
                    config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup"));
        }
    }
}
