package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateNoZone;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Patent
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:patent", INT4, new DatabaseTable(schema, "patents"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/"));
        config.addIriClass(
                new StringUserIriClass("pubchem:inventor", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_"));
        config.addIriClass(
                new StringUserIriClass("pubchem:applicant", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:patent");

        {
            DatabaseTable table = new DatabaseTable(schema, "patents");
            TermMapping subject = config.createIriMapping("pubchem:patent", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Patent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("epo:Publication"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:abstract"),
                    config.createLiteralMapping(xsdString, "abstract"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:titleOfInvention"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:publicationNumber"),
                    config.createLiteralMapping(xsdString, "publication_number"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:filingDate"),
                    config.createLiteralMapping(xsdDateNoZone, "filing_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:grantDate"),
                    config.createLiteralMapping(xsdDateNoZone, "grant_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:publicationDate"),
                    config.createLiteralMapping(xsdDateNoZone, "publication_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:priorityDate"),
                    config.createLiteralMapping(xsdDateNoZone, "priority_date"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_citations");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isCitedBy"),
                    config.createIriMapping("pubchem:patent", "citation"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_cpc_additional_classifications");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationCPCAdditional"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_cpc_inventive_classifications");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationCPCInventive"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }


        {
            DatabaseTable table = new DatabaseTable(schema, "patent_ipc_additional_classifications");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationIPCAdditional"),
                    config.createIriMapping("pubchem:patentipc", "classification"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_ipc_inventive_classifications");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationIPCInventive"),
                    config.createIriMapping("pubchem:patentipc", "classification"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_inventors");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:inventorVC"),
                    config.createIriMapping("pubchem:inventor", "inventor"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patent_applicants");
            TermMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:applicantVC"),
                    config.createIriMapping("pubchem:applicant", "applicant"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "inventors");
            TermMapping subject = config.createIriMapping("pubchem:inventor", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:PatentInventor"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "applicants");
            TermMapping subject = config.createIriMapping("pubchem:applicant", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:PatentAssignee"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "name"));
        }
    }
}
