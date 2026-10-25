package cz.iocb.chemweb.server.filters.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



/**
 * Filter forbidding the caching of the responses, except for the servlet paths listed (separated by spaces) in the
 * {@code excludedUrls} init parameter.
 */
public class CacheFilter implements Filter
{
    /**
     * Configuration of the filter.
     */
    private FilterConfig filterConfig;

    /**
     * Servlet paths whose responses may be cached.
     */
    private HashSet<String> excludedUrls = new HashSet<>();


    /**
     * Creates the filter.
     */
    public CacheFilter()
    {
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        String path = ((HttpServletRequest) request).getServletPath();

        if(!excludedUrls.contains(path))
        {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
        }

        filterChain.doFilter(request, response);
    }


    @Override
    public void init(FilterConfig _filterConfig) throws ServletException
    {
        filterConfig = _filterConfig;

        String excludes = filterConfig.getInitParameter("excludedUrls");

        if(excludes != null)
            excludedUrls.addAll(Set.of(excludes.split(" ")));
    }


    @Override
    public void destroy()
    {
        filterConfig = null;
    }
}
