package cz.iocb.load.common;

import java.io.IOException;



/**
 * A problem of a part of the loaded data, such as a triple or a record that the loader cannot load. The reader of the
 * part reports it as an error of the given kind and skips the part, so that the load goes on and reports the further
 * problems as well; the detail, which may be null, describes the problem beyond the part itself.
 */
@SuppressWarnings("serial")
public class DataException extends IOException
{
    /**
     * Kind of the problem.
     */
    private final String kind;

    /**
     * Description of the problem beyond the part itself, or null.
     */
    private final String detail;


    /**
     * Creates the exception.
     *
     * @param kind the kind of the problem
     * @param detail the description of the problem beyond the part itself, or null
     */
    public DataException(String kind, String detail)
    {
        super(detail == null ? kind : kind + ": " + detail);
        this.kind = kind;
        this.detail = detail;
    }


    /**
     * Creates the exception without a detail.
     *
     * @param kind the kind of the problem
     */
    public DataException(String kind)
    {
        this(kind, null);
    }


    /**
     * Returns the kind of the problem.
     *
     * @return the kind of the problem
     */
    public String getKind()
    {
        return kind;
    }


    /**
     * Returns the description of the problem beyond the part itself.
     *
     * @return the detail, or null
     */
    public String getDetail()
    {
        return detail;
    }
}
