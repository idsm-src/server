package cz.iocb.chemweb.server.sparql.config.sachem;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class SachemConfiguration extends SparqlDatabaseConfiguration
{
    public SachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema, String index,
            String iriPrefix, int idLength) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses(index, iriPrefix, idLength);
        addQuadMappings(index);
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


    private void addQuadMappings(String index)
    {
        MolFiles.addQuadMappings(this, index + ":compound", index + ":molfile", new Table("molecules", index),
                List.of(new TableColumn("id")));
    }


    private void addProcedures(String index)
    {
        Sachem.addProcedures(this, index, index + ":compound", List.of(new TableColumn("compound")));
    }
}
