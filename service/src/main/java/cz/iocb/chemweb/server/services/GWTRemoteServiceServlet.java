package cz.iocb.chemweb.server.services;

import javax.servlet.http.HttpServletRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;



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
