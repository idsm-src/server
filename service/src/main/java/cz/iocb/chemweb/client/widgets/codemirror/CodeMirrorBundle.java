package cz.iocb.chemweb.client.widgets.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;



public class CodeMirrorBundle
{
    public interface CodeMirrorClientBundle extends ClientBundle
    {
        interface Style extends CssResource
        {
            @ClassName("CodeMirror-hint-class")
            String classHint();

            @ClassName("CodeMirror-hint-propery")
            String propertyHint();
        }

        @Source("CodeMirror.gss")
        Style style();

        @Source("class.png")
        ImageResource classSymbol();

        @Source("property.png")
        ImageResource propertySymbol();

        @Source("error.png")
        ImageResource errorSymbol();

        @Source("warning.png")
        ImageResource warningSymbol();

        @Source("multiple.png")
        ImageResource multipleSymbol();
    }


    public CodeMirrorClientBundle create()
    {
        return GWT.create(CodeMirrorClientBundle.class);
    }
}
