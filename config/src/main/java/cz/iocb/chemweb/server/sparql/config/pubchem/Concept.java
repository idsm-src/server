package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT2;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Concept
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:concept", INT2, new DatabaseTable(schema, "concept_bases"),
                new TableColumn("id", INT2), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:concept");

        {
            DatabaseTable table = new DatabaseTable(schema, "concept_bases");
            TermMapping subject = config.createIriMapping("pubchem:concept", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Concept"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("skos:ConceptScheme"),
                    config.createAreEqualCondition("iri", "'ATC'::varchar", "'SubstanceCategorization'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("skos:Concept"),
                    config.createAreNotEqualCondition("iri", "'ATC'::varchar", "'SubstanceCategorization'::varchar"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:prefLabel"),
                    config.createLiteralMapping(xsdString, "label"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:inScheme"),
                    config.createIriMapping("pubchem:concept", "scheme"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping("pubchem:concept", "broader"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("<http://purl.org/pav/importedFrom>"),
                    config.createIriMapping("source:ID11950"),
                    config.createAreEqualCondition("(iri like 'ATC%')::boolean", "'true'::boolean"));
        }
    }
}
