package cz.iocb.chemweb.server.servlets.hints;

import java.util.Map.Entry;
import cz.iocb.sparql.engine.config.SparqlDatabaseConfiguration;
import cz.iocb.sparql.engine.model.IriNode;



/**
 * Conversions of IRIs to the prefixed names of the prefixes of a configuration.
 */
public class NormalizeIRI
{
    /**
     * Prefixed name decomposed into the prefix and the local part.
     */
    public static class PrefixedName
    {
        /**
         * Creates the prefixed name.
         *
         * @param prefix the prefix
         * @param name the local part
         */
        public PrefixedName(String prefix, String name)
        {
            this.name = name;
            this.prefix = prefix;
        }

        /**
         * Prefix of the name.
         */
        public final String prefix;

        /**
         * Local part of the name.
         */
        public final String name;
    }


    /**
     * Regular expression of the production {@code PN_CHARS_BASE} of the SPARQL grammar.
     */
    private static String PN_CHARS_BASE = """
            ([A-Za-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\
            \\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD]|\
            [\\uD840-\\uDBBF][\\uDC00–\\uDFFF])""";

    /**
     * Regular expression of the production {@code PN_CHARS_U} of the SPARQL grammar.
     */
    private static String PN_CHARS_U = "(" + PN_CHARS_BASE + "|_)";

    /**
     * Regular expression of the production {@code PN_CHARS} of the SPARQL grammar.
     */
    private static String PN_CHARS = "(" + PN_CHARS_U + "|[-0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040])";

    /**
     * Regular expression of the production {@code PERCENT} of the SPARQL grammar.
     */
    private static String PERCENT = "(%[0-9A-Fa-f][0-9A-Fa-f])";

    /**
     * Regular expression of the production {@code PN_LOCAL_ESC} of the SPARQL grammar.
     */
    private static String PN_LOCAL_ESC = "(\\\\[-_~.!$&'()*+,;=/?#@%])";

    /**
     * Regular expression of the production {@code PLX} of the SPARQL grammar.
     */
    private static String PLX = "(" + PERCENT + "|" + PN_LOCAL_ESC + ")";

    /**
     * Regular expression of the production {@code PN_LOCAL} of the SPARQL grammar: the local part of a prefixed name.
     */
    private static String PN_LOCAL = "((" + PN_CHARS_U + "|[0-9:]|" + PLX + ")((" + PN_CHARS + "|[.:]|" + PLX + ")*("
            + PN_CHARS + "|:|" + PLX + "))?)?";


    /**
     * Not instantiable.
     */
    private NormalizeIRI()
    {
    }


    /**
     * Converts an IRI to a prefixed name, if one of the prefixes of a configuration allows it.
     *
     * @param dbConfig the configuration
     * @param iri the IRI, may be null
     * @return the prefixed name, the IRI if it has no prefixed name, or null for a null IRI
     */
    public static String normalize(SparqlDatabaseConfiguration dbConfig, String iri)
    {
        if(iri == null)
            return null;

        String prefixedIRI = IriNode.toPrefixedIri(iri, dbConfig.getPrefixes());
        return prefixedIRI != null ? prefixedIRI : iri;
    }


    /**
     * Decomposes an IRI into the shortest valid prefixed name the prefixes of a configuration allow; of the names with
     * the same local part, the one with the shorter prefix is chosen.
     *
     * @param dbConfig the configuration
     * @param iri the IRI, may be null
     * @return the prefixed name, null if the IRI is null or has no valid prefixed name
     */
    public static PrefixedName decompose(SparqlDatabaseConfiguration dbConfig, String iri)
    {
        if(iri == null)
            return null;


        PrefixedName result = null;

        for(Entry<String, String> definition : dbConfig.getPrefixes().entrySet())
        {
            if(iri.startsWith(definition.getValue()))
            {
                String prefix = definition.getKey();
                String name = iri.substring(definition.getValue().length());

                if((result == null || result.name.length() > name.length()
                        || result.name.length() == name.length() && result.prefix.length() > prefix.length())
                        && name.matches(PN_LOCAL))
                    result = new PrefixedName(prefix, name);
            }
        }

        return result;
    }
}
