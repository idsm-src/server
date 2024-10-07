package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateM4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Reference
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:reference", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:reference");

        {
            Table table = new Table(schema, "reference_bases");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:date"),
                    config.createLiteralMapping(xsdDateM4, "dcdate"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:bibliographicCitation"),
                    config.createLiteralMapping(xsdString, "citation"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:publicationName"),
                    config.createLiteralMapping(xsdString, "publication"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:issueIdentifier"),
                    config.createLiteralMapping(xsdString, "issue"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:startingPage"),
                    config.createLiteralMapping(xsdString, "starting_page"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:endingPage"),
                    config.createLiteralMapping(xsdString, "ending_page"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:pageRange"),
                    config.createLiteralMapping(xsdString, "page_range"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:language"),
                    config.createLiteralMapping(xsdString, "lang"));
        }

        {
            Table table = new Table(schema, "reference_discusses");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:discusses"),
                    config.createIriMapping("mesh:heading", "statement"));
        }

        {
            Table table = new Table(schema, "reference_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasSubjectTerm"),
                    config.createIriMapping("mesh:heading", "subject"));
        }

        {
            Table table = new Table(schema, "reference_anzsrc_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasSubjectTerm"),
                    config.createIriMapping("anzsrc:term", "subject"));
        }

        {
            Table table = new Table(schema, "reference_primary_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasPrimarySubjectTerm"),
                    config.createIriMapping("mesh:heading", "subject"));
        }

        {
            Table table = new Table(schema, "reference_content_types");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:contentType"),
                    config.createLiteralMapping(xsdString, "type"));
        }

        {
            Table table = new Table(schema, "reference_issn_numbers");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:issn"),
                    config.createLiteralMapping(xsdString, "issn"));
        }

        {
            Table table = new Table(schema, "reference_authors");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:creator"),
                    config.createIriMapping("pubchem:author", "author"));
        }

        {
            Table table = new Table(schema, "reference_grants");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:isSupportedBy"),
                    config.createIriMapping("pubchem:grant", "grantid"));
        }

        {
            Table table = new Table(schema, "reference_organizations");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasFundingAgency"),
                    config.createIriMapping("pubchem:organization", "organization"));
        }

        {
            Table table = new Table(schema, "reference_journals");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("pubchem:journal", "journal"));
        }

        {
            Table table = new Table(schema, "reference_books");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("pubchem:book", "book"));
        }

        {
            Table table = new Table(schema, "reference_isbn_books");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("identifier:isbn", "isbn"));
        }

        {
            Table table = new Table(schema, "reference_issn_journals");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("identifier:issn", "issn"));
        }

        {
            Table table = new Table(schema, "reference_mined_compounds");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            Table table = new Table(schema, "reference_mined_diseases");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:disease", "disease"));
        }

        {
            Table table = new Table(schema, "reference_mined_genes");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:gene_symbol", "gene_symbol"));
        }

        {
            Table table = new Table(schema, "reference_mined_enzymes");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:enzyme", "enzyme"));
        }

        {
            Table table = new Table(schema, "reference_doi_identifiers");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createIriMapping("identifier:doi", "doi"));
        }

        {
            Table table = new Table(schema, "reference_pubmed_identifiers");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createIriMapping("identifier:pubmed", "pubmed"));
        }

        {
            Table table = new Table(schema, "reference_sources");
            NodeMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.drugbank.ca/>"),
                    config.createAreEqualCondition("source_type", "'DRUGBANK'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://datacite.org/>"),
                    config.createAreEqualCondition("source_type", "'DATACITE'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<http://www.thieme-chemistry.com/>"), config.createAreEqualCondition(
                            "source_type", "'THIEME_CHEMISTRY'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://hmdb.ca/>"),
                    config.createAreEqualCondition("source_type", "'HMDB'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://link.springer.com/>"),
                    config.createAreEqualCondition("source_type", "'SPRINGER'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://scigraph.springernature.com/>"), config.createAreEqualCondition(
                            "source_type", "'SPRINGERNATURE'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://pubmed.ncbi.nlm.nih.gov/>"),
                    config.createAreEqualCondition("source_type", "'PUBMED'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.crossref.org/>"),
                    config.createAreEqualCondition("source_type", "'CROSSREF'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/natcatal/>"), config.createAreEqualCondition(
                            "source_type", "'NATURE_NATCATAL'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/nature-portfolio/>"),
                    config.createAreEqualCondition("source_type",
                            "'NATURE_PORTFOLIO'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/natsynth/>"), config.createAreEqualCondition(
                            "source_type", "'NATURE_NATSYNTH'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/nchembio/>"), config.createAreEqualCondition(
                            "source_type", "'NATURE_NCHEMBIO'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/ncomms/>"), config.createAreEqualCondition(
                            "source_type", "'NATURE_NCOMMS'::" + schema + ".reference_source_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("<https://www.nature.com/nchem/>"), config.createAreEqualCondition(
                            "source_type", "'NATURE_NCHEM'::" + schema + ".reference_source_type"));
        }
    }
}
