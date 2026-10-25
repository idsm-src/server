package cz.iocb.chemweb.shared.services;

/**
 * Failure of a request that refers to a query the server session does not hold (the session does not exist or does not
 * contain the query id).
 */
public class SessionException extends Exception
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates the exception without a message.
     */
    public SessionException()
    {
    }


    /**
     * Creates the exception with a message.
     *
     * @param message the message for the user
     */
    public SessionException(String message)
    {
        super(message);
    }
}
