package cz.iocb.chemweb.client.resources.pager;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.SimplePager;



public interface PagerResources extends SimplePager.Resources
{
    @Override
    @Source("go-first-view.png")
    ImageResource simplePagerFirstPage();

    @Override
    @Source("go-last-view.png")
    ImageResource simplePagerLastPage();

    @Override
    @Source("go-next-view.png")
    ImageResource simplePagerNextPage();

    @Override
    @Source("go-previous-view.png")
    ImageResource simplePagerPreviousPage();


    @Override
    @Source("go-first-view-disabled.png")
    ImageResource simplePagerFirstPageDisabled();

    @Override
    @Source("go-last-view-disabled.png")
    ImageResource simplePagerLastPageDisabled();

    @Override
    @Source("go-next-view-disabled.png")
    ImageResource simplePagerNextPageDisabled();

    @Override
    @Source("go-previous-view-disabled.png")
    ImageResource simplePagerPreviousPageDisabled();
}
