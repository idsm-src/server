package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Journal
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:journal", "integer",
                "http://rdf.ebi.ac.uk/resource/chembl/journal/CHEMBL_JRN_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");

        Table table = new Table(schema, "journal_dictionary");
        NodeMapping subject = config.createIriMapping("chembl:journal", "id");

        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Journal"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "label"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                config.createLiteralMapping(xsdString, "title"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:shortTitle"),
                config.createLiteralMapping(xsdString, "short_title"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:issn"),
                config.createLiteralMapping(xsdString, "issn"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:eissn"),
                config.createLiteralMapping(xsdString, "eissn"));

        // extension
        config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                config.createLiteralMapping("chembl/Journal.vm"));
    }
}
