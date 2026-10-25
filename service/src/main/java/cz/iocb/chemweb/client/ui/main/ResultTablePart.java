package cz.iocb.chemweb.client.ui.main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent;
import com.google.gwt.view.client.RowCountChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import cz.iocb.chemweb.client.resources.datagrid.DataGridBundle;
import cz.iocb.chemweb.client.resources.pager.PagerResourcesFactory;
import cz.iocb.chemweb.client.services.query.QueryServiceStub;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.services.query.QueryResult;



/**
 * Tab with the results of the query in a data grid: the results are queried page by page and the rows of the page can
 * be filtered by a text. The start and the end of the evaluation and the visits of the resources clicked in the grid
 * are announced by events.
 */
public class ResultTablePart extends ResizeComposite
{
    /**
     * Binder of the tab template {@code ResultTablePart.ui.xml}.
     */
    interface ResultTablePartUiBinder extends UiBinder<Widget, ResultTablePart>
    {
    }


    /**
     * Status of the evaluation of the query.
     */
    public static enum Status
    {
        /**
         * The query runs.
         */
        RUNNING,

        /**
         * The query has completed, failed or been cancelled.
         */
        FINISHED
    }


    /**
     * Event announcing a change of the status of the evaluation.
     */
    public static class StatusChangeEvent extends GwtEvent<StatusChangeEventHandler>
    {
        /**
         * Type of the event.
         */
        public static Type<StatusChangeEventHandler> TYPE = new Type<>();

        /**
         * The new status.
         */
        private final Status status;

        /**
         * Creates the event.
         *
         * @param status the new status
         */
        public StatusChangeEvent(Status status)
        {
            this.status = status;
        }

        @Override
        public Type<StatusChangeEventHandler> getAssociatedType()
        {
            return TYPE;
        }

        @Override
        protected void dispatch(StatusChangeEventHandler handler)
        {
            handler.onStatusChange(this);
        }

        /**
         * Returns the new status.
         *
         * @return the status
         */
        public Status getStatus()
        {
            return status;
        }
    }


    /**
     * Handler of the events announcing a change of the status of the evaluation.
     */
    public interface StatusChangeEventHandler extends EventHandler
    {
        /**
         * Called when the status of the evaluation changes.
         *
         * @param event the event
         */
        void onStatusChange(StatusChangeEvent event);
    }


    /**
     * Event announcing that a resource clicked in the grid is to be visited.
     */
    public static class VisitItemEvent extends GwtEvent<VisitItemEventHandler>
    {
        /**
         * Type of the event.
         */
        public static Type<VisitItemEventHandler> TYPE = new Type<>();

        /**
         * IRI of the resource.
         */
        private final String iri;

        /**
         * Creates the event.
         *
         * @param iri the IRI of the resource
         */
        public VisitItemEvent(String iri)
        {
            this.iri = iri;
        }

        @Override
        public Type<VisitItemEventHandler> getAssociatedType()
        {
            return TYPE;
        }

        @Override
        protected void dispatch(VisitItemEventHandler handler)
        {
            handler.onVisitItem(this);
        }

        /**
         * Returns the resource to visit.
         *
         * @return the IRI of the resource
         */
        public String getIri()
        {
            return iri;
        }
    }


    /**
     * Handler of the events announcing that a resource is to be visited.
     */
    public interface VisitItemEventHandler extends EventHandler
    {
        /**
         * Called when a resource is to be visited.
         *
         * @param event the event
         */
        void onVisitItem(VisitItemEvent event);
    }


    /**
     * Display standing between the provider of the results and the data grid: it receives the rows of the current page
     * and passes those that pass the filter to the grid. Only the members the provider and the pager use are
     * implemented.
     */
    private static class HasDataFilter implements HasData<DataGridNode[]>
    {
        /**
         * Filter of the rows.
         */
        private static interface IFilter
        {
            /**
             * Decides whether a row passes the filter.
             *
             * @param value the row
             * @return true if the row is shown
             */
            boolean isValid(DataGridNode[] value);
        }


        /**
         * Data grid showing the rows that pass the filter.
         */
        DataGrid<DataGridNode[]> dataGrid;

        /**
         * Filter of the rows, null when all rows are shown.
         */
        IFilter filter = null;

        /**
         * Rows of the current page.
         */
        List<? extends DataGridNode[]> values = new ArrayList<>();

        /**
         * Manager of the range, row count and cell preview handlers.
         */
        private final HandlerManager handlerManager = new HandlerManager(this);

        /**
         * Whether the row count is exact.
         */
        private boolean exact = false;

        /**
         * Visible range of the rows.
         */
        private Range range = new Range(0, 0);

        /**
         * Number of the rows of the result known so far.
         */
        private int count = -1;


        /**
         * Creates the display.
         *
         * @param dataGrid the data grid showing the rows that pass the filter
         */
        public HasDataFilter(DataGrid<DataGridNode[]> dataGrid)
        {
            this.dataGrid = dataGrid;
        }


        /**
         * Shows the rows of the current page that pass the filter in the data grid.
         */
        @SuppressWarnings("unchecked")
        private void show()
        {
            List<DataGridNode[]> results = null;

            if(filter != null)
            {
                results = new ArrayList<>(values.size());

                for(DataGridNode[] item : values)
                    if(filter.isValid(item))
                        results.add(item);
            }
            else
            {
                results = (List<DataGridNode[]>) values;
            }

            dataGrid.setPageSize(results.size());
            dataGrid.setRowCount(results.size());
            dataGrid.setRowData(0, results);
        }


        /**
         * Sets the filter of the rows and shows the rows that pass it.
         *
         * @param filter the filter, or null to show all rows
         */
        public void setFilter(IFilter filter)
        {
            this.filter = filter;
            show();
        }


        @Override
        public HandlerRegistration addRangeChangeHandler(RangeChangeEvent.Handler handler)
        {
            return handlerManager.addHandler(RangeChangeEvent.getType(), handler);
        }

        @Override
        public HandlerRegistration addRowCountChangeHandler(RowCountChangeEvent.Handler handler)
        {
            return handlerManager.addHandler(RowCountChangeEvent.getType(), handler);
        }

        @Override
        public int getRowCount()
        {
            return count;
        }

        @Override
        public Range getVisibleRange()
        {
            return range;
        }

        @Override
        public boolean isRowCountExact()
        {
            return exact;
        }

        @Override
        public void setRowCount(int count)
        {
            setRowCount(count, true);
        }

        @Override
        public void setRowCount(int count, boolean exact)
        {
            this.count = count;
            this.exact = exact;
            fireEvent(new RowCountChangeEvent(count, exact)
            {
            });
        }

        @Override
        public void setVisibleRange(int start, int length)
        {
            setVisibleRange(new Range(start, length));
        }

        @Override
        public void setVisibleRange(Range range)
        {
            this.range = range;
            fireEvent(new RangeChangeEvent(range)
            {
            });
        }

        @Override
        public void fireEvent(GwtEvent<?> event)
        {
            handlerManager.fireEvent(event);
        }

        @Override
        public HandlerRegistration addCellPreviewHandler(CellPreviewEvent.Handler<DataGridNode[]> handler)
        {
            return handlerManager.addHandler(CellPreviewEvent.getType(), handler);
        }

        @Override
        public SelectionModel<? super DataGridNode[]> getSelectionModel()
        {
            Window.alert("getSelectionModel");
            return null;
        }

        @Override
        public DataGridNode[] getVisibleItem(int indexOnPage)
        {
            Window.alert("getVisibleItem");
            return null;
        }

        @Override
        public int getVisibleItemCount()
        {
            Window.alert("getVisibleItemCount");
            return 0;
        }

        @Override
        public Iterable<DataGridNode[]> getVisibleItems()
        {
            Window.alert("getVisibleItems");
            return null;
        }

        @Override
        public void setRowData(int start, List<? extends DataGridNode[]> values)
        {
            this.values = values;
            show();
        }

        @Override
        public void setSelectionModel(SelectionModel<? super DataGridNode[]> selectionModel)
        {
            Window.alert("setSelectionModel");

        }

        @Override
        public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent)
        {
            Window.alert("setVisibleRangeAndClearData");
        }
    }


    /**
     * Provider of the rows, which queries the results of the query on the visible range.
     */
    private class AsyncResultsProvider extends AsyncDataProvider<DataGridNode[]>
    {
        /**
         * Number of the queries for the rows, which identifies the latest one.
         */
        private int asyncResultsCounter = 0;

        /**
         * The query whose results are provided.
         */
        private String queryString = null;

        /**
         * Whether the columns are to be rebuilt from the heads of the next result.
         */
        private boolean resetColumns = false;

        /**
         * Number of the rows known so far, i.e. the end of the furthest page received.
         */
        private int maxRows = 0;

        /**
         * Whether more rows may follow the rows received so far; the row count is exact once a page is not truncated.
         */
        private boolean truncated = true;

        /**
         * The last query for the rows, cancelled by the next one.
         */
        private QueryServiceStub.Query runningQuery = null;

        /**
         * Rows of an empty range.
         */
        List<DataGridNode[]> emptyData = new LinkedList<>();


        /**
         * Creates the provider.
         */
        private AsyncResultsProvider()
        {
        }


        /**
         * Sets the query whose results are provided; the columns are rebuilt from its first result.
         *
         * @param query the SPARQL query
         */
        void setQuery(String query)
        {
            queryString = query;
            resetColumns = true;
        }


        /**
         * Cancels the last query for the rows and ignores its response.
         */
        void cancelQuery()
        {
            asyncResultsCounter++;

            if(runningQuery != null)
                QueryServiceStub.cancel(runningQuery);
        }


        @Override
        protected void onRangeChanged(HasData<DataGridNode[]> display)
        {
            if(queryString == null)
                return;

            asyncResultsCounter++;

            final Range range = display.getVisibleRange();
            final int start = range.getStart();
            final int length = range.getLength();


            if(length == 0)
            {
                updateRowData(start, emptyData);
                return;
            }


            handlerManager.fireEvent(new StatusChangeEvent(Status.RUNNING));


            if(runningQuery != null)
                QueryServiceStub.cancel(runningQuery);


            runningQuery = QueryServiceStub.query(queryString, start, length, new AsyncCallback<QueryResult>()
            {
                int request = asyncResultsCounter;

                @Override
                public void onSuccess(final QueryResult result)
                {
                    if(request != asyncResultsCounter)
                        return; // request is not up to date


                    if(resetColumns)
                    {
                        while(resultsDataGrid.getColumnCount() > 0)
                            resultsDataGrid.removeColumn(0);

                        removeUnusedDataGridColumns(resultsDataGrid);


                        for(int i = 0; i < result.getHeads().size(); i++)
                        {
                            addColumn(resultsDataGrid, i, result.getHeads().get(i));
                            searchListBox.addItem("in " + result.getHeads().get(i), Integer.toString(i));
                        }

                        resultsDataGrid.setMinimumTableWidth(minimalColumnWidth * result.getHeads().size(), Unit.PX);

                        resetColumns = false;
                        truncated = true;
                        maxRows = 0;
                    }

                    maxRows = Math.max(maxRows, start + result.getItems().size());
                    truncated = truncated && result.isTruncated();

                    //resultsDataGrid.setRowCount(maxRows, !truncated);
                    hasDataFilter.setRowCount(maxRows, !truncated);

                    updateRowData(start, result.getItems());

                    handlerManager.fireEvent(new StatusChangeEvent(Status.FINISHED));
                }

                @Override
                public void onFailure(Throwable caught)
                {
                    if(request != asyncResultsCounter)
                        return; // request is not up to date

                    clear();
                    handlerManager.fireEvent(new StatusChangeEvent(Status.FINISHED));

                    ErrorMessage.show(ErrorMessage.Context.QUERY, caught);
                }
            });
        }
    }


    /**
     * Minimal width of a column of the data grid in pixels.
     */
    private static final int minimalColumnWidth = 250;

    /**
     * Binder of the tab template.
     */
    private static ResultTablePartUiBinder uiBinder = GWT.create(ResultTablePartUiBinder.class);

    /**
     * Factory of the resources of the pager.
     */
    private static PagerResourcesFactory pagerResourcesFactory = GWT.create(PagerResourcesFactory.class);

    /**
     * Resources of the pager.
     */
    private static Resources pagerIcons = pagerResourcesFactory.create();

    /**
     * Text box with the text the shown rows must contain.
     */
    @UiField
    TextBox searchTextBox;

    /**
     * List of the columns the text is searched in.
     */
    @UiField
    ListBox searchListBox;

    /**
     * Data grid of the results.
     */
    @UiField(provided = true)
    DataGrid<DataGridNode[]> resultsDataGrid;

    /**
     * Pager of the results.
     */
    @UiField(provided = true)
    SimplePager resultsPager;

    /**
     * Manager of the handlers of the status change and visit events.
     */
    private final HandlerManager handlerManager = new HandlerManager(this);

    /**
     * Display filtering the rows of the current page.
     */
    private final HasDataFilter hasDataFilter;

    /**
     * Provider of the rows.
     */
    private final AsyncResultsProvider resultsAsyncProvider = new AsyncResultsProvider();


    /**
     * Creates the tab.
     */
    public ResultTablePart()
    {
        resultsDataGrid = new DataGrid<>(1, DataGridBundle.INSTANCE)
        {
            // https://code.google.com/p/google-web-toolkit/issues/detail?id=6865
            @Override
            public void setRowData(int start, List<? extends DataGridNode[]> values)
            {
                final HeaderPanel headerPanel = (HeaderPanel) getWidget();
                final ScrollPanel scrollPanel = (ScrollPanel) headerPanel.getContentWidget();
                scrollPanel.scrollToTop();
                super.setRowData(start, values);
            }
        };


        resultsPager = new SimplePager(SimplePager.TextLocation.RIGHT, pagerIcons, false, 0, true)
        {
            @Override
            public void setPageStart(int index)
            {
                if(getDisplay() != null)
                {
                    Range range = getDisplay().getVisibleRange();
                    int pageSize = range.getLength();

                    if(isRangeLimited() && getDisplay().isRowCountExact())
                        index = Math.min(index, getDisplay().getRowCount() - 1);

                    index = Math.max(0, index);

                    if(index != range.getStart())
                        getDisplay().setVisibleRange(index, pageSize);
                }
            }
        };


        hasDataFilter = new HasDataFilter(resultsDataGrid);

        initWidget(uiBinder.createAndBindUi(this));

        //resultsDataGrid.setPageSize(0);
        //resultsDataGrid.setRowCount(0, true);
        //hasDataFilter.setPageSize(0);
        hasDataFilter.setRowCount(0, true);
        hasDataFilter.setVisibleRange(0, 0);

        resultsAsyncProvider.addDataDisplay(hasDataFilter);
        resultsPager.setDisplay(hasDataFilter);

        searchTextBox.getElement().setAttribute("spellCheck", "false");
    }


    /**
     * Adds a handler of the events announcing a change of the status of the evaluation.
     *
     * @param handler the handler
     * @return the registration of the handler
     */
    public HandlerRegistration addStatusChangeEventHandler(StatusChangeEventHandler handler)
    {
        return handlerManager.addHandler(StatusChangeEvent.TYPE, handler);
    }


    /**
     * Adds a handler of the events announcing that a resource is to be visited.
     *
     * @param handler the handler
     * @return the registration of the handler
     */
    public HandlerRegistration addVisitItemEventHandler(VisitItemEventHandler handler)
    {
        return handlerManager.addHandler(VisitItemEvent.TYPE, handler);
    }


    /**
     * Filters the rows as the search text is typed.
     *
     * @param e the key up event
     */
    @UiHandler("searchTextBox")
    void complexesTextBoxKeyUp(KeyUpEvent e)
    {
        resetFilter();
    }


    /**
     * Filters the rows when the searched column changes.
     *
     * @param e the change event
     */
    @UiHandler("searchListBox")
    void searchListBoxChangeEvent(ChangeEvent e)
    {
        resetFilter();
    }


    /**
     * Filters the rows of the current page by the search text, which the text of a cell (of the chosen column or of any
     * column) without its HTML tags must contain, ignoring the case.
     */
    private void resetFilter()
    {
        final String value = searchTextBox.getValue().toLowerCase();
        final int index = Integer.parseInt(searchListBox.getSelectedValue());


        hasDataFilter.setFilter(new HasDataFilter.IFilter()
        {
            @Override
            public boolean isValid(DataGridNode[] row)
            {
                if(index != -1)
                    return row[index].html.replaceAll("<[^>]*>", " ").toLowerCase().indexOf(value) >= 0;

                for(int i = 0; i < row.length; i++)
                    if(row[i].html.replaceAll("<[^>]*>", " ").toLowerCase().indexOf(value) >= 0)
                        return true;

                return false;
            }
        });
    }


    /**
     * Runs a query and shows the first page of its results.
     *
     * @param query the SPARQL query
     */
    public void run(String query)
    {
        clear();

        resultsAsyncProvider.setQuery(query);
        //resultsDataGrid.setPageSize(1000);
        //resultsDataGrid.setRowCount(1000);
        //hasDataFilter.setPageSize(1000);
        hasDataFilter.setRowCount(0);
        hasDataFilter.setVisibleRange(0, 200);
    }


    /**
     * Cancels the running query and announces that the evaluation has finished.
     */
    public void cancel()
    {
        resultsAsyncProvider.cancelQuery();
        handlerManager.fireEvent(new StatusChangeEvent(Status.FINISHED));
    }


    /**
     * Removes the columns of the results and resets the search.
     */
    public void clear()
    {
        searchListBox.clear();
        searchListBox.addItem("in any column", "-1");
        searchTextBox.setValue("");
        resetFilter();


        while(resultsDataGrid.getColumnCount() > 0)
            resultsDataGrid.removeColumn(0);

        removeUnusedDataGridColumns(resultsDataGrid);
    }


    /**
     * Adds a column whose cells announce a visit of the resource they show when they are clicked or Enter is pressed on
     * them.
     *
     * @param grid the data grid
     * @param i the index of the column in a row
     * @param name the header of the column
     */
    private void addColumn(DataGrid<DataGridNode[]> grid, final int i, final String name)
    {
        LinkCell cell = new LinkCell()
        {
            @Override
            public void onBrowserEvent(Context context, Element parent, DataGridNode value, NativeEvent event,
                    ValueUpdater<DataGridNode> valueUpdater)
            {
                if(event.getType() == "click"
                        || event.getType() == "keypress" && event.getKeyCode() == KeyCodes.KEY_ENTER)
                {
                    if(value.ref != null)
                        handlerManager.fireEvent(new VisitItemEvent(value.ref));
                }

                super.onBrowserEvent(context, parent, value, event, valueUpdater);
            }
        };

        Column<DataGridNode[], DataGridNode> column = new Column<>(cell)
        {
            @Override
            public DataGridNode getValue(DataGridNode[] object)
            {
                return object[i];
            }

        };

        grid.addColumn(column, name);
    }



    /**
     * Removes the {@code col} elements left behind by the removed columns of a data grid.
     *
     * @param dataGrid the data grid
     */
    private static void removeUnusedDataGridColumns(DataGrid<?> dataGrid)
    {
        int columnCount = dataGrid.getColumnCount();
        NodeList<Element> colGroups = dataGrid.getElement().getElementsByTagName("colgroup");

        for(int i = 0; i < colGroups.getLength(); i++)
        {
            Element colGroupEle = colGroups.getItem(i);
            NodeList<Element> colList = colGroupEle.getElementsByTagName("col");

            for(int j = colList.getLength() - 1; j >= columnCount; j--)
            {
                colGroupEle.removeChild(colList.getItem(j));
            }
        }
    }
}
