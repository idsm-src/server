package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateM4;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Substance
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("pubchem:substance", "integer",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:substance");

        {
            Table table = new Table(schema, "substance_bases");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:available"),
                    config.createLiteralMapping(xsdDateM4, "available"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:modified"),
                    config.createLiteralMapping(xsdDateM4, "modified"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:CHEMINF_000477"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            Table table = new Table(schema, "substance_types");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEBI, "chebi"));
        }

        {
            Table table = new Table(schema, "measuregroup_substances");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000056"),
                    config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup"));
        }

        {
            Table table = new Table(schema, "substance_chembl_matches");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("linkedchemistry:chembl", "chembl"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("chembl:compound", "chembl"));
        }

        {
            Table table = new Table(schema, "substance_glytoucan_matches");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:exactMatch"),
                    config.createIriMapping("identifiers:glytoucan", "glytoucan"));
        }

        {
            Table table = new Table(schema, "substance_references");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("cito:isDiscussedBy"),
                    config.createIriMapping("pubchem:reference", "reference"));
        }

        {
            Table table = new Table(schema, "substance_pdblinks");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("pdbo40:link_to_pdb"),
                    config.createIriMapping("rdf:wwpdb", "pdblink"));
        }

        {
            Table table = new Table(schema, "substance_synonyms");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

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
            Table table = new Table(schema, "descriptor_substance_bases");
            NodeMapping subject = config.createIriMapping("pubchem:substance", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000008"),
                    config.createIriMapping("pubchem:substance_version", "substance"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:substance_version", "substance"),
                    config.createIriMapping("sio:SIO_000011"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-attribute"),
                    config.createIriMapping("pubchem:substance_version", "substance"));
        }
    }
}
