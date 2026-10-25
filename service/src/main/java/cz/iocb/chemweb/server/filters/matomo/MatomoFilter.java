package cz.iocb.chemweb.server.filters.matomo;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import javax.xml.bind.DatatypeConverter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.matomo.java.tracking.MatomoRequest;
import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.TrackerConfiguration;
import org.matomo.java.tracking.parameters.VisitorId;



/**
 * Filter reporting the requests to the Matomo web analytics configured by the context parameters {@code matomoAddress},
 * {@code matomoAuthToken} and {@code matomoSiteId}. The action is named by the {@code actionName} init parameter or,
 * for GWT RPC calls (init parameter {@code rpc}), after the called method.
 */
public class MatomoFilter implements Filter
{
    /**
     * Tracker sending the reports to Matomo.
     */
    private MatomoTracker tracker = null;

    /**
     * Name prefix of the Matomo cookie holding the visitor id.
     */
    private String cookieName = null;

    /**
     * Action name of all filtered requests, null if the actions are named by the requests.
     */
    private String actionName = null;

    /**
     * Whether the filtered requests are GWT RPC calls, whose actions are named after the methods called.
     */
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


    /**
     * Creates the filter.
     */
    public MatomoFilter()
    {
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
