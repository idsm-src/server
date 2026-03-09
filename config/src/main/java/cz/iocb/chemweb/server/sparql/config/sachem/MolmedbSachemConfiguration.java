package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.mapping.classes.BuiltinClasses.xsdString;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.sparql.engine.database.Conditions;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.Table;
import cz.iocb.sparql.engine.database.TableColumn;
import cz.iocb.sparql.engine.mapping.NodeMapping;
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

        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Sachem.addResourceClasses(this);

        String prefix = "https://rdf.molmedb.upol.cz/substance/";

        addIriClass(new MapUserIriClass("molmedb:substance", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), "https://identifiers.org/molmedb/", "MM[0-9.]+"));

        addIriClass(new MapUserIriClass("molmedb:smiles", "integer", new Table(schema, "substance_bases"),
                new TableColumn("id"), new TableColumn("identifier"), prefix, "MM[0-9.]+", "_SMILES"));
    }


    private void addQuadMappings()
    {
        Table table = new Table(schema, "substance_bases");
        NodeMapping subject = createIriMapping("molmedb:smiles", "id");
        Conditions cnd = createIsNotNullCondition("canonical_smiles");

        addQuadMapping(table, null, createIriMapping("molmedb:substance", "id"), createIriMapping("sio:SIO_000008"),
                subject, createIsNotNullCondition("canonical_smiles"));

        addQuadMapping(table, null, subject, createIriMapping("rdf:type"), createIriMapping("sio:CHEMINF_000018"), cnd);

        addQuadMapping(table, null, subject, createIriMapping("sio:SIO_000300"),
                createLiteralMapping(xsdString, "canonical_smiles"));
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "molmedb", "molmedb:substance", List.of(new TableColumn("compound")));
    }
}
