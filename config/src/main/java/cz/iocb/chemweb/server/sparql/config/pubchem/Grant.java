package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.schema;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.ConstantIriMapping;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class Grant
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:grant", "integer", new Table(schema, "grant_bases"),
                new TableColumn("id"), new TableColumn("iri"), "http://rdf.ncbi.nlm.nih.gov/pubchem/grant/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:grant");

        {
            Table table = new Table(schema, "grant_bases");
            NodeMapping subject = config.createIriMapping("pubchem:grant", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("frapo:Grant"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasGrantNumber"),
                    config.createLiteralMapping(xsdString, "number"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("frapo:hasFundingAgency"),
                    config.createIriMapping("pubchem:organization", "organization"));
        }
    }
}
