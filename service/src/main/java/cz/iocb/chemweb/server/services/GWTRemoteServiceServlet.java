package cz.iocb.chemweb.server.services;

import jakarta.servlet.http.HttpServletRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;



public class GWTRemoteServiceServlet extends RemoteServiceServlet
{
    private static final long serialVersionUID = 1L;


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
