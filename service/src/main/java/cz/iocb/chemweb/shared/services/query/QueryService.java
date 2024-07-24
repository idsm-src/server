package cz.iocb.chemweb.shared.services.query;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.SessionException;



@RemoteServiceRelativePath("query")
public interface QueryService extends RemoteService
{
    long query(String code) throws SessionException, QueryException, DatabaseException;

    long query(String code, int offset, int limit) throws SessionException, QueryException, DatabaseException;

    QueryResult getResult(long id) throws SessionException, DatabaseException;

    void cancel(long id) throws SessionException, DatabaseException;

    int countOfProperties(String iri) throws DatabaseException;
}
