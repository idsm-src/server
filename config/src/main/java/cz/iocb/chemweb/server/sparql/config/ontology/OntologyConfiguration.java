package cz.iocb.chemweb.server.sparql.config.ontology;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.common.SparqlDatabaseOptimisedConfiguration;
import cz.iocb.chemweb.server.sparql.config.mesh.Mesh;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class OntologyConfiguration extends SparqlDatabaseOptimisedConfiguration
{
    static final String schema = "ontology";


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
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");
        addPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
    }


    private void addResourceClasses() throws SQLException
    {
        // the classes of the SKOS mappings to the IRIs of other datasets
        Common.addResourceClasses(this);
        Mesh.addResourceClasses(this);

        Ontology.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Ontology.addQuadMappings(this);
    }
}
