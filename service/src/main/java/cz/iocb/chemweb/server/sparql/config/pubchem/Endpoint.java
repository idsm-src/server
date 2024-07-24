package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdFloat;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.util.List;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.GeneralUserIriClass;



public class Endpoint
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new GeneralUserIriClass("pubchem:endpoint", schema, "endpoint",
                List.of("integer", "integer", "integer", "integer"),
                "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/endpoint/SID[1-9][0-9]*_AID[1-9][0-9]*(_(PMID([1-9][0-9]*)?|[1-9][0-9]*|0))?_VALUE[1-9][0-9]*"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:endpoint");

        {
            Table table = new Table(schema, "endpoint_bases");
            NodeMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:IAO_0000136"),
                    config.createIriMapping("pubchem:substance", "substance"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:PubChemAssayOutcome"),
                    config.createIriMapping("ontology:resource", Ontology.unitUncategorized, "outcome_id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Endpoint.vm"));
        }

        {
            Table table = new Table(schema, "endpoint_measurements");
            NodeMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");
            Conditions condition = config.createIsNotNullCondition("measurement");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000221"),
                    config.createIriMapping("obo:UO_0000064"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitBAO, "endpoint_type_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'300'::int"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000738"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'738'::int"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000734"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'734'::int"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000735"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'735'::int"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000733"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'733'::int"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000699"),
                    config.createLiteralMapping(xsdFloat, "measurement"),
                    config.createAreEqualCondition("measurement_type_id", "'699'::int"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-unit"),
                    config.createIriMapping("obo:UO_0000064"), condition);
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdFloat, "measurement"));
        }

        {
            Table table = new Table(schema, "endpoint_references");
            NodeMapping subject = config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup",
                    "value");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:citesAsDataSource"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }
    }
}
