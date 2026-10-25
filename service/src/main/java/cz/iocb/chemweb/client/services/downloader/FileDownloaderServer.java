package cz.iocb.chemweb.client.services.downloader;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;



/**
 * Downloader that posts the data to the download servlet of the server, which sends it back as an attachment; used by
 * the browsers that cannot download a {@code data:} URL.
 */
public class FileDownloaderServer extends FileDownloader
{
    /**
     * Hidden field naming the action of the servlet.
     */
    private final Hidden callField;

    /**
     * Hidden field with the encoding of the data.
     */
    private final Hidden encodingField;

    /**
     * Hidden field with the name of the file.
     */
    private final Hidden filenameField;

    /**
     * Hidden field with the MIME type of the file.
     */
    private final Hidden mimetypeField;

    /**
     * Hidden field with the content of the file.
     */
    private final Hidden dataField;

    /**
     * Form posting the fields to the download servlet.
     */
    private final FormPanel form;


    /**
     * Creates the downloader with its form.
     */
    private FileDownloaderServer()
    {
        form = new FormPanel("_blank");
        form.setAction("download/helper/query.sparql");
        form.setMethod(FormPanel.METHOD_POST);

        VerticalPanel panel = new VerticalPanel();
        form.setWidget(panel);

        callField = new Hidden("call", "saveFile");
        panel.add(callField);

        encodingField = new Hidden("encoding", "base64");
        panel.add(encodingField);

        filenameField = new Hidden("filename");
        panel.add(filenameField);

        mimetypeField = new Hidden("mimetype");
        panel.add(mimetypeField);

        dataField = new Hidden("data");
        panel.add(dataField);
    }


    @Override
    public void download(String name, String mime, String data)
    {
        DOM.appendChild(RootPanel.getBodyElement(), form.getElement());

        filenameField.setValue(name);
        mimetypeField.setValue(mime);
        dataField.setValue(data);

        form.submit();

        RootPanel.getBodyElement().removeChild(form.getElement());
    }
}
