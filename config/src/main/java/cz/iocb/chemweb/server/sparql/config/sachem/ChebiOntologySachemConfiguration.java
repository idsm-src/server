package cz.iocb.chemweb.server.sparql.config.sachem;

import static cz.iocb.sparql.engine.database.SqlType.INT4;
import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.StringSubsetLiteralClass;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.database.DatabaseTable;
import cz.iocb.sparql.engine.mapping.classes.IntegerUserIriClass;



public class ChebiOntologySachemConfiguration extends SparqlDatabaseConfiguration
{
    public ChebiOntologySachemConfiguration(String service, DataSource connectionPool, DatabaseSchema schema)
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
        MolFiles.addPrefixes(this);

        addPrefix("obo", "http://purl.obolibrary.org/obo/");
    }


    private void addResourceClasses() throws SQLException
    {
        Sachem.addResourceClasses(this);
        Ontology.addResourceClasses(this);

        addIriClass(
                new IntegerUserIriClass("chebi:molfile", INT4, "http://purl.obolibrary.org/obo/CHEBI_", "_Molfile"));
    }


    private void addQuadMappings()
    {
        MolFiles.addQuadMappings(this, "ontology:resource", "chebi:molfile", new DatabaseTable("molecules", "chebi"),
                getColumns(getIriClass("ontology:resource"), Ontology.unitCHEBI, "id"),
                new StringSubsetLiteralClass("chebi-molfile"));
    }


    private void addProcedures()
    {
        Sachem.addProcedures(this, "chebi", "ontology:resource",
                getColumns(getIriClass("ontology:resource"), Ontology.unitCHEBI, "compound"));
    }
}
