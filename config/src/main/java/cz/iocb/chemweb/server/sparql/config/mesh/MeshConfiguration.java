package cz.iocb.chemweb.server.sparql.config.mesh;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;
import cz.iocb.sparql.engine.mapping.classes.LangStringConstantTagClass;



public class MeshConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "mesh";

    static final LangStringConstantTagClass rdfLangStringEn = LangStringConstantTagClass.get("en");


    public MeshConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("meshv", "http://id.nlm.nih.gov/mesh/vocab#");
        addPrefix("mesh", "http://id.nlm.nih.gov/mesh/");
    }


    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Mesh.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Mesh.addQuadMappings(this);
    }
}
