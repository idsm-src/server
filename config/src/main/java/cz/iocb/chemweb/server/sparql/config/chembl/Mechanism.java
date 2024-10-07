package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Mechanism
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:mechanism", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/drug_mechanism/CHEMBL_MEC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        Table table = new Table(schema, "drug_mechanism");
        NodeMapping subject = config.createIriMapping("chembl:mechanism", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Mechanism"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasBindingSite"),
                config.createIriMapping("chembl:binding_site", "site_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasMolecule"),
                config.createIriMapping("chembl:compound", "molecule_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTarget"),
                config.createIriMapping("chembl:target", "target_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:mechanismDescription"),
                config.createLiteralMapping(xsdString, "mechanism_of_action"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:mechanismActionType"),
                config.createLiteralMapping(xsdString, "action_type"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:binding_site", "site_id"),
                config.createIriMapping("cco:isBindingSiteForMechanism"), subject);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:compound", "molecule_id"),
                config.createIriMapping("cco:hasMechanism"), subject);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target_id"),
                config.createIriMapping("cco:isTargetForMechanism"), subject);
    }
}
