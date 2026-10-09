package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateM4;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Substance
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:substance", INT4,
                "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:substance");

        {
            DatabaseTable table = new DatabaseTable(schema, "substances");
            TermMapping subject = config.createIriMapping("pubchem:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Substance"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:available"),
                    config.createLiteralMapping(xsdDateM4, "available"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:modified"),
                    config.createLiteralMapping(xsdDateM4, "modified"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000477"),
                    config.createIriMapping("pubchem:compound", "compound"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:identifier"),
                    config.createLiteralMapping(xsdString, "(id)::varchar"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_types");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:chebi", "chebi"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_substances");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000056"),
                    config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_chembl_matches");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:chembl", "match"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("chembl:molecule", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_glytoucan_matches");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("identifiers:glytoucan", "match"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("glycoinfo:glycan", "match"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_references");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_patents");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:patent", "patent"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_pdblinks");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb", "pdblink"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_synonyms");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:synonym", "synonym"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:synonym", "synonym"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:synonym", "synonym"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_versions");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:substance_version", "substance"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:substance_version", "substance"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:substance_version", "substance"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "substance_versions");
            TermMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("vocab:substance_version"),
                    config.createLiteralMapping(xsdInt, "version"));
        }
    }
}
