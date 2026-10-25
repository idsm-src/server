package cz.iocb.chemweb.client.widgets.dialog;

import java.util.LinkedList;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import cz.iocb.chemweb.client.resources.datagrid.DataGridBundle;
import cz.iocb.chemweb.client.widgets.codemirror.CodeMirror;



/**
 * Dialog offering the example queries of the endpoint (loaded by the page from {@code chemweb/js/examples-data.js})
 * with a preview of the selected one.
 */
public class SelectExampleDialog extends DialogBox
{
    /**
     * Example query as a JavaScript object of the example data.
     */
    public static class Example extends JavaScriptObject
    {
        /**
         * Not instantiable from Java; instances come from the example data.
         */
        protected Example()
        {
        }

        /**
         * Returns the description of the example.
         *
         * @return the description in HTML
         */
        public final native String getDescription()
        /*-{
            return this.description;
        }-*/;

        /**
         * Returns the code of the example.
         *
         * @return the SPARQL query
         */
        public final native String getCode()
        /*-{
            return this.code;
        }-*/;

        /**
         * Returns the code of the example preceded by a comment line with its description in plain text.
         *
         * @return the commented SPARQL query
         */
        public final String getCommentedCode()
        {
            return "# " + getDescription().replaceAll("<[^>]+>", "").replaceAll("[\n ]+", " ").trim() + "\n" + "\n"
                    + getCode();
        }
    }

    /**
     * Binder of the dialog template {@code SelectExampleDialog.ui.xml}.
     */
    interface ExamplesPageUiBinder extends UiBinder<Widget, SelectExampleDialog>
    {
    }

    /**
     * Receiver of the selected example.
     */
    public static abstract class Callback
    {
        /**
         * Creates the receiver.
         */
        protected Callback()
        {
        }


        /**
         * Called with the selected example.
         *
         * @param code the commented code of the example
         */
        abstract public void onSelection(String code);
    }



    /**
     * Binder of the dialog template.
     */
    private static ExamplesPageUiBinder uiBinder = GWT.create(ExamplesPageUiBinder.class);

    /**
     * Grid of the descriptions of the examples.
     */
    @UiField(provided = true)
    DataGrid<Example> examplesDataGrid;

    /**
     * Text area replaced by the read-only editor previewing the selected example.
     */
    @UiField
    TextArea previewTextArea;

    /**
     * Button selecting the example.
     */
    @UiField
    Button selectButton;

    /**
     * Editor previewing the selected example.
     */
    private CodeMirror codemirror;

    /**
     * Receiver of the example selected in the open dialog.
     */
    private Callback callback;

    /**
     * Selection model of the grid of the examples.
     */
    private final SingleSelectionModel<Example> selectionModel;


    /**
     * Creates the dialog and fills it with the examples once the page is loaded.
     */
    public SelectExampleDialog()
    {
        examplesDataGrid = new DataGrid<>(1, DataGridBundle.INSTANCE);

        AbstractCell<String> cell = new AbstractCell<>()
        {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb)
            {
                if(value != null)
                    sb.append(SafeHtmlUtils.fromTrustedString(value));
            }
        };

        Column<Example, String> column = new Column<>(cell)
        {
            @Override
            public String getValue(Example value)
            {
                return value.getDescription();
            }
        };

        examplesDataGrid.addColumn(column, "Example");

        selectionModel = new SingleSelectionModel<>();

        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler()
        {
            @Override
            public void onSelectionChange(SelectionChangeEvent event)
            {
                String code = selectionModel.getSelectedObject().getCode();
                codemirror.setValue(code);
            }
        });

        examplesDataGrid.setSelectionModel(selectionModel);
        examplesDataGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);



        examplesDataGrid.addCellPreviewHandler(new Handler<Example>()
        {
            @Override
            public void onCellPreview(CellPreviewEvent<Example> event)
            {
                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
                {
                    SelectExampleDialog.this.selectButtonClick(null);
                }
                else if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE)
                {
                    SelectExampleDialog.this.cancelButtonClick(null);
                }
            }

        });

        uiBinder.createAndBindUi(this);


        examplesDataGrid.addDomHandler(new DoubleClickHandler()
        {
            @Override
            public void onDoubleClick(final DoubleClickEvent event)
            {
                Example selected = selectionModel.getSelectedObject();
                if(selected != null)
                {
                    selectButtonClick(null);
                }
            }
        }, DoubleClickEvent.getType());


        /*
        examplesDataGrid.addDomHandler(new MouseDownHandler()
        {
            @Override
            public void onMouseDown(MouseDownEvent event)
            {
                event.preventDefault();
            }
        }, MouseDownEvent.getType());
        */


        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
        {
            @Override
            public void execute()
            {
                JsArray<Example> data = getData();

                LinkedList<Example> list = new LinkedList<>();

                for(int i = 0; i < data.length(); ++i)
                    list.add(data.get(i));

                examplesDataGrid.setPageSize(list.size());
                examplesDataGrid.setRowCount(list.size(), true);
                examplesDataGrid.setRowData(0, list);

                codemirror = new CodeMirror(previewTextArea, true);

                if(data.length() > 0)
                {
                    selectionModel.setSelected(data.get(0), true);
                    selectButton.setEnabled(true);
                }
            }
        });
    }


    /**
     * Closes the dialog and passes the selected example to the callback; called also for the Enter key and a double
     * click.
     *
     * @param e the click event, or null
     */
    @UiHandler("selectButton")
    void selectButtonClick(ClickEvent e)
    {
        hide();

        Example select = selectionModel.getSelectedObject();
        callback.onSelection(select.getCommentedCode());
    }


    /**
     * Closes the dialog without a selection; called also for the Escape key.
     *
     * @param e the click event, or null
     */
    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }

    /**
     * Opens the dialog in the centre of the page.
     *
     * @param callback the receiver of the selected example
     */
    public void open(Callback callback)
    {
        this.callback = callback;
        center();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
        {
            @Override
            public void execute()
            {
                String code = selectionModel.getSelectedObject().getCode();
                codemirror.setValue(code);
                codemirror.refresh();
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand()
                {
                    @Override
                    public void execute()
                    {

                        examplesDataGrid.setFocus(true);
                    }
                });
            }
        });
    }

    /**
     * Provides the dialog itself as the root of the template.
     *
     * @return the dialog
     */
    @UiFactory
    DialogBox instance()
    {
        return this;
    }


    /**
     * Returns the example queries defined by the example data.
     *
     * @return the examples
     */
    public static final native JsArray<Example> getData()
    /*-{
        return $wnd.examples;
    }-*/;
}
