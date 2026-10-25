package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;



/**
 * Resources used by the entire application.
 */
public interface Icons extends ClientBundle
{
    /**
     * Icon of opening a local file.
     *
     * @return the icon
     */
    @Source("document-open.png")
    ImageResource open();

    /**
     * Icon of opening an example query.
     *
     * @return the icon
     */
    @Source("document-open-remote.png")
    ImageResource remoteOpen();

    /**
     * Icon of saving the query.
     *
     * @return the icon
     */
    @Source("document-save.png")
    ImageResource save();

    /**
     * Icon of running the query.
     *
     * @return the icon
     */
    @Source("arrow-right.png")
    ImageResource run();

    /**
     * Icon of cancelling the running query.
     *
     * @return the icon
     */
    @Source("dialog-close.png")
    ImageResource cancel();

    /**
     * Icon of going back in the history of the visited resources.
     *
     * @return the icon
     */
    @Source("go-previous.png")
    ImageResource prev();

    /**
     * Icon of going forward in the history of the visited resources.
     *
     * @return the icon
     */
    @Source("go-next.png")
    ImageResource next();

    /**
     * Icon of reloading the details or the properties of a resource.
     *
     * @return the icon
     */
    @Source("view-refresh.png")
    ImageResource reload();

    /**
     * Animated icon shown while the details or the properties of a resource are loaded.
     *
     * @return the icon
     */
    @Source("view-refresh.gif")
    ImageResource loading();

    /**
     * Icon of the wizard of search queries.
     *
     * @return the icon
     */
    @Source("quickopen-class.png")
    ImageResource wizard();
}
