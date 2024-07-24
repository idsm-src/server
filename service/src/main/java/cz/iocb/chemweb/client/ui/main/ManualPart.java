package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;



public class ManualPart extends Composite
{
    private static ManualPartUiBinder uiBinder = GWT.create(ManualPartUiBinder.class);


    interface ManualPartUiBinder extends UiBinder<Widget, ManualPart>
    {
    }


    public ManualPart()
    {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
