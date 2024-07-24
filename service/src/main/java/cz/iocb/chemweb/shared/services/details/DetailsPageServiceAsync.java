package cz.iocb.chemweb.shared.services.details;

import com.google.gwt.user.client.rpc.AsyncCallback;



public interface DetailsPageServiceAsync
{
    void details(String iri, AsyncCallback<String> callback);
}
