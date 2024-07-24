package cz.iocb.chemweb.shared.services.info;

import java.io.Serializable;



public class CountItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    public String name;
    public long count;


    public CountItem()
    {
    }


    public CountItem(String name, long count)
    {
        this.name = name;
        this.count = count;
    }
}
