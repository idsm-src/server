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



public class ResultTablePart extends ResizeComposite
{
    interface ResultTablePartUiBinder extends UiBinder<Widget, ResultTablePart>
    {
    }


    public static enum Status
    {
        RUNNING, FINISHED
    }


    public static class StatusChangeEvent extends GwtEvent<StatusChangeEventHandler>
    {
        public static Type<StatusChangeEventHandler> TYPE = new Type<StatusChangeEventHandler>();

        private final Status status;

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

        public Status getStatus()
        {
            return status;
        }
    }


    public interface StatusChangeEventHandler extends EventHandler
    {
        void onStatusChange(StatusChangeEvent event);
    }


    public static class VisitItemEvent extends GwtEvent<VisitItemEventHandler>
    {
        public static Type<VisitItemEventHandler> TYPE = new Type<VisitItemEventHandler>();

        private final String iri;

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

        public String getIri()
        {
            return iri;
        }
    }


    public interface VisitItemEventHandler extends EventHandler
    {
        void onVisitItem(VisitItemEvent event);
    }


    private static class HasDataFilter implements HasData<DataGridNode[]>
    {
        private static interface IFilter
        {
            boolean isValid(DataGridNode[] value);
        }


        DataGrid<DataGridNode[]> dataGrid;
        IFilter filter = null;
        List<? extends DataGridNode[]> values = new ArrayList<DataGridNode[]>();

        private final HandlerManager handlerManager = new HandlerManager(this);
        private boolean exact = false;
        private Range range = new Range(0, 0);
        private int count = -1;


        public HasDataFilter(DataGrid<DataGridNode[]> dataGrid)
        {
            this.dataGrid = dataGrid;
        }


        @SuppressWarnings("unchecked")
        private void show()
        {
            List<DataGridNode[]> results = null;

            if(filter != null)
            {
                results = new ArrayList<DataGridNode[]>(values.size());

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


    private class AsyncResultsProvider extends AsyncDataProvider<DataGridNode[]>
    {
        private int asyncResultsCounter = 0;
        private String queryString = null;
        private boolean resetColumns = false;
        private int maxRows = 0;
        private boolean truncated = true;
        private QueryServiceStub.Query runningQuery = null;
        List<DataGridNode[]> emptyData = new LinkedList<DataGridNode[]>();


        void setQuery(String query)
        {
            queryString = query;
            resetColumns = true;
        }


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


    private static final int minimalColumnWidth = 250;

    private static ResultTablePartUiBinder uiBinder = GWT.create(ResultTablePartUiBinder.class);
    private static PagerResourcesFactory pagerResourcesFactory = GWT.create(PagerResourcesFactory.class);
    private static Resources pagerIcons = pagerResourcesFactory.create();

    @UiField TextBox searchTextBox;
    @UiField ListBox searchListBox;
    @UiField(provided = true) DataGrid<DataGridNode[]> resultsDataGrid;
    @UiField(provided = true) SimplePager resultsPager;

    private final HandlerManager handlerManager = new HandlerManager(this);
    private final HasDataFilter hasDataFilter;
    private final AsyncResultsProvider resultsAsyncProvider = new AsyncResultsProvider();


    public ResultTablePart()
    {
        resultsDataGrid = new DataGrid<DataGridNode[]>(1, DataGridBundle.INSTANCE)
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


    public HandlerRegistration addStatusChangeEventHandler(StatusChangeEventHandler handler)
    {
        return handlerManager.addHandler(StatusChangeEvent.TYPE, handler);
    }


    public HandlerRegistration addVisitItemEventHandler(VisitItemEventHandler handler)
    {
        return handlerManager.addHandler(VisitItemEvent.TYPE, handler);
    }


    @UiHandler("searchTextBox")
    void complexesTextBoxKeyUp(KeyUpEvent e)
    {
        resetFilter();
    }


    @UiHandler("searchListBox")
    void searchListBoxChangeEvent(ChangeEvent e)
    {
        resetFilter();
    }


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


    public void cancel()
    {
        resultsAsyncProvider.cancelQuery();
        handlerManager.fireEvent(new StatusChangeEvent(Status.FINISHED));
    }


    public void clear()
    {
        //List<DataGridNode[]> empty = new Vector<DataGridNode[]>();
        //resultsDataGrid.setPageSize(0);
        //resultsDataGrid.setRowCount(0, true);
        //resultsDataGrid.setRowData(0, empty);
        //dataProvider.getList().clear();

        searchListBox.clear();
        searchListBox.addItem("in any column", "-1");
        searchTextBox.setValue("");
        resetFilter();


        while(resultsDataGrid.getColumnCount() > 0)
            resultsDataGrid.removeColumn(0);

        removeUnusedDataGridColumns(resultsDataGrid);
    }


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

        Column<DataGridNode[], DataGridNode> column = new Column<DataGridNode[], DataGridNode>(cell)
        {
            @Override
            public DataGridNode getValue(DataGridNode[] object)
            {
                return object[i];
            }

        };

        grid.addColumn(column, name);
    }



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
