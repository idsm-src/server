package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;



@SuppressWarnings("serial")
public class CompoundSpectrumServlet extends HttpServlet
{
    private DataSource connectionPool;
    private String query;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");
        String schema = config.getInitParameter("schema");
        String table = config.getInitParameter("table");
        String id = config.getInitParameter("id");
        String spectrum = config.getInitParameter("spectrum");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        if(id == null)
            id = "id";

        if(spectrum == null)
            spectrum = "molfile";

        query = "select \"" + spectrum.replace("\"", "\"\"") + "\" from \"" + schema.replace("\"", "\"\"") + "\".\""
                + table.replace("\"", "\"\"") + "\" where \"" + id.replace("\"", "\"\"") + "\" = ?";

        try
        {
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
        processRequest(req, res);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        processRequest(req, res);
    }


    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        try
        {
            String id = req.getParameter("id");

            String spectrum = getSpectrum(id);

            if(spectrum == null)
                throw new NoSuchElementException("invalid spectrum id");

            res.setContentType("application/json");

            try(ServletOutputStream out = res.getOutputStream())
            {
                out.print(spectrum);
            }
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


    @Override
    public String getServletInfo()
    {
        return "Compound Spectrum Servlet";
    }


    private String getSpectrum(String id) throws SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(true);

            try(PreparedStatement statement = connection.prepareStatement(query))
            {
                statement.setString(1, id);
                ResultSet result = statement.executeQuery();

                if(result.next())
                    return "{\"peaks\":[{\"mz\":"
                            + result.getString(1).replaceAll(":", ",\"intensity\":").replaceAll(" ", "},{\"mz\":")
                            + "}]}";
            }

            return null;
        }
    }
}
