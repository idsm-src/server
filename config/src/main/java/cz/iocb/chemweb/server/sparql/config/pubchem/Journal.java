package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Journal
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:journal", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:journal");

        {
            DatabaseTable table = new DatabaseTable(schema, "journals");
            TermMapping subject = config.createIriMapping("pubchem:journal", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Journal"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("fabio:Journal"));
            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("fabio:hasNationalLibraryOfMedicineJournalId"),
                    config.createLiteralMapping(xsdString, "catalog_id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("fabio:hasNLMJournalTitleAbbreviation"),
                    config.createLiteralMapping(xsdString, "abbreviation"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:issn"),
                    config.createLiteralMapping(xsdString, "issn"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:eissn"),
                    config.createLiteralMapping(xsdString, "eissn"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("ncbi:journal", "catalog_id"));
        }
    }
}
