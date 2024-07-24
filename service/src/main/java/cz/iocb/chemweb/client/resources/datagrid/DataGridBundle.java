package cz.iocb.chemweb.client.resources.datagrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;



public interface DataGridBundle extends DataGrid.Resources
{
    interface ResultTableCSS extends DataGrid.Style
    {
    }

    public static final DataGridBundle INSTANCE = GWT.create(DataGridBundle.class);

    @Override
    @Source({ DataGrid.Style.DEFAULT_CSS, "datagrid.css" })
    ResultTableCSS dataGridStyle();
}
