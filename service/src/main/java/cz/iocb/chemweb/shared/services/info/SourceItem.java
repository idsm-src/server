package cz.iocb.chemweb.shared.services.info;

import java.io.Serializable;



public class SourceItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String url;
    public String name;
    public String version;


    public SourceItem()
    {
    }


    public SourceItem(String url, String name, String version)
    {
        this.url = url;
        this.name = name;
        this.version = version;
    }
}
