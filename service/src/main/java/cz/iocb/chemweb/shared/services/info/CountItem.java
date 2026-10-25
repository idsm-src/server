package cz.iocb.chemweb.shared.services.info;

import java.io.Serializable;



/**
 * Row of the statistics shown on the about page: the name of a counted kind of data and its count.
 */
public class CountItem implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the counted kind of data.
     */
    public String name;

    /**
     * Count of the items.
     */
    public long count;


    /**
     * Creates an empty row for the serialization.
     */
    public CountItem()
    {
    }


    /**
     * Creates the row.
     *
     * @param name the name of the counted kind of data
     * @param count the count of the items
     */
    public CountItem(String name, long count)
    {
        this.name = name;
        this.count = count;
    }
}
