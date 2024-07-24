package cz.iocb.chemweb.shared.services;

public class SessionException extends Exception
{
    private static final long serialVersionUID = 1L;


    public SessionException()
    {
    }


    public SessionException(String message)
    {
        super(message);
    }
}
