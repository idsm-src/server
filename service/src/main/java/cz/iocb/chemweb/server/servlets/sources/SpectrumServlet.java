package cz.iocb.chemweb.server.servlets.sources;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@SuppressWarnings("serial")
public class SpectrumServlet extends SourceServlet
{
    @Override
    public String getServletInfo()
    {
        return "Compound Spectrum Servlet";
    }


    @Override
    protected void generateResponse(HttpServletRequest req, HttpServletResponse res) throws IOException, SQLException
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


    protected String getSpectrum(String id) throws SQLException
    {
        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(true);

            try(PreparedStatement statement = connection.prepareStatement(access))
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
