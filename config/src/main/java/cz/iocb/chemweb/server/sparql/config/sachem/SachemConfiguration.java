package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class SachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public SachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema, String index,
            String iriPrefix, int idLength, LiteralClass molfileLiteralClass) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses(index, iriPrefix, idLength);
        addQuadMappings(index, molfileLiteralClass);
        addProcedures(index);
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);
    }


    private void addResourceClasses(String index, String iriPrefix, int idLength)
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass(index + ":compound", "integer", iriPrefix, idLength));
        addIriClass(new IntegerUserIriClass(index + ":molfile", "integer", iriPrefix, idLength, "_Molfile"));
    }


    private void addQuadMappings(String index, LiteralClass molfileLiteralClass)
    {
        MolFiles.addQuadMappings(this, index + ":compound", index + ":molfile", new Table("molecules", index),
                List.of(new TableColumn("id")), molfileLiteralClass);
    }


    private void addProcedures(String index)
    {
        Sachem.addProcedures(this, index, index + ":compound", List.of(new TableColumn("compound")));
    }
}
