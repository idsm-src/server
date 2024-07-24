package cz.iocb.chemweb.shared.services.info;

import java.util.List;
import com.google.gwt.user.client.rpc.AsyncCallback;



public interface InfoServiceAsync
{
    void getCounts(AsyncCallback<List<CountItem>> callback);

    void getSources(AsyncCallback<List<SourceItem>> callback);
}
