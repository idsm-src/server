package cz.iocb.chemweb.server.sparql.config.wikidata;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class Wikidata
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new IntegerUserIriClass("wikidata:entity", "integer", "http://www.wikidata.org/entity/Q"));

        config.addIriClass(
                new IntegerUserIriClass("wikidata:smiles", "integer", "http://www.wikidata.org/entity/Q", "_SMILES"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("wikidata:");

        {
            Table table = new Table("molecules", "wikidata");
            NodeMapping subject = config.createIriMapping("wikidata:entity", "id");

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("wikidata/Compound.vm"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:pageTemplate"),
                    config.createLiteralMapping("wikidata/Compound.vm"));
        }

        {
            Table table = new Table("molecules", "wikidata");
            NodeMapping subject = config.createIriMapping("wikidata:smiles", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000018"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000011"),
                    config.createIriMapping("wikidata:entity", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "smiles"));

            // extension
            config.addQuadMapping(table, graph, config.createIriMapping("wikidata:entity", "id"),
                    config.createIriMapping("sio:SIO_000008"), subject);

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:is-attribute-of"),
                    config.createIriMapping("wikidata:entity", "id"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdString, "smiles"));
        }
    }
}
