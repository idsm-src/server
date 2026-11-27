package cz.iocb.chemweb.server.filters.log;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map.Entry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import cz.iocb.sparql.endpoint.EndpointServlet;



/**
 * Filter of the SPARQL endpoints logging their failed, timed out and slow queries by {@link QueryLog}. The endpoint
 * ({@link EndpointServlet}) keeps the query, the exception it has ended by and the status of the request in attributes
 * of the request; the duration is the time the request has spent in the filters after this one and in the endpoint, so
 * the filter is to be mapped last.
 */
public class QueryLogFilter implements Filter
{
    /**
     * Log of the queries.
     */
    private QueryLog log;


    /**
     * Creates the filter.
     */
    public QueryLogFilter()
    {
    }


    @Override
    public void init(FilterConfig config) throws ServletException
    {
        log = new QueryLog(config.getServletContext());
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        long begin = System.nanoTime();

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            if(request instanceof HttpServletRequest req
                    && req.getAttribute(EndpointServlet.QUERY_ATTRIBUTE) instanceof String query)
            {
                long duration = System.nanoTime() - begin;

                Integer status = req.getAttribute(EndpointServlet.STATUS_ATTRIBUTE) instanceof Integer code ? code :
                        null;
                Throwable error = req.getAttribute(EndpointServlet.ERROR_ATTRIBUTE) instanceof Throwable exception ?
                        exception : null;
                boolean cancelled = status != null && status == EndpointServlet.CLIENT_CLOSED_REQUEST;

                log.log(QueryLog.Origin.of(req, describe(req)), query, duration, status, error, cancelled);
            }
        }
    }


    /**
     * Describes the request by its method, its path and its parameters other than the query.
     *
     * @param req the HTTP request
     * @return the method, the path and the parameters of the request, without the query
     */
    private static String describe(HttpServletRequest req)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(req.getMethod()).append(' ').append(req.getRequestURI());

        char separator = '?';

        for(Entry<String, String[]> parameter : req.getParameterMap().entrySet())
        {
            if(parameter.getKey().equals("query"))
                continue;

            for(String value : parameter.getValue())
            {
                builder.append(separator).append(URLEncoder.encode(parameter.getKey(), UTF_8)).append('=')
                        .append(URLEncoder.encode(value, UTF_8));
                separator = '&';
            }
        }

        return builder.toString();
    }
}
