package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import cz.iocb.chemweb.server.sparql.database.Table;
import cz.iocb.chemweb.server.sparql.database.TableColumn;
import cz.iocb.chemweb.server.sparql.mapping.ConstantIriMapping;
import cz.iocb.chemweb.server.sparql.mapping.NodeMapping;
import cz.iocb.chemweb.server.sparql.mapping.classes.MapUserIriClass;
import cz.iocb.chemweb.server.sparql.mapping.classes.UserIriClass;



class Concept
{
    static void addIriClasses(PubChemConfiguration config)
    {
        config.addIriClass(
                new MapUserIriClass("concept", "smallint", new Table(schema, "concept_bases"), new TableColumn("id"),
                        new TableColumn("iri"), "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/concept/.*"));
    }


    static void addQuadMapping(PubChemConfiguration config)
    {
        UserIriClass concept = config.getIriClass("concept");
        ConstantIriMapping graph = config.createIriMapping("pubchem:concept");

        {
            config.addQuadMapping(null, null, graph, config.createIriMapping("concept:ATC"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("skos:ConceptScheme"));
            config.addQuadMapping(null, null, graph, config.createIriMapping("concept:SubstanceCategorization"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("skos:ConceptScheme"));
        }

        {
            String table = "concept_bases";
            NodeMapping subject = config.createIriMapping(concept, "id");

            config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/Concept.vm"));
            config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
            config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("skos:inScheme"),
                    config.createIriMapping(concept, "scheme"));
            config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping(concept, "broader"));

            config.addQuadMapping(schema, table, graph, subject,
                    config.createIriMapping("<http://purl.org/pav/importedFrom>"),
                    config.createIriMapping("source:WHO"),
                    "iri like 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/ATC%'");
            config.addQuadMapping(schema, table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("skos:Concept"),
                    "(iri <> 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/SubstanceCategorization'"
                            + " and iri <> 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/ATC')");
        }
    }
}
