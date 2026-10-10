package cz.iocb.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;



/*
 * Reads a stream of triples and passes every triple to parse(). A problem of the data of a triple, i.e. a
 * DataException, a malformed number or a node of an unexpected kind, such as a literal in place of an IRI, is reported
 * as an error and the triple is skipped, so that the reading goes on.
 */
public abstract class TripleStreamProcessor
{
    private static final String xsd = "http://www.w3.org/2001/XMLSchema#";
    private static final String xsdString = xsd + "string";
    private static final String xsdInteger = xsd + "integer";
    private static final String xsdInt = xsd + "int";
    private static final String xsdDouble = xsd + "double";
    private static final String xsdBoolean = xsd + "boolean";
    private static final String xsdFloat = xsd + "float";
    private static final String xsdDecimal = xsd + "decimal";
    private static final String xsdDate = xsd + "date";
    private static final Pattern zonePattern = Pattern.compile("(Z|[+-][0-9]{2}:[0-9]{2})$");
    private static final Pattern datePattern = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");


    public void load(InputStream stream) throws IOException
    {
        load(RDFParser.source(stream).lang(Lang.TURTLE));
    }


    /*
     * Reads a stream in the given syntax, resolving relative IRIs against the base.
     */
    public void load(InputStream stream, Lang lang, String base) throws IOException
    {
        load(RDFParser.source(stream).lang(lang).base(base));
    }


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


    protected abstract void parse(Node subject, Node predicate, Node object) throws SQLException, IOException;


    /*
     * Reports a problem of the data of a triple as an error of its kind in the triples of the predicate.
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


    /*
     * Reports a triple whose predicate the loader does not know, i.e. a triple that it does not load, as an error.
     */
    public static void unexpected(Node subject, Node predicate, Node object)
    {
        Problems.error("unexpected predicate " + predicate.getURI(), text(subject) + " " + text(object));
    }


    /*
     * Tests whether the triple has the expected predicate; a triple with another predicate is reported as an error.
     */
    public static boolean checkPredicate(Node subject, Node predicate, Node object, String expected)
    {
        if(predicate.getURI().equals(expected))
            return true;

        unexpected(subject, predicate, object);
        return false;
    }


    /*
     * Tests whether the triple has one of the expected predicates; a triple with another predicate is reported as an
     * error.
     */
    public static boolean checkPredicate(Node subject, Node predicate, Node object, String... expected)
    {
        for(String iri : expected)
            if(predicate.getURI().equals(iri))
                return true;

        unexpected(subject, predicate, object);
        return false;
    }


    /*
     * Reports a value of a predicate that the loader does not expect as an error.
     */
    public static void unexpectedValue(Node subject, Node predicate, Node object)
    {
        Problems.error("unexpected value of " + predicate.getURI(), text(subject) + " " + text(object));
    }


    /*
     * Reports a type of a subject that the loader does not know as an error.
     */
    public static void unexpectedType(Node subject, Node type)
    {
        Problems.error("unexpected rdf:type " + text(type), text(subject));
    }


    /*
     * Tests whether the type is one of the expected ones; another type is reported as an error.
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


    /*
     * Returns the IRI of an IRI node and the string form of any other node, as the messages show them.
     */
    public static String text(Node node)
    {
        return node.isURI() ? node.getURI() : node.toString();
    }


    public static int getIntID(Node node, String prefix, String suffix) throws IOException
    {
        return parseID(getStringID(node, prefix, suffix), node);
    }


    public static int getIntID(Node node, String prefix) throws IOException
    {
        return parseID(getStringID(node, prefix), node);
    }


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


    public static String getStringID(Node node, String prefix, String suffix) throws IOException
    {
        String value = node.isURI() ? node.getURI() : null;

        if(value == null || !value.startsWith(prefix) || !value.endsWith(suffix))
            throw new DataException("unexpected IRI", text(node));

        return value.substring(prefix.length(), value.length() - suffix.length());
    }


    public static String getStringID(Node node, String prefix) throws IOException
    {
        String value = node.isURI() ? node.getURI() : null;

        if(value == null || !value.startsWith(prefix))
            throw new DataException("unexpected IRI", text(node));

        return value.substring(prefix.length());
    }


    private static DataException unexpectedLiteral(Node node, String datatype)
    {
        return new DataException("unexpected value instead of an " + datatype + " literal", text(node));
    }


    private static DataException malformedValue(Node node, String datatype)
    {
        return new DataException("malformed " + datatype + " literal", text(node));
    }


    /*
     * Returns the value of an xsd:string literal; any other value, including a language-tagged string, is refused.
     */
    public static String getString(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals(xsdString))
            throw unexpectedLiteral(node, "xsd:string");

        return node.getLiteralLexicalForm();
    }


    /*
     * Returns the date of an xsd:date literal without its timezone. The date has to have one of the given timezones,
     * or no timezone when none is given, because the mapping gives all dates of a property the same timezone or none.
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


    /*
     * Tests whether the text is a date that the database can store: a day of the years 1 to 9999 written as the
     * lexical form of an xsd:date literal without its timezone.
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


    /*
     * Returns the lexical form of a literal of any datatype.
     */
    public static String getLexicalForm(Node node) throws IOException
    {
        if(!node.isLiteral())
            throw new DataException("unexpected value instead of a literal", text(node));

        return node.getLiteralLexicalForm();
    }


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


    public static float getFloatFromDecimal(Node node) throws IOException
    {
        if(!node.isLiteral()
                || !node.getLiteralDatatypeURI().equals(xsdInteger) && !node.getLiteralDatatypeURI().equals(xsdDecimal))
            throw unexpectedLiteral(node, "xsd:decimal");

        try
        {
            return Float.parseFloat(node.getLiteralLexicalForm());
        }
        catch(NumberFormatException e)
        {
            throw malformedValue(node, "xsd:decimal");
        }
    }
}
