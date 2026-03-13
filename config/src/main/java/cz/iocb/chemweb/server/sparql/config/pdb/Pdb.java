package cz.iocb.chemweb.server.sparql.config.pdb;

import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Pdb
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pdb:compound", "integer", new Table("pdb", "compound_bases"),
                new TableColumn("id"), new TableColumn("name"), "https://identifiers.org/pdb-ccd/", ".*"));

        config.addIriClass(
                new MapUserIriClass("pdb:molfile", "integer", new Table("pdb", "compound_bases"), new TableColumn("id"),
                        new TableColumn("name"), "https://idsm.elixir-czech.cz/rdf/pdb-ccd/", ".*", "_molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pdb-ccd:");

        {
            Table table = new Table("pdb", "compound_bases");
            NodeMapping subject = config.createIriMapping("pdb:molfile", "id");
            LiteralClass molfileLiteral = new StringSubsetLiteralClass("pdb-molfile");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("pdb:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("pdb:compound", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("pdb:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));
        }
    }
}
