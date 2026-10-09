package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.GenericUserIriClass;



public class Measuregroup
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new GenericUserIriClass("pubchem:measuregroup", schema, "measuregroup", List.of(INT4, INT4),
                "http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/measuregroup/AID[0-9]+(_(PMID([1-9][0-9]*)?|[1-9][0-9]*|0)?)?"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:measuregroup");

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroups");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:MeasureGroup"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("bao:BAO_0000040"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:source"),
                    config.createIriMapping("pubchem:source", "source"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "endpoints");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:OBI_0000299"),
                    config.createIriMapping("pubchem:endpoint", "substance", "bioassay", "measuregroup", "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_genes");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:gene", "gene"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_proteins");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:protein", "protein"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_taxonomies");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:taxonomy", "taxonomy"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_cells");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:cell", "cell"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "measuregroup_anatomies");
            TermMapping subject = config.createIriMapping("pubchem:measuregroup", "bioassay", "measuregroup");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("obo:RO_0000057"),
                    config.createIriMapping("pubchem:anatomy", "anatomy"));
        }
    }
}
