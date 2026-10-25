package cz.iocb.load.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;



/**
 * Base of the loaders, whose static members run a load: the connection to the database, in which the whole load is one
 * transaction, the files of the data directory, the transfer of sets and maps of values between the database and the
 * memory, the versions, statistics and Sachem indexes of the loaded sources, and the commit or the rollback of the
 * load.
 */
public class Updater
{
    /**
     * Processing of a file of the data directory.
     */
    protected static interface FileNameSqlFunction
    {
        /**
         * Processes a file.
         *
         * @param file the path of the file in the data directory
         * @throws IOException if an I/O error occurs
         * @throws SQLException if a database error occurs
         */
        void apply(String file) throws IOException, SQLException;
    }


    /**
     * Processing of an XML file of the data directory.
     */
    protected static interface FileNameXmlFunction
    {
        /**
         * Processes a file.
         *
         * @param file the path of the file in the data directory
         * @throws IOException if an I/O error occurs
         * @throws XPathException if an XPath expression fails
         * @throws ParserConfigurationException if the XML parser cannot be created
         * @throws SAXException if the XML cannot be parsed
         * @throws SQLException if a database error occurs
         */
        void apply(String file)
                throws IOException, XPathException, ParserConfigurationException, SAXException, SQLException;
    }


    /**
     * Set of values that are read from the columns of a query result and set to the parameters of a command, in the
     * order of their components.
     *
     * @param <T> the type of the values
     */
    @SuppressWarnings("serial")
    public static abstract class SqlSet<T> extends HashSet<T>
    {
        /**
         * Creates an empty set.
         */
        protected SqlSet()
        {
        }

        /**
         * Reads a value from the first columns of a result.
         *
         * @param result the result
         * @return the value
         * @throws SQLException if a database error occurs
         */
        public abstract T get(ResultSet result) throws SQLException;

        /**
         * Sets a value to the first parameters of a statement.
         *
         * @param statement the statement
         * @param value the value
         * @throws SQLException if a database error occurs
         */
        public abstract void set(PreparedStatement statement, T value) throws SQLException;
    }


    /**
     * Map whose keys and values are read from the columns of a query result and set to the parameters of a command, the
     * key first, in the order of their components.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     */
    @SuppressWarnings("serial")
    public static abstract class SqlMap<K, V> extends HashMap<K, V>
    {
        /**
         * Creates an empty map.
         */
        protected SqlMap()
        {
        }

        /**
         * Reads a key from the first columns of a result.
         *
         * @param result the result
         * @return the key
         * @throws SQLException if a database error occurs
         */
        public abstract K getKey(ResultSet result) throws SQLException;

        /**
         * Reads a value from the columns of a result that follow the key.
         *
         * @param result the result
         * @return the value
         * @throws SQLException if a database error occurs
         */
        public abstract V getValue(ResultSet result) throws SQLException;

        /**
         * Sets a key and its value to the first parameters of a statement.
         *
         * @param statement the statement
         * @param key the key
         * @param value the value
         * @throws SQLException if a database error occurs
         */
        public abstract void set(PreparedStatement statement, K key, V value) throws SQLException;
    }


    /**
     * Set of integers.
     */
    @SuppressWarnings("serial")
    public static class IntSet extends SqlSet<Integer>
    {
        /**
         * Creates an empty set.
         */
        public IntSet()
        {
        }

        @Override
        public Integer get(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public void set(PreparedStatement statement, Integer value) throws SQLException
        {
            statement.setInt(1, value);
        }
    }


    /**
     * Set of strings.
     */
    @SuppressWarnings("serial")
    public static class StringSet extends SqlSet<String>
    {
        /**
         * Creates an empty set.
         */
        public StringSet()
        {
        }

        @Override
        public String get(ResultSet result) throws SQLException
        {
            return result.getString(1);
        }

        @Override
        public void set(PreparedStatement statement, String value) throws SQLException
        {
            statement.setString(1, value);
        }
    }


    /**
     * Set of pairs of integers.
     */
    @SuppressWarnings("serial")
    public static class IntPairSet extends SqlSet<Pair<Integer, Integer>>
    {
        /**
         * Creates an empty set.
         */
        public IntPairSet()
        {
        }

        @Override
        public Pair<Integer, Integer> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getInt(2));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Integer> value) throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setInt(2, value.getTwo());
        }
    }


    /**
     * Set of pairs of an integer and a string.
     */
    @SuppressWarnings("serial")
    public static class IntStringSet extends SqlSet<Pair<Integer, String>>
    {
        /**
         * Creates an empty set.
         */
        public IntStringSet()
        {
        }

        @Override
        public Pair<Integer, String> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getString(2));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, String> value) throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setString(2, value.getTwo());
        }
    }


    /**
     * Set of pairs of an integer and a float.
     */
    @SuppressWarnings("serial")
    public static class IntFloatSet extends SqlSet<Pair<Integer, Float>>
    {
        /**
         * Creates an empty set.
         */
        public IntFloatSet()
        {
        }

        @Override
        public Pair<Integer, Float> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getFloat(2));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Float> value) throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setFloat(2, value.getTwo());
        }
    }


    /**
     * Set of pairs of strings.
     */
    @SuppressWarnings("serial")
    public static class StringPairSet extends SqlSet<Pair<String, String>>
    {
        /**
         * Creates an empty set.
         */
        public StringPairSet()
        {
        }

        @Override
        public Pair<String, String> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getString(1), result.getString(2));
        }

        @Override
        public void set(PreparedStatement statement, Pair<String, String> value) throws SQLException
        {
            statement.setString(1, value.getOne());
            statement.setString(2, value.getTwo());
        }
    }


    /**
     * Set of pairs of an integer and a pair of integers.
     */
    @SuppressWarnings("serial")
    public static class IntIntPairSet extends SqlSet<Pair<Integer, Pair<Integer, Integer>>>
    {
        /**
         * Creates an empty set.
         */
        public IntIntPairSet()
        {
        }

        @Override
        public Pair<Integer, Pair<Integer, Integer>> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), Pair.getPair(result.getInt(2), result.getInt(3)));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Pair<Integer, Integer>> value) throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setInt(2, value.getTwo().getOne());
            statement.setInt(3, value.getTwo().getTwo());
        }
    }


    /**
     * Set of pairs of an integer and a pair of a float and an integer.
     */
    @SuppressWarnings("serial")
    public static class IntFloatIntPairSet extends SqlSet<Pair<Integer, Pair<Float, Integer>>>
    {
        /**
         * Creates an empty set.
         */
        public IntFloatIntPairSet()
        {
        }

        @Override
        public Pair<Integer, Pair<Float, Integer>> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), Pair.getPair(result.getFloat(2), result.getInt(3)));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Pair<Float, Integer>> value) throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setFloat(2, value.getTwo().getOne());
            statement.setInt(3, value.getTwo().getTwo());
        }
    }


    /**
     * Set of pairs of an integer and a pair of a pair of floats and an integer.
     */
    @SuppressWarnings("serial")
    public static class IntFloatPairIntPairSet extends SqlSet<Pair<Integer, Pair<Pair<Float, Float>, Integer>>>
    {
        /**
         * Creates an empty set.
         */
        public IntFloatPairIntPairSet()
        {
        }

        @Override
        public Pair<Integer, Pair<Pair<Float, Float>, Integer>> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1),
                    Pair.getPair(Pair.getPair(result.getFloat(2), result.getFloat(3)), result.getInt(4)));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Pair<Pair<Float, Float>, Integer>> value)
                throws SQLException
        {
            statement.setInt(1, value.getOne());
            statement.setFloat(2, value.getTwo().getOne().getOne());
            statement.setFloat(3, value.getTwo().getOne().getTwo());
            statement.setInt(4, value.getTwo().getTwo());
        }
    }


    /**
     * Set of pairs of a pair of integers and an integer.
     */
    @SuppressWarnings("serial")
    public static class IntPairIntSet extends SqlSet<Pair<Pair<Integer, Integer>, Integer>>
    {
        /**
         * Creates an empty set.
         */
        public IntPairIntSet()
        {
        }

        @Override
        public Pair<Pair<Integer, Integer>, Integer> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(Pair.getPair(result.getInt(1), result.getInt(2)), result.getInt(3));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Pair<Integer, Integer>, Integer> value) throws SQLException
        {
            statement.setInt(1, value.getOne().getOne());
            statement.setInt(2, value.getOne().getTwo());
            statement.setInt(3, value.getTwo());
        }
    }


    /**
     * Set of pairs of two pairs of integers.
     */
    @SuppressWarnings("serial")
    public static class IntPairIntPairSet extends SqlSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>
    {
        /**
         * Creates an empty set.
         */
        public IntPairIntPairSet()
        {
        }

        @Override
        public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> get(ResultSet result) throws SQLException
        {
            return Pair.getPair(Pair.getPair(result.getInt(1), result.getInt(2)),
                    Pair.getPair(result.getInt(3), result.getInt(4)));
        }

        @Override
        public void set(PreparedStatement statement, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> value)
                throws SQLException
        {
            statement.setInt(1, value.getOne().getOne());
            statement.setInt(2, value.getOne().getTwo());
            statement.setInt(3, value.getTwo().getOne());
            statement.setInt(4, value.getTwo().getTwo());
        }
    }


    /**
     * Map of integers to integers.
     */
    @SuppressWarnings("serial")
    public static class IntIntMap extends SqlMap<Integer, Integer>
    {
        /**
         * Creates an empty map.
         */
        public IntIntMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(2);
        }

        @Override
        public void set(PreparedStatement statement, Integer key, Integer value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setInt(2, value);
        }
    }


    /**
     * Map of integers to floats.
     */
    @SuppressWarnings("serial")
    public static class IntFloatMap extends SqlMap<Integer, Float>
    {
        /**
         * Creates an empty map.
         */
        public IntFloatMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public Float getValue(ResultSet result) throws SQLException
        {
            return result.getFloat(2);
        }

        @Override
        public void set(PreparedStatement statement, Integer key, Float value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setFloat(2, value);
        }
    }


    /**
     * Map of integers to strings.
     */
    @SuppressWarnings("serial")
    public static class IntStringMap extends SqlMap<Integer, String>
    {
        /**
         * Creates an empty map.
         */
        public IntStringMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public String getValue(ResultSet result) throws SQLException
        {
            return result.getString(2);
        }

        @Override
        public void set(PreparedStatement statement, Integer key, String value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setString(2, value);
        }
    }


    /**
     * Map of strings to integers.
     */
    @SuppressWarnings("serial")
    public static class StringIntMap extends SqlMap<String, Integer>
    {
        /**
         * Creates an empty map.
         */
        public StringIntMap()
        {
        }

        @Override
        public String getKey(ResultSet result) throws SQLException
        {
            return result.getString(1);
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(2);
        }

        @Override
        public void set(PreparedStatement statement, String key, Integer value) throws SQLException
        {
            statement.setString(1, key);
            statement.setInt(2, value);
        }
    }


    /**
     * Map of strings to strings.
     */
    @SuppressWarnings("serial")
    public static class StringStringMap extends SqlMap<String, String>
    {
        /**
         * Creates an empty map.
         */
        public StringStringMap()
        {
        }

        @Override
        public String getKey(ResultSet result) throws SQLException
        {
            return result.getString(1);
        }

        @Override
        public String getValue(ResultSet result) throws SQLException
        {
            return result.getString(2);
        }

        @Override
        public void set(PreparedStatement statement, String key, String value) throws SQLException
        {
            statement.setString(1, key);
            statement.setString(2, value);
        }
    }


    /**
     * Map of pairs of integers to integers.
     */
    @SuppressWarnings("serial")
    public static class IntPairIntMap extends SqlMap<Pair<Integer, Integer>, Integer>
    {
        /**
         * Creates an empty map.
         */
        public IntPairIntMap()
        {
        }

        @Override
        public Pair<Integer, Integer> getKey(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getInt(2));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(3);
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Integer> key, Integer value) throws SQLException
        {
            statement.setInt(1, key.getOne());
            statement.setInt(2, key.getTwo());
            statement.setInt(3, value);
        }
    }


    /**
     * Map of pairs of strings to integers.
     */
    @SuppressWarnings("serial")
    public static class StringPairIntMap extends SqlMap<Pair<String, String>, Integer>
    {
        /**
         * Creates an empty map.
         */
        public StringPairIntMap()
        {
        }

        @Override
        public Pair<String, String> getKey(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getString(1), result.getString(2));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(3);
        }

        @Override
        public void set(PreparedStatement statement, Pair<String, String> key, Integer value) throws SQLException
        {
            statement.setString(1, key.getOne());
            statement.setString(2, key.getTwo());
            statement.setInt(3, value);
        }
    }


    /**
     * Map of pairs of integers to strings.
     */
    @SuppressWarnings("serial")
    public static class IntPairStringMap extends SqlMap<Pair<Integer, Integer>, String>
    {
        /**
         * Creates an empty map.
         */
        public IntPairStringMap()
        {
        }

        @Override
        public Pair<Integer, Integer> getKey(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getInt(2));
        }

        @Override
        public String getValue(ResultSet result) throws SQLException
        {
            return result.getString(3);
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, Integer> key, String value) throws SQLException
        {
            statement.setInt(1, key.getOne());
            statement.setInt(2, key.getTwo());
            statement.setString(3, value);
        }
    }


    /**
     * Map of integers to pairs of a string and an integer.
     */
    @SuppressWarnings("serial")
    public static class IntStringIntPairMap extends SqlMap<Integer, Pair<String, Integer>>
    {
        /**
         * Creates an empty map.
         */
        public IntStringIntPairMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public Pair<String, Integer> getValue(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getString(2), result.getInt(3));
        }

        @Override
        public void set(PreparedStatement statement, Integer key, Pair<String, Integer> value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setString(2, value.getOne());
            statement.setInt(3, value.getTwo());
        }
    }


    /**
     * Map of integers to pairs of strings.
     */
    @SuppressWarnings("serial")
    public static class IntStringPairMap extends SqlMap<Integer, Pair<String, String>>
    {
        /**
         * Creates an empty map.
         */
        public IntStringPairMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public Pair<String, String> getValue(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getString(2), result.getString(3));
        }

        @Override
        public void set(PreparedStatement statement, Integer key, Pair<String, String> value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setString(2, value.getOne());
            statement.setString(3, value.getTwo());
        }
    }


    /**
     * Map of strings to pairs of a string and an integer.
     */
    @SuppressWarnings("serial")
    public static class StringStringIntPairMap extends SqlMap<String, Pair<String, Integer>>
    {
        /**
         * Creates an empty map.
         */
        public StringStringIntPairMap()
        {
        }

        @Override
        public String getKey(ResultSet result) throws SQLException
        {
            return result.getString(1);
        }

        @Override
        public Pair<String, Integer> getValue(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getString(2), result.getInt(3));
        }

        @Override
        public void set(PreparedStatement statement, String key, Pair<String, Integer> value) throws SQLException
        {
            statement.setString(1, key);
            statement.setString(2, value.getOne());
            statement.setInt(3, value.getTwo());
        }
    }


    /**
     * Map of pairs of an integer and a string to integers.
     */
    @SuppressWarnings("serial")
    public static class IntStringPairIntMap extends SqlMap<Pair<Integer, String>, Integer>
    {
        /**
         * Creates an empty map.
         */
        public IntStringPairIntMap()
        {
        }

        @Override
        public Pair<Integer, String> getKey(ResultSet result) throws SQLException
        {
            return Pair.getPair(result.getInt(1), result.getString(2));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(3);
        }

        @Override
        public void set(PreparedStatement statement, Pair<Integer, String> key, Integer value) throws SQLException
        {
            statement.setInt(1, key.getOne());
            statement.setString(2, key.getTwo());
            statement.setInt(3, value);
        }
    }


    /**
     * Map of integers to pairs of two pairs of integers.
     */
    @SuppressWarnings("serial")
    public static class IntIntPairIntPairPairMap
            extends SqlMap<Integer, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>
    {
        /**
         * Creates an empty map.
         */
        public IntIntPairIntPairPairMap()
        {
        }

        @Override
        public Integer getKey(ResultSet result) throws SQLException
        {
            return result.getInt(1);
        }

        @Override
        public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getValue(ResultSet result) throws SQLException
        {
            return Pair.getPair(Pair.getPair(result.getInt(2), result.getInt(3)),
                    Pair.getPair(result.getInt(4), result.getInt(5)));
        }

        @Override
        public void set(PreparedStatement statement, Integer key,
                Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> value) throws SQLException
        {
            statement.setInt(1, key);
            statement.setInt(2, value.getOne().getOne());
            statement.setInt(3, value.getOne().getTwo());
            statement.setInt(4, value.getTwo().getOne());
            statement.setInt(5, value.getTwo().getTwo());
        }
    }


    /**
     * Number of the commands sent to the database in one batch.
     */
    protected static final int batchSize = 100000;

    /**
     * Data directory, ending with a slash; the paths of the files are relative to it.
     */
    protected static String baseDirectory = null;

    /**
     * Connection to the database, in which the whole load runs as one transaction.
     */
    protected static Connection connection;

    /**
     * Normalized paths of the files that the loader has opened, see {@link #checkFiles(String, String...)}.
     */
    private static final Set<String> readFiles = ConcurrentHashMap.newKeySet();

    /**
     * Whether the summary of the problems has been printed.
     */
    private static boolean summarized = false;

    /**
     * Number of the commands of the current store, by which they are sent in batches.
     */
    private static int count;

    /**
     * Debugging switch: when set, the queries and the commands are only printed, not run.
     */
    private static final boolean dryRun = false;


    /**
     * Creates the updater.
     */
    protected Updater()
    {
    }


    /**
     * Adds the values that a query returns to a set.
     *
     * @param <T> the type of the values
     * @param query the query
     * @param set the set
     * @throws SQLException if a database error occurs
     */
    public static <T> void load(String query, SqlSet<T> set) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        if(dryRun)
            return;

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    set.add(set.get(result));
            }
        }

        System.out.println(
                " -> count: " + set.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
    }


    /**
     * Puts the keys and the values that a query returns into a map.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     * @param query the query
     * @param set the map
     * @throws SQLException if a database error occurs
     */
    public static <K, V> void load(String query, SqlMap<K, V> set) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        if(dryRun)
            return;

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    set.put(set.getKey(result), set.getValue(result));
            }
        }

        System.out.println(
                " -> count: " + set.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
    }


    /**
     * Runs a command for each value of a set, sent in batches.
     *
     * @param <T> the type of the values
     * @param command the command
     * @param set the set
     * @throws SQLException if a database error occurs
     */
    protected static <T> void store(String command, SqlSet<T> set) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + set.size());

        if(dryRun)
            return;

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                set.forEach(value -> {
                    try
                    {
                        set.set(statement, value);
                        statement.addBatch();

                        if(++count % batchSize == 0)
                            statement.executeBatch();
                    }
                    catch(SQLException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
            catch(RuntimeException e)
            {
                if(e.getCause() instanceof SQLException ex)
                    throw ex;

                throw e;
            }

            if(count % batchSize != 0)
                statement.executeBatch();
        }
    }


    /**
     * Runs a command for each key and value of a map, sent in batches.
     *
     * @param <K> the type of the keys
     * @param <V> the type of the values
     * @param command the command
     * @param set the map
     * @throws SQLException if a database error occurs
     */
    protected static <K, V> void store(String command, SqlMap<K, V> set) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + set.size());

        if(dryRun)
            return;

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                set.forEach((key, value) -> {
                    try
                    {
                        set.set(statement, key, value);
                        statement.addBatch();

                        if(++count % batchSize == 0)
                            statement.executeBatch();
                    }
                    catch(SQLException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
            catch(RuntimeException e)
            {
                if(e.getCause() instanceof SQLException ex)
                    throw ex;

                throw e;
            }

            if(count % batchSize != 0)
                statement.executeBatch();
        }
    }


    /**
     * Connects to the database given by datasource.properties. If the properties name a log directory (logs), the
     * output of the loader is copied into a log file in its subdirectory named by the loader, see {@link Log}.
     *
     * @param loader the name of the loader
     * @throws SQLException if the connection fails
     * @throws IOException if the properties cannot be read or the log file cannot be created
     */
    protected static void init(String loader) throws SQLException, IOException
    {
        // the limits of the XML parsers, which the defaults of the JDK set too low for the data, e.g. for the deep
        // lists of the ontologies and the large files of ChEBI and of the bioassays; a limit given on the command line
        // is kept
        for(String limit : List.of("jdk.xml.maxGeneralEntitySizeLimit", "jdk.xml.totalEntitySizeLimit",
                "jdk.xml.maxElementDepth", "jdk.xml.entityExpansionLimit"))
            if(System.getProperty(limit) == null)
                System.setProperty(limit, "0");

        Properties properties = new Properties();

        try(FileInputStream in = new FileInputStream("datasource.properties"))
        {
            properties.load(in);
        }

        String logs = properties.getProperty("logs");
        properties.remove("logs");

        if(logs != null && !logs.isEmpty())
            Log.start(Path.of(logs, loader));

        String url = properties.getProperty("url");
        properties.remove("url");

        // the data of a load are committed at once or not at all, so that incomplete data are never committed
        if(Boolean.parseBoolean(properties.getProperty("autoCommit")))
            throw new IllegalStateException("autoCommit is not supported");

        properties.remove("autoCommit");

        baseDirectory = properties.getProperty("base");
        properties.remove("base");

        if(!baseDirectory.endsWith("/"))
            baseDirectory += "/";

        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(false);
    }


    /**
     * Opens a file of the data directory and records that the loader reads it, see
     * {@link #checkFiles(String, String...)}. A missing file is reported as an error and null is returned, so that the
     * load goes on without its data.
     *
     * @param file the path of the file in the data directory
     * @return the stream of the file, or null if the file is missing
     * @throws IOException if the file cannot be opened
     */
    protected static InputStream openFile(String file) throws IOException
    {
        boolean first = readFiles.add(Path.of(file).normalize().toString());

        if(!Files.isRegularFile(Path.of(baseDirectory, file)))
        {
            if(first)
                Problems.error("no input file", file);

            return null;
        }

        return new FileInputStream(baseDirectory + file);
    }


    /**
     * Reports the files of the given directory of the data directory, including its subdirectories, that the loader has
     * not read as errors, as their data would be left out, e.g. a new part of a release. The files whose paths relative
     * to the directory match one of the given patterns are known not to be data of the loader.
     *
     * @param directory the path of the directory in the data directory
     * @param ignored the regular expressions of the relative paths of the files that are not data of the loader
     * @throws IOException if the directory cannot be listed
     */
    protected static void checkFiles(String directory, String... ignored) throws IOException
    {
        System.out.println("check files ...");

        Path start = Path.of(baseDirectory, directory);
        List<Path> paths = List.of();

        if(Files.isDirectory(start))
        {
            start = start.toRealPath();

            try(Stream<Path> stream = Files.walk(start))
            {
                paths = stream.filter(Files::isRegularFile).sorted().toList();
            }
        }

        for(Path path : paths)
        {
            String name = start.relativize(path).toString();
            String file = Path.of(directory, name).toString();

            if(!readFiles.contains(file) && Arrays.stream(ignored).noneMatch(name::matches))
                Problems.error("unknown file", file);
        }

        System.out.println();
    }


    /**
     * Opens a file of the data directory, such as a ZIP archive, as a buffered stream; a missing file, which is
     * reported, gives an empty stream.
     *
     * @param file the path of the file in the data directory
     * @return the stream of the file
     * @throws IOException if the file cannot be opened
     */
    protected static InputStream getZipStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = openFile(file);

        if(fis == null)
            return InputStream.nullInputStream();

        return new BufferedInputStream(fis);
    }


    /**
     * Opens a gzipped Turtle file of the data directory as a stream repaired by {@link InputStreamFixer}; a missing
     * file, which is reported, gives an empty stream.
     *
     * @param file the path of the file in the data directory
     * @return the stream of the uncompressed file
     * @throws IOException if the file cannot be opened
     */
    protected static InputStream getTtlStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = openFile(file);

        if(fis == null)
            return InputStream.nullInputStream();

        fis = new GZIPInputStream(fis, 65536);

        return new InputStreamFixer(new BufferedInputStream(fis));
    }


    /**
     * Opens a gzipped UTF-8 text file of the data directory as a reader; a missing file, which is reported, gives an
     * empty reader.
     *
     * @param file the path of the file in the data directory
     * @return the reader of the uncompressed file
     * @throws IOException if the file cannot be opened
     */
    protected static BufferedReader getReader(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = openFile(file);

        if(fis == null)
            return new BufferedReader(Reader.nullReader());

        GZIPInputStream gis = new GZIPInputStream(fis, 65536);
        InputStreamReader isr = new InputStreamReader(gis, Charset.forName("UTF-8"));
        return new BufferedReader(isr);
    }


    /**
     * Returns the names of the files, not of the subdirectories, of the directory that match the pattern; null stands
     * for a missing directory.
     *
     * @param path the path of the directory in the data directory
     * @param name the regular expression of the names of the files
     * @return the names of the files, or null for a missing directory
     */
    private static String[] listFiles(String path, String name)
    {
        return new File(baseDirectory + path).list((dir, file) -> file.matches(name) && new File(dir, file).isFile());
    }


    /**
     * Processes the files of the directory whose names match the pattern, several of them in parallel. No such file is
     * an error, as the data of the files would be missing.
     *
     * @param path the path of the directory in the data directory
     * @param name the regular expression of the names of the files
     * @param func the processing of a file
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    protected static void processFiles(String path, String name, FileNameSqlFunction func)
            throws IOException, SQLException
    {
        String[] files = listFiles(path, name);

        if(files == null || files.length == 0)
        {
            Problems.error("no input file", path + File.separatorChar + name);
            return;
        }

        try
        {
            Arrays.asList(files).parallelStream().forEach(file -> {
                try
                {
                    func.apply(path + File.separatorChar + file);
                }
                catch(IOException | SQLException e)
                {
                    throw new RuntimeException(e);
                }
            });
        }
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof SQLException)
                throw(SQLException) e.getCause();
            else if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else
                throw e;
        }
    }


    /**
     * Processes the XML files of the directory whose names match the pattern, several of them in parallel. No such file
     * is an error, as the data of the files would be missing.
     *
     * @param path the path of the directory in the data directory
     * @param name the regular expression of the names of the files
     * @param func the processing of a file
     * @throws IOException if an I/O error occurs
     * @throws XPathException if an XPath expression fails
     * @throws ParserConfigurationException if the XML parser cannot be created
     * @throws SAXException if the XML cannot be parsed
     * @throws SQLException if a database error occurs
     */
    protected static void processXmlFiles(String path, String name, FileNameXmlFunction func)
            throws IOException, XPathException, ParserConfigurationException, SAXException, SQLException
    {
        String[] files = listFiles(path, name);

        if(files == null || files.length == 0)
        {
            Problems.error("no input file", path + File.separatorChar + name);
            return;
        }

        try
        {
            Arrays.asList(files).parallelStream().forEach(file -> {
                try
                {
                    func.apply(path + File.separatorChar + file);
                }
                catch(IOException | XPathException | ParserConfigurationException | SAXException | SQLException e)
                {
                    throw new RuntimeException(e);
                }
            });
        }
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else if(e.getCause() instanceof XPathExpressionException)
                throw(XPathExpressionException) e.getCause();
            else if(e.getCause() instanceof ParserConfigurationException)
                throw(ParserConfigurationException) e.getCause();
            else if(e.getCause() instanceof SAXException)
                throw(SAXException) e.getCause();
            else
                throw e;
        }
    }


    /**
     * Sets the number of the entities of the statistics of the given name. A database without the table of the
     * statistics is left as it is, a missing statistics in it is an error.
     *
     * @param name the name of the statistics
     * @param count the number of the entities
     * @throws SQLException if a database error occurs
     */
    protected static void setCount(String name, int count) throws SQLException
    {
        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "idsm", "stats", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update idsm.stats set count=? where name=?"))
                {
                    statement.setInt(1, count);
                    statement.setString(2, name);

                    if(statement.executeUpdate() != 1)
                        Problems.error("count not set", name);
                }
            }
        }
    }


    /**
     * Sets the version of the given source; an unknown version is an error. A database without the table of the sources
     * is left as it is, a missing source in it is an error.
     *
     * @param name the name of the source
     * @param version the version, or null or empty if it is unknown
     * @throws SQLException if a database error occurs
     */
    protected static void setVersion(String name, String version) throws SQLException
    {
        if(version == null || version.isEmpty())
            Problems.error("unknown version", name);

        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "idsm", "sources", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update idsm.sources set version=? where name=?"))
                {
                    statement.setString(1, version == null ? "" : version);
                    statement.setString(2, name);

                    if(statement.executeUpdate() != 1)
                        Problems.error("version not set", name + ": " + version);
                }
            }
        }
    }


    /**
     * Moves the date of the version of the database to the current time, unless it is later. A database without the
     * table of the version is left as it is.
     *
     * @param connection the connection to the database
     * @throws SQLException if a database error occurs
     */
    public static void updateVersion(Connection connection) throws SQLException
    {
        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "idsm", "version", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(Statement statement = connection.createStatement())
                {
                    if(statement.executeUpdate(
                            "update idsm.version set date = greatest(date, date_trunc('second', now()))") != 1)
                        Problems.error("version date not set", null);
                }
            }
        }
    }


    /**
     * Moves the date of the version of the database to the current time, unless it is later, within the transaction of
     * the load.
     *
     * @throws SQLException if a database error occurs
     */
    public static void updateVersion() throws SQLException
    {
        updateVersion(connection);
    }


    /**
     * Updates the Sachem index of the given name according to the audited changes of its table: removes the stale
     * versions of the index and synchronises it. The cleanup has to precede the synchronisation, because after it the
     * cleanup would delete the previous version although the new one is not committed yet. Prints the size of the index
     * and reports the molecules that the indexer has rejected meanwhile as warnings. An index that is not registered
     * yet is left out: the first load fills its table without the audit trigger, and the index is registered and built
     * from the whole table after the load (sachem.sql of the dbfinish step).
     *
     * @param index the name of the index
     * @param optimize whether the synchronisation optimizes the index
     * @throws SQLException if a database error occurs
     */
    protected static void syncIndex(String index, boolean optimize) throws SQLException
    {
        if(Problems.hasErrors())
            throw new IllegalStateException("incomplete data cannot be indexed");

        if(dryRun)
            return;

        try(PreparedStatement statement = connection
                .prepareStatement("select 1 from sachem.configuration where index_name = ?"))
        {
            statement.setString(1, index);

            try(ResultSet result = statement.executeQuery())
            {
                if(!result.next())
                {
                    System.out.println("sachem index " + index + " not registered yet, it is built after the load");
                    System.out.println();
                    return;
                }
            }
        }

        System.out.println("sync sachem index " + index + " ...");

        long time = System.currentTimeMillis();
        int lastError = 0;

        try(PreparedStatement statement = connection.prepareStatement("select coalesce(max(e.id), 0) "
                + "from sachem.compound_errors e, sachem.configuration c where e.index = c.id and c.index_name = ?"))
        {
            statement.setString(1, index);

            try(ResultSet result = statement.executeQuery())
            {
                result.next();
                lastError = result.getInt(1);
            }
        }

        try(PreparedStatement statement = connection.prepareStatement("select sachem.cleanup(?)"))
        {
            statement.setString(1, index);
            statement.execute();
        }

        try(PreparedStatement statement = connection.prepareStatement("select sachem.sync_data(?, false, ?)"))
        {
            statement.setString(1, index);
            statement.setBoolean(2, optimize);
            statement.execute();
        }

        try(PreparedStatement statement = connection.prepareStatement("select sachem.index_size(?)"))
        {
            statement.setString(1, index);

            try(ResultSet result = statement.executeQuery())
            {
                result.next();
                System.out.println("  sachem.index_size('" + index + "') -> " + result.getInt(1) + " / time: "
                        + ((System.currentTimeMillis() - time) / 6000 / 10.0));
            }
        }

        try(PreparedStatement statement = connection.prepareStatement("""
                select e.compound, e.message \
                from sachem.compound_errors e, sachem.configuration c where e.index = c.id and c.index_name = ? \
                and e.id > ? order by e.id"""))
        {
            statement.setString(1, index);
            statement.setInt(2, lastError);

            try(ResultSet result = statement.executeQuery())
            {
                int errors = 0;

                for(; result.next(); errors++)
                    Problems.warning("structure refused by Sachem", result.getInt(1) + ": " + result.getString(2));

                if(errors > 0)
                    System.out.println("  sachem.compound_errors -> count: " + errors);
            }
        }

        System.out.println();
    }


    /**
     * Commits the loaded data; incomplete data, i.e. data with a reported error, are refused.
     *
     * @throws SQLException if a database error occurs
     */
    protected static void commit() throws SQLException
    {
        if(Problems.hasErrors())
            throw new IllegalStateException("incomplete data cannot be committed");

        if(dryRun)
            return;

        if(!connection.getAutoCommit())
            connection.commit();

        connection.close();
    }


    /**
     * Rolls the loaded data back and closes the connection; without a connection, nothing is done.
     *
     * @throws SQLException if a database error occurs
     */
    protected static void rollback() throws SQLException
    {
        if(dryRun || connection == null)
            return;

        if(!connection.getAutoCommit())
            connection.rollback();

        connection.close();
    }


    /**
     * Ends the reading of the data: prints the summary of the problems and, if the data are incomplete, fails the load
     * at once, so that they are neither indexed nor committed.
     */
    protected static void checkProblems()
    {
        summarized = true;
        Problems.printSummary();

        if(Problems.hasErrors())
            fail(null);
    }


    /**
     * Fails the load: prints the exception, if any, with the summary of the problems reported before it, rolls the data
     * back and exits with a non-zero status, so that the scripts running the loaders can tell the failure.
     *
     * @param exception the exception that has failed the load, or null
     */
    protected static void fail(Throwable exception)
    {
        if(exception != null)
        {
            exception.printStackTrace();

            if(!summarized && Problems.hasProblems())
                Problems.printSummary();
        }

        try
        {
            rollback();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }

        System.out.println("the load has failed, its data have been rolled back");
        System.exit(1);
    }


    /**
     * Obtains the exclusive lock of a file, released when the virtual machine exits, so that no other loader holding
     * the same lock runs at the same time.
     *
     * @param lockName the path of the lock file
     * @throws IOException if the lock is held by another process or the file cannot be opened
     */
    public static void lock(String lockName) throws IOException
    {
        File file = new File(lockName);
        @SuppressWarnings("resource")
        FileChannel channel = new FileOutputStream(file).getChannel();
        FileLock lock = channel.tryLock();

        if(lock == null)
            throw new IOException("cannot obtain lock file " + lockName);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try
            {
                lock.release();
                channel.close();
                file.delete();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }));
    }
}
