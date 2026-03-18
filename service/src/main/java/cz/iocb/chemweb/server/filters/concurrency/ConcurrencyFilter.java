package cz.iocb.chemweb.server.filters.concurrency;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;



public class ConcurrencyFilter implements Filter
{
    private static class IpLimiter
    {
        final Semaphore semaphore;
        final AtomicInteger references;

        public IpLimiter(int limit)
        {
            semaphore = new Semaphore(limit, true);
            references = new AtomicInteger(0);
        }
    }


    private int limit = 10;
    private final ConcurrentHashMap<String, IpLimiter> limiters = new ConcurrentHashMap<>();


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        String ip = request.getRemoteAddr();

        IpLimiter limiter = limiters.compute(ip, (k, v) -> {
            if(v == null)
                v = new IpLimiter(limit);

            v.references.incrementAndGet();
            return v;
        });

        boolean acquired = false;


        try
        {
            limiter.semaphore.acquire();
            acquired = true;
            chain.doFilter(request, response);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new ServletException("Request interrupted while waiting for IP slot", e);
        }
        finally
        {
            if(acquired)
                limiter.semaphore.release();

            limiters.computeIfPresent(ip, (k, v) -> {
                if(v.references.decrementAndGet() == 0 && v.semaphore.availablePermits() == limit)
                    return null;

                return v;
            });
        }
    }


    @Override
    public void init(FilterConfig config) throws ServletException
    {
        try
        {
            limit = Integer.parseInt(config.getInitParameter("max-concurrent-per-ip"));
        }
        catch(NullPointerException e)
        {
            throw new ServletException("parameter max-concurrent-per-ip is not specified");
        }
        catch(NumberFormatException e)
        {
            throw new ServletException("parameter max-concurrent-per-ip cannot be parsed");
        }
    }


    @Override
    public void destroy()
    {
    }
}
