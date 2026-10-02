package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class PdbSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    public PdbSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
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
    }


    private void addResourceClasses()
    {
        Sachem.addResourceClasses(this);

        addIriClass(new MapUserIriClass("pdb:molfile", INT4, new DatabaseTable("pdb", "compound_bases"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR),
                "https://idsm.elixir-czech.cz/rdf/pdb-ccd/", ".*", "_molfile"));

        addIriClass(new MapUserIriClass("pdb:compound", INT4, new DatabaseTable("pdb", "compound_bases"),
                new TableColumn("id", INT4), new TableColumn("name", VARCHAR), "https://identifiers.org/pdb-ccd/",
                ".*"));
    }


    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "pdb:compound", "pdb:molfile", new DatabaseTable("pdb", "compound_bases"),
                getColumns(getIriClass("pdb:compound"), "id"), "id", "molfile", xsdString);
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "pdb", "pdb:compound", getColumns(getIriClass("pdb:compound"), "compound"));
    }
}
