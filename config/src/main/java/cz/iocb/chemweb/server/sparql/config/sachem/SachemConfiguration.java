package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.LiteralClass;



public class SachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public SachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema, String index,
            String entity, String iriPrefix, int idLength, DatabaseTable table, String idColumn,
            LiteralClass molfileLiteralClass) throws SQLException
    {
        super(service, connectionPool, schema);

        String compound = index + ":" + entity;

        addPrefixes();
        addResourceClasses(index, compound, iriPrefix, idLength);
        addQuadMappings(index, compound, table, idColumn, molfileLiteralClass);
        addProcedures(index, compound);
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);
        Sachem.addPrefixes(this);
        MolFiles.addPrefixes(this);
    }


    private void addResourceClasses(String index, String compound, String iriPrefix, int idLength)
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass(compound, INT4, iriPrefix, idLength));
        addIriClass(new IntegerUserIriClass(index + ":molfile", INT4, iriPrefix, idLength, "_Molfile"));
    }


    private void addQuadMappings(String index, String compound, DatabaseTable table, String idColumn,
            LiteralClass molfileLiteralClass)
    {
        MolFiles.addQuadMappings(this, compound, index + ":molfile", table, getColumns(getIriClass(compound), idColumn),
                idColumn, "molfile", molfileLiteralClass);
    }


    private void addProcedures(String index, String compound)
    {
        Sachem.addProcedures(this, index, compound, getColumns(getIriClass(compound), "compound"));
    }
}
