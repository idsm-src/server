package cz.iocb.chemweb.server.services.check;

import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import cz.iocb.chemweb.server.services.GWTRemoteServiceServlet;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.check.CheckResult;
import cz.iocb.chemweb.shared.services.check.CheckService;
import cz.iocb.chemweb.shared.services.check.CheckerWarning;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateMessage;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.Request;



public class CheckServiceImpl extends GWTRemoteServiceServlet implements CheckService
{
    private static final long serialVersionUID = 1L;

    private Engine engine;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");

        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            SparqlDatabaseConfiguration sparqlConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);

            engine = new Engine(sparqlConfig);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }

        super.init(config);
    }


    @Override
    public CheckResult check(String code) throws DatabaseException
    {
        CheckResult result = new CheckResult();

        try(Request request = engine.getRequest())
        {
            for(TranslateMessage message : request.check(code))
            {
                try
                {
                    result.warnings.add(new CheckerWarning(message.getRange().getStart().getLineNumber() - 1,
                            message.getRange().getStart().getPositionInLine(),
                            message.getRange().getEnd().getLineNumber() - 1,
                            message.getRange().getEnd().getPositionInLine() + 1, message.getCategory().getText(),
                            message.getMessage()));
                }
                catch(Throwable e)
                {
                    System.err.println("CheckService: log begin");
                    System.err.println(code);
                    e.printStackTrace(System.err);
                    System.err.println("CheckService: log end");
                }
            }
        }
        catch(SQLException e)
        {
            throw new DatabaseException(e);
        }

        return result;
    }
}
