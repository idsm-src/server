package cz.iocb.chemweb.client.resources.pager;

import com.google.gwt.resources.client.RetinaImageResource;



public interface RetinaPagerResources extends PagerResources
{
    @Override
    @Source("go-first-view@2x.png")
    RetinaImageResource simplePagerFirstPage();

    @Override
    @Source("go-last-view@2x.png")
    RetinaImageResource simplePagerLastPage();

    @Override
    @Source("go-next-view@2x.png")
    RetinaImageResource simplePagerNextPage();

    @Override
    @Source("go-previous-view@2x.png")
    RetinaImageResource simplePagerPreviousPage();


    @Override
    @Source("go-first-view-disabled@2x.png")
    RetinaImageResource simplePagerFirstPageDisabled();

    @Override
    @Source("go-last-view-disabled@2x.png")
    RetinaImageResource simplePagerLastPageDisabled();

    @Override
    @Source("go-next-view-disabled@2x.png")
    RetinaImageResource simplePagerNextPageDisabled();

    @Override
    @Source("go-previous-view-disabled@2x.png")
    RetinaImageResource simplePagerPreviousPageDisabled();
}
