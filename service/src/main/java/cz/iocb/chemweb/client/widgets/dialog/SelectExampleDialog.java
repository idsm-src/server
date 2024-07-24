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



public class SelectExampleDialog extends DialogBox
{
    public static class Example extends JavaScriptObject
    {
        protected Example()
        {
        }

        public final native String getDescription()
        /*-{
            return this.description;
        }-*/;

        public final native String getCode()
        /*-{
            return this.code;
        }-*/;

        public final String getCommentedCode()
        {
            return "# " + getDescription().replaceAll("<[^>]+>", "").replaceAll("[\n ]+", " ") + "\n" + "\n"
                    + getCode();
        }
    }

    interface ExamplesPageUiBinder extends UiBinder<Widget, SelectExampleDialog>
    {
    }

    public static abstract class Callback
    {
        abstract public void onSelection(String code);
    }



    private static ExamplesPageUiBinder uiBinder = GWT.create(ExamplesPageUiBinder.class);

    @UiField(provided = true) DataGrid<Example> examplesDataGrid;
    @UiField TextArea previewTextArea;
    @UiField Button selectButton;

    private CodeMirror codemirror;
    private Callback callback;
    private final SingleSelectionModel<Example> selectionModel;


    public SelectExampleDialog()
    {
        examplesDataGrid = new DataGrid<Example>(1, DataGridBundle.INSTANCE);

        AbstractCell<String> cell = new AbstractCell<String>()
        {
            @Override
            public void render(Context context, String value, SafeHtmlBuilder sb)
            {
                if(value != null)
                    sb.append(SafeHtmlUtils.fromTrustedString(value));
            }
        };

        Column<Example, String> column = new Column<Example, String>(cell)
        {
            @Override
            public String getValue(Example value)
            {
                return value.getDescription();
            }
        };

        examplesDataGrid.addColumn(column, "Example");

        selectionModel = new SingleSelectionModel<Example>();

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

                LinkedList<Example> list = new LinkedList<Example>();

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


    @UiHandler("selectButton")
    void selectButtonClick(ClickEvent e)
    {
        hide();

        Example select = selectionModel.getSelectedObject();
        callback.onSelection(select.getCommentedCode());
    }


    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }

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

    @UiFactory
    DialogBox instance()
    {
        return this;
    }


    public static final native JsArray<Example> getData()
    /*-{
        return $wnd.examples;
    }-*/;
}
