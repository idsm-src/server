package cz.iocb.chemweb.server.servlets.config;

import java.io.IOException;
import java.util.Map.Entry;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;



public class GenerateConfig extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private SparqlDatabaseConfiguration dbConfig;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            dbConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        processRequest(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        processRequest(req, res);
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        ServletOutputStream out = res.getOutputStream();


        out.println("var prefixes = [");

        boolean first = true;

        for(Entry<String, String> prefix : dbConfig.getPrefixes().entrySet())
        {
            if(!first)
                out.println(",");
            else
                first = false;

            out.print("{ \"name\": \"");
            out.print(prefix.getKey());
            out.print(":\", \"iri\":\"");
            out.print(prefix.getValue());
            out.print("\"}");
        }

        out.println("];");
        out.println();
        out.println("var startIri = \"http://www.w3.org/2002/07/owl#Class\";");

        out.close();
    }
}
