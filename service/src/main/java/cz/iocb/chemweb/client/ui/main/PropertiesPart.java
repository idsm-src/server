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



/**
 * Tab with the properties of the visited resource, i.e. the triples having it as their subject, listed page by page in
 * a data grid. It has its own navigation in the shared history of the visited resources and fires a selection event
 * with the IRI of each resource it visits.
 */
public class PropertiesPart extends ResizeComposite implements HasSelectionHandlers<String>
{
    /**
     * Binder of the tab template {@code PropertiesPart.ui.xml}.
     */
    interface PropertiesPartUiBinder extends UiBinder<Widget, PropertiesPart>
    {
    }


    /**
     * Provider of the rows of the data grid, which queries the properties of the resource on the visible range.
     */
    private class AsyncPropertiesProvider extends AsyncDataProvider<DataGridNode[]>
    {
        /**
         * Number of the queries for the rows, which identifies the latest one.
         */
        private int asyncPropertiesCounter = 0;

        /**
         * IRI of the resource whose properties are provided.
         */
        private String propertiesIri = null;

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
        private AsyncPropertiesProvider()
        {
        }


        /**
         * Sets the resource whose properties are provided.
         *
         * @param iri the IRI of the resource
         */
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


            // a property with several labels is ordered by the preferred one: an English label, a label without a
            // language, a label in another language, and otherwise the greatest one, as the ontology pages choose it
            String query = "SELECT ?Property ?Value WHERE { " + "<" + propertiesIri + "> ?Property ?Value. "
                    + "OPTIONAL { ?Property rdf:type rdf:Property; rdfs:label ?Label. }} GROUP BY ?Property ?Value "
                    + "ORDER BY (count(?Label) = 0) (substr(max(concat(if(langMatches(lang(?Label), \"en\"), \"2\", "
                    + "if(lang(?Label) = \"\", \"1\", \"0\")), str(?Label))), 2))";

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


    /**
     * Minimal width of a column of the data grid in pixels.
     */
    private static final int minimalColumnWidth = 250;

    /**
     * Binder of the tab template.
     */
    private static PropertiesPartUiBinder uiBinder = GWT.create(PropertiesPartUiBinder.class);

    /**
     * Factory of the resources of the pager.
     */
    private static PagerResourcesFactory pagerResourcesFactory = GWT.create(PagerResourcesFactory.class);

    /**
     * Factory of the icons.
     */
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);

    /**
     * Resources of the pager.
     */
    private static Resources pagerIcons = pagerResourcesFactory.create();

    /**
     * Icons of the toolbar.
     */
    @UiField(provided = true)
    Icons res = iconsClientBundleFactory.create();

    /**
     * Button going back to the previous resource.
     */
    @UiField
    ImageButton prevButton;

    /**
     * Button going forward to the next resource.
     */
    @UiField
    ImageButton nextButton;

    /**
     * Button reloading the properties of the current resource.
     */
    @UiField
    ImageButton reloadButton;

    /**
     * Indicator shown instead of the reload button while the properties are loaded.
     */
    @UiField
    ImageButton stopButton;

    /**
     * Text box with the IRI or the prefixed name of the current resource, where another one can be entered.
     */
    @UiField
    TextBox iriTextBox;

    /**
     * Data grid of the properties and their values.
     */
    @UiField(provided = true)
    DataGrid<DataGridNode[]> propertiesDataGrid;

    /**
     * Pager of the data grid.
     */
    @UiField(provided = true)
    SimplePager pager;

    /**
     * Provider of the rows of the data grid.
     */
    private final AsyncPropertiesProvider propertiesAsyncProvider = new AsyncPropertiesProvider();

    /**
     * Manager of the selection handlers.
     */
    private final HandlerManager handlerManager = new HandlerManager(this);

    /**
     * History of the visited resources shared with the other tabs.
     */
    private final VisitingHistory history;

    /**
     * IRI of the resource whose properties are shown, null when none are.
     */
    private String showedProperties;

    /**
     * Number of the requests for the properties, which identifies the latest one.
     */
    private int requestedPropertiesCounter = 0;


    /**
     * Creates the tab.
     *
     * @param visitingHistory the history of the visited resources shared with the other tabs
     */
    public PropertiesPart(VisitingHistory visitingHistory)
    {
        propertiesDataGrid = new DataGrid<>(1, DataGridBundle.INSTANCE)
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


    /**
     * Shows the previous resource of the history.
     *
     * @param e the click event
     */
    @UiHandler("prevButton")
    void prevButtonClick(ClickEvent e)
    {
        show(history.prev(), true);
    }


    /**
     * Shows the next resource of the history.
     *
     * @param e the click event
     */
    @UiHandler("nextButton")
    void nextButtonClick(ClickEvent e)
    {
        show(history.next(), true);
    }


    /**
     * Loads the properties of the current resource again.
     *
     * @param e the click event
     */
    @UiHandler("reloadButton")
    void reloadButtonClick(ClickEvent e)
    {
        showedProperties = null;
        showProperties(history.getCurrent());
    }


    /**
     * Clears the mark of an invalid IRI as the IRI is edited.
     *
     * @param event the key up event
     */
    @UiHandler("iriTextBox")
    void iriTextBoxKeyUp(KeyUpEvent event)
    {
        if(KeyCodes.KEY_ENTER != event.getNativeEvent().getKeyCode())
            iriTextBox.removeStyleDependentName("error");
    }


    /**
     * Visits the resource entered in the text box when Enter is pressed, or marks the value as invalid when it is not a
     * valid IRI.
     *
     * @param event the key press event
     */
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


    /**
     * Visits a resource unless it is the current one.
     *
     * @param iri the IRI of the resource
     */
    public void visit(String iri)
    {
        if(iri.equals(history.getCurrent()))
            return;

        history.visit(iri);
        show(iri, true);
    }


    /**
     * Shows the current resource of the history, which may have been visited by another tab.
     */
    public void show()
    {
        if(history.getCurrent() == null)
            return;

        show(history.getCurrent(), false);
    }


    /**
     * Shows a resource: updates the toolbar and loads the properties.
     *
     * @param iri the IRI of the resource
     * @param emitEvent whether to fire a selection event with the IRI
     */
    private void show(String iri, boolean emitEvent)
    {
        prevButton.setEnabled(history.hasPrev());
        nextButton.setEnabled(history.hasNext());
        reloadButton.setEnabled(true);
        iriTextBox.setValue(IriHelper.prefixedIRI(iri));

        iriTextBox.removeStyleDependentName("error");

        showProperties(iri);

        if(emitEvent)
            handlerManager.fireEvent(new SelectionEvent<>(iri)
            {
            });
    }


    /**
     * Counts the properties of a resource and lets the data grid load its first page, unless the properties are already
     * shown; the response to an outdated request is ignored.
     *
     * @param iri the IRI of the resource
     */
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


    /**
     * Removes the shown properties.
     */
    public void clear()
    {
        showedProperties = null;

        propertiesDataGrid.setPageSize(0);
        propertiesDataGrid.setPageStart(0);
    }


    /**
     * Adds a column whose cells visit the resource they show when they are clicked or Enter is pressed on them.
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
                        visit(value.ref);
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
}
