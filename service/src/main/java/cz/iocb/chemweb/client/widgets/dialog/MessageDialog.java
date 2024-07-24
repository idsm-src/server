package cz.iocb.chemweb.client.widgets.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;



public class MessageDialog extends DialogBox
{
    private static MessageDialogUiBinder uiBinder = GWT.create(MessageDialogUiBinder.class);

    interface MessageDialogUiBinder extends UiBinder<Widget, MessageDialog>
    {
    }

    @UiField HTML html;


    public MessageDialog(String caption, String messages)
    {
        uiBinder.createAndBindUi(this);

        getCaption().setHTML("<b>" + caption + "</b>");
        html.setHTML(
                "<div lang=\"en\" style=\"-webkit-hyphens: auto; -moz-hyphens: auto; -ms-hyphens: auto; hyphens: auto;\">"
                        + messages + "</div>");
    }


    @UiFactory
    DialogBox instance()
    {
        return this;
    }

    @UiHandler("okButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }
}
