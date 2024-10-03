package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class InchiKey
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:inchikey", "integer", new Table(schema, "inchikey_bases"),
                new TableColumn("id"), new TableColumn("inchikey"), "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:inchikey");

        {
            Table table = new Table(schema, "inchikey_bases");
            NodeMapping subject = config.createIriMapping("pubchem:inchikey", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000399"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(rdfLangStringEn, "inchikey"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(rdfLangStringEn, "inchikey"));
        }

        {
            Table table = new Table(schema, "inchikey_compounds");
            NodeMapping subject = config.createIriMapping("pubchem:inchikey", "inchikey");

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
            Table table = new Table(schema, "inchikey_subjects");
            NodeMapping subject = config.createIriMapping("pubchem:inchikey", "inchikey");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("mesh:heading", "subject"));
        }
    }
}
