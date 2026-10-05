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



public class Protein
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:enzyme", INT4, new DatabaseTable(schema, "enzyme_bases"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/EC_"));
        config.addIriClass(new MapUserIriClass("pubchem:protein", INT4, new DatabaseTable(schema, "protein_bases"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:protein");

        {
            DatabaseTable table = new DatabaseTable(schema, "enzyme_bases");
            TermMapping subject = config.createIriMapping("pubchem:enzyme", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010343"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("pubchem:enzyme", "parent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:subClassOf"),
                    config.createIriMapping("up:Enzyme"), config.createIsNullCondition(table, "parent"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("purl:enzyme", "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "title"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("up:Enzyme"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "enzyme_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:enzyme", "enzyme");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_bases");
            TermMapping subject = config.createIriMapping("pubchem:protein", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Protein"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("bao:BAO_0002817"),
                    config.createLiteralMapping(xsdString, "sequence"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:ncbiprotein", "iri"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "iri"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bp:Protein"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
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
            DatabaseTable table = new DatabaseTable(schema, "protein_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_pdblinks");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb", "pdblink"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_similarproteins");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:hasSimilarProtein"),
                    config.createIriMapping("pubchem:protein", "simprotein"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_genes");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:encodedBy"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_uniprot_enzymes");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:enzyme"),
                    config.createIriMapping("purl:enzyme", "enzyme"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_enzymes");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:enzyme"),
                    config.createIriMapping("pubchem:enzyme", "enzyme"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_ncbi_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:refseq", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_uniprot_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("purl:uniprot", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:uniprot", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:heading", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_glygen_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("glygen:protein", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_glycosmos_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("glycosmos:glycoproteins", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_alphafold_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("alphafold:entry", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_pharos_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("pharos:target", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_proconsortium_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:pr", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_wormbase_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wormbase:protein", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_brenda_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("brenda:enzyme", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_intact_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("intact:interactor", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_interpro_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("interpro:protein", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_nextprot_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:nextprot", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_stringdb_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("stringdb:network", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_enzymedatabase_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("enzymedatabase:ec", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_chembl_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("chembl:target", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_conserveddomains");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pubchem:conserveddomain", "domain"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_continuantparts");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pubchem:protein", "part"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_families");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("pfam:family", "family"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_interpro_families");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0002180"),
                    config.createIriMapping("interpro:entry", "family"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_types");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", "type_unit", "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_references");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "protein_patents");
            TermMapping subject = config.createIriMapping("pubchem:protein", "protein");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }
    }
}
