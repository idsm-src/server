package cz.iocb.chemweb.client.ui.main;

import java.util.LinkedList;
import java.util.List;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import cz.iocb.chemweb.client.resources.datagrid.DataGridBundle;
import cz.iocb.chemweb.client.resources.icons.Icons;
import cz.iocb.chemweb.client.resources.icons.IconsClientBundleFactory;
import cz.iocb.chemweb.client.resources.pager.PagerResourcesFactory;
import cz.iocb.chemweb.client.services.query.QueryServiceStub;
import cz.iocb.chemweb.client.widgets.button.ImageButton;
import cz.iocb.chemweb.shared.services.query.DataGridNode;
import cz.iocb.chemweb.shared.services.query.QueryResult;



public class PropertiesPart extends ResizeComposite implements HasSelectionHandlers<String>
{
    interface PropertiesPartUiBinder extends UiBinder<Widget, PropertiesPart>
    {
    }


    private class AsyncPropertiesProvider extends AsyncDataProvider<DataGridNode[]>
    {
        private int asyncPropertiesCounter = 0;
        private String propertiesIri = null;
        private QueryServiceStub.Query runningQuery = null;
        List<DataGridNode[]> emptyData = new LinkedList<DataGridNode[]>();


        void setPropertiesIri(String iri)
        {
            propertiesIri = iri;
        }


        @Override
        protected void onRangeChanged(HasData<DataGridNode[]> display)
        {
            if(propertiesIri == null)
                return;

            asyncPropertiesCounter++;

            final Range range = display.getVisibleRange();
            final int start = range.getStart();
            final int length = range.getLength();


            if(length == 0)
            {
                updateRowData(start, emptyData);
                return;
            }


            stopButton.setVisible(true);
            reloadButton.setVisible(false);


            if(runningQuery != null)
                QueryServiceStub.cancel(runningQuery);


            String query = "SELECT ?Property ?Value WHERE { " + "<" + propertiesIri + "> ?Property ?Value. "
                    + "OPTIONAL { ?Property rdf:type rdf:Property; rdfs:label ?Label. }} "
                    + "ORDER BY (! bound(?Label)) str(?Label)";

            runningQuery = QueryServiceStub.query(query, start, length, new AsyncCallback<QueryResult>()
            {
                int request = asyncPropertiesCounter;

                @Override
                public void onSuccess(final QueryResult result)
                {
                    if(request != asyncPropertiesCounter)
                        return; // request is not up to date

                    updateRowData(start, result.getItems());

                    stopButton.setVisible(false);
                    reloadButton.setVisible(true);
                }

                @Override
                public void onFailure(Throwable caught)
                {
                    if(request != asyncPropertiesCounter)
                        return; // request is not up to date

                    //clear();
                    stopButton.setVisible(false);
                    reloadButton.setVisible(true);

                    ErrorMessage.show(ErrorMessage.Context.PROPERTIES, caught);
                }
            });
        }
    }


    private static final int minimalColumnWidth = 250;

    private static PropertiesPartUiBinder uiBinder = GWT.create(PropertiesPartUiBinder.class);
    private static PagerResourcesFactory pagerResourcesFactory = GWT.create(PagerResourcesFactory.class);
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);
    private static Resources pagerIcons = pagerResourcesFactory.create();

    @UiField(provided = true) Icons res = iconsClientBundleFactory.create();
    @UiField ImageButton prevButton;
    @UiField ImageButton nextButton;
    @UiField ImageButton reloadButton;
    @UiField ImageButton stopButton;
    @UiField TextBox iriTextBox;
    @UiField(provided = true) DataGrid<DataGridNode[]> propertiesDataGrid;
    @UiField(provided = true) SimplePager pager;

    private final AsyncPropertiesProvider propertiesAsyncProvider = new AsyncPropertiesProvider();
    private final HandlerManager handlerManager = new HandlerManager(this);
    private final VisitingHistory history;
    private String showedProperties;
    private int requestedPropertiesCounter = 0;


    public PropertiesPart(VisitingHistory visitingHistory)
    {
        propertiesDataGrid = new DataGrid<DataGridNode[]>(1, DataGridBundle.INSTANCE)
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

        propertiesDataGrid.setMinimumTableWidth(2 * minimalColumnWidth, Unit.PX);

        addColumn(propertiesDataGrid, 0, "Property");
        addColumn(propertiesDataGrid, 1, "Value");


        pager = new SimplePager(SimplePager.TextLocation.RIGHT, pagerIcons, false, 0, true)
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

        initWidget(uiBinder.createAndBindUi(this));

        history = visitingHistory;

        propertiesDataGrid.setPageSize(0);
        propertiesDataGrid.setRowCount(0, true);
        propertiesAsyncProvider.addDataDisplay(propertiesDataGrid);
        pager.setDisplay(propertiesDataGrid);

        iriTextBox.getElement().setAttribute("spellCheck", "false");
    }


    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler)
    {
        return handlerManager.addHandler(SelectionEvent.getType(), handler);
    }


    @UiHandler("prevButton")
    void prevButtonClick(ClickEvent e)
    {
        show(history.prev(), true);
    }


    @UiHandler("nextButton")
    void nextButtonClick(ClickEvent e)
    {
        show(history.next(), true);
    }


    @UiHandler("reloadButton")
    void reloadButtonClick(ClickEvent e)
    {
        showedProperties = null;
        showProperties(history.getCurrent());
    }


    @UiHandler("iriTextBox")
    void iriTextBoxKeyUp(KeyUpEvent event)
    {
        if(KeyCodes.KEY_ENTER != event.getNativeEvent().getKeyCode())
            iriTextBox.removeStyleDependentName("error");
    }


    @UiHandler("iriTextBox")
    void iriTextBoxKeyPress(KeyPressEvent event)
    {
        if(KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode())
        {
            String value = ((TextBox) event.getSource()).getValue();
            value = IriHelper.deprefixedIRI(value);

            if(IriHelper.isValid(value))
            {
                iriTextBox.removeStyleDependentName("error");
                visit(value);
            }
            else
            {
                iriTextBox.addStyleDependentName("error");
            }
        }
    }


    public void visit(String iri)
    {
        if(iri.equals(history.getCurrent()))
            return;

        history.visit(iri);
        show(iri, true);
    }


    public void show()
    {
        if(history.getCurrent() == null)
            return;

        show(history.getCurrent(), false);
    }


    private void show(String iri, boolean emitEvent)
    {
        prevButton.setEnabled(history.hasPrev());
        nextButton.setEnabled(history.hasNext());
        reloadButton.setEnabled(true);
        iriTextBox.setValue(IriHelper.prefixedIRI(iri));

        iriTextBox.removeStyleDependentName("error");

        showProperties(iri);

        if(emitEvent)
            handlerManager.fireEvent(new SelectionEvent<String>(iri)
            {
            });
    }


    private void showProperties(final String iri)
    {
        propertiesDataGrid.redraw(); // DataGrig in TabLayoutPanel workaround

        requestedPropertiesCounter++;

        if(iri.equals(showedProperties))
        {
            stopButton.setVisible(false);
            reloadButton.setVisible(true);
            return;
        }

        reloadButton.setVisible(false);
        stopButton.setVisible(true);
        clear();


        QueryServiceStub.countOfProperties(iri, new AsyncCallback<Integer>()
        {
            int request = requestedPropertiesCounter;

            @Override
            public void onSuccess(Integer result)
            {
                if(request != requestedPropertiesCounter)
                    return; // request is not up to date

                stopButton.setVisible(false);
                reloadButton.setVisible(true);

                propertiesAsyncProvider.setPropertiesIri(iri);
                propertiesDataGrid.setRowCount(result, true);
                propertiesDataGrid.setPageSize(0);
                propertiesDataGrid.setPageStart(0);
                propertiesDataGrid.setPageSize(100);

                showedProperties = iri;
            }

            @Override
            public void onFailure(Throwable caught)
            {
                if(request != requestedPropertiesCounter)
                    return; // request is not up to date

                clear();

                stopButton.setVisible(false);
                reloadButton.setVisible(true);

                ErrorMessage.show(ErrorMessage.Context.PROPERTIES, caught);
            }
        });
    }


    public void clear()
    {
        showedProperties = null;

        propertiesDataGrid.setPageSize(0);
        propertiesDataGrid.setPageStart(0);
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
                        visit(value.ref);
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
}
