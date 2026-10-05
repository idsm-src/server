package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Taxonomy
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:taxonomy", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:taxonomy");

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_bases");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Taxonomy"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010000"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:taxonomy", "id"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ncbi:taxonomy", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:ncbitaxon", "id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_references");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_patents");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:heading", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_catalogueoflife_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:col", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "taxonomy_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }
    }
}
