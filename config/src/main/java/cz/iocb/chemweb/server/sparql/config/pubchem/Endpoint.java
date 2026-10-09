package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.GenericUserIriClass;



public class Endpoint
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new GenericUserIriClass("pubchem:endpoint", schema, "endpoint",
                List.of(INT4, INT4, INT4, INT4),
                "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/endpoint/SID[1-9][0-9]*_AID[1-9][0-9]*(_(PMID([1-9][0-9]*)?|[1-9][0-9]*|0))?_VALUE[1-9][0-9]*"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:endpoint");

        {
            DatabaseTable table = new DatabaseTable(schema, "endpoints");
            TermMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Endpoint"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000136"),
                    config.createIriMapping("pubchem:substance", "substance"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:PubChemAssayOutcome"),
                    config.createIriMapping("ontology:uncategorized", "outcome_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "endpoint_measurements");
            TermMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");
            Conditions condition = config.createIsNotNullCondition(table, "measurement");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"),
                    config.createIriMapping("obo:UO_0000064"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:bao", "endpoint_type_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "measurement"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:hasQualifier"),
                    config.createLiteralMapping(xsdString, "qualifier"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-unit"),
                    config.createIriMapping("obo:UO_0000064"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdFloat, "measurement"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "endpoint_references");
            TermMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:citesAsDataSource"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }
    }
}
