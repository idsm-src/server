package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Synonym
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:synonym", INT4, new DatabaseTable(schema, "synonym_bases"),
                new TableColumn("id", INT4), new TableColumn("md5", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:synonym");

        {
            DatabaseTable table = new DatabaseTable(schema, "synonym_values");
            TermMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Synonym"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "value"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "value"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "synonym_types");
            TermMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("ontology:resource", Ontology.unitCHEMINF, "type_id"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "synonym_compounds");
            TermMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pubchem:compound", "compound"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pubchem:compound", "compound"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pubchem:compound", "compound"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "synonym_mesh_subjects");
            TermMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("mesh:heading", "subject"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "synonym_concept_subjects");
            TermMapping subject = config.createIriMapping("pubchem:synonym", "synonym");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("pubchem:concept", "concept"));
        }
    }
}
