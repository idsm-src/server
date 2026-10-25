package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.openscience.cdk.exception.CDKException;



/**
 * Base of the servlets serving a record of the database selected by the {@code id} parameter: the record is read by the
 * SQL query given by the {@code access} init parameter from the data source named by the {@code resource} init
 * parameter (a JNDI resource). The responses allow any origin.
 */
@SuppressWarnings("serial")
public abstract class SourceServlet extends HttpServlet
{
    /**
     * Data source of the database.
     */
    protected DataSource connectionPool;

    /**
     * SQL query reading the record of an id, which is its only parameter.
     */
    protected String access;


    /**
     * Creates the servlet.
     */
    protected SourceServlet()
    {
    }


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        access = config.getInitParameter("access");

        if(access == null || access.isEmpty())
            throw new ServletException("access query is not set");


        try
        {
            String resourceName = config.getInitParameter("resource");

            if(resourceName == null || resourceName.isEmpty())
                throw new ServletException("resource name is not set");

            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            connectionPool = (DataSource) context.lookup(resourceName);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        setBasicHttpHeaders(req, res);
        processRequest(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        setBasicHttpHeaders(req, res);
        processRequest(req, res);
    }


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        setBasicHttpHeaders(req, res);
        super.doOptions(req, res);
    }


    /**
     * Adds the headers common to all responses, which allow any origin to read them.
     *
     * @param req the request
     * @param res the response
     */
    protected static void setBasicHttpHeaders(HttpServletRequest req, HttpServletResponse res)
    {
        res.setHeader("Access-Control-Allow-Origin", "*");
    }

    /**
     * Generates the response and turns the failures into error responses: an unknown id into 404, an invalid parameter
     * into 400, a database error into 503 and any other failure into 500.
     *
     * @param req the request
     * @param res the response
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        try
        {
            generateResponse(req, res);
        }
        catch(NoSuchElementException e)
        {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
        catch(IllegalArgumentException e)
        {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
        catch(SQLException e)
        {
            res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }
        catch(Throwable e)
        {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * Generates the response for the requested record; an unknown id is reported by a {@link NoSuchElementException},
     * an invalid parameter by an {@link IllegalArgumentException}.
     *
     * @param req the request
     * @param res the response
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     * @throws CDKException if the structure of a compound cannot be processed
     */
    protected abstract void generateResponse(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException, CDKException;
}
