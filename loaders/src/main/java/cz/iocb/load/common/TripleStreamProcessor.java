package cz.iocb.load.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.lang.LabelToNode;



/**
 * Reads a stream of triples and passes every triple to {@link #parse(Node, Node, Node)}. A problem of the data of a
 * triple, i.e. a DataException, a malformed number or a node of an unexpected kind, such as a literal in place of an
 * IRI, is reported as an error and the triple is skipped, so that the reading goes on. The static methods extract and
 * check the values of the nodes and report the unexpected triples.
 */
public abstract class TripleStreamProcessor
{
    /**
     * Namespace of the XML Schema datatypes.
     */
    private static final String xsd = "http://www.w3.org/2001/XMLSchema#";

    /**
     * IRI of {@code xsd:string}.
     */
    private static final String xsdString = xsd + "string";

    /**
     * IRI of {@code xsd:integer}.
     */
    private static final String xsdInteger = xsd + "integer";

    /**
     * IRI of {@code xsd:int}.
     */
    private static final String xsdInt = xsd + "int";

    /**
     * IRI of {@code xsd:double}.
     */
    private static final String xsdDouble = xsd + "double";

    /**
     * IRI of {@code xsd:boolean}.
     */
    private static final String xsdBoolean = xsd + "boolean";

    /**
     * IRI of {@code xsd:float}.
     */
    private static final String xsdFloat = xsd + "float";

    /**
     * IRI of {@code xsd:decimal}.
     */
    private static final String xsdDecimal = xsd + "decimal";

    /**
     * IRI of {@code xsd:date}.
     */
    private static final String xsdDate = xsd + "date";

    /**
     * Timezone at the end of a lexical form.
     */
    private static final Pattern zonePattern = Pattern.compile("(Z|[+-][0-9]{2}:[0-9]{2})$");

    /**
     * Lexical form of an {@code xsd:integer} literal.
     */
    private static final Pattern integerPattern = Pattern.compile("[+-]?[0-9]+");

    /**
     * Lexical form of an {@code xsd:decimal} literal.
     */
    private static final Pattern decimalPattern = Pattern.compile("[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)");

    /**
     * Lexical form of an {@code xsd:date} literal without its timezone.
     */
    private static final Pattern datePattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");


    /**
     * Creates the processor.
     */
    protected TripleStreamProcessor()
    {
    }


    /**
     * Reads a Turtle stream.
     *
     * @param stream the stream
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream stream) throws IOException
    {
        load(RDFParser.source(stream).lang(Lang.TURTLE));
    }


    /**
     * Reads a stream in the given syntax, resolving relative IRIs against the base. The blank nodes are labelled by a
     * seed derived from the base, so that a stream read again gives the same blank nodes, whereas the streams of
     * different bases give different ones.
     *
     * @param stream the stream
     * @param lang the syntax of the stream
     * @param base the base IRI
     * @throws IOException if an I/O error occurs
     */
    public void load(InputStream stream, Lang lang, String base) throws IOException
    {
        load(RDFParser.source(stream).lang(lang).base(base)
                .labelToNode(LabelToNode.createScopeByDocumentHash(UUID.nameUUIDFromBytes(base.getBytes(UTF_8)))));
    }


    /**
     * Parses the source of the parser and passes its triples on; a database error is thrown wrapped in a
     * {@link RuntimeException}.
     *
     * @param parser the parser of the stream
     * @throws IOException if an I/O error occurs
     */
    private void load(RDFParserBuilder parser) throws IOException
    {
        try
        {
            parser.parse(new VoidStreamRDF()
            {
                @Override
                public void triple(Triple triple)
                {
                    try
                    {
                        parse(triple.getSubject(), triple.getPredicate(), triple.getObject());
                    }
                    catch(DataException | NumberFormatException | UnsupportedOperationException e)
                    {
                        report(e, triple.getSubject(), triple.getPredicate(), triple.getObject());
                    }
                    catch(SQLException | IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else
                throw e;
        }
    }


    /**
     * Processes a triple of the stream.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @throws SQLException if a database error occurs
     * @throws IOException if the triple cannot be loaded (a {@link DataException}, which is reported) or an I/O error
     *             occurs
     */
    protected abstract void parse(Node subject, Node predicate, Node object) throws SQLException, IOException;


    /**
     * Reports a problem of the data of a triple as an error of its kind in the triples of the predicate.
     *
     * @param e the problem: a {@link DataException}, a {@link NumberFormatException} or an
     *            {@link UnsupportedOperationException}
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     */
    public static void report(Exception e, Node subject, Node predicate, Node object)
    {
        String kind = e instanceof DataException data ? data.getKind() :
                e instanceof UnsupportedOperationException ? "unexpected kind of a node" : "malformed number";
        String detail = e instanceof DataException data ? data.getDetail() : e.getMessage();
        String triple = text(subject) + " " + text(object);

        if(detail != null && !detail.equals(text(subject)) && !detail.equals(text(object)))
            triple += " (" + detail + ")";

        Problems.error(kind + " in " + predicate.getURI(), triple);
    }


    /**
     * Reports a triple whose predicate the loader does not know, i.e. a triple that it does not load, as an error.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     */
    public static void unexpected(Node subject, Node predicate, Node object)
    {
        Problems.error("unexpected predicate " + predicate.getURI(), text(subject) + " " + text(object));
    }


    /**
     * Tests whether the triple has the expected predicate; a triple with another predicate is reported as an error.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @param expected the IRI of the expected predicate
     * @return true if the triple has the expected predicate
     */
    public static boolean checkPredicate(Node subject, Node predicate, Node object, String expected)
    {
        if(predicate.getURI().equals(expected))
            return true;

        unexpected(subject, predicate, object);
        return false;
    }


    /**
     * Tests whether the triple has one of the expected predicates; a triple with another predicate is reported as an
     * error.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the object of the triple
     * @param expected the IRIs of the expected predicates
     * @return true if the triple has one of the expected predicates
     */
    public static boolean checkPredicate(Node subject, Node predicate, Node object, String... expected)
    {
        for(String iri : expected)
            if(predicate.getURI().equals(iri))
                return true;

        unexpected(subject, predicate, object);
        return false;
    }


    /**
     * Reports a value of a predicate that the loader does not expect as an error.
     *
     * @param subject the subject of the triple
     * @param predicate the predicate of the triple
     * @param object the unexpected value
     */
    public static void unexpectedValue(Node subject, Node predicate, Node object)
    {
        Problems.error("unexpected value of " + predicate.getURI(), text(subject) + " " + text(object));
    }


    /**
     * Reports a type of a subject that the loader does not know as an error.
     *
     * @param subject the subject
     * @param type the unexpected type
     */
    public static void unexpectedType(Node subject, Node type)
    {
        Problems.error("unexpected rdf:type " + text(type), text(subject));
    }


    /**
     * Tests whether the type is one of the expected ones; another type is reported as an error.
     *
     * @param subject the subject
     * @param type the type of the subject
     * @param expected the IRIs of the expected types
     * @return true if the type is one of the expected ones
     */
    public static boolean checkType(Node subject, Node type, String... expected)
    {
        if(type.isURI())
            for(String iri : expected)
                if(type.getURI().equals(iri))
                    return true;

        unexpectedType(subject, type);
        return false;
    }


    /**
     * Returns the IRI of an IRI node and the string form of any other node, as the messages show them.
     *
     * @param node the node
     * @return the text of the node
     */
    public static String text(Node node)
    {
        return node.isURI() ? node.getURI() : node.toString();
    }


    /**
     * Returns the integer identifier of an IRI that consists of the prefix, the identifier and the suffix.
     *
     * @param node the IRI
     * @param prefix the prefix of the IRI
     * @param suffix the suffix of the IRI
     * @return the identifier
     * @throws IOException if the node is not such an IRI (a {@link DataException})
     */
    public static int getIntID(Node node, String prefix, String suffix) throws IOException
    {
        return parseID(getStringID(node, prefix, suffix), node);
    }


    /**
     * Returns the integer identifier of an IRI that consists of the prefix and the identifier.
     *
     * @param node the IRI
     * @param prefix the prefix of the IRI
     * @return the identifier
     * @throws IOException if the node is not such an IRI (a {@link DataException})
     */
    public static int getIntID(Node node, String prefix) throws IOException
    {
        return parseID(getStringID(node, prefix), node);
    }


    /**
     * Returns the integer identifier of the text of an IRI that consists of the prefix and the identifier.
     *
     * @param value the text of the IRI
     * @param prefix the prefix of the IRI
     * @return the identifier
     * @throws IOException if the text is not such an IRI (a {@link DataException})
     */
    public static int getIntID(String value, String prefix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        try
        {
            return Integer.parseInt(value.substring(prefix.length()));
        }
        catch(NumberFormatException e)
        {
            throw new DataException("unexpected IRI", value);
        }
    }


    /**
     * Parses the integer identifier of an IRI.
     *
     * @param id the text of the identifier
     * @param node the IRI, shown in the reported problem
     * @return the identifier
     * @throws DataException if the identifier is not an integer
     */
    private static int parseID(String id, Node node) throws DataException
    {
        try
        {
            return Integer.parseInt(id);
        }
        catch(NumberFormatException e)
        {
            throw new DataException("unexpected IRI", text(node));
        }
    }


    /**
     * Returns the identifier of an IRI that consists of the prefix, the identifier and the suffix.
     *
     * @param node the IRI
     * @param prefix the prefix of the IRI
     * @param suffix the suffix of the IRI
     * @return the identifier
     * @throws IOException if the node is not such an IRI (a {@link DataException})
     */
    public static String getStringID(Node node, String prefix, String suffix) throws IOException
    {
        String value = node.isURI() ? node.getURI() : null;

        if(value == null || !value.startsWith(prefix) || !value.endsWith(suffix))
            throw new DataException("unexpected IRI", text(node));

        return value.substring(prefix.length(), value.length() - suffix.length());
    }


    /**
     * Returns the identifier of an IRI that consists of the prefix and the identifier.
     *
     * @param node the IRI
     * @param prefix the prefix of the IRI
     * @return the identifier
     * @throws IOException if the node is not such an IRI (a {@link DataException})
     */
    public static String getStringID(Node node, String prefix) throws IOException
    {
        String value = node.isURI() ? node.getURI() : null;

        if(value == null || !value.startsWith(prefix))
            throw new DataException("unexpected IRI", text(node));

        return value.substring(prefix.length());
    }


    /**
     * Returns the problem of a node that is not a literal of the datatype.
     *
     * @param node the node
     * @param datatype the prefixed name of the datatype
     * @return the problem
     */
    private static DataException unexpectedLiteral(Node node, String datatype)
    {
        return new DataException("unexpected value instead of an " + datatype + " literal", text(node));
    }


    /**
     * Returns the problem of a malformed literal of the datatype.
     *
     * @param node the literal
     * @param datatype the prefixed name of the datatype
     * @return the problem
     */
    private static DataException malformedValue(Node node, String datatype)
    {
        return new DataException("malformed " + datatype + " literal", text(node));
    }


    /**
     * Returns the value of an xsd:string literal; any other value, including a language-tagged string, is refused.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:string} literal (a {@link DataException})
     */
    public static String getString(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdString))
            throw unexpectedLiteral(node, "xsd:string");

        return node.getLiteralLexicalForm();
    }


    /**
     * Returns the date of an xsd:date literal without its timezone. The date has to have one of the given timezones,
     * or no timezone when none is given, because the mapping gives all dates of a property the same timezone or none.
     *
     * @param node the node
     * @param zones the allowed timezones, such as {@code Z} or {@code +02:00}
     * @return the lexical form of the date without its timezone
     * @throws IOException if the node is not an {@code xsd:date} literal of a storable date with an allowed timezone (a
     *             {@link DataException})
     */
    public static String getDate(Node node, String... zones) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdDate))
            throw unexpectedLiteral(node, "xsd:date");

        String date = node.getLiteralLexicalForm();
        Matcher matcher = zonePattern.matcher(date);

        if(matcher.find())
        {
            if(!List.of(zones).contains(matcher.group()))
                throw new DataException("unexpected timezone of an xsd:date literal", text(node));

            date = date.substring(0, matcher.start());
        }
        else if(zones.length > 0)
        {
            throw new DataException("missing timezone of an xsd:date literal", text(node));
        }

        if(!isDate(date))
            throw unexpectedLiteral(node, "xsd:date");

        return date;
    }


    /**
     * Tests whether the text is a date that the database can store: a day of the years 1 to 9999 written as the
     * lexical form of an xsd:date literal without its timezone.
     *
     * @param text the text
     * @return true if the text is a storable date
     */
    public static boolean isDate(String text)
    {
        Matcher matcher = datePattern.matcher(text);

        if(!matcher.matches() || Integer.parseInt(matcher.group(1)) == 0)
            return false;

        try
        {
            LocalDate.of(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
            return true;
        }
        catch(DateTimeException e)
        {
            return false;
        }
    }


    /**
     * Returns the lexical form of a literal of any datatype.
     *
     * @param node the node
     * @return the lexical form of the literal
     * @throws IOException if the node is not a literal (a {@link DataException})
     */
    public static String getLexicalForm(Node node) throws IOException
    {
        if(!node.isLiteral())
            throw new DataException("unexpected value instead of a literal", text(node));

        return node.getLiteralLexicalForm();
    }


    /**
     * Returns the value of an xsd:integer literal, which has to fit into an {@code int}.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:integer} literal or the literal is malformed (a
     *             {@link DataException})
     */
    public static int getIntFromInteger(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdInteger))
            throw unexpectedLiteral(node, "xsd:integer");

        try
        {
            return Integer.parseInt(node.getLiteralLexicalForm());
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:integer");
        }
    }


    /**
     * Returns the value of an xsd:int literal.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:int} literal or the literal is malformed (a
     *             {@link DataException})
     */
    public static int getInt(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdInt))
            throw unexpectedLiteral(node, "xsd:int");

        try
        {
            return Integer.parseInt(node.getLiteralLexicalForm());
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:int");
        }
    }


    /**
     * Returns the value of an xsd:double literal.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:double} literal or the literal is malformed (a
     *             {@link DataException})
     */
    public static double getDouble(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdDouble))
            throw unexpectedLiteral(node, "xsd:double");

        try
        {
            return Double.parseDouble(node.getLiteralLexicalForm());
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:double");
        }
    }


    /**
     * Returns the value of an xsd:boolean literal.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:boolean} literal or the literal is malformed (a
     *             {@link DataException})
     */
    public static boolean getBoolean(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdBoolean))
            throw unexpectedLiteral(node, "xsd:boolean");

        return switch(node.getLiteralLexicalForm())
        {
            case "true", "1" -> true;
            case "false", "0" -> false;
            default -> throw malformedValue(node, "xsd:boolean");
        };
    }


    /**
     * Returns the value of an xsd:float literal.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:float} literal or the literal is malformed (a
     *             {@link DataException})
     */
    public static float getFloat(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdFloat))
            throw unexpectedLiteral(node, "xsd:float");

        try
        {
            return Float.parseFloat(node.getLiteralLexicalForm());
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:float");
        }
    }


    /**
     * Returns the value of an xsd:decimal literal or of an xsd:integer literal, whose values are decimals as well. A
     * decimal in the exponent notation, which the lexical space of xsd:decimal does not allow, is repaired with a
     * warning.
     *
     * @param node the node
     * @return the value of the literal
     * @throws IOException if the node is not an {@code xsd:decimal} or {@code xsd:integer} literal or the literal is
     *             malformed (a {@link DataException})
     */
    public static BigDecimal getDecimal(Node node) throws IOException
    {
        if(!node.isLiteral()
                || !node.getLiteralDatatypeURI().equals(xsdInteger) && !node.getLiteralDatatypeURI().equals(xsdDecimal))
            throw unexpectedLiteral(node, "xsd:decimal");

        String value = node.getLiteralLexicalForm();
        boolean integer = node.getLiteralDatatypeURI().equals(xsdInteger);

        if((integer ? integerPattern : decimalPattern).matcher(value).matches())
            return new BigDecimal(value);

        if(integer)
            throw malformedValue(node, "xsd:integer");

        try
        {
            BigDecimal repaired = new BigDecimal(value);
            Problems.warning("repaired malformed xsd:decimal literal", text(node));
            return repaired;
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:decimal");
        }
    }


    /**
     * Returns the text of a decimal in the form that a numeric column returns it, without an exponent and without
     * trailing zeros of the fraction, so that a stored value and a loaded one compare as equal texts.
     *
     * @param value the decimal
     * @return the text of the decimal
     */
    public static String getText(BigDecimal value)
    {
        return value.stripTrailingZeros().toPlainString();
    }
}
