package cz.iocb.chemweb.server.filters.matomo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;



/**
 * Request wrapper caching the body, so that the body can be read more than once: by the Matomo filter and then by the
 * servlet. Taken from
 * http://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once
 */

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper
{
    /**
     * Cached body of the request, null until the body is read first.
     */
    private ByteArrayOutputStream cachedBytes;

    /**
     * Creates the wrapper.
     *
     * @param request the wrapped request
     */
    public MultiReadHttpServletRequest(HttpServletRequest request)
    {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        if(cachedBytes == null)
            cacheInputStream();

        return new CachedServletInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException
    {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    /**
     * Reads the body of the wrapped request into the cache.
     *
     * @throws IOException if an I/O error occurs
     */
    private void cacheInputStream() throws IOException
    {
        /* Cache the inputstream in order to read it multiple times. For
         * convenience, I use apache.commons IOUtils
         */
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), cachedBytes);
    }

    /**
     * Input stream reading the cached body of the request.
     */
    public class CachedServletInputStream extends ServletInputStream
    {
        /**
         * Stream of the cached body.
         */
        private final ByteArrayInputStream input;

        /**
         * Creates a stream reading the cached body from its beginning.
         */
        public CachedServletInputStream()
        {
            /* create a new input stream from the cached request body */
            input = new ByteArrayInputStream(cachedBytes.toByteArray());
        }

        @Override
        public int read() throws IOException
        {
            return input.read();
        }

        @Override
        public boolean isFinished()
        {
            return input.available() == 0;
        }

        @Override
        public boolean isReady()
        {
            return input.available() > 0;
        }

        @Override
        public void setReadListener(ReadListener arg)
        {

        }
    }
}
