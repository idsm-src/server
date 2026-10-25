package cz.iocb.chemweb.client.resources.pager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;



/**
 * Factory of the resources of the pagers for high-density (retina) displays, chosen by deferred binding.
 */
public class RetinaPagerResourcesFactory extends PagerResourcesFactory
{
    /**
     * Creates the factory.
     */
    public RetinaPagerResourcesFactory()
    {
    }


    @Override
    public SimplePager.Resources create()
    {
        return GWT.create(RetinaPagerResources.class);
    }
}
