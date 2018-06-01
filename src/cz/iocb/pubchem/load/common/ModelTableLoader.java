package cz.iocb.pubchem.load.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;



public abstract class ModelTableLoader extends TableLoader
{
    protected QuerySolution solution = null;
    protected Model model;
    protected String sparql;
    protected String sql;


    public ModelTableLoader(Model model, String sparql, String sql)
    {
        this.model = model;
        this.sparql = sparql;
        this.sql = sql;
    }


    public void load() throws SQLException, IOException
    {
        try(Connection connection = Loader.getConnection())
        {
            try(PreparedStatement insertStatement = connection.prepareStatement(sql))
            {
                statement = insertStatement;
                int count = 0;

                Query query = QueryFactory.create(Loader.getPrefixes() + sparql);

                try(QueryExecution qexec = QueryExecutionFactory.create(query, model))
                {
                    ResultSet results = qexec.execSelect();
                    while(results.hasNext())
                    {
                        solution = results.nextSolution();
                        set = false;

                        insert();

                        if(!set)
                            continue;


                        insertStatement.addBatch();

                        if(++count % Loader.batchSize == 0)
                            insertStatement.executeBatch();
                    }
                }

                if(count % Loader.batchSize != 0)
                    insertStatement.executeBatch();
            }
        }
    }


    public abstract void insert() throws SQLException, IOException;


    protected String getIRI(String name)
    {
        Resource resource = solution.getResource(name);

        if(resource == null)
            return null;

        return resource.getURI();
    }


    protected Integer getIntID(String name, String prefix) throws IOException
    {
        Resource resource = solution.getResource(name);

        if(resource == null)
            return null;

        String value = resource.getURI();

        if(!value.startsWith(prefix))
            throw new IOException();

        return Integer.parseInt(value.substring(prefix.length()));
    }


    protected String getStringID(String name, String prefix) throws IOException
    {
        Resource resource = solution.getResource(name);

        if(resource == null)
            return null;

        String value = resource.getURI();

        if(!value.startsWith(prefix))
            throw new IOException();

        return value.substring(prefix.length());
    }


    protected Short getMapID(String name, Map<String, Short> map)
    {
        Resource resource = solution.getResource(name);

        if(resource == null)
            return null;

        return map.get(resource.getURI());
    }


    protected String getLiteralValue(String name)
    {
        Literal literal = solution.getLiteral(name);

        if(literal == null)
            return null;

        return literal.getString();
    }
}
