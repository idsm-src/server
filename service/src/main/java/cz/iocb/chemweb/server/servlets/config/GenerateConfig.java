package cz.iocb.chemweb.server.servlets.config;

import java.io.IOException;
import java.util.Map.Entry;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;



/**
 * Servlet generating the script {@code chemweb/js/config.js} of the user interface: the prefixes of the configuration
 * named by the {@code resource} init parameter (a JNDI resource) and the IRI shown first.
 */
public class GenerateConfig extends HttpServlet
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration whose prefixes are generated.
     */
    private SparqlDatabaseConfiguration dbConfig;


    /**
     * Creates the servlet.
     */
    public GenerateConfig()
    {
    }


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


    /**
     * Writes the script as the response.
     *
     * @param req the request
     * @param res the response
     * @throws IOException if an I/O error occurs
     */
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
