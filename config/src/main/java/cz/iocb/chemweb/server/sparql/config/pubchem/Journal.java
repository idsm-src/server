package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
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
        config.addIriClass(
                new IntegerUserIriClass("pubchem:journal", "integer", "http://rdf.ncbi.nlm.nih.gov/pubchem/journal/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:journal");

        {
            Table table = new Table(schema, "journal_bases");
            NodeMapping subject = config.createIriMapping("pubchem:journal", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("fabio:Journal"));
            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("fabio:hasNationalLibraryOfMedicineJournalId"),
                    config.createLiteralMapping(xsdString, "catalogid"));
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
                    config.createIriMapping("ncbi:journal", "catalogid"));
        }
    }
}
