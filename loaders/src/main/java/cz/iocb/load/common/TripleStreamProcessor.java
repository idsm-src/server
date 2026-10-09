package cz.iocb.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;



/*
 * Reads a stream of triples and passes every triple to parse(). A problem of the data of a triple, i.e. a
 * DataException or a malformed number, is reported as an error and the triple is skipped, so that the reading goes on.
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
                    catch(DataException | NumberFormatException e)
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
        String kind = e instanceof DataException data ? data.getKind() : "malformed number";
        String detail = e instanceof DataException data ? data.getDetail() : e.getMessage();
        String triple = text(subject) + " " + text(object);

        if(detail != null && !detail.equals(text(subject)) && !detail.equals(text(object)))
            triple += " (" + detail + ")";

        Problems.error(kind + " in " + predicate.getURI(), triple);
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


    private static DataException unexpectedValue(Node node, String datatype)
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
            throw unexpectedValue(node, "xsd:string");

        return node.getLiteralLexicalForm();
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
            throw unexpectedValue(node, "xsd:integer");

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
            throw unexpectedValue(node, "xsd:int");

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
            throw unexpectedValue(node, "xsd:double");

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
            throw unexpectedValue(node, "xsd:boolean");

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
            throw unexpectedValue(node, "xsd:float");

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
            throw unexpectedValue(node, "xsd:decimal");

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
