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



public class Organization
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:organization", INT4,
                new DatabaseTable(schema, "organization_bases"), new TableColumn("id", INT4),
                new TableColumn("iri", VARCHAR), "http://rdf.ncbi.nlm.nih.gov/pubchem/organization/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:organization");

        {
            DatabaseTable table = new DatabaseTable(schema, "organization_bases");
            TermMapping subject = config.createIriMapping("pubchem:organization", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Organization"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vcard:Organization"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("frapo:FundingAgency"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "organization_country_names");
            TermMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:country-name"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "organization_formatted_names");
            TermMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "organization_crossref_matches");
            TermMapping subject = config.createIriMapping("pubchem:organization", "organization");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("crossref:funder", "crossref"));
        }
    }
}
