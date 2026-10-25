package cz.iocb.chemweb.shared.services;


/**
 * Failure of a service on the server side (a database error, a failed or interrupted query), reported to the client.
 */
public class DatabaseException extends Exception
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception without a message.
     */
    public DatabaseException()
    {

    }

    /**
     * Creates the exception caused by another one, taking over its message.
     *
     * @param cause the cause of the failure
     */
    public DatabaseException(Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
