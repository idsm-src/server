package cz.iocb.chemweb.server.filters.piwik;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
//import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.piwik.java.tracking.PiwikRequest;



public class PiwikFilter implements Filter
{
    private int siteId = -1;
    private String address = null;
    private String authToken = null;
    private String actionName = null;
    private boolean rpc = false;

    private String cookieName = null;
    private PiwikAsyncTracker tracker = null;


    static
    {
        /* memory leak protection*/
        ClassLoader active = Thread.currentThread().getContextClassLoader();
        ClassLoader root = active;

        while(root.getParent() != null)
            root = root.getParent();

        Thread.currentThread().setContextClassLoader(root);
        //DatatypeConverter.printHexBinary(new byte[0]);
        Thread.currentThread().setContextClassLoader(active);
    }


    @Override
    public void init(FilterConfig config) throws ServletException
    {
        /* obtain the value of init parameter piwikAddress */
        address = config.getServletContext().getInitParameter("piwikAddress");

        if(address == null || address.isEmpty())
            throw new ServletException("Parameter piwikAddress is not specified");


        /* obtain the value of init parameter authToken */
        authToken = config.getServletContext().getInitParameter("piwikAuthToken");

        if(authToken == null || authToken.isEmpty())
            throw new ServletException("Parameter piwikAuthToken is not specified");


        /* obtain the value of init parameter piwikSiteId */
        String siteIdString = config.getServletContext().getInitParameter("piwikSiteId");

        if(siteIdString == null || siteIdString.isEmpty())
            throw new ServletException("Parameter piwikSiteId is not specified");

        siteId = Integer.parseInt(siteIdString);


        /* obtain the value of parameter filterName */
        actionName = config.getInitParameter("actionName");


        /* obtain the value of parameter rpc */
        String rpcString = config.getInitParameter("rpc");

        if(rpcString != null && !rpcString.isEmpty())
            rpc = Boolean.parseBoolean(rpcString);

        cookieName = "_pk_id." + siteId + ".";

        tracker = new PiwikAsyncTracker(address, 20000);
    }


    @Override
    public void destroy()
    {
        try
        {
            tracker.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String actionUrl = httpRequest.getRequestURL().toString();
        String actionName = null;


        if(rpc)
        {
            httpRequest = new MultiReadHttpServletRequest(httpRequest);

            StringWriter writer = new StringWriter();
            IOUtils.copy(httpRequest.getReader(), writer);
            String body = writer.toString();
            String[] args = body.split("\\|");

            if(args.length > 6)
            {
                actionName = "GWT RPC: " + args[5] + "." + args[6] + "()";
                actionUrl = actionUrl + "/" + args[6];
            }
        }
        else
        {
            actionUrl = actionUrl.replaceFirst(((HttpServletRequest) request).getContextPath(), "");
        }

        if(this.actionName != null)
            actionName = this.actionName;


        try
        {
            /* create piwik request */
            PiwikRequest piwikRequest = new PiwikRequest(siteId, (new URI(actionUrl)).toURL());
            piwikRequest.setActionName(actionName);
            piwikRequest.setAuthToken(authToken);


            /* set visitor id */
            String visitorId = null;

            if(httpRequest.getCookies() != null)
                for(Cookie cookie : httpRequest.getCookies())
                    if(cookie.getName().startsWith(cookieName) && cookie.getValue().length() >= 16)
                        visitorId = cookie.getValue().substring(0, 16);

            if(visitorId != null)
                piwikRequest.setVisitorId(visitorId);


            /* set visitor ip */
            String forwarded = httpRequest.getHeader("X-Forwarded-For");
            String address = null;

            if(forwarded != null)
            {
                for(String forward : forwarded.split(","))
                {
                    String trimmed = forward.trim();

                    try
                    {
                        if(!InetAddress.getByName(trimmed).isSiteLocalAddress())
                        {
                            address = trimmed;
                            break;
                        }
                    }
                    catch(UnknownHostException e)
                    {
                        request.getServletContext().log("PiwikFilter: UnknownHostException: " + e.getMessage());
                    }
                }
            }

            piwikRequest.setVisitorIp(address != null ? address : httpRequest.getRemoteAddr());


            /* set user agent */
            String agent = httpRequest.getHeader("User-Agent");

            if(agent != null)
                piwikRequest.setHeaderUserAgent(agent);


            /* set action time */
            long actionTime = System.currentTimeMillis();
            filterChain.doFilter(httpRequest, response);
            actionTime = System.currentTimeMillis() - actionTime;
            piwikRequest.setActionTime(actionTime);


            tracker.sendRequest(piwikRequest, new FutureCallback<HttpResponse>()
            {
                @Override
                public void failed(Exception e)
                {
                    request.getServletContext()
                            .log("PiwikFilter: Exception: " + e.getClass().getCanonicalName() + ": " + e.getMessage());
                }

                @Override
                public void cancelled()
                {
                    request.getServletContext().log("PiwikFilter: cancelled");
                }

                @Override
                public void completed(HttpResponse response)
                {
                }
            });
        }
        catch(Throwable e)
        {
            e.printStackTrace();

            try
            {
                tracker.close();
            }
            catch(Throwable error)
            {
                e.printStackTrace();
            }

            tracker = new PiwikAsyncTracker(this.address, 20000);
        }
    }
}
