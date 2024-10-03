package cz.iocb.chemweb.server.sparql.config.wikidata;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class WikidataConfiguration extends SparqlDatabaseConfiguration
{
    public WikidataConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("wikidata", "http://www.wikidata.org/entity/");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Wikidata.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Wikidata.addQuadMappings(this);
    }
}
