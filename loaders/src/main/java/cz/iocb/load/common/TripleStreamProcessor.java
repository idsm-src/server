package cz.iocb.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;



public abstract class TripleStreamProcessor
{
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


    public static int getIntID(Node node, String prefix, String suffix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        if(!value.endsWith(suffix))
            throw new IOException("unexpected IRI: " + value);

        return Integer.parseInt(value.substring(prefix.length(), value.length() - suffix.length()));
    }


    public static int getIntID(Node node, String prefix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return Integer.parseInt(value.substring(prefix.length()));
    }


    public static int getIntID(String value, String prefix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return Integer.parseInt(value.substring(prefix.length()));
    }


    public static String getStringID(Node node, String prefix, String suffix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        if(!value.endsWith(suffix))
            throw new IOException("unexpected IRI: " + value);

        return value.substring(prefix.length(), value.length() - suffix.length());
    }


    public static String getStringID(Node node, String prefix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return value.substring(prefix.length());
    }


    /*
     * Returns the value of an xsd:string literal; any other value, including a language-tagged string, is refused.
     */
    public static String getString(Node node) throws IOException
    {
        if(!node.isLiteral() || !node.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#string"))
            throw new IOException("unexpected value instead of an xsd:string literal: " + node);

        return node.getLiteralLexicalForm();
    }


    /*
     * Returns the lexical form of a literal of any datatype.
     */
    public static String getLexicalForm(Node node) throws IOException
    {
        if(!node.isLiteral())
            throw new IOException("unexpected value instead of a literal: " + node);

        return node.getLiteralLexicalForm();
    }


    public static int getIntFromInteger(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#integer"))
            throw new IOException("unexpected literal datatype");

        return Integer.parseInt(literal.getLiteralLexicalForm());
    }


    public static int getInt(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#int"))
            throw new IOException("unexpected literal datatype");

        return Integer.parseInt(literal.getLiteralLexicalForm());
    }


    public static double getDouble(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#double"))
            throw new IOException("unexpected literal datatype");

        return Double.parseDouble(literal.getLiteralLexicalForm());
    }


    public static boolean getBoolean(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#boolean"))
            throw new IOException("unexpected literal datatype");

        return switch(literal.getLiteralLexicalForm())
        {
            case "true", "1" -> true;
            case "false", "0" -> false;
            default -> throw new IOException("unexpected boolean value");
        };
    }


    public static float getFloat(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#float"))
            throw new IOException("unexpected literal datatype");

        return Float.parseFloat(literal.getLiteralLexicalForm());
    }


    public static float getFloatFromDecimal(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#integer")
                && !literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#decimal"))
            throw new IOException("unexpected literal datatype");

        return Float.parseFloat(literal.getLiteralLexicalForm());
    }
}
