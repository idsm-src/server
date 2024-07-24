package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;



public class IriHelper
{
    private static class JsPrefixDefinition extends JavaScriptObject
    {
        protected JsPrefixDefinition()
        {
        }

        public final native String getName()
        /*-{
            return this.name;
        }-*/;

        public final native String getIri()
        /*-{
            return this.iri;
        }-*/;
    }


    private static String PN_CHARS_BASE = "([A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-"
            + "\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]|"
            + "[\\uD840-\\uDBBF][\\uDC00–\\uDFFF])";
    private static String PN_CHARS_U = "(" + PN_CHARS_BASE + "|_)";
    private static String PN_CHARS = "(" + PN_CHARS_U + "|[-0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040])";
    private static String PERCENT = "(%[0-9A-Fa-f][0-9A-Fa-f])";
    private static String PN_LOCAL_ESC = "(\\\\[-_~.!$&'()*+,;=/?#@%])";
    private static String PLX = "(" + PERCENT + "|" + PN_LOCAL_ESC + ")";
    private static String PN_LOCAL = "((" + PN_CHARS_U + "|[0-9:]|" + PLX + ")((" + PN_CHARS + "|[.:]|" + PLX + ")*("
            + PN_CHARS + "|:|" + PLX + "))?)?";

    static private final RegExp URLreqexp = RegExp
            .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");


    static public boolean isValid(String value)
    {
        return URLreqexp.exec(value) != null;
    }


    static public String prefixedIRI(String iri)
    {
        String result = iri;
        int size = iri.length();

        JsArray<JsPrefixDefinition> prefixes = getPrefixes();

        for(int i = 0; i < prefixes.length(); i++)
        {
            JsPrefixDefinition prefix = prefixes.get(i);

            if(iri.startsWith(prefix.getIri()))
            {
                String name = iri.substring(prefix.getIri().length());

                if((size > name.length() || size == name.length() && result.length() - size > prefix.getName().length())
                        && name.matches(PN_LOCAL))
                {
                    result = prefix.getName() + name;
                    size = name.length();
                }
            }
        }

        return result;
    }


    static public String deprefixedIRI(String iri)
    {
        JsArray<JsPrefixDefinition> prefixes = getPrefixes();

        for(int i = 0; i < prefixes.length(); ++i)
        {
            JsPrefixDefinition prefix = prefixes.get(i);

            if(iri.startsWith(prefix.getName()))
                return iri.replaceFirst(prefix.getName(), prefix.getIri());
        }

        return iri;
    }


    static public final native JsArray<JsPrefixDefinition> getPrefixes()
    /*-{
        return $wnd.prefixes;
    }-*/;
}
