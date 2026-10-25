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



/**
 * Modal dialog showing a message in HTML with a caption and an OK button.
 */
public class MessageDialog extends DialogBox
{
    /**
     * Binder of the dialog template.
     */
    private static MessageDialogUiBinder uiBinder = GWT.create(MessageDialogUiBinder.class);

    /**
     * Binder of the dialog template {@code MessageDialog.ui.xml}.
     */
    interface MessageDialogUiBinder extends UiBinder<Widget, MessageDialog>
    {
    }

    /**
     * Panel showing the message.
     */
    @UiField
    HTML html;


    /**
     * Creates the dialog; it is shown by the caller.
     *
     * @param caption the caption of the dialog
     * @param messages the message in HTML
     */
    public MessageDialog(String caption, String messages)
    {
        uiBinder.createAndBindUi(this);

        getCaption().setHTML("<b>" + caption + "</b>");
        html.setHTML(
                "<div lang=\"en\" style=\"-webkit-hyphens: auto; -moz-hyphens: auto; -ms-hyphens: auto; hyphens: auto;\">"
                        + messages + "</div>");
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
     * Closes the dialog when the OK button is clicked.
     *
     * @param e the click event
     */
    @UiHandler("okButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }
}
