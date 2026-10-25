package cz.iocb.load.mona;



/**
 * A classification item of a compound in the JSON export of MoNA, such as a ClassyFire class.
 */
public class Classification
{
    /**
     * The category of the item.
     */
    String category;

    /**
     * Whether the item is computed.
     */
    boolean computed;

    /**
     * Whether the item is hidden.
     */
    boolean hidden;

    /**
     * The name of the item, such as the level of a ClassyFire class.
     */
    String name;

    /**
     * The URL of the item.
     */
    String url;

    /**
     * The value of the item, such as the name of a ClassyFire class.
     */
    String value;


    /**
     * Creates an empty classification item.
     */
    public Classification()
    {
    }
}
