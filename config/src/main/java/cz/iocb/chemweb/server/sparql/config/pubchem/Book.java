package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Book
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:book", "integer", "http://rdf.ncbi.nlm.nih.gov/pubchem/book/NBK"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:book");

        {
            Table table = new Table(schema, "book_bases");
            NodeMapping subject = config.createIriMapping("pubchem:book", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("fabio:Book"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:publisher"),
                    config.createLiteralMapping(xsdString, "publisher"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:location"),
                    config.createLiteralMapping(xsdString, "location"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:subtitle"),
                    config.createLiteralMapping(xsdString, "subtitle"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:date"),
                    config.createLiteralMapping(xsdString, "date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:isbn"),
                    config.createLiteralMapping(xsdString, "isbn"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("ncbi:book", "id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Book.vm"));
        }

        {
            Table table = new Table(schema, "book_authors");
            NodeMapping subject = config.createIriMapping("pubchem:book", "book");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:creator"),
                    config.createIriMapping("pubchem:author", "author"));
        }
    }
}
