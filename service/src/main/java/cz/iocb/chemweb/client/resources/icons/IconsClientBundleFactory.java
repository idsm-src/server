package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.core.client.GWT;



/**
 * Factory of the icons for displays of a normal pixel density; replaced by {@link RetinaIconsClientBundleFactory}
 * through deferred binding on high-density displays.
 */
public class IconsClientBundleFactory
{
    /**
     * Creates the factory.
     */
    public IconsClientBundleFactory()
    {
    }


    /**
     * Creates the icons.
     *
     * @return the icons
     */
    public Icons create()
    {
        return GWT.create(Icons.class);
    }
}
