package cz.iocb.chemweb.shared.services.query;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.iocb.chemweb.shared.services.DatabaseException;
import cz.iocb.chemweb.shared.services.SessionException;



/**
 * Service evaluating SPARQL queries for the user interface. A query is submitted first and runs in the background under
 * an id kept in the server session; its result is then requested by the id, or the query is cancelled.
 */
@RemoteServiceRelativePath("query")
public interface QueryService extends RemoteService
{
    /**
     * Submits a query for all its results.
     *
     * @param code the text of the query
     * @return the id of the query in the server session
     */
    long query(String code);

    /**
     * Submits a query for a range of its results.
     *
     * @param code the text of the query
     * @param offset the number of leading results to skip
     * @param limit the maximum number of results, negative for no limit
     * @return the id of the query in the server session
     */
    long query(String code, int offset, int limit);

    /**
     * Waits for a submitted query to finish and returns its result; the query is removed from the session.
     *
     * @param id the id of the query
     * @return the result of the query
     * @throws SessionException if the session does not exist or does not hold the query
     * @throws DatabaseException if the evaluation of the query failed
     */
    QueryResult getResult(long id) throws SessionException, DatabaseException;

    /**
     * Cancels a submitted query and removes it from the session.
     *
     * @param id the id of the query
     * @throws SessionException if the session does not exist or does not hold the query
     * @throws DatabaseException if a database error occurs
     */
    void cancel(long id) throws SessionException, DatabaseException;

    /**
     * Returns the number of the triples having a resource as their subject.
     *
     * @param iri the IRI of the resource
     * @return the number of the triples
     * @throws DatabaseException if the IRI is invalid or the query fails
     */
    int countOfProperties(String iri) throws DatabaseException;
}
