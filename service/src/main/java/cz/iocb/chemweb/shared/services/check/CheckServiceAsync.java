package cz.iocb.chemweb.shared.services.check;

import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * Asynchronous counterpart of {@link CheckService} used by the client.
 */
public interface CheckServiceAsync
{
    /**
     * Requests the check of a query.
     *
     * @param code the text of the query
     * @param callback the callback receiving the messages of the checker
     */
    void check(String code, AsyncCallback<CheckResult> callback);
}
