package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateNoZone;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Reference
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:reference", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:reference");

        {
            DatabaseTable table = new DatabaseTable(schema, "references");
            TermMapping subject = config.createIriMapping("pubchem:reference", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Reference"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:date"),
                    config.createLiteralMapping(xsdDateNoZone, "dcdate"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:date"),
                    config.createLiteralMapping(xsdString, "date"));
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
            DatabaseTable table = new DatabaseTable(schema, "reference_discussed_headings");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:discusses"),
                    config.createIriMapping("mesh:resource", "heading"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_subjects");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasSubjectTerm"),
                    config.createIriMapping("mesh:resource", "subject"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_anzsrc_subjects");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasSubjectTerm"),
                    config.createIriMapping("anzsrc:term", "subject"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_primary_subjects");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("fabio:hasPrimarySubjectTerm"),
                    config.createIriMapping("mesh:resource", "subject"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_content_types");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:contentType"),
                    config.createLiteralMapping(xsdString, "type"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_issn_numbers");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:issn"),
                    config.createLiteralMapping(xsdString, "issn"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_isbn_numbers");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("prism:isbn"),
                    config.createLiteralMapping(xsdString, "isbn"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_authors");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:creator"),
                    config.createIriMapping("pubchem:author", "author"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_grants");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:isSupportedBy"),
                    config.createIriMapping("pubchem:grant", "supporting_grant"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_organizations");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasFundingAgency"),
                    config.createIriMapping("pubchem:organization", "organization"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_journals");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("pubchem:journal", "journal"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_books");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("pubchem:book", "book"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_isbn_books");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("identifier:isbn", "isbn"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_issn_journals");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:isPartOf"),
                    config.createIriMapping("identifier:issn", "issn"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_mined_compounds");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_mined_diseases");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:disease", "disease"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_mined_genesymbols");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:genesymbol", "genesymbol"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_mined_enzymes");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject,
                    config.createIriMapping("vocab:discussesAsDerivedByTextMining"),
                    config.createIriMapping("pubchem:enzyme", "enzyme"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_identifiers");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "identifier"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "reference_source_types");
            TermMapping subject = config.createIriMapping("pubchem:reference", "reference");

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
