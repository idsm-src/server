package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.DOM;
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
import cz.iocb.chemweb.shared.utils.Encode;



public class DetailsPart extends ResizeComposite implements HasSelectionHandlers<String>
{
    interface DetailsPanelUiBinder extends UiBinder<Widget, DetailsPart>
    {
    }


    private static DetailsPanelUiBinder uiBinder = GWT.create(DetailsPanelUiBinder.class);
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);
    private static DetailsPageServiceAsync detailsService = (DetailsPageServiceAsync) GWT
            .create(DetailsPageService.class);

    @UiField(provided = true) Icons res = iconsClientBundleFactory.create();
    @UiField ImageButton prevButton;
    @UiField ImageButton nextButton;
    @UiField ImageButton reloadButton;
    @UiField ImageButton stopButton;
    @UiField TextBox iriTextBox;
    @UiField ScrollPanel pageScrollPanel;
    @UiField HTML pageHTML;

    private final HandlerManager handlerManager = new HandlerManager(this);
    private final VisitingHistory history;
    private String showedDetails;
    private int requestedDetailsCounter = 0;


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
        showedDetails = null;
        showDetails(history.getCurrent());
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

        showDetails(iri);

        if(emitEvent)
            handlerManager.fireEvent(new SelectionEvent<String>(iri)
            {
            });
    }


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

                String elementId = "SEL-NODE_" + Encode.base32m(iri);
                Element element = DOM.getElementById(elementId);

                if(element != null)
                    element.scrollIntoView();

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


    private native void initJS()
    /*-{
        var that = this;

        function handlingMsg(e) {
            that.@cz.iocb.chemweb.client.ui.main.DetailsPart::visit(Ljava/lang/String;)(e.data);
        }

        $wnd.addEventListener("message", handlingMsg, true);
    }-*/;
}
