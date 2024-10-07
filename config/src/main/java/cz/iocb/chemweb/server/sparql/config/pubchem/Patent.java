package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateM4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class Patent
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:patent", "integer", "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/"));
        config.addIriClass(
                new StringUserIriClass("pubchem:inventor", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentinventor/MD5_"));
        config.addIriClass(
                new StringUserIriClass("pubchem:applicant", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentassignee/MD5_"));
        config.addIriClass(
                new StringUserIriClass("pubchem:patentcpc", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/"));
        config.addIriClass(
                new StringUserIriClass("pubchem:patentipc", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentipc/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:patent");

        {
            Table table = new Table(schema, "patent_bases");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("epo:Publication"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:abstract"),
                    config.createLiteralMapping(xsdString, "abstract"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:titleOfInvention"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:publicationNumber"),
                    config.createLiteralMapping(xsdString, "publication_number"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:filingDate"),
                    config.createLiteralMapping(xsdDateM4, "filing_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:grantDate"),
                    config.createLiteralMapping(xsdDateM4, "grant_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:publicationDate"),
                    config.createLiteralMapping(xsdDateM4, "publication_date"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:priorityDate"),
                    config.createLiteralMapping(xsdDateM4, "priority_date"));
        }

        {
            Table table = new Table(schema, "patent_citations");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isCitedBy"),
                    config.createIriMapping("pubchem:patent", "citation"));
        }

        {
            Table table = new Table(schema, "patent_cpc_additional_classifications");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationCPCAdditional"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }

        {
            Table table = new Table(schema, "patent_cpc_inventive_classifications");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationCPCInventive"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }


        {
            Table table = new Table(schema, "patent_ipc_additional_classifications");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationIPCAdditional"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }

        {
            Table table = new Table(schema, "patent_ipc_inventive_classifications");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:classificationIPCInventive"),
                    config.createIriMapping("pubchem:patentcpc", "classification"));
        }

        {
            Table table = new Table(schema, "patent_substances");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:substance", "substance"));
        }

        {
            Table table = new Table(schema, "patent_compounds");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            Table table = new Table(schema, "patent_genes");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            Table table = new Table(schema, "patent_proteins");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:protein", "protein"));
        }

        {
            Table table = new Table(schema, "patent_taxonomies");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:taxonomy", "taxonomy"));
        }

        {
            Table table = new Table(schema, "patent_anatomies");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:anatomy", "anatomy"));
        }

        {
            Table table = new Table(schema, "patent_inventors");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:inventorVC"),
                    config.createIriMapping("pubchem:inventor", "inventor"));
        }

        {
            Table table = new Table(schema, "patent_applicants");
            NodeMapping subject = config.createIriMapping("pubchem:patent", "patent");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("epo:applicantVC"),
                    config.createIriMapping("pubchem:applicant", "applicant"));
        }

        {
            Table table = new Table(schema, "patent_inventor_names");
            NodeMapping subject = config.createIriMapping("pubchem:inventor", "inventor");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "formatted_name"));
        }

        {
            Table table = new Table(schema, "patent_applicant_names");
            NodeMapping subject = config.createIriMapping("pubchem:applicant", "applicant");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vcard:fn"),
                    config.createLiteralMapping(xsdString, "formatted_name"));
        }
    }
}
