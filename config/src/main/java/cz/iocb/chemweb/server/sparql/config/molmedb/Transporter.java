package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Transporter
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("molmedb:transporter", INT4, "https://rdf.molmedb.upol.cz/transporter/tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:transporter_measure_group", INT4,
                "https://rdf.molmedb.upol.cz/transporter/measure_group_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_substrate", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_substrate_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_inhibitor", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_inhibitor_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_pkm", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_pKm_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_pec50", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_pEC50_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_pki", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_pKi_tra"));

        config.addIriClass(new IntegerUserIriClass("molmedb:endpoint_pic50", INT4,
                "https://rdf.molmedb.upol.cz/transporter/endpoint_pIC50_tra"));

        config.addIriClass(
                new IntegerUserIriClass("molmedb:protein", INT4, "https://rdf.molmedb.upol.cz/transporter/target"));

        config.addIriClass(new IntegerUserIriClass("molmedb:protein_uniprot", INT4,
                "https://rdf.molmedb.upol.cz/transporter/target", "_UniProt"));

        config.addIriClass(new StringUserIriClass("purl:uniprot", "http://purl.uniprot.org/uniprot/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("<https://rdf.molmedb.upol.cz>");


        /*
         * transporter interaction cenral node
         */

        // triples map #1
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            TermMapping subject = config.createIriMapping("molmedb:transporter", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0003008")); // transporter assay

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:protein", "protein"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000209"), // has measure group
                    config.createIriMapping("molmedb:transporter_measure_group", "id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prov:wasQuotedFrom"),
                    config.createIriMapping("molmedb:reference", "model_reference"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prov:hadPrimarySource"),
                    config.createIriMapping("molmedb:reference", "reference"));
        }


        /*
         * transporter interaction measure group
         */

        // triples map #2
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            TermMapping subject = config.createIriMapping("molmedb:transporter_measure_group", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000040")); // measure group

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_pkm", "id"),
                    config.createIsNotNullCondition(table, "km"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_pec50", "id"),
                    config.createIsNotNullCondition(table, "ec50"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_pki", "id"),
                    config.createIsNotNullCondition(table, "ki"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"), // has endpoint
                    config.createIriMapping("molmedb:endpoint_pic50", "id"),
                    config.createIsNotNullCondition(table, "ic50"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:substance", "substance"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0090012"), // has participant
                    config.createIriMapping("molmedb:protein", "protein"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000426"), // is measure group of
                    config.createIriMapping("molmedb:transporter", "id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:comment"),
                    config.createLiteralMapping(xsdString, "comment"));
        }


        /*
         * transporter proteins
         */

        // triples map #3
        {
            DatabaseTable table = new DatabaseTable(schema, "proteins");
            TermMapping subject = config.createIriMapping("molmedb:protein", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000283")); // transporter

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "name"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("purl:uniprot", "uniprot_id"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("molmedb:protein_uniprot", "id"));
        }

        // triples map #4
        {
            DatabaseTable table = new DatabaseTable(schema, "proteins");
            TermMapping subject = config.createIriMapping("molmedb:protein_uniprot", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("edam:data_2291")); // UniProt ID //FIXME

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "uniprot_id"));
        }


        /*
         * transporter endpoints
         */

        // triples map #5
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createAreEqualCondition("category", "'512'::integer", "'513'::integer");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_substrate", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:SubstrateBindingAssay"), cnd);

            // TODO: check in more detail
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createIriMapping("<https://w3id.org/reproduceme#PositiveResult>"),
                    config.createAreEqualCondition("category", "'512'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createIriMapping("<https://w3id.org/reproduceme#NegativeResult>"),
                    config.createAreEqualCondition("category", "'513'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0095000"), // is derived from
                    config.createIriMapping("molmedb:endpoint_pec50", "id"),
                    Conditions.and(cnd, config.createIsNotNullCondition(table, "ec50")));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0095000"), // is derived from
                    config.createIriMapping("molmedb:endpoint_pkm", "id"),
                    Conditions.and(cnd, config.createIsNotNullCondition(table, "km")));
        }

        // triples map #6
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createAreEqualCondition("category", "'514'::integer", "'515'::integer");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_inhibitor", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:InhibitionAssay"), cnd);

            // TODO: check in more detail
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createIriMapping("<https://w3id.org/reproduceme#PositiveResult>"),
                    config.createAreEqualCondition("category", "'514'::integer"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createIriMapping("<https://w3id.org/reproduceme#NegativeResult>"),
                    config.createAreEqualCondition("category", "'515'::integer"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0095000"), // is derived from
                    config.createIriMapping("molmedb:endpoint_pic50", "id"),
                    Conditions.and(cnd, config.createIsNotNullCondition(table, "ic50")));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0095000"), // is derived from
                    config.createIriMapping("molmedb:endpoint_pki", "id"),
                    Conditions.and(cnd, config.createIsNotNullCondition(table, "ki")));
        }

        // triples map #7
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createIsNotNullCondition(table, "km");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_pkm", "id");

            //TODO: check in more detail
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("mmdbvoc:PKm"), cnd);

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "km"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000062"), cnd); // molar

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "km_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:transporter_measure_group", "id"), cnd);
        }

        // triples map #8
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createIsNotNullCondition(table, "ec50");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_pec50", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0002583"), cnd); // pEC50

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ec50"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000062"), cnd); // molar

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "ec50_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:transporter_measure_group", "id"), cnd);
        }

        // triples map #9
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createIsNotNullCondition(table, "ki");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_pki", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0190004"), cnd); // pKi

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ki"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000062"), cnd); // molar

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "ki_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:transporter_measure_group", "id"), cnd);
        }

        // triples map #10
        {
            DatabaseTable table = new DatabaseTable(schema, "transporters");
            Conditions cnd = config.createIsNotNullCondition(table, "ic50");
            TermMapping subject = config.createIriMapping("molmedb:endpoint_pic50", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000199"), cnd); // pIC50

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"), // has value
                    config.createLiteralMapping(xsdFloat, "ic50"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"), // has unit
                    config.createIriMapping("obo:UO_0000062"), cnd); // molar

            config.addQuadMapping(table, graph, subject, config.createIriMapping("mmdbvoc:hasStDev"),
                    config.createLiteralMapping(xsdFloat, "ic50_accuracy"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000559"), // is endpoint of
                    config.createIriMapping("molmedb:transporter_measure_group", "id"), cnd);
        }
    }
}
