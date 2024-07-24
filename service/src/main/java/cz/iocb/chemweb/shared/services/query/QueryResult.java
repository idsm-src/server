package cz.iocb.chemweb.shared.services.query;

import java.io.Serializable;
import java.util.Vector;



public class QueryResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Vector<String> heads;
    private Vector<DataGridNode[]> items;
    private boolean truncated;


    public QueryResult()
    {
    }


    public QueryResult(Vector<String> heads, Vector<DataGridNode[]> items, boolean truncated)
    {
        this.heads = heads;
        this.items = items;
        this.truncated = truncated;
    }


    public Vector<String> getHeads()
    {
        return heads;
    }


    public Vector<DataGridNode[]> getItems()
    {
        return items;
    }


    public boolean isTruncated()
    {
        return truncated;
    }
}
