package cz.iocb.chemweb.client.resources.pager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;



/**
 * Factory of the resources of the pagers for displays of a normal pixel density; replaced by
 * {@link RetinaPagerResourcesFactory} through deferred binding on high-density displays.
 */
public class PagerResourcesFactory
{
    /**
     * Creates the factory.
     */
    public PagerResourcesFactory()
    {
    }


    /**
     * Creates the resources of the pagers.
     *
     * @return the resources of the pagers
     */
    public SimplePager.Resources create()
    {
        return GWT.create(PagerResources.class);
    }
}
