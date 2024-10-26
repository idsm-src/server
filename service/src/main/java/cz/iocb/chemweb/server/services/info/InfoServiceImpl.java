package cz.iocb.chemweb.server.services.info;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import cz.iocb.chemweb.server.services.GWTRemoteServiceServlet;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.info.CountItem;
import cz.iocb.chemweb.shared.services.info.InfoService;
import cz.iocb.chemweb.shared.services.info.SourceItem;



public class InfoServiceImpl extends GWTRemoteServiceServlet implements InfoService
{
    private static final long serialVersionUID = 1L;

    private DataSource connectionPool;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            connectionPool = (DataSource) context.lookup(resourceName);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }

        super.init(config);
    }


    @Override
    public List<CountItem> getCounts() throws DatabaseException
    {
        List<CountItem> list = new ArrayList<CountItem>();

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(true);

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select name, count from info.idsm_counts order by id"))
                {
                    while(result.next())
                        list.add(new CountItem(result.getString(1), result.getInt(2)));
                }
            }
        }
        catch(SQLException e)
        {
            throw new DatabaseException(e);
        }

        return list;
    }


    @Override
    public List<SourceItem> getSources() throws DatabaseException
    {
        List<SourceItem> list = new ArrayList<SourceItem>();

        try(Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(true);

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement
                        .executeQuery("select url, name, version from info.idsm_sources order by id"))
                {
                    while(result.next())
                        list.add(new SourceItem(result.getString(1), result.getString(2), result.getString(3)));
                }
            }
        }
        catch(SQLException e)
        {
            throw new DatabaseException(e);
        }

        return list;
    }
}
