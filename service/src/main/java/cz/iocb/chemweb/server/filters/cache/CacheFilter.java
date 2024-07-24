package cz.iocb.chemweb.server.filters.cache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class CacheFilter implements Filter
{
    private FilterConfig filterConfig;
    private HashSet<String> excludedUrls = new HashSet<String>();


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
