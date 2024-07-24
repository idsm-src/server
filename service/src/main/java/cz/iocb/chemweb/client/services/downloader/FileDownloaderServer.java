package cz.iocb.chemweb.client.services.downloader;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;



public class FileDownloaderServer extends FileDownloader
{
    private final Hidden callField;
    private final Hidden encodingField;
    private final Hidden filenameField;
    private final Hidden mimetypeField;
    private final Hidden dataField;
    private final FormPanel form;


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
