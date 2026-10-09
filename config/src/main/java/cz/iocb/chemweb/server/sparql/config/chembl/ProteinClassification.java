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
        config.addIriClass(new IntegerUserIriClass("chembl:protein_class", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/protclass/CHEMBL_PC_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_classes");
            TermMapping subject = config.createIriMapping("chembl:protein_class", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cco:ProteinClassification"),
                    config.createIsNotNullCondition(table, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                    config.createLiteralMapping(xsdString, "chembl_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:classLevel"),
                    config.createLiteralMapping(xsdString, "level"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:classPath"),
                    config.createLiteralMapping(xsdString, "path"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping("chembl:protein_class", "parent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("chembl:protein_class", "parent"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:protein_class", "parent"),
                    config.createIriMapping("skos:narrower"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_class_component_descendants");
            TermMapping subject = config.createIriMapping("chembl:protein_class", "protein_class");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetComponentDescendant"),
                    config.createIriMapping("chembl:component", "component"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:component", "component"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_class_target_descendants");
            TermMapping subject = config.createIriMapping("chembl:protein_class", "protein_class");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasTargetDescendant"),
                    config.createIriMapping("chembl:target", "target"));
            config.addQuadMapping(table, graph, config.createIriMapping("chembl:target", "target"),
                    config.createIriMapping("cco:hasProteinClassification"), subject);
        }
    }
}
