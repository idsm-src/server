package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;



/**
 * Resources used by the entire application.
 */
public interface Icons extends ClientBundle
{
    @Source("document-open.png")
    ImageResource open();

    @Source("document-open-remote.png")
    ImageResource remoteOpen();

    @Source("document-save.png")
    ImageResource save();

    @Source("arrow-right.png")
    ImageResource run();

    @Source("dialog-close.png")
    ImageResource cancel();

    @Source("go-previous.png")
    ImageResource prev();

    @Source("go-next.png")
    ImageResource next();

    @Source("view-refresh.png")
    ImageResource reload();

    @Source("view-refresh.gif")
    ImageResource loading();

    @Source("quickopen-class.png")
    ImageResource wizard();
}
