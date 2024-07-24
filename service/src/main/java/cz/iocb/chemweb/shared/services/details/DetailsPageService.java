package cz.iocb.chemweb.shared.services.details;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;



@RemoteServiceRelativePath("details")
public interface DetailsPageService extends RemoteService
{
    String details(String iri);
}
