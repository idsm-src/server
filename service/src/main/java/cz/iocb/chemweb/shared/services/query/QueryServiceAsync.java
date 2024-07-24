package cz.iocb.chemweb.shared.services.query;

import com.google.gwt.user.client.rpc.AsyncCallback;



public interface QueryServiceAsync
{
    void query(String code, AsyncCallback<Long> callback);

    void query(String code, int offset, int limit, AsyncCallback<Long> callback);

    void getResult(long id, AsyncCallback<QueryResult> callback);

    void cancel(long id, AsyncCallback<Void> callback);

    void countOfProperties(String iri, AsyncCallback<Integer> callback);
}
