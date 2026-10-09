package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Cell
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("pubchem:cell", INT4, "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:cell");

        {
            DatabaseTable table = new DatabaseTable(schema, "cells");
            TermMapping subject = config.createIriMapping("pubchem:cell", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Cell"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_010054"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("up:organism"),
                    config.createIriMapping("pubchem:taxonomy", "organism"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:altLabel"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_occurrences");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:BFO_0000050"),
                    config.createLiteralMapping(xsdString, "occurrence"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_references");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_matches");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("ontology:resource", "match_unit", "match_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_mesh_matches");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("mesh:resource", "match"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:mesh", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_wikidata_matches");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("wikidata:entity", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_cellosaurus_matches");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:cellosaurus", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_chembl_card_matches");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("chembl:cell_line", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "cell_anatomies");
            TermMapping subject = config.createIriMapping("pubchem:cell", "cell");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0001000"),
                    config.createIriMapping("pubchem:anatomy", "anatomy"));
        }
    }
}
