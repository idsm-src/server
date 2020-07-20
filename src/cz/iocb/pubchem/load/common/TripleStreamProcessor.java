package cz.iocb.pubchem.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;



public abstract class TripleStreamProcessor
{
    public void load(InputStream stream) throws IOException
    {
        try
        {
            RDFDataMgr.parse(new VoidStreamRDF()
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
            }, stream, Lang.TURTLE);
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
            throw new IOException();

        if(!value.endsWith(suffix))
            throw new IOException();

        return Integer.parseInt(value.substring(prefix.length(), value.length() - suffix.length()));
    }


    public static int getIntID(Node node, String prefix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException();

        return Integer.parseInt(value.substring(prefix.length()));
    }


    public static String getStringID(Node node, String prefix) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith(prefix))
            throw new IOException();

        return value.substring(prefix.length());
    }


    public static String getString(Node node) throws IOException
    {
        return ((Node_Literal) node).getLiteralLexicalForm();
    }


    public static int getInteger(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#int"))
            throw new IOException();

        return Integer.parseInt(literal.getLiteralLexicalForm());
    }


    public static float getFloat(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#float"))
            throw new IOException();

        return Float.parseFloat(literal.getLiteralLexicalForm());
    }


    public static MD5 getSynonymMD5(Node node) throws IOException
    {
        String value = node.getURI();

        if(!value.startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_"))
            throw new IOException();

        return new MD5(value, 48);
    }
}
