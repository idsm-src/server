package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import static cz.iocb.sparql.engine.database.SqlType.VARCHAR;
import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.TermMapping;
import cz.iocb.sparql.engine.mapping.classes.MapUserIriClass;



public class MolmedbSachemConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    private final String schema = "molmedb";


    public MolmedbSachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
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

        addPrefix("molmedb", "https://identifiers.org/molmedb/");
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Sachem.addResourceClasses(this);

        String prefix = "https://rdf.molmedb.upol.cz/substance/";

        addIriClass(new MapUserIriClass("molmedb:substance", INT4, new DatabaseTable(schema, "substances"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), "https://identifiers.org/molmedb/",
                "MM[0-9.]+"));

        addIriClass(new MapUserIriClass("molmedb:smiles", INT4, new DatabaseTable(schema, "substances"),
                new TableColumn("id", INT4), new TableColumn("identifier", VARCHAR), prefix, "MM[0-9.]+", "_SMILES"));
    }


    private void addQuadMappings()
    {
        DatabaseTable table = new DatabaseTable(schema, "substances");
        TermMapping subject = createIriMapping("molmedb:smiles", "id");
        Conditions cnd = createIsNotNullCondition(table, "canonical_smiles");

        addQuadMapping(table, null, createIriMapping("molmedb:substance", "id"), createIriMapping("sio:SIO_000008"),
                subject, createIsNotNullCondition(table, "canonical_smiles"));

        addQuadMapping(table, null, subject, createIriMapping("rdf:type"), createIriMapping("sio:CHEMINF_000018"), cnd);

        addQuadMapping(table, null, subject, createIriMapping("sio:SIO_000300"),
                createLiteralMapping(xsdString, "canonical_smiles"));
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "molmedb", "molmedb:substance",
                getColumns(getIriClass("molmedb:substance"), "compound"));
    }
}
