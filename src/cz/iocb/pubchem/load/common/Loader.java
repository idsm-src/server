package cz.iocb.pubchem.load.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
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
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;



public class Loader
{
    protected static final int batchSize = 1000;
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
            pool.setLog_Enable(2);

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


    protected static InputStream getStream(String file, boolean gzipped) throws FileNotFoundException, IOException
    {
        System.out.println("load " + file);

        InputStream fis = new FileInputStream(getPubchemDirectory() + file);

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
        System.out.println("load " + file);

        FileInputStream fis = new FileInputStream(getPubchemDirectory() + file);
        GZIPInputStream gis = new GZIPInputStream(fis, 65536);
        InputStreamReader isr = new InputStreamReader(gis, Charset.forName("UTF-8"));
        return new BufferedReader(isr);
    }


    protected static Model getModel(String file, String lang) throws IOException
    {
        System.out.println("load " + file);

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(getPubchemDirectory() + file);
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


    protected static String generateSourceTitle(String iri)
    {
        String base = iri.replaceFirst("^http://rdf.ncbi.nlm.nih.gov/pubchem/source/", "");

        if(base.startsWith("ID"))
            return base.substring(2);
        else
            return base.replace('_', ' ');
    }
}
