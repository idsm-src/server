package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.IntegerUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;



class Mechanism
{
    static void addIriClasses(ChemblConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("mechanism", "bigint",
                "http://rdf.ebi.ac.uk/resource/chembl/drug_mechanism/CHEMBL_MEC_"));
    }


    static void addQuadMapping(ChemblConfiguration config)
    {
        UserIriClass mechanism = config.getIriClass("mechanism");
        ConstantIriMapping graph = config.createIriMapping("<http://rdf.ebi.ac.uk/dataset/chembl>");

        String table = "drug_mechanism";
        NodeMapping subject = config.createIriMapping(mechanism, "mec_id");

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Mechanism"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:hasBindingSite"),
                config.createIriMapping("binding_site", "site_id"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:hasMolecule"),
                config.createIriMapping("molecule", "molregno"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:hasTarget"),
                config.createIriMapping("target", "tid"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "('CHEMBL_MEC_' || mec_id)"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "('CHEMBL_MEC_' || mec_id)"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:mechanismDescription"),
                config.createLiteralMapping(xsdString, "mechanism_of_action"));

        config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("cco:mechanismActionType"),
                config.createLiteralMapping(xsdString, "action_type"));

        config.addQuadMapping(schema, table, graph, config.createIriMapping("binding_site", "site_id"),
                config.createIriMapping("cco:isBindingSiteForMechanism"), subject);

        config.addQuadMapping(schema, table, graph, config.createIriMapping("molecule", "molregno"),
                config.createIriMapping("cco:hasMechanism"), subject);

        config.addQuadMapping(schema, table, graph, config.createIriMapping("target", "tid"),
                config.createIriMapping("cco:isTargetForMechanism"), subject);
    }
}
