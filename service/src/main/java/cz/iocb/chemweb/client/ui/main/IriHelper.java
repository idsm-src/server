package cz.iocb.chemweb.client.ui.main;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;



/**
 * Abbreviation and expansion of IRIs by the prefixes of the endpoint (defined by {@code chemweb/js/config.js}) and the
 * check of the IRIs entered by the user.
 */
public class IriHelper
{
    /**
     * Prefix definition as a JavaScript object of the configuration.
     */
    private static class JsPrefixDefinition extends JavaScriptObject
    {
        /**
         * Not instantiable from Java; instances come from the configuration.
         */
        protected JsPrefixDefinition()
        {
        }

        /**
         * Returns the name of the prefix.
         *
         * @return the name followed by a colon
         */
        public final native String getName()
        /*-{
            return this.name;
        }-*/;

        /**
         * Returns the IRI of the prefix.
         *
         * @return the IRI
         */
        public final native String getIri()
        /*-{
            return this.iri;
        }-*/;
    }


    /**
     * Pattern of the {@code PN_CHARS_BASE} production of the SPARQL grammar. GWT compiles {@link String#matches} to a
     * JavaScript regular expression without the flag {@code u}, which matches UTF-16 code units, so the characters
     * above U+FFFF are matched as surrogate pairs.
     */
    private static String PN_CHARS_BASE = """
            ([A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\
            \\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]|\
            [\\uD800-\\uDB7F][\\uDC00-\\uDFFF])""";

    /**
     * Pattern of the {@code PN_CHARS_U} production of the SPARQL grammar.
     */
    private static String PN_CHARS_U = "(" + PN_CHARS_BASE + "|_)";

    /**
     * Pattern of the {@code PN_CHARS} production of the SPARQL grammar.
     */
    private static String PN_CHARS = "(" + PN_CHARS_U + "|[-0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040])";

    /**
     * Pattern of the {@code PERCENT} production of the SPARQL grammar.
     */
    private static String PERCENT = "(%[0-9A-Fa-f][0-9A-Fa-f])";

    /**
     * Pattern of the {@code PN_LOCAL_ESC} production of the SPARQL grammar.
     */
    private static String PN_LOCAL_ESC = "(\\\\[-_~.!$&'()*+,;=/?#@%])";

    /**
     * Pattern of the {@code PLX} production of the SPARQL grammar.
     */
    private static String PLX = "(" + PERCENT + "|" + PN_LOCAL_ESC + ")";

    /**
     * Pattern of the {@code PN_LOCAL} production of the SPARQL grammar, the local part of a prefixed name.
     */
    private static String PN_LOCAL = "((" + PN_CHARS_U + "|[0-9:]|" + PLX + ")((" + PN_CHARS + "|[.:]|" + PLX + ")*("
            + PN_CHARS + "|:|" + PLX + "))?)?";

    /**
     * Pattern of the HTTP, HTTPS and FTP URLs accepted as the IRIs of resources.
     */
    static private final RegExp URLreqexp = RegExp
            .compile("^((ftp|http|https)://[\\w@.\\-\\_]+(:\\d{1,5})?(/[\\w#!:.?+=&%@!\\_\\-/]+)*){1}$");


    /**
     * Not instantiable.
     */
    private IriHelper()
    {
    }


    /**
     * Checks that a value entered by the user is an HTTP, HTTPS or FTP URL.
     *
     * @param value the value
     * @return true if the value is a valid URL
     */
    static public boolean isValid(String value)
    {
        return URLreqexp.exec(value) != null;
    }


    /**
     * Abbreviates an IRI to a prefixed name by the prefix that leaves the shortest valid local name.
     *
     * @param iri the IRI
     * @return the prefixed name, or the IRI itself when no prefix fits
     */
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


    /**
     * Expands a prefixed name by the first prefix whose name it starts with.
     *
     * @param iri the prefixed name or an IRI
     * @return the expanded IRI, or the value itself when it starts with no prefix name
     */
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


    /**
     * Returns the prefixes of the endpoint defined by the configuration.
     *
     * @return the prefix definitions
     */
    static public final native JsArray<JsPrefixDefinition> getPrefixes()
    /*-{
        return $wnd.prefixes;
    }-*/;
}
