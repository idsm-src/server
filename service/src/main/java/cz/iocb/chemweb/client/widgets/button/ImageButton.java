package cz.iocb.chemweb.client.widgets.button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;



public class ImageButton extends FocusPanel implements HasEnabled, HasText
{
    protected static ImageButtonResources resources = GWT.create(ImageButtonResources.class);

    private boolean enabled = true;
    protected Label label = new Label();


    protected ImageButton()
    {
        resources.css().ensureInjected();
        addStyleName(resources.css().imageButton());

        Event.addNativePreviewHandler(new Event.NativePreviewHandler()
        {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event)
            {
                if(enabled)
                    return;

                Element target = event.getNativeEvent().getEventTarget().cast();
                Element element = getElement();

                // block all events targetted at the children of the composite
                if(element.isOrHasChild(target))
                    event.cancel();
            }
        });
    }


    @UiConstructor
    public ImageButton(SafeUri src, String position)
    {
        this();
        add(label);

        Style style = getElement().getStyle();
        style.setProperty("backgroundImage", "url('" + src.asString() + "')");
        style.setProperty("backgroundPosition", position);
    }


    @Override
    public String getText()
    {
        return label.getText();
    }


    @Override
    public void setText(String text)
    {
        label.setText(text);
    }


    @Override
    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public void setEnabled(boolean enabled)
    {
        if(enabled)
            removeStyleName(resources.css().imageButtonDisabled());
        else
            addStyleName(resources.css().imageButtonDisabled());

        this.enabled = enabled;
    }
}
