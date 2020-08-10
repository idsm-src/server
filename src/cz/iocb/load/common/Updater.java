package cz.iocb.load.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.eclipse.collections.api.collection.primitive.MutableIntCollection;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;
import org.eclipse.collections.api.tuple.primitive.ObjectIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntFloatHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import org.xml.sax.SAXException;



public class Updater
{
    protected static enum Direction
    {
        DIRECT, REVERSE
    }


    protected static interface SQLFunction<T, R>
    {
        R apply(T t) throws SQLException;
    }


    protected static interface SQLProcedure<T>
    {
        void apply(PreparedStatement s, T i) throws SQLException;
    }


    protected static interface SQLIntProcedure
    {
        void apply(PreparedStatement s, int i) throws SQLException;
    }


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
    protected static class IntPairSet extends HashSet<IntIntPair>
    {
        public IntPairSet(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntTripletSet extends HashSet<IntTriplet>
    {
        public IntTripletSet(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntQuaterpletSet extends HashSet<IntQuaterplet>
    {
        public IntQuaterpletSet(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntTripletFloatSet extends HashSet<ObjectFloatPair<IntTriplet>>
    {
        public IntTripletFloatSet(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntStringPairSet extends HashSet<IntObjectPair<String>>
    {
        public IntStringPairSet(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class StringPairSet extends HashSet<StringPair>
    {
        public StringPairSet(int capacity)
        {
            super(capacity);
        }
    }


    protected static class IntPairIntMap extends ObjectIntHashMap<IntIntPair>
    {
        public IntPairIntMap(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntPairStringMap extends HashMap<IntIntPair, String>
    {
        public IntPairStringMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class IntTripletIntMap extends ObjectIntHashMap<IntTriplet>
    {
        public IntTripletIntMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class IntTripletFloatMap extends ObjectFloatHashMap<IntTriplet>
    {
        public IntTripletFloatMap(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class IntTripletStringMap extends HashMap<IntTriplet, String>
    {
        public IntTripletStringMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class IntStringPairIntMap extends ObjectIntHashMap<IntObjectPair<String>>
    {
        public IntStringPairIntMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class IntStringMap extends IntObjectHashMap<String>
    {
        public IntStringMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class StringIntMap extends ObjectIntHashMap<String>
    {
        public StringIntMap(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class StringStringMap extends HashMap<String, String>
    {
        public StringStringMap(int capacity)
        {
            super(capacity);
        }
    }


    @SuppressWarnings("serial")
    protected static class StringStringIntPairMap extends HashMap<String, ObjectIntPair<String>>
    {
        public StringStringIntPairMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static class MD5IntMap extends ObjectIntHashMap<MD5>
    {
        public MD5IntMap(int capacity)
        {
            super(capacity);
        }
    }


    protected static final int NO_VALUE = Integer.MIN_VALUE;
    protected static final int batchSize = 100000;
    protected static String baseDirectory = null;
    protected static Connection connection;
    static String prefixes = null;
    private static int count;


    protected static void init() throws SQLException, IOException
    {
        prefixes = new String(Files.readAllBytes(Paths.get("query/prefixes.sparql")));

        Properties properties = new Properties();
        properties.load(new FileInputStream("datasource.properties"));

        String url = properties.getProperty("url");
        properties.remove("url");

        baseDirectory = properties.getProperty("base");
        properties.remove("base");

        if(!baseDirectory.endsWith("/"))
            baseDirectory += "/";

        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(false);
    }


    protected static InputStream getStream(String file, boolean gzipped) throws FileNotFoundException, IOException
    {
        System.out.println("  load " + file);

        InputStream fis = new FileInputStream(baseDirectory + file);

        if(gzipped)
            fis = new GZIPInputStream(fis, 65536);

        return new BufferedInputStream(fis);
    }


    protected static InputStream getStream(String file) throws IOException
    {
        return getStream(file, true);
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
            ResultSet results = qexec.execSelect();
            while(results.hasNext())
            {
                QuerySolution solution = results.nextSolution();
                Resource iri = solution.getResource("iri");

                System.out.println("  missing " + iri);
            }
        }
    }


    protected static IntHashSet getIntSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntHashSet set = new IntHashSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    set.add(result.getInt(1));
            }
        }

        System.out.println(
                " -> count: " + set.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return set;
    }


    protected static IntPairSet getIntPairSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntPairSet values = new IntPairSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(PrimitiveTuples.pair(result.getInt(1), result.getInt(2)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntTripletSet getIntTripletSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntTripletSet values = new IntTripletSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(new IntTriplet(result.getInt(1), result.getInt(2), result.getInt(3)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntQuaterpletSet getIntQuaterpletSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntQuaterpletSet values = new IntQuaterpletSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(
                            new IntQuaterplet(result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntTripletFloatSet getIntTripletFloatSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntTripletFloatSet values = new IntTripletFloatSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(PrimitiveTuples.pair(
                            new IntTriplet(result.getInt(1), result.getInt(2), result.getInt(3)), result.getFloat(4)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntStringPairSet getIntStringPairSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntStringPairSet values = new IntStringPairSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(PrimitiveTuples.pair(result.getInt(1), result.getString(2)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static StringPairSet getStringPairSet(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        StringPairSet values = new StringPairSet(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.add(new StringPair(result.getString(1), result.getString(2)));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntIntHashMap getIntIntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntIntHashMap map = new IntIntHashMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getInt(1), result.getInt(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntFloatHashMap getIntFloatMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntFloatHashMap map = new IntFloatHashMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getInt(1), result.getFloat(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntPairIntMap getIntPairIntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntPairIntMap values = new IntPairIntMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.put(PrimitiveTuples.pair(result.getInt(1), result.getInt(2)), result.getInt(3));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }


    protected static IntPairStringMap getIntPairStringMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntPairStringMap map = new IntPairStringMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(PrimitiveTuples.pair(result.getInt(1), result.getInt(2)), result.getString(3));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntTripletIntMap getIntTripletIntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntTripletIntMap map = new IntTripletIntMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(new IntTriplet(result.getInt(1), result.getInt(2), result.getInt(3)), result.getInt(4));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntTripletFloatMap getIntTripletFloatMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntTripletFloatMap map = new IntTripletFloatMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(new IntTriplet(result.getInt(1), result.getInt(2), result.getInt(3)), result.getFloat(4));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntTripletStringMap getIntTripletStringMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntTripletStringMap map = new IntTripletStringMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(new IntTriplet(result.getInt(1), result.getInt(2), result.getInt(3)), result.getString(4));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static IntStringPairIntMap getIntStringPairIntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntStringPairIntMap values = new IntStringPairIntMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    values.put(PrimitiveTuples.pair(result.getInt(1), result.getString(2)), result.getInt(3));
            }
        }

        System.out.println(
                " -> count: " + values.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return values;
    }

    protected static IntStringMap getIntStringMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        IntStringMap map = new IntStringMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getInt(1), result.getString(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static StringIntMap getStringIntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        StringIntMap map = new StringIntMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getString(1), result.getInt(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static StringStringMap getStringStringMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        StringStringMap map = new StringStringMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getString(1), result.getString(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static StringStringIntPairMap getStringStringIntPairMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        StringStringIntPairMap map = new StringStringIntPairMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(result.getString(1), PrimitiveTuples.pair(result.getString(2), result.getInt(3)));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static MD5IntMap getMD5IntMap(String query, int capacity) throws SQLException
    {
        long time = System.currentTimeMillis();
        System.out.print("  " + query);

        MD5IntMap map = new MD5IntMap(capacity);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            statement.setFetchSize(1000000);

            try(java.sql.ResultSet result = statement.executeQuery())
            {
                while(result.next())
                    map.put(new MD5(result.getString(1)), result.getInt(2));
            }
        }

        System.out.println(
                " -> count: " + map.size() + " / time: " + ((System.currentTimeMillis() - time) / 6000 / 10.0));
        return map;
    }


    protected static int getIntValue(String query) throws SQLException
    {
        System.out.print("  " + query);

        try(PreparedStatement statement = connection.prepareStatement(query))
        {
            try(java.sql.ResultSet result = statement.executeQuery())
            {
                result.next();
                int value = result.getInt(1);
                System.out.println(" -> value: " + value);
                return value;
            }
        }
    }


    protected static void batch(String command, MutableIntCollection set) throws SQLException
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
                        statement.setInt(1, value);
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


    protected static void batch(String command, IntPairSet set) throws SQLException
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
                        statement.setInt(1, value.getOne());
                        statement.setInt(2, value.getTwo());
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


    protected static void batch(String command, IntTripletSet set) throws SQLException
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
                        statement.setInt(1, value.getOne());
                        statement.setInt(2, value.getTwo());
                        statement.setInt(3, value.getThree());
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


    protected static void batch(String command, IntQuaterpletSet set) throws SQLException
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
                        statement.setInt(1, value.getOne());
                        statement.setInt(2, value.getTwo());
                        statement.setInt(3, value.getThree());
                        statement.setInt(4, value.getFour());
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


    protected static void batch(String command, IntTripletFloatSet set) throws SQLException
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
                        statement.setInt(1, value.getOne().getOne());
                        statement.setInt(2, value.getOne().getTwo());
                        statement.setInt(3, value.getOne().getThree());
                        statement.setFloat(4, value.getTwo());
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


    protected static void batch(String command, IntStringPairSet set) throws SQLException
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
                        statement.setInt(1, value.getOne());
                        statement.setString(2, value.getTwo());
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


    protected static void batch(String command, StringPairSet set) throws SQLException
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
                        statement.setString(1, value.getOne());
                        statement.setString(2, value.getTwo());
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


    protected static void batch(String command, IntHashSet set, SQLIntProcedure procedure) throws SQLException
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
                        procedure.apply(statement, value);
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


    protected static <T> void batch(String command, Set<T> set, SQLProcedure<T> procedure) throws SQLException
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
                        procedure.apply(statement, value);
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


    protected static void batch(String command, IntIntHashMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key);
                        statement.setInt(direction == Direction.REVERSE ? 1 : 2, value);
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


    protected static void batch(String command, IntFloatHashMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key);
                        statement.setFloat(direction == Direction.REVERSE ? 1 : 2, value);
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


    protected static void batch(String command, IntPairIntMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key.getOne());
                        statement.setInt(direction == Direction.REVERSE ? 3 : 2, key.getTwo());
                        statement.setInt(direction == Direction.REVERSE ? 1 : 3, value);
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


    protected static void batch(String command, IntPairStringMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEach((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key.getOne());
                        statement.setInt(direction == Direction.REVERSE ? 3 : 2, key.getTwo());
                        statement.setString(direction == Direction.REVERSE ? 1 : 3, value);
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


    protected static void batch(String command, IntTripletIntMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key.getOne());
                        statement.setInt(direction == Direction.REVERSE ? 3 : 2, key.getTwo());
                        statement.setInt(direction == Direction.REVERSE ? 4 : 3, key.getThree());
                        statement.setInt(direction == Direction.REVERSE ? 1 : 4, value);
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


    protected static void batch(String command, IntTripletFloatMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key.getOne());
                        statement.setInt(direction == Direction.REVERSE ? 3 : 2, key.getTwo());
                        statement.setInt(direction == Direction.REVERSE ? 4 : 3, key.getThree());
                        statement.setFloat(direction == Direction.REVERSE ? 1 : 4, value);
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


    protected static void batch(String command, IntTripletStringMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEach((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key.getOne());
                        statement.setInt(direction == Direction.REVERSE ? 3 : 2, key.getTwo());
                        statement.setInt(direction == Direction.REVERSE ? 4 : 3, key.getThree());
                        statement.setString(direction == Direction.REVERSE ? 1 : 4, value);
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


    protected static void batch(String command, IntStringMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(direction == Direction.REVERSE ? 2 : 1, key);
                        statement.setString(direction == Direction.REVERSE ? 1 : 2, value);
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


    protected static void batch(String command, StringIntMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setString(direction == Direction.REVERSE ? 2 : 1, key);
                        statement.setInt(direction == Direction.REVERSE ? 1 : 2, value);
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


    protected static void batch(String command, StringStringMap map, Direction direction) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEach((key, value) -> {
                    try
                    {
                        statement.setString(direction == Direction.REVERSE ? 2 : 1, key);
                        statement.setString(direction == Direction.REVERSE ? 1 : 2, value);
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


    protected static void batch(String command, IntStringPairIntMap map) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setInt(1, key.getOne());
                        statement.setString(2, key.getTwo());
                        statement.setInt(3, value);
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


    protected static void batch(String command, StringStringIntPairMap map) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEach((key, value) -> {
                    try
                    {
                        statement.setString(1, key);
                        statement.setString(2, value.getOne());
                        statement.setInt(3, value.getTwo());
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


    protected static void batch(String command, MD5IntMap map) throws SQLException
    {
        System.out.println("  " + command + " -> count: " + map.size());

        try(PreparedStatement statement = connection.prepareStatement(command))
        {
            count = 0;

            try
            {
                map.forEachKeyValue((key, value) -> {
                    try
                    {
                        statement.setString(1, key.toString());
                        statement.setInt(2, value);
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


    protected static void batch(String command, IntIntHashMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }


    protected static void batch(String command, IntFloatHashMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }


    protected static void batch(String command, IntStringMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }


    protected static void batch(String command, IntPairIntMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }

    protected static void batch(String command, IntPairStringMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }


    protected static void batch(String command, StringIntMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
    }


    protected static void batch(String command, StringStringMap map) throws SQLException
    {
        batch(command, map, Direction.DIRECT);
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


    protected static void commit() throws SQLException
    {
        connection.commit();
    }


    protected static void rollback() throws SQLException
    {
        if(connection != null)
            connection.rollback();
    }
}
