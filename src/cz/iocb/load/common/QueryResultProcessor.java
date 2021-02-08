package cz.iocb.load.common;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;



public abstract class QueryResultProcessor
{
    protected QuerySolution solution = null;
    protected String sparql;


    protected QueryResultProcessor(String sparql)
    {
        this.sparql = sparql;
    }


    public void load(Model model) throws IOException, SQLException
    {
        Query query = QueryFactory.create(Updater.prefixes + sparql);

        try(QueryExecution qexec = QueryExecutionFactory.create(query, model))
        {
            ResultSet results = qexec.execSelect();
            while(results.hasNext())
            {
                solution = results.nextSolution();
                parse();
            }
        }
    }


    protected abstract void parse() throws IOException, SQLException;


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
            throw new IOException("unexpected IRI: " + value);

        return Integer.parseInt(value.substring(prefix.length()));
    }


    protected String getStringID(String name, String prefix) throws IOException
    {
        Resource resource = solution.getResource(name);

        if(resource == null)
            return null;

        String value = resource.getURI();

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return value.substring(prefix.length());
    }


    protected String getString(String name)
    {
        Literal literal = solution.getLiteral(name);

        if(literal == null)
            return null;

        return literal.getString();
    }


    protected Integer getInt(String name)
    {
        Literal literal = solution.getLiteral(name);

        if(literal == null)
            return null;

        return literal.getInt();
    }


    protected Boolean getBoolean(String name)
    {
        Literal literal = solution.getLiteral(name);

        if(literal == null)
            return null;

        return literal.getBoolean();
    }
}
