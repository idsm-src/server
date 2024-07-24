/*
 * Piwik Java Tracker
 *
 * @link https://github.com/piwik/piwik-java-tracker
 * @license https://github.com/piwik/piwik-java-tracker/blob/master/LICENSE BSD-3 Clause
 */
package cz.iocb.chemweb.server.filters.piwik;

import java.io.IOException;
import java.util.concurrent.Future;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.piwik.java.tracking.PiwikRequest;



/**
 * A class that sends {@link PiwikRequest}s to a specified Piwik server.
 *
 * @author brettcsorba
 */
public class PiwikAsyncTracker
{
    private static final String AUTH_TOKEN = "token_auth";
    private static final String REQUESTS = "requests";
    private static final int DEFAULT_TIMEOUT = 5000;
    private final UriBuilder uriBuilder;
    private final CloseableHttpAsyncClient client;


    /**
     * Creates a tracker that will send {@link PiwikRequest}s to the specified Tracking HTTP API endpoint.
     *
     * @param hostUrl url endpoint to send requests to. Usually in the format
     *            <strong>http://your-piwik-domain.tld/piwik.php</strong>.
     */
    public PiwikAsyncTracker(String hostUrl)
    {
        this(hostUrl, DEFAULT_TIMEOUT);
    }


    /**
     * Creates a tracker that will send {@link PiwikRequest}s to the specified Tracking HTTP API endpoint.
     *
     * @param hostUrl url endpoint to send requests to. Usually in the format
     *            <strong>http://your-piwik-domain.tld/piwik.php</strong>.
     * @param timeout the timeout of the sent request in milliseconds
     */
    public PiwikAsyncTracker(String hostUrl, int timeout)
    {
        uriBuilder = UriBuilder.fromPath(hostUrl);
        this.client = getHttpClient(null, 0, timeout);
    }


    /**
     * Creates a tracker that will send {@link PiwikRequest}s to the specified Tracking HTTP API endpoint via the
     * provided proxy
     *
     * @param hostUrl url endpoint to send requests to. Usually in the format
     *            <strong>http://your-piwik-domain.tld/piwik.php</strong>.
     * @param proxyHost url endpoint for the proxy
     * @param proxyPort proxy server port number
     */
    public PiwikAsyncTracker(String hostUrl, String proxyHost, int proxyPort)
    {
        this(hostUrl, proxyHost, proxyPort, DEFAULT_TIMEOUT);
    }


    public PiwikAsyncTracker(String hostUrl, String proxyHost, int proxyPort, int timeout)
    {
        uriBuilder = UriBuilder.fromPath(hostUrl);
        this.client = getHttpClient(proxyHost, proxyPort, timeout);
    }


    /**
     * Send a request.
     *
     * @param request request to send
     * @return the response from this request
     * @throws IOException thrown if there was a problem with this connection
     */
    public Future<HttpResponse> sendRequest(PiwikRequest request, FutureCallback<HttpResponse> callback)
            throws IOException
    {
        uriBuilder.replaceQuery(request.getUrlEncodedQueryString());
        HttpGet get = new HttpGet(uriBuilder.build());

        if(!client.isRunning())
            client.start();

        return client.execute(get, callback);
    }


    /**
     * Send multiple requests in a single HTTP call. More efficient than sending several individual requests.
     *
     * @param requests the requests to send
     * @return the response from these requests
     * @throws IOException thrown if there was a problem with this connection
     */
    public Future<HttpResponse> sendBulkRequest(Iterable<PiwikRequest> requests, FutureCallback<HttpResponse> callback)
            throws IOException
    {
        return sendBulkRequest(requests, callback, null);
    }


    /**
     * Send multiple requests in a single HTTP call. More efficient than sending several individual requests. Specify
     * the AuthToken if parameters that require an auth token is used.
     *
     * @param requests the requests to send
     * @param authToken specify if any of the parameters use require AuthToken
     * @return the response from these requests
     * @throws IOException thrown if there was a problem with this connection
     */
    public Future<HttpResponse> sendBulkRequest(Iterable<PiwikRequest> requests, FutureCallback<HttpResponse> callback,
            String authToken) throws IOException
    {
        if(authToken != null && authToken.length() != PiwikRequest.AUTH_TOKEN_LENGTH)
        {
            throw new IllegalArgumentException(
                    authToken + " is not " + PiwikRequest.AUTH_TOKEN_LENGTH + " characters long.");
        }

        JsonObjectBuilder ob = Json.createObjectBuilder();
        JsonArrayBuilder ab = Json.createArrayBuilder();

        for(PiwikRequest request : requests)
        {
            ab.add("?" + request.getQueryString());
        }

        ob.add(REQUESTS, ab);

        if(authToken != null)
        {
            ob.add(AUTH_TOKEN, authToken);
        }

        HttpPost post = new HttpPost(uriBuilder.build());
        post.setEntity(new StringEntity(ob.build().toString(), ContentType.APPLICATION_JSON));

        try
        {
            if(!client.isRunning())
                client.start();

            return client.execute(post, callback);
        }
        finally
        {
            post.releaseConnection();
        }
    }


    /**
     * Get a HTTP client. With proxy if a proxy is provided in the constructor.
     *
     * @return a HTTP client
     */
    protected CloseableHttpAsyncClient getHttpClient(String proxyHost, int proxyPort, int timeout)
    {
        HttpAsyncClientBuilder builder = HttpAsyncClientBuilder.create();

        if(proxyHost != null && proxyPort != 0)
        {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            builder.setRoutePlanner(routePlanner);
        }

        RequestConfig.Builder config = RequestConfig.custom().setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout).setSocketTimeout(timeout);

        builder.setDefaultRequestConfig(config.build());

        return builder.build();
    }


    public void close() throws IOException
    {
        client.close();
    }
}
