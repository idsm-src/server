package cz.iocb.chemweb.server.sparql.config.chebi;

import java.sql.SQLException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.sparql.config.common.Common;
import cz.iocb.chemweb.server.sparql.config.ontology.Ontology;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.database.DatabaseSchema;



public class ChebiConfiguration extends SparqlDatabaseConfiguration
{
    static final String schema = "chebi";


    public ChebiConfiguration(String service, DataSource connectionPool, DatabaseSchema schema) throws SQLException
    {
        super(service, connectionPool, schema);

        addPrefixes();
        addResourceClasses();
        addQuadMappings();
    }


    private void addPrefixes()
    {
        Common.addPrefixes(this);

        addPrefix("ebi", "http://rdf.ebi.ac.uk/dataset/");
        addPrefix("obo", "http://purl.obolibrary.org/obo/");
        addPrefix("chebi", "http://purl.obolibrary.org/obo/chebi/");
        addPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");

        // extension
        addPrefix("sio", "http://semanticscience.org/resource/");
    }


    private void addResourceClasses() throws SQLException
    {
        Ontology.addResourceClasses(this);

        Chebi.addResourceClasses(this);
    }


    private void addQuadMappings()
    {
        Chebi.addQuadMappings(this);
    }
}
