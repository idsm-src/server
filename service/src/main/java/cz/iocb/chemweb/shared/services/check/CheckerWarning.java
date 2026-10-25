package cz.iocb.chemweb.shared.services.check;

import java.io.Serializable;



/**
 * Message of the query checker together with the range of the query text it refers to; lines and columns are
 * zero-based, as the code editor expects them.
 */
public class CheckerWarning implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an empty message for the serialization.
     */
    public CheckerWarning()
    {
    }

    /**
     * Creates the message.
     *
     * @param beginLine the line where the range begins
     * @param beginColumnt the column where the range begins
     * @param endLine the line where the range ends
     * @param endColumnt the column where the range ends (exclusive)
     * @param type the category of the message ({@code error} or {@code warning})
     * @param message the text of the message
     */
    public CheckerWarning(int beginLine, int beginColumnt, int endLine, int endColumnt, String type, String message)
    {
        this.beginLine = beginLine;
        this.beginColumnt = beginColumnt;
        this.endLine = endLine;
        this.endColumnt = endColumnt;
        this.type = type;
        this.message = message;
    }

    /**
     * Line where the range begins.
     */
    public int beginLine;

    /**
     * Column where the range begins.
     */
    public int beginColumnt;

    /**
     * Line where the range ends.
     */
    public int endLine;

    /**
     * Column where the range ends (exclusive).
     */
    public int endColumnt;

    /**
     * Category of the message ({@code error} or {@code warning}).
     */
    public String type;

    /**
     * Text of the message.
     */
    public String message;
}
