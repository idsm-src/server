package cz.iocb.chemweb.client.widgets.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;



/**
 * Factory of the resources of the CodeMirror editor for displays of a normal pixel density; replaced by
 * {@link RetinaCodeMirrorBundle} through deferred binding on high-density displays.
 */
public class CodeMirrorBundle
{
    /**
     * Resources of the CodeMirror editor: its style and the symbols of the hints and the lint markers.
     */
    public interface CodeMirrorClientBundle extends ClientBundle
    {
        /**
         * Style of the editor.
         */
        interface Style extends CssResource
        {
            /**
             * Class of the symbol of a class hint.
             *
             * @return the class name
             */
            @ClassName("CodeMirror-hint-class")
            String classHint();

            /**
             * Class of the symbol of a property hint.
             *
             * @return the class name
             */
            @ClassName("CodeMirror-hint-propery")
            String propertyHint();
        }

        /**
         * Style of the editor.
         *
         * @return the style
         */
        @Source("CodeMirror.gss")
        Style style();

        /**
         * Symbol of a class hint.
         *
         * @return the image
         */
        @Source("class.png")
        ImageResource classSymbol();

        /**
         * Symbol of a property hint.
         *
         * @return the image
         */
        @Source("property.png")
        ImageResource propertySymbol();

        /**
         * Lint marker of an error.
         *
         * @return the image
         */
        @Source("error.png")
        ImageResource errorSymbol();

        /**
         * Lint marker of a warning.
         *
         * @return the image
         */
        @Source("warning.png")
        ImageResource warningSymbol();

        /**
         * Lint marker of several messages on one line.
         *
         * @return the image
         */
        @Source("multiple.png")
        ImageResource multipleSymbol();
    }


    /**
     * Creates the factory.
     */
    public CodeMirrorBundle()
    {
    }


    /**
     * Creates the resources of the editor.
     *
     * @return the resources
     */
    public CodeMirrorClientBundle create()
    {
        return GWT.create(CodeMirrorClientBundle.class);
    }
}
