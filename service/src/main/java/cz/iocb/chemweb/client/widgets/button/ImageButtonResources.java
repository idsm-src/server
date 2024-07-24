package cz.iocb.chemweb.client.widgets.button;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;



public interface ImageButtonResources extends ClientBundle
{
    interface ImageButtonCss extends CssResource
    {
        String imageButton();

        String imageButtonDisabled();

        String fileUploadImageButtonInner();
    }


    @Source("ImageButton.css")
    ImageButtonCss css();
}
