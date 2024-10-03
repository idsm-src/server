package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Organization
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:organization", "integer",
                new Table(schema, "organization_bases"), new TableColumn("id"), new TableColumn("iri"),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:organization");

        {
            Table table = new Table(schema, "organization_bases");
            NodeMapping subject = config.createIriMapping("pubchem:organization", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vcard:Organization"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("frapo:FundingAgency"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Organization.vm"));
        }

        {
            Table table = new Table(schema, "organization_country_names");
            NodeMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:country-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "organization_formatted_names");
            NodeMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            Table table = new Table(schema, "organization_crossref_matches");
            NodeMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("crossref:funder", "crossref"));
        }
    }
}
