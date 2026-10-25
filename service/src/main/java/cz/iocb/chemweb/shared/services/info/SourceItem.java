package cz.iocb.chemweb.shared.services.info;

import java.io.Serializable;



/**
 * Data source listed on the about page: its name, its home page and the version of the loaded data.
 */
public class SourceItem implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * URL of the home page of the source.
     */
    public String url;

    /**
     * Name of the source.
     */
    public String name;

    /**
     * Version of the loaded data.
     */
    public String version;


    /**
     * Creates an empty source for the serialization.
     */
    public SourceItem()
    {
    }


    /**
     * Creates the source.
     *
     * @param url the URL of the home page of the source
     * @param name the name of the source
     * @param version the version of the loaded data
     */
    public SourceItem(String url, String name, String version)
    {
        this.url = url;
        this.name = name;
        this.version = version;
    }
}
