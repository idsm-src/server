package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import cz.iocb.chemweb.client.resources.icons.Icons;
import cz.iocb.chemweb.client.resources.icons.IconsClientBundleFactory;
import cz.iocb.chemweb.client.services.downloader.FileDownloader;
import cz.iocb.chemweb.client.widgets.button.FileUploadImageButton;
import cz.iocb.chemweb.client.widgets.button.ImageButton;
import cz.iocb.chemweb.client.widgets.codemirror.CodeMirror;
import cz.iocb.chemweb.client.widgets.dialog.SearchQueryWizardDialog;
import cz.iocb.chemweb.client.widgets.dialog.SelectExampleDialog;
import cz.iocb.chemweb.shared.services.check.CheckResult;
import cz.iocb.chemweb.shared.services.check.CheckService;
import cz.iocb.chemweb.shared.services.check.CheckServiceAsync;
import cz.iocb.chemweb.shared.services.check.CheckerWarning;
import cz.iocb.chemweb.shared.utils.Encode;



/**
 * Tab with the SPARQL editor and its toolbar: opening a local file, an example query or the wizard of search queries,
 * saving the query, and running or cancelling it. The editor checks the query on the server as it is edited; running
 * and cancelling are announced by events.
 */
public class QueryPart extends ResizeComposite implements HasHandlers
{
    /**
     * Binder of the tab template {@code QueryPart.ui.xml}.
     */
    interface QueryPartUiBinder extends UiBinder<Widget, QueryPart>
    {
    }


    /**
     * Event announcing that a query is to be run.
     */
    public static class QuerySubmitEvent extends GwtEvent<QuerySubmitEventHandler>
    {
        /**
         * Type of the event.
         */
        public static Type<QuerySubmitEventHandler> TYPE = new Type<>();

        /**
         * The query to run.
         */
        private final String query;

        /**
         * Creates the event.
         *
         * @param query the query to run
         */
        public QuerySubmitEvent(String query)
        {
            this.query = query;
        }

        @Override
        public Type<QuerySubmitEventHandler> getAssociatedType()
        {
            return TYPE;
        }

        @Override
        protected void dispatch(QuerySubmitEventHandler handler)
        {
            handler.onQuerySubmit(this);
        }

        /**
         * Returns the query to run.
         *
         * @return the query
         */
        public String getQuery()
        {
            return query;
        }
    }


    /**
     * Handler of the events announcing that a query is to be run.
     */
    public interface QuerySubmitEventHandler extends EventHandler
    {
        /**
         * Called when a query is to be run.
         *
         * @param event the event
         */
        void onQuerySubmit(QuerySubmitEvent event);
    }


    /**
     * Event announcing that the running query is to be cancelled.
     */
    public static class QueryCancelEvent extends GwtEvent<QueryCancelEventHandler>
    {
        /**
         * Type of the event.
         */
        public static Type<QueryCancelEventHandler> TYPE = new Type<>();

        /**
         * Creates the event.
         */
        public QueryCancelEvent()
        {
        }

        @Override
        public Type<QueryCancelEventHandler> getAssociatedType()
        {
            return TYPE;
        }

        @Override
        protected void dispatch(QueryCancelEventHandler handler)
        {
            handler.onQueryCancel(this);
        }
    }


    /**
     * Handler of the events announcing that the running query is to be cancelled.
     */
    public interface QueryCancelEventHandler extends EventHandler
    {
        /**
         * Called when the running query is to be cancelled.
         *
         * @param event the event
         */
        void onQueryCancel(QueryCancelEvent event);
    }


    /**
     * Binder of the tab template.
     */
    private static QueryPartUiBinder uiBinder = GWT.create(QueryPartUiBinder.class);

    /**
     * Factory of the icons.
     */
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);

    /**
     * Asynchronous proxy of the check service.
     */
    private static CheckServiceAsync checkService = (CheckServiceAsync) GWT.create(CheckService.class);

    /**
     * Downloader saving the query to a file.
     */
    private static FileDownloader fileDownloader = GWT.create(FileDownloader.class);

    /**
     * Icons of the toolbar.
     */
    @UiField(provided = true)
    Icons res = iconsClientBundleFactory.create();

    /**
     * Button opening a local file.
     */
    @UiField
    FileUploadImageButton openButton;

    /**
     * Button opening an example query.
     */
    @UiField
    ImageButton examplesButton;

    /**
     * Button opening the wizard of search queries.
     */
    @UiField
    ImageButton wizardButton;

    /**
     * Button saving the query to a file.
     */
    @UiField
    ImageButton saveButton;

    /**
     * Button running the query, disabled while the query has an error.
     */
    @UiField
    ImageButton runButton;

    /**
     * Button cancelling the running query, shown instead of the run button.
     */
    @UiField
    ImageButton cancelButton;

    /**
     * Text area replaced by the editor.
     */
    @UiField
    TextArea queryTextArea;

    /**
     * The editor, created once the tab is loaded.
     */
    private CodeMirror codemirror;

    /**
     * Manager of the handlers of the query events.
     */
    private final HandlerManager handlerManager = new HandlerManager(this);

    /**
     * Dialog selecting an example query.
     */
    private final SelectExampleDialog exampleDialog;

    /**
     * Wizard of search queries.
     */
    private final SearchQueryWizardDialog wizardDialog;

    /**
     * Query the editor starts with.
     */
    private final String defaultQuery;


    /**
     * Creates the tab.
     *
     * @param query the query the editor starts with
     */
    public QueryPart(String query)
    {
        initWidget(uiBinder.createAndBindUi(this));

        exampleDialog = new SelectExampleDialog();
        wizardDialog = new SearchQueryWizardDialog();
        defaultQuery = query;


        openButton.getUploadFile().addChangeHandler(new ChangeHandler()
        {
            @Override
            public void onChange(ChangeEvent event)
            {
                handleFileSelect(openButton.getUploadFile().getElement());
            }
        });
    }


    /**
     * Adds a handler of the events announcing that a query is to be run.
     *
     * @param handler the handler
     * @return the registration of the handler
     */
    public HandlerRegistration addQuerySubmitEventHandler(QuerySubmitEventHandler handler)
    {
        return handlerManager.addHandler(QuerySubmitEvent.TYPE, handler);
    }


    /**
     * Adds a handler of the events announcing that the running query is to be cancelled.
     *
     * @param handler the handler
     * @return the registration of the handler
     */
    public HandlerRegistration addQueryCancelEventHandler(QueryCancelEventHandler handler)
    {
        return handlerManager.addHandler(QueryCancelEvent.TYPE, handler);
    }


    /**
     * Opens the dialog of the example queries, whose selected query replaces the code of the editor.
     *
     * @param e the click event
     */
    @UiHandler("examplesButton")
    void examplesButtonClick(ClickEvent e)
    {
        exampleDialog.open(new SelectExampleDialog.Callback()
        {
            @Override
            public void onSelection(String code)
            {
                codemirror.setValue(code);
            }
        });
    }


    /**
     * Opens the wizard of search queries, whose generated query replaces the code of the editor.
     *
     * @param e the click event
     */
    @UiHandler("wizardButton")
    void wizardButtonClick(ClickEvent e)
    {
        wizardDialog.open(new SearchQueryWizardDialog.Callback()
        {
            @Override
            public void onSelection(String code)
            {
                codemirror.setValue(code);
            }
        });
    }


    /**
     * Offers the query for download as {@code query.sparql}.
     *
     * @param e the click event
     */
    @UiHandler("saveButton")
    void saveButtonClick(ClickEvent e)
    {
        String data = Encode.base64(codemirror.getValue());
        fileDownloader.download("query.sparql", "application/x-sparql-query", data);
    }


    /**
     * Announces that the query of the editor is to be run.
     *
     * @param e the click event, or null
     */
    @UiHandler("runButton")
    void runButtonClick(ClickEvent e)
    {
        handlerManager.fireEvent(new QuerySubmitEvent(codemirror.getValue()));
    }


    /**
     * Announces that the running query is to be cancelled.
     *
     * @param e the click event
     */
    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        handlerManager.fireEvent(new QueryCancelEvent());
    }


    /**
     * Replaces the code of the editor by the content of an opened file and resets the file input, so that the same file
     * can be opened again.
     *
     * @param code the content of the file
     */
    public void openClick(String code)
    {
        codemirror.setValue(code);
        openButton.getUploadFile().getElement().setPropertyString("value", "");
    }


    /**
     * Redraws the editor, needed after the size of the tab changed.
     */
    public void refresh()
    {
        if(codemirror != null)
            codemirror.refresh();
    }


    /**
     * Shows the cancel button while a query runs and the run button otherwise.
     *
     * @param isRunning whether a query runs
     */
    public void setRunning(boolean isRunning)
    {
        runButton.setVisible(!isRunning);
        cancelButton.setVisible(isRunning);
    }


    /**
     * Creates the editor with the default query and its validator, which checks the query on the server and enables the
     * run button unless an error is found.
     */
    private void init()
    {
        codemirror = new CodeMirror(queryTextArea);
        codemirror.setValue(defaultQuery);


        codemirror.setValidator(new CodeMirror.Validator()
        {
            @Override
            public void validate(final String code, final JavaScriptObject callbackFunction,
                    final JavaScriptObject options, final JavaScriptObject cm)
            {
                if(code.trim().equals(""))
                {
                    JsArray<JavaScriptObject> warnings = JavaScriptObject.createArray().cast();
                    CodeMirror.callback(callbackFunction, warnings, cm);

                    runButton.setEnabled(false);
                    return;
                }

                checkService.check(code, new AsyncCallback<CheckResult>()
                {
                    @Override
                    public void onSuccess(CheckResult result)
                    {
                        if(!code.equals(codemirror.getValue()))
                            return;

                        JsArray<JavaScriptObject> warnings = JavaScriptObject.createArray().cast();

                        boolean error = false;

                        for(CheckerWarning warn : result.warnings)
                        {
                            error = error || warn.type.equals("error");

                            JavaScriptObject w = CodeMirror.createWarning(warn.beginLine, warn.beginColumnt,
                                    warn.endLine, warn.endColumnt, warn.type, warn.message);
                            warnings.push(w);
                        }

                        runButton.setEnabled(!error);
                        CodeMirror.callback(callbackFunction, warnings, cm);
                    }

                    @Override
                    public void onFailure(Throwable caught)
                    {
                        if(!code.equals(codemirror.getValue()))
                            return;

                        JsArray<JavaScriptObject> warnings = JavaScriptObject.createArray().cast();

                        warnings.push(CodeMirror.createWarning(0, 0, 0, 0, "warning",
                                "The connection with the server has failed."));

                        runButton.setEnabled(true);
                        CodeMirror.callback(callbackFunction, warnings, cm);
                    }
                });
            }
        });
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
                init();
            }
        });
    }


    /**
     * Reads the file selected in the file input and passes its content to {@link #openClick(String)}.
     *
     * @param evt the file input element
     */
    private native void handleFileSelect(@SuppressWarnings("deprecation") com.google.gwt.user.client.Element evt)
    /*-{
        var file = evt.files[0];

        //if (!file.type.match('text.*'))
        //  return;

        if (!$wnd.File || !$wnd.FileReader || !$wnd.FileList || !$wnd.Blob)
            $wnd.alert("The File APIs are not fully supported by your browser.");

        var reader = new $wnd.FileReader();
        var that = this;

        reader.onload = (function(theFile) {
            return function(e) {
                that.@cz.iocb.chemweb.client.ui.main.QueryPart::openClick(Ljava/lang/String;)(e.target.result);
            };
        })(file);

        reader.readAsText(file);
    }-*/;
}
