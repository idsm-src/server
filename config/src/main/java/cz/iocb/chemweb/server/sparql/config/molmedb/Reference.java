package cz.iocb.chemweb.server.sparql.config.molmedb;

import static cz.iocb.chemweb.server.sparql.config.molmedb.MolmedbConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.ListUserIriClass;



public class Reference
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new IntegerUserIriClass("molmedb:reference", "integer", "https://rdf.molmedb.upol.cz/reference/ref"));

        config.addIriClass(new ListUserIriClass("molmedb:reference_homepage", new Table(schema, "reference_bases"),
                new TableColumn("homepage")));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("<https://rdf.molmedb.upol.cz>");

        // triples map #1
        // triples map #2
        {
            Table table = new Table(schema, "reference_bases");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("dcterms:BibliographicResource"), config.createIsNullCondition("label"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:bibliographicCitation"),
                    config.createLiteralMapping(xsdString, "citation"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:doi"),
                    config.createLiteralMapping(xsdString, "doi"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("bibo:pmid"),
                    config.createLiteralMapping(xsdString, "pmid"));
        }

        // triples map #3
        // triples map #4
        // triples map #5
        // triples map #6
        {
            Table table = new Table(schema, "reference_substances");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "reference_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("molmedb:substance", "substance_id"));
        }

        // triples map #7
        {
            Table table = new Table(schema, "reference_membranes");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "reference_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("molmedb:membrane", "membrane_id"));
        }

        // triples map #8
        {
            Table table = new Table(schema, "reference_methods");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "reference_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("molmedb:method", "method_id"));
        }

        // triples map #9
        {
            Table table = new Table(schema, "reference_proteins");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "reference_id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("molmedb:target", "protein_id"));
        }


        /*
         * databases in references
         */

        {
            Table table = new Table(schema, "reference_bases");
            NodeMapping subject = config.createIriMapping("molmedb:reference", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_000089"), config.createIsNotNullCondition("label"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:label"),
                    config.createLiteralMapping(xsdString, "label"));

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdfs:seeAlso"),
                    config.createIriMapping("molmedb:reference_homepage", "homepage"));
        }
    }
}
