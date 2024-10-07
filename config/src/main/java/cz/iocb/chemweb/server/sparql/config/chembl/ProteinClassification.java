package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class ProteinClassification
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:protclass", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            Table table = new Table(schema, "protein_classification");
            NodeMapping subject = config.createIriMapping("chembl:protclass", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinClassification"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "pref_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "pref_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:classLevel"),
                    config.createLiteralMapping(xsdString, "class_level_name"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping("chembl:protclass", "parent_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("chembl:protclass", "parent_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:protclass", "parent_id"),
                    config.createIriMapping("skos:narrower"), subject);
        }

        {
            Table table = new Table(schema, "protein_classification_paths");
            NodeMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:classPath"),
                    config.createLiteralMapping(xsdString, "path"));
        }


        {
            Table table = new Table(schema, "component_classes");
            NodeMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetComponentDescendant"),
                    config.createIriMapping("chembl:targetcomponent", "component_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:targetcomponent", "component_id"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }

        {
            Table table = new Table(schema, "target_classes");
            NodeMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetDescendant"),
                    config.createIriMapping("chembl:target", "target_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target_id"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }
    }
}
