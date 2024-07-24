package cz.iocb.chemweb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.iocb.chemweb.client.ui.main.MainPage;



public class Chemweb implements EntryPoint
{
    @Override
    public void onModuleLoad()
    {
        final RootLayoutPanel rootPanel = RootLayoutPanel.get();
        rootPanel.setStyleName("MainPanel");

        final MainPage page = new MainPage();
        rootPanel.add(page);
    }
}
