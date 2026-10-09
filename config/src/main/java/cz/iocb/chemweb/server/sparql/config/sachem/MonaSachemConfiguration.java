package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.mona.Mona;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class MonaSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public MonaSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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
        MolFiles.addPrefixes(this);

        addPrefix("mona", Mona.mona);
        addPrefix("bnmona", Mona.bnmona);
    }


    private void addResourceClasses()
    {
        Sachem.addResourceClasses(this);

        addIriClass(new IntegerUserIriClass("mona:molfile", INT4, Mona.bnmona + "id", "_molfile"));

        addIriClass(new MapUserIriClass("mona:compound", INT4, new DatabaseTable("mona", "spectra"),
                new TableColumn("id", INT4), new TableColumn("accession", VARCHAR), Mona.mona, ".*", "_CMPD"));
    }


    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "mona:compound", "mona:molfile",
                new DatabaseTable("mona", "compound_structures"), getColumns(getIriClass("mona:compound"), "compound"),
                "compound", "structure", xsdString);
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "mona", "mona:compound", getColumns(getIriClass("mona:compound"), "compound"));
    }
}
