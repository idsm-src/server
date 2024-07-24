package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.core.client.GWT;



public class IconsClientBundleFactory
{
    public Icons create()
    {
        return GWT.create(Icons.class);
    }
}
