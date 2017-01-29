package cz.iocb.pubchem.load.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;



public abstract class SimpleFileTableLoader extends TableLoader
{
    private BufferedReader reader;
    private String sql;


    public SimpleFileTableLoader(BufferedReader reader, String sql)
    {
        this.reader = reader;
        this.sql = sql;
    }


    public void load() throws SQLException, IOException
    {
        try (Connection connection = Loader.getConnection())
        {
            try (PreparedStatement insertStatement = connection.prepareStatement(sql))
            {
                statement = insertStatement;
                int count = 0;

                for(String line = reader.readLine(); line != null; line = reader.readLine())
                {
                    if(line.startsWith("@prefix"))
                    {
                        String[] parts = line.split("[\t ]");

                        if(parts.length != 4)
                            throw new IOException();

                        if(!parts[0].equals("@prefix") && parts[3].equals("."))
                            throw new IOException();

                        prefix(parts[1], parts[2]);
                        continue;
                    }


                    String[] parts = line.split("\t");

                    if(parts.length != 3)
                        throw new IOException();

                    if(!parts[2].endsWith(" ."))
                        throw new IOException();

                    set = false;

                    insert(parts[0], parts[1], parts[2].substring(0, parts[2].length() - 2));

                    if(!set)
                        continue;


                    insertStatement.addBatch();

                    if(++count % Loader.batchSize == 0)
                    {
                        beforeBatch();
                        insertStatement.executeBatch();
                    }
                }

                reader.close();

                if(count % Loader.batchSize != 0)
                {
                    beforeBatch();
                    insertStatement.executeBatch();
                }
            }
        }
    }


    public abstract void insert(String subject, String predicate, String object) throws SQLException, IOException;


    public abstract void prefix(String name, String iri) throws SQLException, IOException;


    public void beforeBatch() throws SQLException, IOException
    {
    }


    public static int getIntID(String node, String prefix, String suffix) throws IOException
    {
        if(!node.startsWith(prefix))
            throw new IOException();

        if(!node.endsWith(suffix))
            throw new IOException();

        return Integer.parseInt(node.substring(prefix.length(), node.length() - suffix.length()));
    }


    public String getString(String node) throws IOException
    {
        if(!node.startsWith("\"") || !node.endsWith("\"@en"))
            throw new IOException();

        return node.substring(1, node.length() - 4);
    }


    public int getInteger(String node) throws IOException
    {
        if(!node.startsWith("\"") || !node.endsWith("\"^^xsd:int"))
            throw new IOException();

        return Integer.parseInt(node.substring(1, node.length() - 10));
    }


    public float getFloat(String node) throws IOException
    {
        if(!node.startsWith("\"") || !node.endsWith("\"^^xsd:float"))
            throw new IOException();

        return Float.parseFloat(node.substring(1, node.length() - 12));
    }
}
