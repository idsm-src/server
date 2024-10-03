package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.util.List;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Column;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.NodeMapping;



public class MolFiles
{
    public static void addPrefixes(SparqlDatabaseConfiguration config)
    {
        config.addPrefix("sio", "http://semanticscience.org/resource/");
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config, String compoundClass, String molfileClass,
            Table table, List<Column> compoundFields, String id, String molfile)
    {
        NodeMapping subject = config.createIriMapping(molfileClass, id);

        config.addQuadMapping(table, null, subject, config.createIriMapping("rdf:type"),
                config.createIriMapping("sio:SIO_011120"));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:SIO_000011"),
                config.createIriMapping(compoundClass, compoundFields));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:SIO_000300"),
                config.createLiteralMapping(xsdString, molfile));

        // extension
        config.addQuadMapping(table, null, config.createIriMapping(compoundClass, compoundFields),
                config.createIriMapping("sio:SIO_000008"), subject);

        // deprecated
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:is-attribute-of"),
                config.createIriMapping(compoundClass, compoundFields));
        config.addQuadMapping(table, null, subject, config.createIriMapping("sio:has-value"),
                config.createLiteralMapping(xsdString, molfile));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config, String compoundClass, String molfileClass,
            Table table, List<Column> compoundFields)
    {
        addQuadMappings(config, compoundClass, molfileClass, table, compoundFields, "id", "molfile");
    }
}
