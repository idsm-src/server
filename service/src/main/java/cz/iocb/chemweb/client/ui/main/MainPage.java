package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.iocb.chemweb.client.ui.main.QueryPart.QueryCancelEvent;
import cz.iocb.chemweb.client.ui.main.QueryPart.QueryCancelEventHandler;
import cz.iocb.chemweb.client.ui.main.QueryPart.QuerySubmitEvent;
import cz.iocb.chemweb.client.ui.main.QueryPart.QuerySubmitEventHandler;
import cz.iocb.chemweb.client.ui.main.ResultTablePart.Status;
import cz.iocb.chemweb.client.ui.main.ResultTablePart.StatusChangeEvent;
import cz.iocb.chemweb.client.ui.main.ResultTablePart.StatusChangeEventHandler;
import cz.iocb.chemweb.client.ui.main.ResultTablePart.VisitItemEvent;
import cz.iocb.chemweb.client.ui.main.ResultTablePart.VisitItemEventHandler;
import cz.iocb.chemweb.client.widgets.dialog.SelectExampleDialog;
import cz.iocb.chemweb.shared.utils.Encode;



/**
 * Main page of the client: the query editor, the result table and the details and properties of the visited resource,
 * side by side in a split layout. It fires a selection event with the IRI of each visited resource and highlights the
 * occurrences of the selected resource.
 */
public class MainPage extends ResizeComposite implements HasSelectionHandlers<String>
{
    /**
     * Binder of the page template {@code MainPage.ui.xml}.
     */
    interface MainPageUiBinder extends UiBinder<Widget, MainPage>
    {
    }


    /**
     * Binder of the page template.
     */
    private static MainPageUiBinder uiBinder = GWT.create(MainPageUiBinder.class);

    /**
     * Tab panel with the query editor.
     */
    @UiField
    TabLayoutPanel queryTabPanel;

    /**
     * Split layout of the query, result and information panels.
     */
    @UiField(provided = true)
    SplitLayoutPanel mainSplitLayoutPanel;

    /**
     * Tab panel with the result table.
     */
    @UiField
    TabLayoutPanel resultTabPanel;

    /**
     * Tab panel with the details and the properties of the visited resource.
     */
    @UiField
    TabLayoutPanel infoTabPanel;

    /**
     * Query editor.
     */
    @UiField(provided = true)
    QueryPart queryPart;

    /**
     * Result table.
     */
    @UiField
    ResultTablePart resultTablePart;

    /**
     * Details of the visited resource.
     */
    @UiField(provided = true)
    DetailsPart detailsPart;

    /**
     * Properties of the visited resource.
     */
    @UiField(provided = true)
    PropertiesPart propertiesPart;

    /**
     * Minimal width of the result panel in pixels.
     */
    private final int minCentralWidth = 300;

    /**
     * Style element highlighting the occurrences of the selected resource.
     */
    private final Element styleOfSelected;

    /**
     * Manager of the selection handlers.
     */
    private final HandlerManager handlerManager = new HandlerManager(this);

    /**
     * History of the visited resources shared by the details and the properties.
     */
    private final VisitingHistory history;


    /**
     * Creates the page; the query editor starts with the query of the {@code query} parameter of the page, or with the
     * first example query.
     */
    public MainPage()
    {
        String query = Location.getParameter("query");

        queryPart = new QueryPart(query != null ? query : getDefultQuery());

        history = new VisitingHistory();
        detailsPart = new DetailsPart(history);
        propertiesPart = new PropertiesPart(history);


        mainSplitLayoutPanel = new SplitLayoutPanel()
        {
            @Override
            public void onResize()
            {
                super.onResize();

                queryPart.refresh();
            }
        };


        initWidget(uiBinder.createAndBindUi(this));


        Window.addResizeHandler(new ResizeHandler()
        {
            @Override
            public void onResize(ResizeEvent event)
            {
                resetSplitLayoutSize();
            }
        });


        infoTabPanel.addSelectionHandler(new SelectionHandler<Integer>()
        {
            @Override
            public void onSelection(final SelectionEvent<Integer> event)
            {
                if(infoTabPanel.getWidget(infoTabPanel.getSelectedIndex()) == detailsPart)
                    detailsPart.show();
                else if(infoTabPanel.getWidget(infoTabPanel.getSelectedIndex()) == propertiesPart)
                    propertiesPart.show();
            }
        });


        queryPart.addQuerySubmitEventHandler(new QuerySubmitEventHandler()
        {
            @Override
            public void onQuerySubmit(QuerySubmitEvent event)
            {
                resultTablePart.run(event.getQuery());
            }
        });


        queryPart.addQueryCancelEventHandler(new QueryCancelEventHandler()
        {
            @Override
            public void onQueryCancel(QueryCancelEvent event)
            {
                resultTablePart.cancel();
            }
        });


        resultTablePart.addStatusChangeEventHandler(new StatusChangeEventHandler()
        {
            @Override
            public void onStatusChange(StatusChangeEvent event)
            {
                queryPart.setRunning(event.getStatus() == Status.RUNNING);
            }
        });


        resultTablePart.addVisitItemEventHandler(new VisitItemEventHandler()
        {
            @Override
            public void onVisitItem(VisitItemEvent event)
            {
                visitIri(event.getIri());
            }
        });


        SelectionHandler<String> selectionHandler = new SelectionHandler<>()
        {
            @Override
            public void onSelection(SelectionEvent<String> event)
            {
                styleOfSelected.setInnerText(".BOX-NODE_" + Encode.base32m(event.getSelectedItem())
                        + " { border-left: 2px solid #aaa !important; }");
            }
        };

        this.addSelectionHandler(selectionHandler);
        detailsPart.addSelectionHandler(selectionHandler);
        propertiesPart.addSelectionHandler(selectionHandler);


        styleOfSelected = DOM.createElement("style");
        Document.get().getHead().appendChild(styleOfSelected);

        Element styleOfNonSelected = DOM.createElement("style");
        styleOfNonSelected.setInnerText(".BOX-selection { border-left: 2px solid transparent;}");
        Document.get().getHead().appendChild(styleOfNonSelected);

        mainSplitLayoutPanel.setWidgetMinSize(resultTabPanel, minCentralWidth);
    }


    @Override
    public void onLoad()
    {
        super.onLoad();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
        {
            @Override
            public void execute()
            {
                resetSplitLayoutSize();
                detailsPart.visit(getStartIri());

                String query = Location.getParameter("query");

                if(query != null && !query.isEmpty())
                    queryPart.runButtonClick(null);
            }
        });
    }


    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<String> handler)
    {
        return handlerManager.addHandler(SelectionEvent.getType(), handler);
    }


    /**
     * Visits a resource: records it in the history, announces it to the selection handlers and shows it in the open
     * information tab.
     *
     * @param iri the IRI of the resource
     */
    private void visitIri(String iri)
    {
        if(iri.equals(history.getCurrent()))
            return;

        history.visit(iri);

        handlerManager.fireEvent(new SelectionEvent<>(iri)
        {
        });

        if(infoTabPanel.getWidget(infoTabPanel.getSelectedIndex()) == detailsPart)
            detailsPart.show();
        else if(infoTabPanel.getWidget(infoTabPanel.getSelectedIndex()) == propertiesPart)
            propertiesPart.show();
    }


    /**
     * Shrinks the query and the information panels, the wider one more, when the result panel would be narrower than
     * its minimal width.
     */
    private void resetSplitLayoutSize()
    {
        int border = 2;
        int margin = 20;

        int queryPanelWidth = Math.max(queryTabPanel.getOffsetWidth() - border, 0);
        int infoPanelWidth = Math.max(infoTabPanel.getOffsetWidth() - border, 0);

        int mainPanelWidth = mainSplitLayoutPanel.getOffsetWidth() - border - margin;
        int resultPanelWidth = mainPanelWidth - queryPanelWidth - infoPanelWidth;

        if(resultPanelWidth < minCentralWidth)
        {
            int diff = minCentralWidth - resultPanelWidth;

            // TODO: Make it better ...
            if(queryPanelWidth == 0)
            {
                mainSplitLayoutPanel.setWidgetSize(infoTabPanel, infoPanelWidth - diff);
            }
            else if(infoPanelWidth == 0)
            {
                mainSplitLayoutPanel.setWidgetSize(queryTabPanel, queryPanelWidth - diff);
            }
            else if(queryPanelWidth > infoPanelWidth)
            {
                double r = (double) queryPanelWidth / (queryPanelWidth + infoPanelWidth);
                int w1 = (int) Math.ceil(r * diff); // we take more from wider
                int w2 = diff - w1;

                mainSplitLayoutPanel.setWidgetSize(queryTabPanel, queryPanelWidth - w1);
                mainSplitLayoutPanel.setWidgetSize(infoTabPanel, infoPanelWidth - w2);
            }
            else
            {
                double r = (double) infoPanelWidth / (queryPanelWidth + infoPanelWidth);
                int w2 = (int) Math.ceil(r * diff); // we take more from wider
                int w1 = diff - w2;

                mainSplitLayoutPanel.setWidgetSize(queryTabPanel, queryPanelWidth - w1);
                mainSplitLayoutPanel.setWidgetSize(infoTabPanel, infoPanelWidth - w2);
            }
        }
    }


    /**
     * Returns the resource shown when the page is loaded, defined by {@code chemweb/js/config.js}.
     *
     * @return the IRI of the start resource
     */
    public final native static String getStartIri()
    /*-{
        return $wnd.startIri;
    }-*/;


    /**
     * Returns the query shown when the page is loaded without a query.
     *
     * @return the first example query with its description
     */
    public final static String getDefultQuery()
    {
        return SelectExampleDialog.getData().get(0).getCommentedCode();
    }
}
