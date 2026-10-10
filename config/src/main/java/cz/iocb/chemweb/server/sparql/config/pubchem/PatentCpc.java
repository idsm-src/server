package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.xsdDateNoZone;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdInteger;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.StringUserIriClass;



public class PatentCpc
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(
                new StringUserIriClass("pubchem:patentcpc", "http://rdf.ncbi.nlm.nih.gov/pubchem/patentcpc/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:patentcpc");

        {
            DatabaseTable table = new DatabaseTable(schema, "patentcpcs");
            TermMapping subject = config.createIriMapping("pubchem:patentcpc", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:PatentCPC"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:Section"),
                    config.createAreEqualCondition("type", "'SECTION'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:SubSection"),
                    config.createAreEqualCondition("type", "'SUBSECTION'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:Class"),
                    config.createAreEqualCondition("type", "'CLASS'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:SubClass"),
                    config.createAreEqualCondition("type", "'SUBCLASS'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:MainGroup"),
                    config.createAreEqualCondition("type", "'MAINGROUP'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("cpc:SubGroup"),
                    config.createAreEqualCondition("type", "'SUBGROUP'::" + schema + ".patentcpc_type_type"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cpc:level"),
                    config.createLiteralMapping(xsdInteger, "level"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cpc:symbol"),
                    config.createLiteralMapping(xsdString, "symbol"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:title"),
                    config.createLiteralMapping(xsdString, "title"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("cpc:concordantIPC"),
                    config.createIriMapping("pubchem:patentipc", "concordant_ipc"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patentcpc_broaders");
            TermMapping subject = config.createIriMapping("pubchem:patentcpc", "patentcpc");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("skos:broader"),
                    config.createIriMapping("pubchem:patentcpc", "broader"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "patentcpc_modified_dates");
            TermMapping subject = config.createIriMapping("pubchem:patentcpc", "patentcpc");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:modified"),
                    config.createLiteralMapping(xsdDateNoZone, "date"));
        }
    }
}
