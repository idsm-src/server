package cz.iocb.chemweb.server.sparql.config.pubchem;

import static cz.iocb.chemweb.server.sparql.config.pubchem.PubChemConfiguration.rdfLangStringEn;
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



public class InchiKey
{
    public static void addResourceClasses(SparqlDatabaseConfiguration config)
    {
        config.addIriClass(new MapUserIriClass("pubchem:inchikey", INT4, new DatabaseTable(schema, "inchikeys"),
                new TableColumn("id", INT4), new TableColumn("inchikey", VARCHAR),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/"));
    }


    public static void addQuadMappings(SparqlDatabaseConfiguration config)
    {
        ConstantIriMapping graph = config.createIriMapping("pubchem:inchikey");

        {
            DatabaseTable table = new DatabaseTable(schema, "inchikeys");
            TermMapping subject = config.createIriMapping("pubchem:inchikey", "id");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("vocab:InChIKey"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("rdf:type"),
                    config.createIriMapping("sio:CHEMINF_000399"));
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:SIO_000300"),
                    config.createLiteralMapping(xsdString, "inchikey"));

            // deprecated
            config.addQuadMapping(table, graph, subject, config.createIriMapping("sio:has-value"),
                    config.createLiteralMapping(rdfLangStringEn, "inchikey"));
        }

        {
            DatabaseTable table = new DatabaseTable(schema, "inchikey_compounds");
            TermMapping subject = config.createIriMapping("pubchem:inchikey", "inchikey");

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
            DatabaseTable table = new DatabaseTable(schema, "inchikey_subjects");
            TermMapping subject = config.createIriMapping("pubchem:inchikey", "inchikey");

            config.addQuadMapping(table, graph, subject, config.createIriMapping("dcterms:subject"),
                    config.createIriMapping("mesh:resource", "subject"));
        }
    }
}
