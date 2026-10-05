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
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Gene
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:gene_symbol", INT4,
                new DatabaseTable(schema, "gene_symbol_bases"), new TableColumn("id", INT4),
                new TableColumn("iri", VARCHAR), "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"));
        config.addIriClass(
                new IntegerUserIriClass("pubchem:gene", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:gene");

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_symbol_bases");
            TermMapping subject = config.createIriMapping("pubchem:gene_symbol", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:GeneSymbol"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_001383"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "symbol"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_bases");
            TermMapping subject = config.createIriMapping("pubchem:gene", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Gene"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010035"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0002870"),
                    config.createIriMapping("pubchem:gene_symbol", "gene_symbol"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bp:Gene"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, new DatabaseTable(schema, "gene_symbol_bases"), "gene_symbol", "id", graph,
                    subject, config.createIriMapping("sio:gene-symbol"), config.createLiteralMapping(xsdString, "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("ontology:ncbitaxon", "organism"));

            // deprecated extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bp:organism"),
                    config.createIriMapping("ontology:ncbitaxon", "organism"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:alternative"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_references");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_patents");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_ensembl_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("rdf:ensembl", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:ensembl", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:heading", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_expasy_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("expasy:enzyme", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_medlineplus_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("medlineplus:gene", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_alliancegenome_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("alliancegenome:gene", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_kegg_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:kegg", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_pharos_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("pharos:target", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_bgee_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:bgee", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_pombase_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:pombase", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_veupathdb_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("veupathdb:gene", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_zfin_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:zfin", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_enzyme_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("purl:enzyme", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_processes");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000056"),
                    config.createIriMapping("ontology:go", "process_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_functions");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000085"),
                    config.createIriMapping("ontology:go", "function_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_locations");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0001025"),
                    config.createIriMapping("ontology:go", "location_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "gene_orthologs");
            TermMapping subject = config.createIriMapping("pubchem:gene", "gene");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000558"),
                    config.createIriMapping("pubchem:gene", "ortholog"));
        }
    }
}
