package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class WikidataSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public WikidataSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
            throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
        addProcedures();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);

        addPrefix("wd", "http://www.wikidata.org/entity/");
        addPrefix("wdt", "http://www.wikidata.org/prop/direct/");
    }


    private void addResourceClasses() throws SQLException
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass("wikidata:entity", INT4, "http://www.wikidata.org/entity/Q"));
    }


    private void addQuadMappings()
    {
        {
            DatabaseTable table = new DatabaseTable("wikidata", "canonical_smiles");
            TermMapping subject = createIriMapping("wikidata:entity", "compound");

            addQuadMapping(table, null, subject, createIriMapping("wdt:P233"),
                    createLiteralMapping(xsdString, "smiles"));
        }

        {
            DatabaseTable table = new DatabaseTable("wikidata", "isomeric_smiles");
            TermMapping subject = createIriMapping("wikidata:entity", "compound");

            addQuadMapping(table, null, subject, createIriMapping("wdt:P2017"),
                    createLiteralMapping(xsdString, "smiles"));
        }
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "wikidata", "wikidata:entity",
                getColumns(getIriClass("wikidata:entity"), "compound"));
    }
}
