package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.core.client.GWT;



/**
 * Factory of the icons for high-density (retina) displays, chosen by deferred binding.
 */
public class RetinaIconsClientBundleFactory extends IconsClientBundleFactory
{
    /**
     * Creates the factory.
     */
    public RetinaIconsClientBundleFactory()
    {
    }


    @Override
    public Icons create()
    {
        return GWT.create(RetinaIcons.class);
    }
}
