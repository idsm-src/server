package cz.iocb.chemweb.client.widgets.button;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;



/**
 * Resources of the image buttons.
 */
public interface ImageButtonResources extends ClientBundle
{
    /**
     * Style classes of the image buttons.
     */
    interface ImageButtonCss extends CssResource
    {
        /**
         * Class of an image button.
         *
         * @return the class name
         */
        String imageButton();

        /**
         * Class of a disabled image button.
         *
         * @return the class name
         */
        String imageButtonDisabled();

        /**
         * Class of the panel holding the label and the file input of a file upload button.
         *
         * @return the class name
         */
        String fileUploadImageButtonInner();
    }


    /**
     * Style of the image buttons.
     *
     * @return the style
     */
    @Source("ImageButton.css")
    ImageButtonCss css();
}
