package cz.iocb.chemweb.shared.services.query;

import java.io.Serializable;
import java.util.List;



/**
 * Result of a query rendered for the result table: the names of the variables and the rows of cells.
 */
public class QueryResult implements Serializable
{
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Names of the result variables.
     */
    private List<String> heads;

    /**
     * Rows of the result, one cell per variable.
     */
    private List<DataGridNode[]> items;

    /**
     * Whether the result has more rows than the requested limit.
     */
    private boolean truncated;


    /**
     * Creates an empty result for the serialization.
     */
    public QueryResult()
    {
    }


    /**
     * Creates the result.
     *
     * @param heads the names of the result variables
     * @param items the rows of the result, one cell per variable
     * @param truncated whether the result has more rows than the requested limit
     */
    public QueryResult(List<String> heads, List<DataGridNode[]> items, boolean truncated)
    {
        this.heads = heads;
        this.items = items;
        this.truncated = truncated;
    }


    /**
     * Returns the names of the result variables.
     *
     * @return the names of the result variables
     */
    public List<String> getHeads()
    {
        return heads;
    }


    /**
     * Returns the rows of the result.
     *
     * @return the rows of the result, one cell per variable
     */
    public List<DataGridNode[]> getItems()
    {
        return items;
    }


    /**
     * Tells whether the result has more rows than the requested limit, the rows beyond the limit being left out.
     *
     * @return true if the result is truncated, false otherwise
     */
    public boolean isTruncated()
    {
        return truncated;
    }
}
