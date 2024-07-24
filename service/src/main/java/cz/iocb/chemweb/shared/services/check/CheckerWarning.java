package cz.iocb.chemweb.shared.services.check;

import java.io.Serializable;



public class CheckerWarning implements Serializable
{
    private static final long serialVersionUID = 1L;

    public CheckerWarning()
    {
    }

    public CheckerWarning(int beginLine, int beginColumnt, int endLine, int endColumnt, String type, String message)
    {
        this.beginLine = beginLine;
        this.beginColumnt = beginColumnt;
        this.endLine = endLine;
        this.endColumnt = endColumnt;
        this.type = type;
        this.message = message;
    }

    public int beginLine;
    public int beginColumnt;

    public int endLine;
    public int endColumnt;

    public String type;
    public String message;
}
