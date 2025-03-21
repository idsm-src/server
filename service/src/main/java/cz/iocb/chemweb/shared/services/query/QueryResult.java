package cz.iocb.chemweb.shared.services.query;

import java.io.Serializable;
import java.util.List;



public class QueryResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private List<String> heads;
    private List<DataGridNode[]> items;
    private boolean truncated;


    public QueryResult()
    {
    }


    public QueryResult(List<String> heads, List<DataGridNode[]> items, boolean truncated)
    {
        this.heads = heads;
        this.items = items;
        this.truncated = truncated;
    }


    public List<String> getHeads()
    {
        return heads;
    }


    public List<DataGridNode[]> getItems()
    {
        return items;
    }


    public boolean isTruncated()
    {
        return truncated;
    }
}
