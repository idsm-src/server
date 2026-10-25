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



/**
 * SPARQL editor wrapping a CodeMirror instance created over a text area, with the completion of prefixed names from the
 * hints of the endpoint and an asynchronous lint whose validator is supplied by the owner.
 */
public class CodeMirror
{
    /**
     * Templates of the entries of the completion list.
     */
    public interface HintTemplate extends SafeHtmlTemplates
    {
        /**
         * Entry with a symbol, a name and an additional information.
         *
         * @param img the class of the symbol
         * @param text the completed name
         * @param info the additional information
         * @return the entry
         */
        @Template("<div class='{0}'></div><div>{1}</div><div>{2}</div>")
        SafeHtml hint(String img, String text, String info);

        /**
         * Entry with a symbol and a name.
         *
         * @param img the class of the symbol
         * @param text the completed name
         * @return the entry
         */
        @Template("<div class='{0}'></div><div>{1}</div>")
        SafeHtml hint(String img, String text);
    }


    /**
     * Validator of the code of the editor, called by the lint of CodeMirror.
     */
    static public abstract class Validator
    {
        /**
         * Creates the validator.
         */
        protected Validator()
        {
        }


        /**
         * Validates the code and passes the found problems to the callback of the lint, see
         * {@link CodeMirror#callback(JavaScriptObject, JavaScriptObject, JavaScriptObject)}.
         *
         * @param code the code of the editor
         * @param callbackFunction the callback of the lint
         * @param options the options of the lint
         * @param cm the CodeMirror instance
         */
        public abstract void validate(String code, JavaScriptObject callbackFunction, JavaScriptObject options,
                JavaScriptObject cm);
    }


    /**
     * Templates of the entries of the completion list.
     */
    private static final HintTemplate template = GWT.create(HintTemplate.class);

    /**
     * Factory of the resources of the editor.
     */
    private static final CodeMirrorBundle factory = GWT.create(CodeMirrorBundle.class);

    /**
     * Resources of the editor.
     */
    private static final CodeMirrorClientBundle resources = factory.create();

    /**
     * Style of the editor.
     */
    private static final Style style = resources.style();

    /**
     * Asynchronous proxy of the check service.
     */
    private static CheckServiceAsync checkService = (CheckServiceAsync) GWT.create(CheckService.class);

    /**
     * The CodeMirror instance.
     */
    private JavaScriptObject codemirror = null;


    static
    {
        style.ensureInjected();
    }


    /**
     * Creates the editor over a text area.
     *
     * @param textArea the text area replaced by the editor
     * @param readOnly whether the editor is read-only (without the lint)
     */
    public CodeMirror(TextArea textArea, boolean readOnly)
    {
        codemirror = init(textArea.getElement(), readOnly);
    }


    /**
     * Creates an editable editor over a text area.
     *
     * @param textArea the text area replaced by the editor
     */
    public CodeMirror(TextArea textArea)
    {
        this(textArea, false);
    }


    /**
     * Sets the validator called by the lint of the editor.
     *
     * @param validator the validator
     */
    public native void setValidator(Validator validator)
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        var lint = editor.getOption("lint");

        if (lint != null)
            lint.javaValidator = validator;
    }-*/;


    /**
     * Returns the validator of the editor.
     *
     * @return the validator
     */
    public native Validator getValidator()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.javaValidator;
    }-*/;


    /**
     * Returns the validator set to the lint of a CodeMirror instance.
     *
     * @param cm the CodeMirror instance
     * @return the validator, or null when the instance has no lint
     */
    public static native Validator getValidator(JavaScriptObject cm)
    /*-{
        var lint = cm.getOption("lint");
        return lint == null ? null : lint.javaValidator;
    }-*/;


    /**
     * Creates a problem reported to the lint.
     *
     * @param beginLine the line where the problem begins
     * @param beginColumn the column where the problem begins
     * @param endLine the line where the problem ends
     * @param endColumn the column where the problem ends
     * @param type the severity of the problem ({@code error} or {@code warning})
     * @param warn the message
     * @return the problem
     */
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


    /**
     * Passes the problems found by a validator to the lint.
     *
     * @param callbackFunction the callback of the lint
     * @param result the array of the problems
     * @param cm the CodeMirror instance
     */
    public native static void callback(JavaScriptObject callbackFunction, JavaScriptObject result, JavaScriptObject cm)
    /*-{
        callbackFunction(cm, result);
    }-*/;


    /**
     * Annotation function of the lint, which delegates the validation to the validator of the instance.
     *
     * @param code the code of the editor
     * @param callbackFunction the callback of the lint
     * @param options the options of the lint
     * @param cm the CodeMirror instance
     */
    protected static void validateCode(final String code, final JavaScriptObject callbackFunction,
            final JavaScriptObject options, final JavaScriptObject cm)
    {
        Validator validator = getValidator(cm);

        if(validator != null)
            validator.validate(code, callbackFunction, options, cm);
    }


    /**
     * Hint function of the editor: completes the prefixed name at the cursor from the hints of its prefix (the names or
     * the information containing the typed local part).
     *
     * @param cm the CodeMirror instance
     * @param cb the callback of the hint (unused)
     * @param options the options of the hint
     * @return the completions and the replaced range, or an empty object when there is nothing to complete
     */
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


    /**
     * Creates a CodeMirror instance in the SPARQL mode from a text area.
     *
     * @param e the text area element
     * @param readOnly whether the instance is read-only (without the lint)
     * @return the CodeMirror instance
     */
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


    /**
     * Renders an entry of the completion list.
     *
     * @param type the type of the hint ({@code C} for a class, a property otherwise)
     * @param text the completed name
     * @param info the additional information, or null
     * @return the element of the entry
     */
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


    /**
     * Returns the code of the editor.
     *
     * @return the code
     */
    public native String getValue()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.getValue();
    }-*/;


    /**
     * Replaces the code of the editor.
     *
     * @param val the new code
     * @return the value returned by CodeMirror, which is undefined
     */
    public native String setValue(String val)
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.setValue(val);
    }-*/;


    /**
     * Redraws the editor, needed after its size or visibility changed.
     *
     * @return the value returned by CodeMirror, which is undefined
     */
    public native String refresh()
    /*-{
        var editor = this.@cz.iocb.chemweb.client.widgets.codemirror.CodeMirror::codemirror;
        return editor.refresh();
    }-*/;
}
