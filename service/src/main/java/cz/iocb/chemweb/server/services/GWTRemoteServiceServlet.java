package cz.iocb.chemweb.server.services;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;



/**
 * Base of the GWT RPC servlets that finds the serialization policy of a call in the directory of the servlet path,
 * where the compiled module keeps its policy files, instead of under the module base URL of the client, so that the
 * services work behind a proxy that maps the module to another path. A call naming a policy the deployment does not
 * have, such as a call from a page loaded before the module was redeployed, fails as incompatible with the service
 * instead of being answered under the legacy policy, which serializes none of the transferred objects.
 */
public class GWTRemoteServiceServlet extends RemoteServiceServlet
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Form of the strong names of the serialization policies, which keeps a requested name inside the module directory.
     */
    private static final Pattern strongNamePattern = Pattern.compile("[0-9A-F]{32}");


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
        if(strongName != null && strongNamePattern.matcher(strongName).matches())
        {
            String servletPath = request.getServletPath();
            String modulePath = servletPath.substring(0, servletPath.lastIndexOf('/') + 1);
            String file = SerializationPolicyLoader.getSerializationPolicyFileName(modulePath + strongName);

            try(InputStream stream = getServletContext().getResourceAsStream(file))
            {
                if(stream != null)
                {
                    SerializationPolicy policy = SerializationPolicyLoader.loadFromStream(stream, null);

                    // the check GWT makes, see https://github.com/gwtproject/gwt/issues/9709
                    if(!policy.hasClientFields() || SerializationPolicyLoader.ENABLE_ENHANCED_CLASSES)
                        return policy;

                    log("ERROR: The serialization policy file '" + file + "' allows enhanced JPA/JDO classes.");
                }
            }
            catch(IOException | ParseException e)
            {
                log("ERROR: Cannot read the serialization policy file '" + file + "'", e);
            }
        }

        // in the super dev mode, the code server holds the policies
        if(getCodeServerPolicyUrl(strongName) != null)
            return null;

        throw new IncompatibleRemoteServiceException("The serialization policy '" + strongName
                + "' is not available for the module '" + moduleBaseURL + "'");
    }
}
