package cz.iocb.load.mona;



/**
 * A tag of a spectrum or a compound in the JSON export of MoNA.
 */
public class Tag
{
    /**
     * Whether the tag is assigned by a rule.
     */
    boolean ruleBased;

    /**
     * The text of the tag.
     */
    String text;


    /**
     * Creates an empty tag.
     */
    public Tag()
    {
    }
}
