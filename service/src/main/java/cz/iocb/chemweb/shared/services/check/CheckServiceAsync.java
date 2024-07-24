package cz.iocb.chemweb.shared.services.check;

import com.google.gwt.user.client.rpc.AsyncCallback;



public interface CheckServiceAsync
{
    void check(String code, AsyncCallback<CheckResult> callback);
}
