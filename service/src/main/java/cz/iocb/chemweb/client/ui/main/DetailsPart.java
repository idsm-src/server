package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import cz.iocb.chemweb.client.resources.icons.Icons;
import cz.iocb.chemweb.client.resources.icons.IconsClientBundleFactory;
import cz.iocb.chemweb.client.widgets.button.ImageButton;
import cz.iocb.chemweb.shared.services.details.DetailsPageService;
import cz.iocb.chemweb.shared.services.details.DetailsPageServiceAsync;



/**
 * Tab with the details of the visited resource, an HTML page rendered by the details service. It has its own navigation
 * in the shared history of the visited resources and fires a selection event with the IRI of each resource it visits.
 */
public class DetailsPart extends ResizeComposite implements HasSelectionHandlers<String>
{
    /**
     * Binder of the tab template {@code DetailsPart.ui.xml}.
     */
    interface DetailsPanelUiBinder extends UiBinder<Widget, DetailsPart>
    {
    }


    /**
     * Binder of the tab template.
     */
    private static DetailsPanelUiBinder uiBinder = GWT.create(DetailsPanelUiBinder.class);

    /**
     * Factory of the icons.
     */
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);

    /**
     * Asynchronous proxy of the details service.
     */
    private static DetailsPageServiceAsync detailsService = (DetailsPageServiceAsync) GWT
            .create(DetailsPageService.class);

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
     * Button reloading the details of the current resource.
     */
    @UiField
    ImageButton reloadButton;

    /**
     * Indicator shown instead of the reload button while the details are loaded.
     */
    @UiField
    ImageButton stopButton;

    /**
     * Text box with the IRI or the prefixed name of the current resource, where another one can be entered.
     */
    @UiField
    TextBox iriTextBox;

    /**
     * Scroll panel of the details.
     */
    @UiField
    ScrollPanel pageScrollPanel;

    /**
     * Panel with the HTML page of the details.
     */
    @UiField
    HTML pageHTML;

    /**
     * Manager of the selection handlers.
     */
    private final HandlerManager handlerManager = new HandlerManager(this);

    /**
     * History of the visited resources shared with the other tabs.
     */
    private final VisitingHistory history;

    /**
     * IRI of the resource whose details are shown, null when none are.
     */
    private String showedDetails;

    /**
     * Number of the requests for the details, which identifies the latest one.
     */
    private int requestedDetailsCounter = 0;


    /**
     * Creates the tab.
     *
     * @param visitingHistory the history of the visited resources shared with the other tabs
     */
    public DetailsPart(VisitingHistory visitingHistory)
    {
        initWidget(uiBinder.createAndBindUi(this));

        history = visitingHistory;
        pageHTML.getElement().setId("pageHTML");
        pageHTML.getElement().setLang("en");
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
     * Loads the details of the current resource again.
     *
     * @param e the click event
     */
    @UiHandler("reloadButton")
    void reloadButtonClick(ClickEvent e)
    {
        showedDetails = null;
        showDetails(history.getCurrent());
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
     * Shows a resource: updates the toolbar and loads the details.
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

        showDetails(iri);

        if(emitEvent)
            handlerManager.fireEvent(new SelectionEvent<>(iri)
            {
            });
    }


    /**
     * Loads the details of a resource unless they are already shown; the response to an outdated request is ignored.
     *
     * @param iri the IRI of the resource
     */
    private void showDetails(final String iri)
    {
        requestedDetailsCounter++;

        if(iri.equals(showedDetails))
        {
            stopButton.setVisible(false);
            reloadButton.setVisible(true);
            return;
        }

        reloadButton.setVisible(false);
        stopButton.setVisible(true);
        //clear();

        detailsService.details(iri, new AsyncCallback<String>()
        {
            int request = requestedDetailsCounter;

            @Override
            public void onSuccess(String result)
            {
                if(request != requestedDetailsCounter)
                    return; // request is not up to date

                pageHTML.setHTML(result);
                pageScrollPanel.scrollToTop();

                showedDetails = iri;
                stopButton.setVisible(false);
                reloadButton.setVisible(true);
            }

            @Override
            public void onFailure(Throwable caught)
            {
                if(request != requestedDetailsCounter)
                    return; // request is not up to date

                clear();

                stopButton.setVisible(false);
                reloadButton.setVisible(true);

                ErrorMessage.show(ErrorMessage.Context.DETAILS, caught);
            }
        });
    }


    /**
     * Removes the shown details.
     */
    public void clear()
    {
        showedDetails = null;
        pageHTML.setHTML("");
    }


    @Override
    protected void onAttach()
    {
        super.onAttach();
        initJS();
    }


    /**
     * Lets the links of the details visit a resource by posting its IRI as a window message.
     */
    private native void initJS()
    /*-{
        var that = this;

        function handlingMsg(e) {
            that.@cz.iocb.chemweb.client.ui.main.DetailsPart::visit(Ljava/lang/String;)(e.data);
        }

        $wnd.addEventListener("message", handlingMsg, true);
    }-*/;
}
