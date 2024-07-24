package cz.iocb.chemweb.client.resources.icons;

import com.google.gwt.resources.client.RetinaImageResource;



public interface RetinaIcons extends Icons
{
    @Override
    @Source("document-open@2x.png")
    RetinaImageResource open();

    @Override
    @Source("document-open-remote@2x.png")
    RetinaImageResource remoteOpen();

    @Override
    @Source("document-save@2x.png")
    RetinaImageResource save();

    @Override
    @Source("arrow-right@2x.png")
    RetinaImageResource run();

    @Override
    @Source("dialog-close@2x.png")
    RetinaImageResource cancel();

    @Override
    @Source("go-previous@2x.png")
    RetinaImageResource prev();

    @Override
    @Source("go-next@2x.png")
    RetinaImageResource next();

    @Override
    @Source("view-refresh@2x.png")
    RetinaImageResource reload();

    @Override
    @Source("view-refresh@2x.gif")
    RetinaImageResource loading();

    @Override
    @Source("quickopen-class@2x.png")
    RetinaImageResource wizard();
}
