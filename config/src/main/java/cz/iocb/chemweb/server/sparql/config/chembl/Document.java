package cz.iocb.chemweb.server.sparql.config.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.ChemblConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Document
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("chembl:document", INT4,
                "http://rdf.ebi.ac.uk/resource/chembl/document/CHEMBL"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("ebi:chembl");
        DatabaseTable table = new DatabaseTable(schema, "documents");

        // a document only referenced by other entities is not described
        Conditions described = config.createIsNotNullCondition(table, "chembl_id");
        Conditions withoutJournal = Conditions.and(described, config.createIsNullCondition(table, "journal"));

        TermMapping subject = config.createIriMapping("chembl:document", "id");
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("cco:Document"), described);
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:pmid"),
                config.createIriMapping("identifiers:pubmed", "pubmed"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasJournal"),
                config.createIriMapping("chembl_journal:CHEMBL_JRN_null"), withoutJournal);
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:hasJournal"),
                config.createIriMapping("chembl:journal", "journal"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:date"),
                config.createLiteralMapping(xsdInt, "year"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:chemblId"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                config.createLiteralMapping(xsdString, "chembl_id"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("cco:documentType"),
                config.createLiteralMapping(xsdString, "type"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                config.createLiteralMapping(xsdString, "title"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:pageStart"),
                config.createLiteralMapping(xsdString, "first_page"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:pageEnd"),
                config.createLiteralMapping(xsdString, "last_page"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:volume"),
                config.createLiteralMapping(xsdString, "volume"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:doi"),
                config.createLiteralMapping(xsdString, "doi"));
        config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:issue"),
                config.createLiteralMapping(xsdString, "issue"));
        config.addQuadMapping(table, graph, config.createIriMapping("chembl_journal:CHEMBL_JRN_null"),
                config.createIriMapping("cco:hasDocument"), subject, withoutJournal);
        config.addQuadMapping(table, graph, config.createIriMapping("chembl:journal", "journal"),
                config.createIriMapping("cco:hasDocument"), subject);

        // extension
        config.addQuadMapping(table, graph, config.createIriMapping("pubchem:reference", "pubmed"),
                config.createIriMapping("skos:exactMatch"), subject);
    }
}
