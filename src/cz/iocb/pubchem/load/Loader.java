package cz.iocb.pubchem.load;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;



public class Loader
{
    abstract protected static class TableLoader
    {
        protected QuerySolution solution = null;
        protected PreparedStatement statement = null;
        protected boolean set;


        public TableLoader(Model model, String sparql, String sql) throws SQLException, IOException
        {
            try (Connection connection = getConnection())
            {
                try (PreparedStatement insertStatement = connection.prepareStatement(sql))
                {
                    statement = insertStatement;
                    int count = 0;

                    Query query = QueryFactory.create(getPrefixes() + sparql);

                    try (QueryExecution qexec = QueryExecutionFactory.create(query, model))
                    {
                        ResultSet results = qexec.execSelect();
                        while(results.hasNext())
                        {
                            solution = results.nextSolution();
                            set = false;

                            insert();

                            if(!set)
                                continue;


                            insertStatement.addBatch();

                            if(++count % batchSize == 0)
                                insertStatement.executeBatch();
                        }
                    }

                    if(count % batchSize != 0)
                        insertStatement.executeBatch();
                }
            }
        }


        public abstract void insert() throws SQLException, IOException;


        protected String getIRI(String name)
        {
            Resource resource = solution.getResource(name);

            if(resource == null)
                return null;

            return resource.getURI();
        }


        protected Integer getIntID(String name, String prefix) throws IOException
        {
            Resource resource = solution.getResource(name);

            if(resource == null)
                return null;

            String value = resource.getURI();

            if(!value.startsWith(prefix))
                throw new IOException();

            return Integer.parseInt(value.substring(prefix.length()));
        }


        protected Short getMapID(String name, Map<String, Short> map)
        {
            Resource resource = solution.getResource(name);

            if(resource == null)
                return null;

            return map.get(resource.getURI());
        }


        protected String getLiteralValue(String name)
        {
            Literal literal = solution.getLiteral(name);

            if(literal == null)
                return null;

            return literal.getString();
        }


        protected void setValue(int idx, Integer value) throws SQLException
        {
            set = true;

            if(value != null)
                statement.setInt(idx, value);
            else
                statement.setNull(idx, Types.INTEGER);
        }


        protected void setValue(int idx, Short value) throws SQLException
        {
            set = true;

            if(value != null)
                statement.setInt(idx, value);
            else
                statement.setNull(idx, Types.SMALLINT);
        }


        protected void setValue(int idx, String value) throws SQLException
        {
            set = true;

            statement.setString(idx, value);
        }
    }


    protected static final int batchSize = 10000;
    private static Properties properties = null;
    private static VirtuosoConnectionPoolDataSource connectionPool = null;
    private static String prefixes = null;
    private static String pubchemDirectory = null;


    protected static Properties getProperties() throws IOException
    {
        if(properties != null)
            return properties;

        synchronized(Loader.class)
        {
            if(properties != null)
                return properties;

            Properties prop = new Properties();
            prop.load(new FileInputStream("datasource.properties"));

            properties = prop;
            return properties;
        }
    }


    protected static VirtuosoConnectionPoolDataSource getConnectionPool() throws SQLException, IOException
    {
        if(connectionPool != null)
            return connectionPool;

        synchronized(Loader.class)
        {
            if(connectionPool != null)
                return connectionPool;

            Properties properties = getProperties();
            VirtuosoConnectionPoolDataSource pool = new VirtuosoConnectionPoolDataSource();
            pool.setCharset("UTF-8");
            pool.setPortNumber(Integer.parseInt(properties.getProperty("port")));
            pool.setUser(properties.getProperty("username"));
            pool.setPassword(properties.getProperty("password"));
            pool.setMaxPoolSize(new Integer(properties.getProperty("maxPoolSize")));

            connectionPool = pool;
            return connectionPool;
        }
    }


    protected static synchronized Connection getConnection() throws SQLException, IOException
    {
        return getConnectionPool().getConnection();
    }


    protected static String getPubchemDirectory() throws IOException
    {
        if(pubchemDirectory != null)
            return pubchemDirectory;

        synchronized(Loader.class)
        {
            if(pubchemDirectory != null)
                return pubchemDirectory;

            String directory = getProperties().getProperty("pubchem");

            if(!directory.endsWith("/"))
                directory += "/";

            pubchemDirectory = directory;
            return pubchemDirectory;
        }
    }


    protected static String getPrefixes() throws IOException
    {
        if(prefixes != null)
            return prefixes;

        synchronized(Loader.class)
        {
            if(prefixes != null)
                return prefixes;

            prefixes = new String(Files.readAllBytes(Paths.get("query/prefixes.sparql")));

            return prefixes;
        }
    }


    protected static Model loadModel(String file) throws IOException
    {
        System.out.println("load " + file);

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(getPubchemDirectory() + file);
        model.read(in, null, "TTL");

        return model;
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


    public static void check(Model model, String file) throws IOException, SQLException
    {
        String sparql = loadQuery(file);
        Query query = QueryFactory.create(getPrefixes() + sparql);

        try (QueryExecution qexec = QueryExecutionFactory.create(query, model))
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


    protected static Map<String, Short> getMapping(String table) throws SQLException, IOException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        try (Connection connection = getConnection())
        {
            try (PreparedStatement statement = connection.prepareStatement("select id, iri from " + table))
            {
                try (java.sql.ResultSet result = statement.executeQuery())
                {
                    while(result.next())
                    {
                        Short id = result.getShort(1);
                        String iri = result.getString(2);

                        map.put(iri, id);
                    }
                }
            }
        }

        return map;
    }
}
