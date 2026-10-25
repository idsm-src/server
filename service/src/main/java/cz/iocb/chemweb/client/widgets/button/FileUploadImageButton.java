package cz.iocb.chemweb.client.widgets.button;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;



/**
 * Image button that opens the file chooser of the browser: a file input is laid over the button.
 */
public class FileUploadImageButton extends ImageButton
{
    /**
     * File input laid over the button.
     */
    private final FileUpload upload;


    /**
     * Creates the button with an image.
     *
     * @param src the URI of the image
     * @param position the CSS background position of the image
     */
    @UiConstructor
    public FileUploadImageButton(SafeUri src, String position)
    {
        FlowPanel panel = new FlowPanel();
        FormPanel form = new FormPanel();
        upload = new FileUpload();

        panel.addStyleName(resources.css().fileUploadImageButtonInner());

        form.add(upload);
        panel.add(label);
        panel.add(form);

        add(panel);

        Style style = getElement().getStyle();
        style.setProperty("position", "relative");
        style.setProperty("backgroundImage", "url('" + src.asString() + "')");
        style.setProperty("backgroundPosition", position);
    }


    /**
     * Returns the file input of the button.
     *
     * @return the file input
     */
    public FileUpload getUploadFile()
    {
        return upload;
    }
}
