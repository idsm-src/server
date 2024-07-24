package cz.iocb.chemweb.shared.services;


public class DatabaseException extends Exception
{
    private static final long serialVersionUID = 1L;

    public DatabaseException()
    {

    }

    public DatabaseException(Throwable cause)
    {
        super(cause.getMessage(), cause);
    }
}
