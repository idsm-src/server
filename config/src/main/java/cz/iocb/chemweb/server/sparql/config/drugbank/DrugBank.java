package cz.iocb.chemweb.server.sparql.config.drugbank;

import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class DrugBank
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("drugbank:compound", "integer",
                "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/DB", 5));

        config.addIriClass(new IntegerUserIriClass("drugbank:molfile", "integer",
                "http://wifo5-04.informatik.uni-mannheim.de/drugbank/resource/drugs/DB", 5, "_Molfile"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("drugbank:");

        {
            Table table = new Table("molecules", "drugbank");
            NodeMapping subject = config.createIriMapping("drugbank:molfile", "id");
            LiteralClass molfileLiteral = new StringSubsetLiteralClass("drugbank-molfile");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:SIO_011120"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("drugbank:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("drugbank:compound", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("drugbank:compound", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(molfileLiteral, "molfile"));
        }
    }
}
