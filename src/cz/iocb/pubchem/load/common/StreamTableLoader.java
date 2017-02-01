package cz.iocb.pubchem.load.common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;



public abstract class StreamTableLoader extends TableLoader
{
    private InputStream stream;
    private String sql;
    int count = 0;


    public StreamTableLoader(InputStream stream, String sql)
    {
        this.stream = stream;
        this.sql = sql;
    }


    public void load() throws SQLException, IOException
    {
        try (Connection connection = Loader.getConnection())
        {
            try (PreparedStatement insertStatement = connection.prepareStatement(sql))
            {
                statement = insertStatement;

                try
                {
                    RDFDataMgr.parse(new VoidStreamRDF()
                    {
                        @Override
                        public void triple(Triple triple)
                        {
                            try
                            {
                                insertStub(triple.getSubject(), triple.getPredicate(), triple.getObject());
                            }
                            catch (SQLException | IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                        }
                    }, stream, Lang.TURTLE);
                }
                catch (RuntimeException e)
                {
                    if(e.getCause() instanceof IOException)
                        throw(IOException) e.getCause();
                    else if(e.getCause() instanceof SQLException)
                        throw(SQLException) e.getCause();
                    else
                        throw e;
                }

                if(count % Loader.batchSize != 0)
                {
                    beforeBatch();
                    insertStatement.executeBatch();
                }

                stream.close();
            }
        }

    }


    public void insertStub(Node subject, Node predicate, Node object) throws SQLException, IOException
    {
        set = false;

        insert(subject, predicate, object);

        if(!set)
            return;

        statement.addBatch();

        if(++count % Loader.batchSize == 0)
        {
            beforeBatch();
            statement.executeBatch();
        }
    }


    public abstract void insert(Node subject, Node predicate, Node object) throws SQLException, IOException;


    public void beforeBatch() throws SQLException, IOException
    {
    }


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


    public String getString(Node node) throws IOException
    {
        return ((Node_Literal) node).getLiteralLexicalForm();
    }


    public int getInteger(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#int"))
            throw new IOException();

        return Integer.parseInt(literal.getLiteralLexicalForm());
    }


    public float getFloat(Node node) throws IOException
    {
        Node_Literal literal = (Node_Literal) node;

        if(!literal.getLiteralDatatypeURI().equals("http://www.w3.org/2001/XMLSchema#float"))
            throw new IOException();

        return Float.parseFloat(literal.getLiteralLexicalForm());
    }


    public short getMapID(Node node, Map<String, Short> map) throws IOException
    {
        Short value = map.get(node.getURI());

        if(value == null)
            throw new IOException();

        return value;
    }
}
