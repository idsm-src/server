package cz.iocb.chemweb.server.services.query;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.iocb.chemweb.server.services.GWTRemoteServiceServlet;
import cz.iocb.chemweb.server.services.SessionData;
import cz.iocb.chemweb.server.velocity.NodeUtils;
import cz.iocb.chemweb.server.velocity.SparqlDirective;
import cz.iocb.chemweb.server.velocity.UrlDirective;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.SessionException;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.services.query.QueryException;
import cz.iocb.chemweb.shared.services.query.QueryResult;
import cz.iocb.chemweb.shared.services.query.QueryService;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.error.TranslateExceptions;
import cz.iocb.sparql.engine.request.Engine;
import cz.iocb.sparql.engine.request.LiteralNode;
import cz.iocb.sparql.engine.request.RdfNode;
import cz.iocb.sparql.engine.request.Request;
import cz.iocb.sparql.engine.request.Result;



public class QueryServiceImpl extends GWTRemoteServiceServlet implements QueryService
{
    private static class QueryState
    {
        Thread thread;
        Request request;
        QueryResult result;
        Throwable exception;
    }


    private static final long serialVersionUID = 1L;
    private static final long timeout = 15 * 60 * 1000000000l; // 15 minutes
    private static final SessionData<QueryState> sessionData = new SessionData<QueryState>("QuerySessionStorage");
    private static final Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);
    private static final int cacheSize = 1000000;
    private static final Map<String, Map<RdfNode, String>> nodeHashMaps = new HashMap<String, Map<RdfNode, String>>();

    private Map<RdfNode, String> nodeHashMap;
    private SparqlDatabaseConfiguration sparqlConfig;
    private Engine engine;
    private VelocityEngine ve;
    private ExecutorService executorService;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String resourceName = config.getInitParameter("resource");

        if(resourceName == null || resourceName.isEmpty())
            throw new ServletException("Resource name is not set");


        try
        {
            Context context = (Context) (new InitialContext()).lookup("java:comp/env");
            sparqlConfig = (SparqlDatabaseConfiguration) context.lookup(resourceName);

            engine = new Engine(sparqlConfig);


            Properties properties = new Properties();
            properties.put("file.resource.loader.path", config.getServletContext().getRealPath("/templates"));
            properties.put("userdirective",
                    "cz.iocb.chemweb.server.velocity.SparqlDirective,cz.iocb.chemweb.server.velocity.UrlDirective");

            ve = new VelocityEngine(properties);
            ve.setApplicationAttribute(SparqlDirective.SPARQL_CONFIG, sparqlConfig);
        }
        catch(NamingException e)
        {
            throw new ServletException(e);
        }


        synchronized(QueryServiceImpl.class)
        {
            nodeHashMap = nodeHashMaps.get(resourceName);

            if(nodeHashMap == null)
            {
                nodeHashMap = Collections.synchronizedMap(new LinkedHashMap<RdfNode, String>(cacheSize, 0.75f, true)
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected boolean removeEldestEntry(Map.Entry<RdfNode, String> eldest)
                    {
                        return size() > cacheSize;
                    }
                });

                nodeHashMaps.put(resourceName, nodeHashMap);
            }
        }


        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        super.init(config);
    }


    @Override
    public void destroy()
    {
        executorService.shutdown();

        super.destroy();
    }


    @Override
    public long query(String query) throws QueryException, DatabaseException
    {
        return query(query, 0, -1);
    }


    @Override
    public long query(String query, int offset, int limit) throws QueryException, DatabaseException
    {
        final HttpSession httpSession = this.getThreadLocalRequest().getSession(true);
        final QueryState queryState = new QueryState();


        logger.info(query.replaceAll("\n", "\\\\n"));

        queryState.request = engine.getRequest();


        final long id = sessionData.insert(httpSession, queryState);

        queryState.thread = new Thread()
        {
            @Override
            public void run()
            {
                try(Result result = queryState.request.execute(query, offset, limit + 1, timeout))
                {
                    Template template = ve.getTemplate("node.vm");
                    Vector<Future<DataGridNode[]>> futures = new Vector<Future<DataGridNode[]>>(limit + 1);

                    while(result.next())
                    {
                        final RdfNode[] row = result.getRow();

                        futures.add(executorService.submit(() -> {
                            DataGridNode[] stringRow = new DataGridNode[row.length];

                            for(int i = 0; i < row.length; i++)
                            {
                                stringRow[i] = new DataGridNode();

                                if(row[i] != null && row[i].isIri())
                                    stringRow[i].ref = row[i].getValue();


                                String html = nodeHashMap.get(row[i]);

                                if(html != null)
                                {
                                    stringRow[i].html = html;
                                }
                                else
                                {
                                    try
                                    {
                                        VelocityContext context = new VelocityContext();
                                        context.put("urlContext", UrlDirective.Context.NODE);
                                        context.put("utils", new NodeUtils(sparqlConfig.getPrefixes()));
                                        context.put("entity", row[i]);

                                        StringWriter writer = new StringWriter();
                                        template.merge(context, writer);

                                        stringRow[i].html = writer.toString();
                                        nodeHashMap.put(row[i], stringRow[i].html);
                                    }
                                    catch(Throwable e)
                                    {
                                        stringRow[i].html = "&lt;template error&gt;";
                                    }
                                }
                            }

                            return stringRow;
                        }));
                    }


                    Vector<DataGridNode[]> items = new Vector<DataGridNode[]>(futures.size());

                    for(Future<DataGridNode[]> future : futures)
                        items.add(future.get());


                    boolean truncated = false;

                    if(limit >= 0 && items.size() > limit)
                    {
                        truncated = true;
                        items.remove(limit);
                    }


                    queryState.result = new QueryResult(result.getHeads(), items, truncated);
                }
                catch(Throwable e)
                {
                    queryState.exception = e;
                }
                finally
                {
                    try
                    {
                        queryState.request.close();
                    }
                    catch(Exception e)
                    {
                    }
                }
            }
        };


        queryState.thread.start();

        return id;
    }


    @Override
    public QueryResult getResult(long queryID) throws SessionException, DatabaseException
    {
        final HttpSession httpSession = this.getThreadLocalRequest().getSession(false);

        if(httpSession == null)
            throw new SessionException("Your server session does not exist.");

        final QueryState queryState = sessionData.get(httpSession, queryID);

        if(queryState == null)
            throw new SessionException("Your server session does not contain the requested query ID.");


        try
        {
            queryState.thread.join();
        }
        catch(InterruptedException e)
        {
            throw new DatabaseException(e); //FIXME: use different exception
        }
        finally
        {
            sessionData.remove(httpSession, queryID);
        }

        if(queryState.exception != null)
        {
            if(queryState.exception instanceof DatabaseException)
                throw(DatabaseException) queryState.exception;
            else
                throw new DatabaseException(queryState.exception); //FIXME: use different exception
        }

        return queryState.result;
    }


    @Override
    public void cancel(long queryID) throws SessionException, DatabaseException
    {
        HttpSession httpSession = this.getThreadLocalRequest().getSession(false);

        if(httpSession == null)
            throw new SessionException("Your server session does not exist.");

        final QueryState queryState = sessionData.getAndRemove(httpSession, queryID);

        if(queryState == null)
            throw new SessionException("Your server session does not contain the requested query ID.");

        try
        {
            queryState.request.cancel();
        }
        catch(SQLException e)
        {
            throw new DatabaseException(e);
        }
    }


    @Override
    public int countOfProperties(String iri) throws DatabaseException
    {
        try(Request request = engine.getRequest())
        {
            String query = "SELECT (count(*) as ?C) WHERE { " + "<" + new URI(iri) + "> ?Property ?Value. }";

            try(Result result = request.execute(query))
            {
                if(!result.next())
                    throw new DatabaseException();

                if(result.getHeads().size() != 1)
                    throw new DatabaseException();

                return Integer.parseInt(((LiteralNode) result.get(0)).getValue());
            }
        }
        catch(URISyntaxException | TranslateExceptions | SQLException e)
        {
            throw new DatabaseException(e);
        }
    }
}
