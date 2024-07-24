package cz.iocb.chemweb.client.widgets.codemirror;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import cz.iocb.chemweb.client.widgets.codemirror.CodeMirrorBundle.CodeMirrorClientBundle;
import cz.iocb.chemweb.client.widgets.codemirror.CodeMirrorBundle.CodeMirrorClientBundle.Style;
import cz.iocb.chemweb.shared.services.check.CheckService;
import cz.iocb.chemweb.shared.services.check.CheckServiceAsync;



public class CodeMirror
{
    public interface HintTemplate extends SafeHtmlTemplates
    {
        @Template("<div class='{0}'></div><div>{1}</div><div>{2}</div>")
        SafeHtml hint(String img, String text, String info);

        @Template("<div class='{0}'></div><div>{1}</div>")
        SafeHtml hint(String img, String text);
    }


    static public abstract class Validator
    {
        public abstract void validate(String code, JavaScriptObject callbackFunction, JavaScriptObject options,
                JavaScriptObject cm);
    }


    private static final HintTemplate template = GWT.create(HintTemplate.class);
    private static final CodeMirrorBundle factory = GWT.create(CodeMirrorBundle.class);
    private static final CodeMirrorClientBundle resources = factory.create();
    private static final Style style = resources.style();
    private static CheckServiceAsync checkService = (CheckServiceAsync) GWT.create(CheckService.class);

    private JavaScriptObject codemirror = null;


    static
    {
        style.ensureInjected();
    }


    public CodeMirror(TextArea textArea, boolean readOnly)
    {
        codemirror = init(textArea.getElement(), readOnly);
    }


    public CodeMirror(TextArea textArea)
    {
        this(textArea, false);
    }


    public native void setValidator(Validator validator)
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        var lint = editor.getOption("lint");

        if (lint != null)
            lint.javaValidator = validator;
    }-*/;


    public native Validator getValidator()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.javaValidator;
    }-*/;


    public static native Validator getValidator(JavaScriptObject cm)
    /*-{
        var lint = cm.getOption("lint");
        return lint == null ? null : lint.javaValidator;
    }-*/;


    public native static JavaScriptObject createWarning(int beginLine, int beginColumn, int endLine, int endColumn,
            String type, String warn)
    /*-{
        var error = {
            from : $wnd.CodeMirror.Pos(beginLine, beginColumn),
            to : $wnd.CodeMirror.Pos(endLine, endColumn),
            message : warn,
            severity : type
        };

        return error;
    }-*/;


    public native static void callback(JavaScriptObject callbackFunction, JavaScriptObject result, JavaScriptObject cm)
    /*-{
        callbackFunction(cm, result);
    }-*/;


    protected static void validateCode(final String code, final JavaScriptObject callbackFunction,
            final JavaScriptObject options, final JavaScriptObject cm)
    {
        Validator validator = getValidator(cm);

        if(validator != null)
            validator.validate(code, callbackFunction, options, cm);
    };


    private native static JavaScriptObject getHints(JavaScriptObject cm, JavaScriptObject cb, JavaScriptObject options)
    /*-{
        var cur = cm.getCursor();
        var token = cm.getTokenAt(cur);

        if (token.type == "string")
            return {};

        var idx = token.string.indexOf(":");
        var prefix = token.string.substring(0, idx).toLowerCase();
        var suffix = token.string.substring(idx + 1).toLowerCase();

        var set = $wnd.hints[prefix];

        if (set == null)
            return {};

        var result = [];

        for ( var i in set.hints) {
            var hint = set.hints[i];

            if (hint.lname == null)
                hint.lname = hint.name.toLowerCase();

            if (hint.linfo == null && hint.info != null)
                hint.linfo = hint.info.replace(/ /g, '').toLowerCase();

            if (hint.lname.includes(suffix) || (hint.linfo != null && hint.linfo.includes(suffix)))
                result
                        .push({
                            text : set.prefix + ":" + hint.name,
                            info : hint.info,
                            type : hint.type,
                            render : function(element, self, data) {
                                var hint = @cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::createHint(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(data.type, data.text, data.info);
                                element.appendChild(hint);
                            }
                        });
        }

        return {
            list : result,
            from : $wnd.CodeMirror.Pos(cur.line, token.start),
            to : $wnd.CodeMirror.Pos(cur.line, token.end)
        };
    }-*/;


    public native static JavaScriptObject init(Element e, boolean readOnly)
    /*-{
        var editor = $wnd.CodeMirror.fromTextArea(e, {
            mode : "sparql",
            tabMode : "indent",
            lineNumbers : true,
            iframeClass : 'CodeMirror',
            matchBrackets : true,
            lineWrapping : true,
            autoCloseBrackets : true,
            readOnly: readOnly,

            gutters : [ "CodeMirror-lint-markers" ],
            lint : readOnly ? null : {
                getAnnotations : @cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::validateCode(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;),
                async : true,
                javaValidator: null
            },

            hintOptions : {
                hint : @cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::getHints(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;),
            },

            extraKeys: {
                "Ctrl-Space": "autocomplete",
                "':'": function(cm) {
                    setTimeout(function(){cm.execCommand("autocomplete");}, 100);
                    throw CodeMirror.Pass; // tell CodeMirror we didn't handle the key
                },
            },

            theme : "chemweb"
        });

        return editor;
    }-*/;


    static private Element createHint(String type, String text, String info)
    {
        String imgClass = "C".equals(type) ? style.classHint() : style.propertyHint();

        SafeHtml html;

        if(info != null)
            html = template.hint(imgClass, text, info);
        else
            html = template.hint(imgClass, text);

        return new HTML(html).getElement();
    }


    public native String getValue()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.getValue();
    }-*/;


    public native String setValue(String val)
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.setValue(val);
    }-*/;


    public native String refresh()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.refresh();
    }-*/;
}
