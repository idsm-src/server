package cz.iocb.chemweb.client.resources.pager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.SimplePager;



public class RetinaPagerResourcesFactory extends PagerResourcesFactory
{
    @Override
    public SimplePager.Resources create()
    {
        return GWT.create(RetinaPagerResources.class);
    }
}
