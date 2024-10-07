package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Bioassay
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_description", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Description"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_protocol", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Protocol"));
        config.addIriClass(new IntegerUserIriClass("pubchem:bioassay_comment", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID", "_Comment"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:bioassay");

        {
            Table table = new Table(schema, "bioassay_bases");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000015"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
        }

        {
            Table table = new Table(schema, "bioassay_stages");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000210"),
                    config.createIriMapping("ontology:resource", Ontology.unitBAO, "stage"));
        }

        {
            Table table = new Table(schema, "bioassay_confirmatory_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000540"),
                    config.createIriMapping("pubchem:bioassay", "confirmatory_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_primary_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001067"),
                    config.createIriMapping("pubchem:bioassay", "primary_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_summary_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0001094"),
                    config.createIriMapping("pubchem:bioassay", "summary_assay"));
        }

        // extensions
        {
            Table table = new Table(schema, "bioassay_chembl_assays");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:assay", "chembl_assay"));
        }

        {
            Table table = new Table(schema, "bioassay_chembl_mechanisms");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:mechanism", "chembl_mechanism"));
        }

        {
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_description", "bioassay");
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
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_protocol", "bioassay");
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
            Table table = new Table(schema, "bioassay_data");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay_comment", "bioassay");
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
            Table table = new Table(schema, "bioassay_patent_references");
            NodeMapping subject = config.createIriMapping("pubchem:bioassay", "bioassay");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }
    }
}
