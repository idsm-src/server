package cz.iocb.chemweb.shared.services.details;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;



/**
 * Service rendering the details pages of resources.
 */
@RemoteServiceRelativePath("details")
public interface DetailsPageService extends RemoteService
{
    /**
     * Renders the details page of a resource by the template matching its IRI.
     *
     * @param iri the IRI of the resource
     * @return the HTML of the page, empty if the IRI is invalid or the template cannot be rendered
     */
    String details(String iri);
}
