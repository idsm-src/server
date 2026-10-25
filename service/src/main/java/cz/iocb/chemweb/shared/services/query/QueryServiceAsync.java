package cz.iocb.chemweb.shared.services.query;

import com.google.gwt.user.client.rpc.AsyncCallback;



/**
 * Asynchronous counterpart of {@link QueryService} used by the client.
 */
public interface QueryServiceAsync
{
    /**
     * Submits a query for all its results.
     *
     * @param code the text of the query
     * @param callback the callback receiving the id of the query
     */
    void query(String code, AsyncCallback<Long> callback);

    /**
     * Submits a query for a range of its results.
     *
     * @param code the text of the query
     * @param offset the number of leading results to skip
     * @param limit the maximum number of results, negative for no limit
     * @param callback the callback receiving the id of the query
     */
    void query(String code, int offset, int limit, AsyncCallback<Long> callback);

    /**
     * Requests the result of a submitted query.
     *
     * @param id the id of the query
     * @param callback the callback receiving the result
     */
    void getResult(long id, AsyncCallback<QueryResult> callback);

    /**
     * Cancels a submitted query.
     *
     * @param id the id of the query
     * @param callback the callback notified when the query is cancelled
     */
    void cancel(long id, AsyncCallback<Void> callback);

    /**
     * Requests the number of the triples having a resource as their subject.
     *
     * @param iri the IRI of the resource
     * @param callback the callback receiving the number of the triples
     */
    void countOfProperties(String iri, AsyncCallback<Integer> callback);
}
