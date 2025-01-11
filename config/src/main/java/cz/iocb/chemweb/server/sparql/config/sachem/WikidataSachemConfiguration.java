package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.NodeMapping;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class WikidataSachemConfiguration extends SparqlDatabaseConfiguration
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

        addIriClass(new IntegerUserIriClass("wikidata:entity", "integer", "http://www.wikidata.org/entity/Q"));
    }


    private void addQuadMappings()
    {
        {
            Table table = new Table("wikidata", "canonical_smiles");
            NodeMapping subject = createIriMapping("wikidata:entity", "compound");

            addQuadMapping(table, null, subject, createIriMapping("wdt:P233"),
                    createLiteralMapping(xsdString, "smiles"));
        }

        {
            Table table = new Table("wikidata", "isomeric_smiles");
            NodeMapping subject = createIriMapping("wikidata:entity", "compound");

            addQuadMapping(table, null, subject, createIriMapping("wdt:P2017"),
                    createLiteralMapping(xsdString, "smiles"));
        }
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "wikidata", "wikidata:entity", List.of(new TableColumn("compound")));
    }
}
