package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;



/**
 * Tab with the manual of the user interface.
 */
public class ManualPart extends Composite
{
    /**
     * Binder of the tab template.
     */
    private static ManualPartUiBinder uiBinder = GWT.create(ManualPartUiBinder.class);


    /**
     * Binder of the tab template {@code ManualPart.ui.xml}.
     */
    interface ManualPartUiBinder extends UiBinder<Widget, ManualPart>
    {
    }


    /**
     * Creates the tab.
     */
    public ManualPart()
    {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
