package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Author
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:author", "integer", new Table(schema, "author_bases"),
                new TableColumn("id"), new TableColumn("iri"), "http://rdf.ncbi.nlm.nih.gov/pubchem/author/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:author");

        {
            Table table = new Table(schema, "author_bases");
            NodeMapping subject = config.createIriMapping("pubchem:author", "id");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Author.vm"));
        }

        {
            Table table = new Table(schema, "author_given_names");
            NodeMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:given-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "author_family_names");
            NodeMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:family-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "author_formatted_names");
            NodeMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "author_organizations");
            NodeMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:organization-name"),
                    config.createLiteralMapping(xsdString, "organization"));
        }


        {
            Table table = new Table(schema, "author_orcids");
            NodeMapping subject = config.createIriMapping("pubchem:author", "author");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://orcid.org>"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:hasUID"),
                    config.createIriMapping("orcid:author", "orcid"));
        }
    }
}
