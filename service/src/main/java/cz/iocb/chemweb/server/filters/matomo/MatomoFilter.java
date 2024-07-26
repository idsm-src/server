package cz.iocb.chemweb.server.filters.matomo;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
//import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.TrackerConfiguration;
import org.matomo.java.tracking.parameters.VisitorId;



public class MatomoFilter implements Filter
{
    private MatomoTracker tracker = null;
    private String cookieName = null;
    private String actionName = null;
    private boolean rpc = false;


    static
    {
        /* memory leak protection*/
        ClassLoader active = Thread.currentThread().getContextClassLoader();
        ClassLoader root = active;

        while(root.getParent() != null)
            root = root.getParent();

        Thread.currentThread().setContextClassLoader(root);
        DatatypeConverter.printHexBinary(new byte[0]);
        Thread.currentThread().setContextClassLoader(active);
    }


    @Override
    public void init(FilterConfig config) throws ServletException
    {
        /* obtain the value of init parameter matomoAddress */
        String address = config.getServletContext().getInitParameter("matomoAddress");

        if(address == null || address.isEmpty())
            throw new ServletException("Parameter matomoAddress is not specified");


        /* obtain the value of init parameter authToken */
        String authToken = config.getServletContext().getInitParameter("matomoAuthToken");

        if(authToken == null || authToken.isEmpty())
            throw new ServletException("Parameter matomoAuthToken is not specified");


        /* obtain the value of init parameter matomoSiteId */
        String siteIdString = config.getServletContext().getInitParameter("matomoSiteId");

        if(siteIdString == null || siteIdString.isEmpty())
            throw new ServletException("Parameter matomoSiteId is not specified");

        int siteId = Integer.parseInt(siteIdString);


        /* obtain the value of parameter filterName */
        actionName = config.getInitParameter("actionName");


        /* obtain the value of parameter rpc */
        String rpcString = config.getInitParameter("rpc");

        if(rpcString != null && !rpcString.isEmpty())
            rpc = Boolean.parseBoolean(rpcString);


        /* set cookie name */
        cookieName = "_pk_id." + siteId + ".";


        try
        {
            TrackerConfiguration trackerConfig = TrackerConfiguration.builder().apiEndpoint(new URI(address))
                    .connectTimeout(Duration.ofSeconds(20)).socketTimeout(Duration.ofSeconds(20))
                    .defaultAuthToken(authToken).defaultSiteId(siteId).build();

            tracker = new MatomoTracker(trackerConfig);
        }
        catch(URISyntaxException e)
        {
            throw new ServletException(e);
        }
    }


    @Override
    public void destroy()
    {
        try
        {
            tracker.close();
        }
        catch(Throwable e)
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
            /* create matomo request */
            MatomoRequest matomoRequest = MatomoRequest.request().actionUrl(actionUrl).actionName(actionName).build();


            /* set visitor id */
            String visitorId = null;

            if(httpRequest.getCookies() != null)
                for(Cookie cookie : httpRequest.getCookies())
                    if(cookie.getName().startsWith(cookieName) && cookie.getValue().length() >= 16)
                        visitorId = cookie.getValue().substring(0, 16);

            if(visitorId != null)
                matomoRequest.setVisitorId(VisitorId.fromHex(visitorId));


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
                        request.getServletContext().log("MatomoFilter: UnknownHostException: " + e.getMessage());
                    }
                }
            }

            matomoRequest.setVisitorIp(address != null ? address : httpRequest.getRemoteAddr());


            /* set user agent */
            String agent = httpRequest.getHeader("User-Agent");

            if(agent != null)
                matomoRequest.setHeaderUserAgent(agent);


            /* set action time */
            long actionTime = System.currentTimeMillis();
            filterChain.doFilter(httpRequest, response);
            actionTime = System.currentTimeMillis() - actionTime;
            matomoRequest.setServerTime(actionTime);

            tracker.sendRequestAsync(matomoRequest);
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
    }
}
