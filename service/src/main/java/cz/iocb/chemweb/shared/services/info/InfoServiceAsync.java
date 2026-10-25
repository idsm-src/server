package cz.iocb.chemweb.shared.services.info;

import java.util.List;
import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * Asynchronous counterpart of {@link InfoService} used by the client.
 */
public interface InfoServiceAsync
{
    /**
     * Requests the statistics of the database.
     *
     * @param callback the callback receiving the rows of the statistics
     */
    void getCounts(AsyncCallback<List<CountItem>> callback);

    /**
     * Requests the list of the data sources.
     *
     * @param callback the callback receiving the data sources
     */
    void getSources(AsyncCallback<List<SourceItem>> callback);
}
