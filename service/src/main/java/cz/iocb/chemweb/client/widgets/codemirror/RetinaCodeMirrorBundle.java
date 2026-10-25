package cz.iocb.chemweb.client.widgets.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;



/**
 * Factory of the resources of the CodeMirror editor for high-density (retina) displays, chosen by deferred binding.
 */
public class RetinaCodeMirrorBundle extends CodeMirrorBundle
{
    /**
     * Resources of the CodeMirror editor with the images in double resolution.
     */
    public interface RetinaCodeMirrorClientBundle extends CodeMirrorClientBundle
    {
        @Override
        @Source("class@2x.png")
        ImageResource classSymbol();

        @Override
        @Source("property@2x.png")
        ImageResource propertySymbol();

        @Override
        @Source("error@2x.png")
        ImageResource errorSymbol();

        @Override
        @Source("warning@2x.png")
        ImageResource warningSymbol();

        @Override
        @Source("multiple@2x.png")
        ImageResource multipleSymbol();
    }


    /**
     * Creates the factory.
     */
    public RetinaCodeMirrorBundle()
    {
    }


    @Override
    public CodeMirrorClientBundle create()
    {
        return GWT.create(RetinaCodeMirrorClientBundle.class);
    }
}
