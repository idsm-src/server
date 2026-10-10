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
    private static final Set<String> readFiles = ConcurrentHashMap.newKeySet();
    private static boolean summarized = false;
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


    /*
     * Opens a file of the data directory and records that the loader reads it, see checkFiles(). A missing file is
     * reported as an error and null is returned, so that the load goes on without its data.
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


    /*
     * Reports the files of the given directory of the data directory, including its subdirectories, that the loader
     * has not read as errors, as their data would be left out, e.g. a new part of a release. The files whose paths
     * relative to the directory match one of the given patterns are known not to be data of the loader.
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


    protected static InputStream getZipStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = openFile(file);

        if(fis == null)
            return InputStream.nullInputStream();

        return new BufferedInputStream(fis);
    }


    protected static InputStream getTtlStream(String file) throws IOException
    {
        System.out.println("  load " + file);

        InputStream fis = openFile(file);

        if(fis == null)
            return InputStream.nullInputStream();

        fis = new GZIPInputStream(fis, 65536);

        return new InputStreamFixer(new BufferedInputStream(fis));
    }


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


    /*
     * Returns the names of the files, not of the subdirectories, of the directory that match the pattern; null stands
     * for a missing directory.
     */
    private static String[] listFiles(String path, String name)
    {
        return new File(baseDirectory + path).list((dir, file) -> file.matches(name) && new File(dir, file).isFile());
    }


    /*
     * Processes the files of the directory whose names match the pattern, several of them in parallel. No such file
     * is an error, as the data of the files would be missing.
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


    /*
     * Sets the number of the entities of the statistics of the given name. A database without the table of the
     * statistics is left as it is, a missing statistics in it is an error.
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


    /*
     * Sets the version of the given source; an unknown version is an error. A database without the table of the
     * sources is left as it is, a missing source in it is an error.
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


    public static void updateVersion() throws SQLException
    {
        updateVersion(connection);
    }


    /*
     * Updates the Sachem index of the given name according to the audited changes of its table: removes the stale
     * versions of the index and synchronises it. The cleanup has to precede the synchronisation, because after it the
     * cleanup would delete the previous version although the new one is not committed yet. Prints the size of the
     * index and reports the molecules that the indexer has rejected meanwhile as warnings.
     */
    protected static void syncIndex(String index, boolean optimize) throws SQLException
    {
        if(Problems.hasErrors())
            throw new IllegalStateException("incomplete data cannot be indexed");

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

                for(; result.next(); errors++)
                    Problems.warning("structure refused by Sachem", result.getInt(1) + ": " + result.getString(2));

                if(errors > 0)
                    System.out.println("  sachem.compound_errors -> count: " + errors);
            }
        }

        System.out.println();
    }


    /*
     * Commits the loaded data; incomplete data, i.e. data with a reported error, are refused.
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


    protected static void rollback() throws SQLException
    {
        if(dryRun || connection == null)
            return;

        if(!connection.getAutoCommit())
            connection.rollback();

        connection.close();
    }


    /*
     * Ends the reading of the data: prints the summary of the problems and, if the data are incomplete, fails the
     * load at once, so that they are neither indexed nor committed.
     */
    protected static void checkProblems()
    {
        summarized = true;
        Problems.printSummary();

        if(Problems.hasErrors())
            fail(null);
    }


    /*
     * Fails the load: prints the exception, if any, with the summary of the problems reported before it, rolls the
     * data back and exits with a non-zero status, so that the scripts running the loaders can tell the failure.
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
