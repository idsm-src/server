package cz.iocb.chemweb.server.services.details;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.request.IriNode;
import cz.iocb.chemweb.server.velocity.NodeUtils;
import cz.iocb.chemweb.server.velocity.SparqlDirective;
import cz.iocb.chemweb.server.velocity.UrlDirective;
import cz.iocb.chemweb.shared.services.details.DetailsPageService;



public class DetailsPageServiceImpl extends RemoteServiceServlet implements DetailsPageService
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DetailsPageServiceImpl.class);

    private SparqlDatabaseConfiguration dbConfig;
    private VelocityEngine ve;


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

        Properties properties = new Properties();
        properties.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        properties.put("file.resource.loader.path", config.getServletContext().getRealPath("/templates"));
        properties.put("userdirective",
                "cz.iocb.chemweb.server.velocity.SparqlDirective,cz.iocb.chemweb.server.velocity.UrlDirective");

        ve = new VelocityEngine(properties);
        ve.setApplicationAttribute(SparqlDirective.SPARQL_CONFIG, dbConfig);

        super.init(config);
    }


    @Override
    public String details(String iriText)
    {
        try
        {
            long time = System.currentTimeMillis();
            URI uri = new URI(iriText);

            StringWriter writer = new StringWriter();
            Template template = ve.getTemplate("detail.vm");

            VelocityContext context = new VelocityContext();
            context.put("urlContext", UrlDirective.Context.DETAILS);
            context.put("utils", new NodeUtils(dbConfig.getPrefixes()));
            context.put("entity", new IriNode(uri.toString()));

            template.merge(context, writer);
            writer.close();

            time = System.currentTimeMillis() - time;
            logger.info(uri + " " + time / 1000.0 + "s");

            return writer.toString();
        }
        catch(URISyntaxException e)
        {
            // TODO:
            return "";
        }
        catch(IOException e)
        {
            // TODO:
            return "";
        }
    }
}
