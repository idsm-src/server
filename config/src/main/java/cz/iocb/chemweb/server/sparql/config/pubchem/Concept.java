package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Concept
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:concept", "smallint", new Table(schema, "concept_bases"),
                new TableColumn("id"), new TableColumn("iri"), "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:concept");

        {
            config.addQuadMapping(null, graph, config.createIriMapping("concept:ATC"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("skos:ConceptScheme"));
            config.addQuadMapping(null, graph, config.createIriMapping("concept:SubstanceCategorization"),
                    config.createIriMapping("rdf:type"), config.createIriMapping("skos:ConceptScheme"));
        }

        {
            Table table = new Table(schema, "concept_bases");
            NodeMapping subject = config.createIriMapping("pubchem:concept", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(rdfLangStringEn, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:inScheme"),
                    config.createIriMapping("pubchem:concept", "scheme"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping("pubchem:concept", "broader"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("<http://purl.org/pav/importedFrom>"),
                    config.createIriMapping("source:WHO"),
                    config.createAreEqualCondition("(iri like 'ATC%')", "'true'::boolean"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("skos:Concept"),
                    config.createAreNotEqualCondition("iri", "'ATC'::varchar", "'SubstanceCategorization'::varchar"));
        }
    }
}
