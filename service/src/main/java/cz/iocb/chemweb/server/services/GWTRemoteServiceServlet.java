package cz.iocb.chemweb.server.services;

import jakarta.servlet.http.HttpServletRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;



/**
 * Base of the GWT RPC servlets that finds the serialization policy under the module base URL given by the
 * {@code X-GWT-Module-Base} header, if present, so that the services work behind a proxy that changes the URL.
 */
public class GWTRemoteServiceServlet extends RemoteServiceServlet
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates the servlet.
     */
    public GWTRemoteServiceServlet()
    {
    }


    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
            String strongName)
    {
        String gwtModuleBase = request.getHeader("X-GWT-Module-Base");

        if(gwtModuleBase != null)
            moduleBaseURL = gwtModuleBase;

        return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
    }
}
