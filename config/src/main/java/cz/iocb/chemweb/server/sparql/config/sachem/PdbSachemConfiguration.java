package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
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

        addIriClass(
                new MapUserIriClass("pdb:molfile", "integer", new Table("pdb", "compound_bases"), new TableColumn("id"),
                        new TableColumn("name"), "https://idsm.elixir-czech.cz/rdf/pdb-ccd/", ".*", "_molfile"));

        addIriClass(new MapUserIriClass("pdb:compound", "integer", new Table("pdb", "compound_bases"),
                new TableColumn("id"), new TableColumn("name"), "https://identifiers.org/pdb-ccd/", ".*"));
    }


    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "pdb:compound", "pdb:molfile", new Table("pdb", "compound_bases"),
                List.of(new TableColumn("id")), "id", "molfile", xsdString);
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "pdb", "pdb:compound", List.of(new TableColumn("compound")));
    }
}
