package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class ProteinClassification
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:protclass", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_classification");
            TermMapping subject = config.createIriMapping("chembl:protclass", "id");

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
            DatabaseTable table = new DatabaseTable(schema, "protein_classification_paths");
            TermMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:classPath"),
                    config.createLiteralMapping(xsdString, "path"));
        }


        {
            DatabaseTable table = new DatabaseTable(schema, "component_classes");
            TermMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetComponentDescendant"),
                    config.createIriMapping("chembl:targetcomponent", "component_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:targetcomponent", "component_id"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "target_classes");
            TermMapping subject = config.createIriMapping("chembl:protclass", "protein_class_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetDescendant"),
                    config.createIriMapping("chembl:target", "target_id"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target_id"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }
    }
}
