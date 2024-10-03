package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInt;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class SubstanceDescriptor
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID";

        config.addIriClass(
                new IntegerUserIriClass("pubchem:substance_version", "integer", prefix, "_Substance_Version"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("descriptor:substance");

        {
            Table table = new Table(schema, "descriptor_substance_bases");
            NodeMapping subject = config.createIriMapping("pubchem:substance_version", "substance");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("obo:IAO_0000129"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdInt, "version"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(xsdInt, "version"));

            // extension
            config.addQuadMapping(table, graph, subject, config.createIriMapping("template:itemTemplate"),
                    config.createLiteralMapping("pubchem/SubstanceDescriptor.vm"));
        }
    }
}
