package cz.iocb.chemweb.client.resources.datagrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;



/**
 * Resources of the data grids of the client, which replace the default style of the grid by {@code datagrid.css}.
 */
public interface DataGridBundle extends DataGrid.Resources
{
    /**
     * Style of the data grids defined by the default style of the grid and {@code datagrid.css}.
     */
    interface ResultTableCSS extends DataGrid.Style
    {
    }

    /**
     * Shared instance of the resources.
     */
    public static final DataGridBundle INSTANCE = GWT.create(DataGridBundle.class);

    @Override
    @Source({ DataGrid.Style.DEFAULT_CSS, "datagrid.css" })
    ResultTableCSS dataGridStyle();
}
