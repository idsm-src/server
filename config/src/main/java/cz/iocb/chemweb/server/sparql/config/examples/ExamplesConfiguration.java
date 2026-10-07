package cz.iocb.chemweb.server.sparql.config.examples;

import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.ListUserIriClass;



public class ExamplesConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    static final String schema = "info";


    public ExamplesConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        Examples.addPrefixes(this);
        FederatedExamples.addPrefixes(this);
    }


    private void addResourceClasses() throws SQLException
    {
        addIriClass(new ListUserIriClass("info:endpoint", new DatabaseTable(schema, "sparql_endpoints"),
                new TableColumn("iri", VARCHAR)));

        Examples.addResourceClasses(this);
        FederatedExamples.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Examples.addQuadMappings(this);
        FederatedExamples.addQuadMappings(this);
    }
}
