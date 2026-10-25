package cz.iocb.load.mona;



/**
 * A metadata item of a spectrum or a compound in the JSON export of MoNA.
 */
public class MetaData
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
     * The name of the item.
     */
    String name;

    /**
     * The unit of the value of the item.
     */
    String unit;

    /**
     * The value of the item.
     */
    String value;


    /**
     * Creates an empty metadata item.
     */
    public MetaData()
    {
    }
}
