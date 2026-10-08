package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Mechanism
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:mechanism", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/drug_mechanism/CHEMBL_MEC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        DatabaseTable table = new DatabaseTable(schema, "mechanism_bases");
        TermMapping subject = config.createIriMapping("chembl:mechanism", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Mechanism"), config.createIsNotNullCondition(table, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasBindingSite"),
                config.createIriMapping("chembl:binding_site", "binding_site"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMolecule"),
                config.createIriMapping("chembl:compound", "molecule"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTarget"),
                config.createIriMapping("chembl:target", "target"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:mechanismDescription"),
                config.createLiteralMapping(xsdString, "description"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:mechanismActionType"),
                config.createLiteralMapping(xsdString, "action_type"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:binding_site", "binding_site"),
                config.createIriMapping("cco:isBindingSiteForMechanism"), subject);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "molecule"),
                config.createIriMapping("cco:hasMechanism"), subject);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target"),
                config.createIriMapping("cco:isTargetForMechanism"), subject);
    }
}
