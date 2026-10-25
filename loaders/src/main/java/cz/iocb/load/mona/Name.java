package cz.iocb.load.mona;



/**
 * A name of a compound in the JSON export of MoNA.
 */
public class Name
{
    /**
     * Whether the name is computed.
     */
    String computed;

    /**
     * The name.
     */
    String name;

    /**
     * The score of the name.
     */
    Integer score;

    /**
     * The source of the name.
     */
    String source;


    /**
     * Creates an empty name.
     */
    public Name()
    {
    }
}
