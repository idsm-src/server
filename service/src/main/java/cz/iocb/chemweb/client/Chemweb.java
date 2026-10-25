package cz.iocb.chemweb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import cz.iocb.chemweb.client.ui.main.MainPage;



/**
 * Entry point of the IDSM web client: fills the root layout panel with the main page.
 */
public class Chemweb implements EntryPoint
{
    /**
     * Creates the entry point.
     */
    public Chemweb()
    {
    }


    @Override
    public void onModuleLoad()
    {
        final RootLayoutPanel rootPanel = RootLayoutPanel.get();
        rootPanel.setStyleName("MainPanel");

        final MainPage page = new MainPage();
        rootPanel.add(page);
    }
}
