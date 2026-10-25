package cz.iocb.chemweb.shared.services.details;

import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * Asynchronous counterpart of {@link DetailsPageService} used by the client.
 */
public interface DetailsPageServiceAsync
{
    /**
     * Requests the details page of a resource.
     *
     * @param iri the IRI of the resource
     * @param callback the callback receiving the HTML of the page
     */
    void details(String iri, AsyncCallback<String> callback);
}
