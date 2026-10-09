package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Grant
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:grant", INT4, new DatabaseTable(schema, "grants"),
                new TableColumn("id", INT4), new TableColumn("iri", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:grant");

        {
            DatabaseTable table = new DatabaseTable(schema, "grants");
            TermMapping subject = config.createIriMapping("pubchem:grant", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:Grant"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("frapo:Grant"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasGrantNumber"),
                    config.createLiteralMapping(xsdString, "number"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasFundingAgency"),
                    config.createIriMapping("pubchem:organization", "organization"));
        }
    }
}
