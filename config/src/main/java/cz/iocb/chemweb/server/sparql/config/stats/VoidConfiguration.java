package cz.iocb.chemweb.server.sparql.config.stats;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class VoidConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "void";


    public VoidConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Void.addPrefixes(this);
    }


    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Void.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Void.addQuadMappings(this);
    }
}
