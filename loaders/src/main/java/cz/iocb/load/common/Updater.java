package cz.iocb.load.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
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
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;



public class Updater
{
    protected static interface FileNameSqlFunction
    {
        void apply(String file) throws IOException, SQLException;
    }


    protected static interface FileNameXmlFunction
    {
        void apply(String file)
                throws IOException, XPathException, ParserConfigurationException, SAXException, SQLException;
    }


    @SuppressWarnings("serial")
    public static abstract class SqlSet<T> extends HashSet<T>
    {
        public abstract T get(ResultSet result) throws SQLException;

        public abstract void set(PreparedStatement statement, T value) throws SQLException;
    }


    @SuppressWarnings("serial")
    public static abstract class SqlMap<K, V> extends HashMap<K, V>
    {
        public abstract K getKey(ResultSet result) throws SQLException;

        public abstract V getValue(ResultSet result) throws SQLException;

        public abstract void set(PreparedStatement statement, K key, V value) throws SQLException;
    }


    @SuppressWarnings("serial")
    public static class IntSet extends SqlSet<Integer>
    {
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


    @SuppressWarnings("serial")
    public static class StringSet extends SqlSet<String>
    {
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


    @SuppressWarnings("serial")
    public static class IntPairSet extends SqlSet<Pair<Integer, Integer>>
    {
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


    @SuppressWarnings("serial")
    public static class IntStringSet extends SqlSet<Pair<Integer, String>>
    {
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


    @SuppressWarnings("serial")
    public static class IntFloatSet extends SqlSet<Pair<Integer, Float>>
    {
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


    @SuppressWarnings("serial")
    public static class StringPairSet extends SqlSet<Pair<String, String>>
    {
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


    @SuppressWarnings("serial")
    public static class IntIntPairSet extends SqlSet<Pair<Integer, Pair<Integer, Integer>>>
    {
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


    @SuppressWarnings("serial")
    public static class IntFloatIntPairSet extends SqlSet<Pair<Integer, Pair<Float, Integer>>>
    {
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


    @SuppressWarnings("serial")
    public static class IntFloatPairIntPairSet extends SqlSet<Pair<Integer, Pair<Pair<Float, Float>, Integer>>>
    {
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


    @SuppressWarnings("serial")
    public static class IntPairIntSet extends SqlSet<Pair<Pair<Integer, Integer>, Integer>>
    {
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


    @SuppressWarnings("serial")
    public static class IntPairIntPairSet extends SqlSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>
    {
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


    @SuppressWarnings("serial")
    public static class IntIntMap extends SqlMap<Integer, Integer>
    {
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


    @SuppressWarnings("serial")
    public static class IntFloatMap extends SqlMap<Integer, Float>
    {
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


    @SuppressWarnings("serial")
    public static class IntStringMap extends SqlMap<Integer, String>
    {
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


    @SuppressWarnings("serial")
    public static class StringIntMap extends SqlMap<String, Integer>
    {
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


    @SuppressWarnings("serial")
    public static class StringStringMap extends SqlMap<String, String>
    {
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


    @SuppressWarnings("serial")
    public static class IntPairIntMap extends SqlMap<Pair<Integer, Integer>, Integer>
    {
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


    @SuppressWarnings("serial")
    public static class StringPairIntMap extends SqlMap<Pair<String, String>, Integer>
    {
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


    @SuppressWarnings("serial")
    public static class IntPairStringMap extends SqlMap<Pair<Integer, Integer>, String>
    {
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


    @SuppressWarnings("serial")
    public static class IntStringIntPairMap extends SqlMap<Integer, Pair<String, Integer>>
    {
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


    @SuppressWarnings("serial")
    public static class IntStringPairMap extends SqlMap<Integer, Pair<String, String>>
    {
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


    @SuppressWarnings("serial")
    public static class StringStringIntPairMap extends SqlMap<String, Pair<String, Integer>>
    {
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


    @SuppressWarnings("serial")
    public static class IntStringPairIntMap extends SqlMap<Pair<Integer, String>, Integer>
    {
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


    @SuppressWarnings("serial")
    public static class IntIntPairIntPairPairMap
            extends SqlMap<Integer, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>
    {
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


    protected static final int batchSize = 100000;
    protected static String baseDirectory = null;
    protected static Connection connection;
    private static int count;
    private static final boolean dryRun = false;


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


    protected static void init() throws SQLException, IOException
    {
        Properties properties = new Properties();

        try(FileInputStream in = new FileInputStream("datasource.properties"))
        {
            properties.load(in);
        }

        String url = properties.getProperty("url");
        properties.remove("url");

        boolean autoCommit = Boolean.valueOf(properties.getProperty("autoCommit"));
        properties.remove("autoCommit");

        baseDirectory = properties.getProperty("base");
        properties.remove("base");

        if(!baseDirectory.endsWith("/"))
            baseDirectory += "/";

        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(autoCommit);
    }


    protected static InputStream getZipStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = new FileInputStream(baseDirectory + file);
        return new BufferedInputStream(fis);
    }


    protected static InputStream getTtlStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = new FileInputStream(baseDirectory + file);

        fis = new GZIPInputStream(fis, 65536);

        return new InputStreamFixer(new BufferedInputStream(fis));
    }


    protected static BufferedReader getReader(String file) throws IOException
    {
        System.out.println("  load " + file);

        FileInputStream fis = new FileInputStream(baseDirectory + file);
        GZIPInputStream gis = new GZIPInputStream(fis, 65536);
        InputStreamReader isr = new InputStreamReader(gis, Charset.forName("UTF-8"));
        return new BufferedReader(isr);
    }


    protected static void processFiles(String path, String name, FileNameSqlFunction func)
            throws IOException, SQLException
    {
        String[] files = new File(baseDirectory + path).list((dir, file) -> file.matches(name));

        if(files.length == 0)
            System.out.println("  warning: file list " + name + " in " + path + " is empty");

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


    protected static void processXmlFiles(String path, String name, FileNameXmlFunction func)
            throws IOException, XPathException, ParserConfigurationException, SAXException, SQLException
    {
        String[] files = new File(baseDirectory + path).list((dir, file) -> file.matches(name));

        if(files.length == 0)
            throw new IOException("file list is empty");

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


    protected static void setCount(String name, int count) throws SQLException
    {
        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "stats", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update info.stats set count=? where name=?"))
                {
                    statement.setInt(1, count);
                    statement.setString(2, name);

                    if(statement.executeUpdate() != 1)
                        System.err.printf("warning: number of '%s' was not set", name);
                }
            }
        }
    }


    protected static void setVersion(String name, String version) throws SQLException
    {
        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "sources", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update info.sources set version=? where name=?"))
                {
                    statement.setString(1, version == null ? "" : version);
                    statement.setString(2, name);

                    if(statement.executeUpdate() != 1)
                        System.err.printf("warning: version '%s' of source '%s' was not set\n", version, name);
                }
            }
        }
    }


    public static void updateVersion(Connection connection) throws SQLException
    {
        if(dryRun)
            return;

        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "version", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(Statement statement = connection.createStatement())
                {
                    if(statement.executeUpdate(
                            "update info.version set date = greatest(date, date_trunc('second', now()))") != 1)
                        System.err.printf("warning: version was not set\n");
                }
            }
        }
    }


    public static void updateVersion() throws SQLException
    {
        updateVersion(connection);
    }


    /*
     * Updates the Sachem index of the given name according to the audited changes of its table: removes the stale
     * versions of the index and synchronises it. The cleanup has to precede the synchronisation, because after it the
     * cleanup would delete the previous version although the new one is not committed yet. Prints the size of the
     * index and the molecules that the indexer has rejected meanwhile.
     */
    protected static void syncIndex(String index, boolean optimize) throws SQLException
    {
        if(dryRun)
            return;

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

                while(result.next())
                    if(++errors <= 10)
                        System.out.println("  error: " + result.getInt(1) + ": " + result.getString(2));

                if(errors > 0)
                    System.out.println("  sachem.compound_errors -> count: " + errors);
            }
        }

        System.out.println();
    }


    protected static void commit() throws SQLException
    {
        if(dryRun)
            return;

        if(!connection.getAutoCommit())
            connection.commit();

        connection.close();
    }


    protected static void rollback() throws SQLException
    {
        if(dryRun)
            return;

        if(connection != null && !connection.getAutoCommit())
            connection.rollback();

        connection.close();
    }


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
