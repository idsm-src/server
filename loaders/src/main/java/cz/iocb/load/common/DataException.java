package cz.iocb.load.common;

import java.io.IOException;



/*
 * A problem of a part of the loaded data, such as a triple or a record that the loader cannot load. The reader of the
 * part reports it as an error of the given kind and skips the part, so that the load goes on and reports the further
 * problems as well; the detail, which may be null, describes the problem beyond the part itself.
 */
@SuppressWarnings("serial")
public class DataException extends IOException
{
    private final String kind;
    private final String detail;


    public DataException(String kind, String detail)
    {
        super(detail == null ? kind : kind + ": " + detail);
        this.kind = kind;
        this.detail = detail;
    }


    public DataException(String kind)
    {
        this(kind, null);
    }


    public String getKind()
    {
        return kind;
    }


    public String getDetail()
    {
        return detail;
    }
}
