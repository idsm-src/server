package cz.iocb.load.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
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
    protected static String prefixes = null;
    private static int count;


    public static <T> void load(String query, SqlSet<T> set) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

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
                if(e.getCause() instanceof SQLException)
                    throw(SQLException) e.getCause();
            }

            if(count % batchSize != 0)
                statement.executeBatch();
        }
    }


    protected static <K, V> void store(String command, SqlMap<K, V> set) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + set.size());

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
                if(e.getCause() instanceof SQLException)
                    throw(SQLException) e.getCause();
            }

            if(count % batchSize != 0)
                statement.executeBatch();
        }
    }


    protected static void init() throws SQLException, IOException
    {
        prefixes = new String(Files.readAllBytes(Paths.get("query/prefixes.sparql")));

        Properties properties = new Properties();
        properties.load(new FileInputStream("config/datasource.properties"));

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


    protected static Model getModel(String file, String lang) throws IOException
    {
        System.out.println("  load " + file);

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(baseDirectory + file);

        if("TTL".equals(lang))
            in = new InputStreamFixer(in);

        return model.read(in, null, lang);
    }


    protected static Model getModel(String file) throws IOException
    {
        return getModel(file, "TTL");
    }


    protected static String loadQuery(String path) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get("query/" + path));
        return new String(encoded, "ASCII");
    }


    protected static String patternQuery(String pattern)
    {
        return "select * where { " + pattern + " }";
    }


    protected static String distinctPatternQuery(String pattern)
    {
        return "select distinct * where { " + pattern + " }";
    }


    protected static void check(Model model, String file) throws IOException, SQLException
    {
        String sparql = loadQuery(file);
        Query query = QueryFactory.create(prefixes + sparql);

        try(QueryExecution qexec = QueryExecutionFactory.create(query, model))
        {
            org.apache.jena.query.ResultSet results = qexec.execSelect();
            while(results.hasNext())
            {
                QuerySolution solution = results.nextSolution();
                Resource iri = solution.getResource("iri");

                System.out.println("    missing " + iri);
            }
        }
    }


    protected static void processFiles(String path, String name, FileNameSqlFunction func)
            throws IOException, SQLException
    {
        String[] files = new File(baseDirectory + path).list((dir, file) -> file.matches(name));

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
        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "idsm_counts", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update info.idsm_counts set count=? where name=?"))
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
        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "idsm_sources", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(PreparedStatement statement = connection
                        .prepareStatement("update info.idsm_sources set version=? where name=?"))
                {
                    statement.setString(1, version);
                    statement.setString(2, name);

                    if(statement.executeUpdate() != 1)
                        System.err.printf("warning: version '%s' of source '%s' was not set\n", version, name);
                }
            }
        }
    }


    public static void updateVersion(Connection connection) throws SQLException
    {
        DatabaseMetaData databaseMetaData = connection.getMetaData();

        try(ResultSet info = databaseMetaData.getTables(null, "info", "idsm_version", new String[] { "TABLE" }))
        {
            if(info.next())
            {
                try(Statement statement = connection.createStatement())
                {
                    if(statement.executeUpdate(
                            "update info.idsm_version set date = greatest(date, date_trunc('second', now()))") != 1)
                        System.err.printf("warning: version was not set\n");
                }
            }
        }
    }


    public static void updateVersion() throws SQLException
    {
        updateVersion(connection);
    }


    protected static void commit() throws SQLException
    {
        if(!connection.getAutoCommit())
            connection.commit();
    }


    protected static void rollback() throws SQLException
    {
        if(connection != null && !connection.getAutoCommit())
            connection.rollback();
    }
}
