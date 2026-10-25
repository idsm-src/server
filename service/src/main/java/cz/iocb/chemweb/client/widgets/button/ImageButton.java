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



/**
 * Button showing an image as its background and an optional text; a disabled button blocks all events aimed at it.
 */
public class ImageButton extends FocusPanel implements HasEnabled, HasText
{
    /**
     * Resources of the image buttons.
     */
    protected static ImageButtonResources resources = GWT.create(ImageButtonResources.class);

    /**
     * Whether the button is enabled.
     */
    private boolean enabled = true;

    /**
     * Label holding the text of the button.
     */
    protected Label label = new Label();


    /**
     * Creates the button without its image and label, installing the style and the blocking of events.
     */
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


    /**
     * Creates the button with an image.
     *
     * @param src the URI of the image
     * @param position the CSS background position of the image
     */
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
