package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class BindingSite
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:binding_site", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/binding_site/CHEMBL_BS_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        Table table = new Table(schema, "binding_sites");
        NodeMapping subject = config.createIriMapping("chembl:binding_site", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:BindingSite"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTarget"),
                config.createIriMapping("chembl:target", "target_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:bindingSiteName"),
                config.createLiteralMapping(xsdString, "site_name"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target_id"),
                config.createIriMapping("cco:hasBindingSite"), subject);

        // extension
        config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                config.createLiteralMapping("chembl/BindingSite.vm"));
    }
}
