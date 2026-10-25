package cz.iocb.chemweb.server.filters.concurrency;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;



/**
 * Filter limiting the number of the requests of one client IP address served at the same time to the value of the
 * {@code max-concurrent-per-ip} init parameter; the further requests wait for a free slot in their order.
 */
public class ConcurrencyFilter implements Filter
{
    /**
     * Slots of the concurrent requests of one IP address.
     */
    private static class IpLimiter
    {
        /**
         * Permits of the requests served at the same time.
         */
        final Semaphore semaphore;

        /**
         * Number of the requests holding or waiting for a permit; the limiter is dropped when it falls to zero.
         */
        final AtomicInteger references;

        /**
         * Creates the limiter.
         *
         * @param limit the maximum number of the requests served at the same time
         */
        public IpLimiter(int limit)
        {
            semaphore = new Semaphore(limit, true);
            references = new AtomicInteger(0);
        }
    }


    /**
     * Maximum number of the requests of one IP address served at the same time.
     */
    private int limit = 10;

    /**
     * Limiters of the IP addresses with requests in progress.
     */
    private final ConcurrentHashMap<String, IpLimiter> limiters = new ConcurrentHashMap<>();


    /**
     * Creates the filter.
     */
    public ConcurrencyFilter()
    {
    }


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
