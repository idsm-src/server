package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.core.client.GWT;



public class RetinaIconsClientBundleFactory extends IconsClientBundleFactory
{
    @Override
    public Icons create()
    {
        return GWT.create(RetinaIcons.class);
    }
}
