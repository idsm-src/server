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
import cz.iocb.sparql.engine.mapping.classes.ListUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Source
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:source", INT2, new DatabaseTable(schema, "source_bases"),
                new TableColumn("id", INT2), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"));
        config.addIriClass(new ListUserIriClass("pubchem:source-license", new DatabaseTable(schema, "source_bases"),
                new TableColumn("license", VARCHAR)));
        config.addIriClass(new ListUserIriClass("pubchem:source-homepage", new DatabaseTable(schema, "source_bases"),
                new TableColumn("homepage", VARCHAR)));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:source");

        {
            DatabaseTable table = new DatabaseTable(schema, "source_bases");
            TermMapping subject = config.createIriMapping("pubchem:source", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("dcterms:Dataset"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:license"),
                    config.createIriMapping("pubchem:source-license", "license"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("foaf:homepage"),
                    config.createIriMapping("pubchem:source-homepage", "homepage"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:rights"),
                    config.createLiteralMapping(xsdString, "rights"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "source_subjects");
            TermMapping subject = config.createIriMapping("pubchem:source", "source");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("pubchem:concept", "subject"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "source_alternatives");
            TermMapping subject = config.createIriMapping("pubchem:source", "source");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:alternative"),
                    config.createLiteralMapping(xsdString, "alternative"));
        }
    }
}
