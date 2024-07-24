package cz.iocb.chemweb.client.services.query;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import cz.iocb.chemweb.shared.services.query.QueryResult;
import cz.iocb.chemweb.shared.services.query.QueryService;
import cz.iocb.chemweb.shared.services.query.QueryServiceAsync;



public class QueryServiceStub
{
    private static QueryServiceAsync queryService = (QueryServiceAsync) GWT.create(QueryService.class);


    static enum QueryState
    {
        SUBMITTED, RUNNING, FINISHED, CANCELED,
    }


    static public class Query
    {
        private Query()
        {
        }

        private QueryState state;
        private Long queryID;
    }


    static public void countOfProperties(String iri, AsyncCallback<Integer> callback)
    {
        queryService.countOfProperties(iri, callback);
    }


    static public Query query(String code, AsyncCallback<QueryResult> callback)
    {
        return query(code, 0, -1, callback);
    }


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


    static public void cancel(Query query)
    {
        if(query.state == QueryState.FINISHED)
            return;

        if(query.state == QueryState.RUNNING)
            cancelQuery(query.queryID);

        query.state = QueryState.CANCELED;
    }


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
