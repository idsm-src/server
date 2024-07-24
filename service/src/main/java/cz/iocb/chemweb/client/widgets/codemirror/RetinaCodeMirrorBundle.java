package cz.iocb.chemweb.client.widgets.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;



public class RetinaCodeMirrorBundle extends CodeMirrorBundle
{
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


    @Override
    public CodeMirrorClientBundle create()
    {
        return GWT.create(RetinaCodeMirrorClientBundle.class);
    }
}
