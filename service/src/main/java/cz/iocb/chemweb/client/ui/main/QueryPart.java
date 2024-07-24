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



public class QueryPart extends ResizeComposite implements HasHandlers
{
    interface QueryPartUiBinder extends UiBinder<Widget, QueryPart>
    {
    }


    public static class QuerySubmitEvent extends GwtEvent<QuerySubmitEventHandler>
    {
        public static Type<QuerySubmitEventHandler> TYPE = new Type<QuerySubmitEventHandler>();

        private final String query;

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

        public String getQuery()
        {
            return query;
        }
    }


    public interface QuerySubmitEventHandler extends EventHandler
    {
        void onQuerySubmit(QuerySubmitEvent event);
    }


    public static class QueryCancelEvent extends GwtEvent<QueryCancelEventHandler>
    {
        public static Type<QueryCancelEventHandler> TYPE = new Type<QueryCancelEventHandler>();

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


    public interface QueryCancelEventHandler extends EventHandler
    {
        void onQueryCancel(QueryCancelEvent event);
    }


    private static QueryPartUiBinder uiBinder = GWT.create(QueryPartUiBinder.class);
    private static IconsClientBundleFactory iconsClientBundleFactory = GWT.create(IconsClientBundleFactory.class);
    private static CheckServiceAsync checkService = (CheckServiceAsync) GWT.create(CheckService.class);
    private static FileDownloader fileDownloader = GWT.create(FileDownloader.class);

    @UiField(provided = true) Icons res = iconsClientBundleFactory.create();
    @UiField FileUploadImageButton openButton;
    @UiField ImageButton examplesButton;
    @UiField ImageButton wizardButton;
    @UiField ImageButton saveButton;
    @UiField ImageButton runButton;
    @UiField ImageButton cancelButton;
    @UiField TextArea queryTextArea;

    private CodeMirror codemirror;
    private final HandlerManager handlerManager = new HandlerManager(this);
    private final SelectExampleDialog exampleDialog;
    private final SearchQueryWizardDialog wizardDialog;
    private final String defaultQuery;


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


    public HandlerRegistration addQuerySubmitEventHandler(QuerySubmitEventHandler handler)
    {
        return handlerManager.addHandler(QuerySubmitEvent.TYPE, handler);
    }


    public HandlerRegistration addQueryCancelEventHandler(QueryCancelEventHandler handler)
    {
        return handlerManager.addHandler(QueryCancelEvent.TYPE, handler);
    }


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


    @UiHandler("saveButton")
    void saveButtonClick(ClickEvent e)
    {
        String data = Encode.base64(codemirror.getValue());
        fileDownloader.download("query.sparql", "application/x-sparql-query", data);
    }


    @UiHandler("runButton")
    void runButtonClick(ClickEvent e)
    {
        handlerManager.fireEvent(new QuerySubmitEvent(codemirror.getValue()));
    }


    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        handlerManager.fireEvent(new QueryCancelEvent());
    }


    public void openClick(String code)
    {
        codemirror.setValue(code);
        openButton.getUploadFile().getElement().setPropertyString("value", "");
    }


    public void refresh()
    {
        if(codemirror != null)
            codemirror.refresh();
    }


    public void setRunning(boolean isRunning)
    {
        runButton.setVisible(!isRunning);
        cancelButton.setVisible(isRunning);
    }


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
