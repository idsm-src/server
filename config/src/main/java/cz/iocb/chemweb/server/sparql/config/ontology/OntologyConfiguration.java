package cz.iocb.chemweb.server.sparql.config.ontology;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.mapping.classes.LangStringConstantTagClass;



public class OntologyConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "ontology";

    public static final LangStringConstantTagClass rdfLangStringEn = LangStringConstantTagClass.get("en");


    public OntologyConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("dataset", "http://bioinfo.iocb.cz/dataset/");
    }


    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Ontology.addQuadMappings(this);
    }
}
