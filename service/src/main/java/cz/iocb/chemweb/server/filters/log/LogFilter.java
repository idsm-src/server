package cz.iocb.chemweb.server.filters.log;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;



public class LogFilter implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);

    @SuppressWarnings("unused")
    private FilterConfig filterConfig;



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
