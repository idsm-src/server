package cz.iocb.chemweb.shared.services.query;



/**
 * Error of a submitted query, declared by the methods of {@link QueryService} submitting queries; currently never
 * thrown, since the errors of a query are reported by {@link QueryService#getResult(long)} as a
 * {@link cz.iocb.chemweb.shared.services.DatabaseException}.
 */
public class QueryException extends Exception
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     */
    public QueryException()
    {
    }
}
