package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.openscience.cdk.exception.CDKException;



@SuppressWarnings("serial")
public abstract class SourceServlet extends HttpServlet
{
    protected DataSource connectionPool;
    protected String access;


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


    protected static void setBasicHttpHeaders(HttpServletRequest req, HttpServletResponse res)
    {
        res.setHeader("Access-Control-Allow-Origin", "*");
    }

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


    protected abstract void generateResponse(HttpServletRequest req, HttpServletResponse res)
            throws IOException, SQLException, CDKException;
}
