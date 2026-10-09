package cz.iocb.chemweb.server.sparql.config.pdb;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Pdb
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pdb:compound", INT4, new DatabaseTable("pdb", "compounds"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR), "https://identifiers.org/pdb-ccd/",
                ".*"));

        config.addIriClass(new MapUserIriClass("pdb:molfile", INT4, new DatabaseTable("pdb", "compounds"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR),
                "https://idsm.elixir-czech.cz/rdf/pdb-ccd/", ".*", "_molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pdb-ccd:");

        {
            DatabaseTable table = new DatabaseTable("pdb", "compounds");
            TermMapping subject = config.createIriMapping("pdb:molfile", "id");
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
