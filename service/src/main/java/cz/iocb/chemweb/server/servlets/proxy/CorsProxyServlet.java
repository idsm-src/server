package cz.iocb.chemweb.server.servlets.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;



@SuppressWarnings("serial")
public class CorsProxyServlet extends HttpServlet
{
    private static final List<String> specialParameters = new ArrayList<String>(
            List.of("endpoint", "requestMethod", "method"));

    private String origin = null;


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        origin = config.getInitParameter("origin");
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException
    {
        setCrossOriginResourceSharingHeaders(res);

        if(!req.getContentType().matches("application/x-www-form-urlencoded.*"))
        {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String accept = req.getHeader("accept");
        String endpoint = req.getParameter("endpoint");
        String method = req.getParameter("method");

        if(method == null)
            method = req.getParameter("requestMethod");

        if(endpoint == null || method == null || !method.matches("GET|POST"))
        {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        String query = "";

        for(Entry<String, String[]> item : req.getParameterMap().entrySet())
            for(String value : item.getValue())
                if(!specialParameters.contains(item.getKey()))
                    query += (query.isEmpty() ? "" : "&") + item.getKey() + "=" + URLEncoder.encode(value, "UTF-8");


        try
        {
            String url = method.equals("GET") ? endpoint + "?" + query : endpoint;
            HttpURLConnection connection = (HttpURLConnection) (new URI(url)).toURL().openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("accept", accept);

            if(method.equals("POST"))
            {
                connection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
                connection.setDoOutput(true);

                try(OutputStream out = connection.getOutputStream())
                {
                    out.write(query.getBytes());
                }
            }

            res.setStatus(connection.getResponseCode());
            res.setContentType(connection.getContentType());

            try(InputStream input = dispatchStream(connection))
            {
                if(input != null)
                {
                    try(OutputStream output = res.getOutputStream())
                    {
                        IOUtils.copy(input, output);
                    }
                }
            }
        }
        catch(URISyntaxException e)
        {
            throw new IOException(e);
        }
    }


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        setCrossOriginResourceSharingHeaders(res);
        super.doOptions(req, res);
    }


    private void setCrossOriginResourceSharingHeaders(HttpServletResponse res)
    {
        if(origin != null)
        {
            res.setHeader("access-control-allow-headers",
                    "x-requested-with, Content-Type, origin, authorization, accept, client-security-token");
            res.setHeader("access-control-allow-origin", origin);
        }
    }


    private InputStream dispatchStream(HttpURLConnection http)
    {
        try
        {
            return http.getInputStream();
        }
        catch(Exception e)
        {
            return http.getErrorStream();
        }
    }
}
