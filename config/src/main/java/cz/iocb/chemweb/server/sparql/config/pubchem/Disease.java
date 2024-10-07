package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Disease
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:disease", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:disease");

        {
            Table table = new Table(schema, "disease_bases");
            NodeMapping subject = config.createIriMapping("pubchem:disease", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:DOID_4"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:MONDO_0000001"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            Table table = new Table(schema, "disease_alternatives");
            NodeMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping("alternative"));
        }

        {
            Table table = new Table(schema, "disease_matches");
            NodeMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            Table table = new Table(schema, "disease_mesh_matches");
            NodeMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:closeMatch"),
                    config.createIriMapping("mesh:heading", "match"));
        }

        {
            Table table = new Table(schema, "disease_related_matches");
            NodeMapping subject = config.createIriMapping("pubchem:disease", "disease");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:relatedMatch"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }
    }
}
