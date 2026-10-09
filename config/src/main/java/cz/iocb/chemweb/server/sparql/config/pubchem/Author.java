package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Author
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:author", INT4, new DatabaseTable(schema, "authors"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:author");

        {
            DatabaseTable table = new DatabaseTable(schema, "authors");
            TermMapping subject = config.createIriMapping("pubchem:author", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Author"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "author_given_names");
            TermMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:given-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "author_family_names");
            TermMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:family-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "author_formatted_names");
            TermMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "author_organizations");
            TermMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:organization-name"),
                    config.createLiteralMapping(xsdString, "organization"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "author_orcids");
            TermMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://orcid.org>"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:hasUID"),
                    config.createIriMapping("orcid:author", "orcid"));
        }
    }
}
