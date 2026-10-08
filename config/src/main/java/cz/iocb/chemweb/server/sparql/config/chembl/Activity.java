package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdBoolean;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdDouble;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Activity
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:activity", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/activity/CHEMBL_ACT_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "activity_bases");
        TermMapping subject = config.createIriMapping("chembl:activity", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Activity"), config.createIsNotNullCondition(table, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0000208"),
                config.createIriMapping("ontology:bao", "endpoint_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasUnitOnto"),
                config.createIriMapping("ontology:uo", "unit_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasQUDT"),
                config.createIriMapping("ontology:uncategorized", "qudt_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMolecule"),
                config.createIriMapping("chembl:compound", "molecule"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasDocument"),
                config.createIriMapping("chembl:document", "document"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:dataValidityIssue"),
                config.createLiteralMapping(true), config.createIsNotNullCondition(table, "validity_comment"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:potentialDuplicate"),
                config.createLiteralMapping(xsdBoolean, "potential_duplicate"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:value"),
                config.createLiteralMapping(xsdDouble, "value"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:standardValue"),
                config.createLiteralMapping(xsdDouble, "standard_value"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:pChembl"),
                config.createLiteralMapping(xsdDouble, "pchembl"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:units"),
                config.createLiteralMapping(xsdString, "units"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:type"),
                config.createLiteralMapping(xsdString, "type"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:relation"),
                config.createLiteralMapping(xsdString, "relation"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:standardUnits"),
                config.createLiteralMapping(xsdString, "standard_units"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:standardType"),
                config.createLiteralMapping(xsdString, "standard_type"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:standardRelation"),
                config.createLiteralMapping(xsdString, "standard_relation"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:activityComment"),
                config.createLiteralMapping(xsdString, "comment"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:dataValidityComment"),
                config.createLiteralMapping(xsdString, "validity_comment"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "molecule"),
                config.createIriMapping("cco:hasActivity"), subject);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:document", "document"),
                config.createIriMapping("cco:hasActivity"), subject);
    }
}
