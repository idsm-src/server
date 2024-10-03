package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Taxonomy
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:taxonomy", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:taxonomy");

        {
            Table table = new Table(schema, "taxonomy_bases");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010000"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("ncbi:taxonomy", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("owl:sameAs"),
                    config.createIriMapping("ontology:resource", Ontology.unitNCBITaxon, "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Taxonomy.vm"));
        }

        {
            Table table = new Table(schema, "taxonomy_alternatives");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            Table table = new Table(schema, "taxonomy_references");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            Table table = new Table(schema, "taxonomy_uniprot_matches");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("purl:taxonomy", "taxonomy"));
        }

        {
            Table table = new Table(schema, "taxonomy_mesh_matches");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:heading", "match"));
        }

        {
            Table table = new Table(schema, "taxonomy_catalogueoflife_matches");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("catalogueoflife:taxon", "match"));
        }

        {
            Table table = new Table(schema, "taxonomy_thesaurus_matches");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", Ontology.unitThesaurus, "match"));
        }

        {
            Table table = new Table(schema, "taxonomy_itis_matches");
            NodeMapping subject = config.createIriMapping("pubchem:taxonomy", "taxonomy");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("itis:taxonomy", "match"));
        }
    }
}
