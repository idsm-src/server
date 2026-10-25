package cz.iocb.chemweb.client.services.query;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import cz.iocb.chemweb.shared.services.query.QueryResult;
import cz.iocb.chemweb.shared.services.query.QueryService;
import cz.iocb.chemweb.shared.services.query.QueryServiceAsync;



/**
 * Client side of the query service: submits a query, waits for its result and cancels it on the server when the query
 * is cancelled or its result cannot be received.
 */
public class QueryServiceStub
{
    /**
     * Asynchronous proxy of the query service.
     */
    private static QueryServiceAsync queryService = (QueryServiceAsync) GWT.create(QueryService.class);


    /**
     * State of a submitted query.
     */
    static enum QueryState
    {
        /**
         * Sent to the server, which has not yet returned its id.
         */
        SUBMITTED,

        /**
         * Running on the server under its id.
         */
        RUNNING,

        /**
         * Completed successfully or with an error.
         */
        FINISHED,

        /**
         * Cancelled by the user.
         */
        CANCELED,
    }


    /**
     * Handle of a submitted query, used to cancel it.
     */
    static public class Query
    {
        /**
         * Creates the handle of a submitted query.
         */
        private Query()
        {
        }

        /**
         * State of the query.
         */
        private QueryState state;

        /**
         * Id of the query on the server, known once the query runs.
         */
        private Long queryID;
    }


    /**
     * Not instantiable.
     */
    private QueryServiceStub()
    {
    }


    /**
     * Counts the properties of a resource, i.e. the triples having it as their subject.
     *
     * @param iri the IRI of the resource
     * @param callback the callback receiving the count
     */
    static public void countOfProperties(String iri, AsyncCallback<Integer> callback)
    {
        queryService.countOfProperties(iri, callback);
    }


    /**
     * Submits a query asking for all its results.
     *
     * @param code the SPARQL query
     * @param callback the callback receiving the result
     * @return the handle of the query
     */
    static public Query query(String code, AsyncCallback<QueryResult> callback)
    {
        return query(code, 0, -1, callback);
    }


    /**
     * Submits a query asking for a range of its results.
     *
     * @param code the SPARQL query
     * @param offset the number of the leading results to skip
     * @param limit the maximum number of results, or -1 for no limit
     * @param callback the callback receiving the result
     * @return the handle of the query
     */
    static public Query query(final String code, final int offset, final int limit,
            final AsyncCallback<QueryResult> callback)
    {
        final Query query = new Query();
        query.state = QueryState.SUBMITTED;


        queryService.query(code, offset, limit, new AsyncCallback<Long>()
        {
            @Override
            public void onSuccess(final Long result)
            {
                if(query.state == QueryState.CANCELED)
                {
                    cancelQuery(result);
                    return;
                }

                query.queryID = result;
                query.state = QueryState.RUNNING;

                queryService.getResult(query.queryID, new AsyncCallback<QueryResult>()
                {
                    @Override
                    public void onSuccess(QueryResult result)
                    {
                        query.state = QueryState.FINISHED;
                        callback.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Throwable caught)
                    {
                        // maybe it is still running on server ...
                        cancelQuery(result);

                        query.state = QueryState.FINISHED;
                        callback.onFailure(caught);
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught)
            {
                query.state = QueryState.FINISHED;
                callback.onFailure(caught);
            }
        });


        return query;
    }


    /**
     * Cancels a query that has not finished yet; a query still waiting for its id is cancelled as soon as the id
     * arrives.
     *
     * @param query the handle of the query
     */
    static public void cancel(Query query)
    {
        if(query.state == QueryState.FINISHED)
            return;

        if(query.state == QueryState.RUNNING)
            cancelQuery(query.queryID);

        query.state = QueryState.CANCELED;
    }


    /**
     * Asks the server to cancel a query, repeating the request every minute (at most ten times) while it fails.
     *
     * @param queryID the id of the query on the server
     */
    static private void cancelQuery(final long queryID)
    {
        Timer timer = new Timer()
        {
            int repeated = 0;

            @Override
            public void run()
            {
                repeated++;

                queryService.cancel(queryID, new AsyncCallback<Void>()
                {
                    @Override
                    public void onSuccess(Void result)
                    {
                        cancel();
                    }

                    @Override
                    public void onFailure(Throwable caught)
                    {
                        if(repeated < 10)
                            schedule(60000);
                    }
                });
            }
        };

        timer.run();
    }
}
