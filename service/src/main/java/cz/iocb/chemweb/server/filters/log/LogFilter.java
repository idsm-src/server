package cz.iocb.chemweb.server.filters.log;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;



/**
 * Filter logging the URI of every request; the remote address, the remote host and the session id are put into the
 * mapped diagnostic context while the request is served.
 */
public class LogFilter implements Filter
{
    /**
     * Logger of the requests.
     */
    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);

    /**
     * Configuration of the filter.
     */
    @SuppressWarnings("unused")
    private FilterConfig filterConfig;


    /**
     * Creates the filter.
     */
    public LogFilter()
    {
    }



    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        MDC.put("remoteaddr", httpRequest.getRemoteAddr());
        MDC.put("remotehost", httpRequest.getRemoteHost());
        MDC.put("session", httpRequest.getSession().getId());

        logger.info(httpRequest.getRequestURI());
        filterChain.doFilter(request, response);

        MDC.remove("remoteaddr");
        MDC.remove("remotehost");
        MDC.remove("session");
    }


    @Override
    public void init(FilterConfig _filterConfig) throws ServletException
    {
        filterConfig = _filterConfig;
    }


    @Override
    public void destroy()
    {
        filterConfig = null;
    }
}
