package cz.iocb.pubchem.load;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;



public class Bioassay
{
    private static class DataItemMain
    {
        int bioassay;
        short source;
        String title;

        public DataItemMain(int bioassay, short source, String title)
        {
            this.bioassay = bioassay;
            this.source = source;
            this.title = title;
        }
    }


    private static class DataItemExtra
    {
        int bioassay;
        short type;
        String value;

        public DataItemExtra(int bioassay, short type, String value)
        {
            this.bioassay = bioassay;
            this.type = type;
            this.value = value;
        }
    }


    private static class ResultItem
    {
        ArrayList<DataItemMain> listMain = new ArrayList<DataItemMain>();
        ArrayList<DataItemExtra> listExtra = new ArrayList<DataItemExtra>();
    }


    static private class MyZipInputStream extends ZipInputStream
    {
        public MyZipInputStream(InputStream in)
        {
            super(in);
        }

        @Override
        public void close() throws IOException
        {
            //super.close();
        }

        public void myClose() throws IOException
        {
            super.close();
        }
    }


    private static VirtuosoConnectionPoolDataSource connectionPool;
    private static Hashtable<String, Short> sourceTable = new Hashtable<String, Short>();
    private static ArrayList<String> sources = new ArrayList<String>();
    private static HashMap<Integer, ResultItem> waiting = new HashMap<Integer, ResultItem>();
    private static int done = 0;
    private static int nextValue = 0;
    private static final int threadCount = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);

    private static final short descriptionClassID = 136;
    private static final short protocolClassID = 1041;
    private static final short commentClassID = 1167;


    private static synchronized void resultAdd(int i, ResultItem result) throws SQLException, InterruptedException
    {
        if(i > done)
        {
            waiting.put(i, result);

            while(waiting.size() > 2 * threadCount)
                Bioassay.class.wait();

            return;
        }


        store(done, result);

        while(true)
        {
            ResultItem res = waiting.get(++done);

            if(res == null)
                break;

            waiting.remove(done);
            store(done, res);
        }

        if(waiting.size() <= 2 * threadCount)
            Bioassay.class.notifyAll();
    }


    private static void store(int done, ResultItem res) throws SQLException
    {
        try (Connection connection = getConnection())
        {
            try (PreparedStatement insertStatement = connection
                    .prepareStatement("insert into bioassay_bases (id, source, title) values (?,?,?)"))
            {
                for(DataItemMain item : res.listMain)
                {
                    insertStatement.setInt(1, item.bioassay);
                    insertStatement.setShort(2, item.source);
                    insertStatement.setString(3, item.title);
                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }


            try (PreparedStatement insertExtraStatement = connection
                    .prepareStatement("insert into bioassay_data (bioassay, type, value) values (?,?,?)"))
            {
                for(DataItemExtra item : res.listExtra)
                {
                    insertExtraStatement.setInt(1, item.bioassay);
                    insertExtraStatement.setShort(2, item.type);
                    insertExtraStatement.setString(3, item.value);
                    insertExtraStatement.addBatch();
                }

                insertExtraStatement.executeBatch();
            }
        }

        System.out.println("store: " + done);
    }


    static synchronized Connection getConnection() throws SQLException
    {
        return connectionPool.getConnection();
    }


    static synchronized int next()
    {
        return nextValue++;
    }


    public static void main(String args[]) throws InterruptedException, SQLException
    {
        int port = Integer.parseInt(args[0]);
        String user = args[1];
        String password = args[2];
        String directoryName = args[3];

        connectionPool = new VirtuosoConnectionPoolDataSource();
        connectionPool.setCharset("UTF-8");
        connectionPool.setInitialPoolSize(3);
        connectionPool.setMaxPoolSize(3);
        connectionPool.setPortNumber(port);
        connectionPool.setUser(user);
        connectionPool.setPassword(password);

        try (Connection connection = getConnection())
        {
            try (PreparedStatement statement = connection.prepareStatement("select id, iri, title from source_bases"))
            {
                try (ResultSet result = statement.executeQuery())
                {
                    while(result.next())
                    {
                        Short id = result.getShort(1);
                        String iri = result.getString(2);
                        String title = result.getString(3);

                        if(title == null)
                        {
                            title = iri.replaceAll("http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/source/", "");

                            if(title.matches("ID[0-9]+"))
                                title = title.replaceAll("^ID", "");

                            title = title.replaceAll("_", " ");
                            System.out.println(iri + " -> " + title);
                        }

                        sourceTable.put(title, id);
                    }
                }
            }
        }



        int offset = sourceTable.size();
        System.out.println("offset:" + offset);
        System.out.println("read: " + directoryName);

        File directory = new File(directoryName);
        final File[] files = directory.listFiles();

        Arrays.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File arg0, File arg1)
            {
                return arg0.getName().compareTo(arg1.getName());
            }
        });;


        Thread[] threads = new Thread[threadCount];

        for(int i = 0; i < threadCount; i++)
        {
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    File file = null;

                    try
                    {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        XPath xPath = XPathFactory.newInstance().newXPath();

                        XPathExpression basePath = xPath.compile(
                                "/PC-AssaySubmit/PC-AssaySubmit_assay/PC-AssaySubmit_assay_descr/PC-AssayDescription");

                        XPathExpression titlePath = xPath.compile("./PC-AssayDescription_name");

                        XPathExpression sourceNamePath = xPath.compile(
                                "./PC-AssayDescription_aid-source/PC-Source/PC-Source_db/PC-DBTracking/PC-DBTracking_name");


                        XPathExpression idPath = xPath.compile("./PC-AssayDescription_aid/PC-ID/PC-ID_id");
                        XPathExpression descriptionPath = xPath
                                .compile("./PC-AssayDescription_description/PC-AssayDescription_description_E");
                        XPathExpression protocolPath = xPath
                                .compile("./PC-AssayDescription_protocol/PC-AssayDescription_protocol_E");
                        XPathExpression commentPath = xPath
                                .compile("./PC-AssayDescription_comment/PC-AssayDescription_comment_E");



                        for(int f = next(); f < files.length; f = next())
                        {
                            /*File*/ file = files[f];

                            if(!file.getName().endsWith(".zip"))
                                continue;

                            System.out.println(f + ": " + file.getAbsolutePath());

                            ResultItem res = new ResultItem();
                            InputStream fileStream = new BufferedInputStream(new FileInputStream(file));

                            try (MyZipInputStream zipStream = new MyZipInputStream(fileStream))
                            {
                                ZipEntry zipEntry = null;

                                while((zipEntry = zipStream.getNextEntry()) != null)
                                {
                                    try (InputStream gzipStream = new GZIPInputStream(zipStream))
                                    {
                                        DocumentBuilder db = dbf.newDocumentBuilder();
                                        Document document = db.parse(gzipStream);


                                        Node baseNode = (Node) basePath.evaluate(document.getDocumentElement(),
                                                XPathConstants.NODE);

                                        if(baseNode == null)
                                            new Exception(file.getName() + ": " + zipEntry.getName() + ": not found");



                                        NodeList idNodes = (NodeList) idPath.evaluate(baseNode, XPathConstants.NODESET);

                                        if(idNodes.getLength() != 1)
                                            new Exception(
                                                    file.getName() + ": " + zipEntry.getName() + ": id not found");

                                        int id = Integer.parseInt(idNodes.item(0).getTextContent());

                                        //--------------------------------------------------------------------------------

                                        NodeList titleNodes = (NodeList) titlePath.evaluate(baseNode,
                                                XPathConstants.NODESET);

                                        if(titleNodes.getLength() != 1)
                                            new Exception("missing title");

                                        String title = titleNodes.item(0).getTextContent();

                                        //--------------------------------------------------------------------------------

                                        NodeList sourceNameNodes = (NodeList) sourceNamePath.evaluate(baseNode,
                                                XPathConstants.NODESET);

                                        if(sourceNameNodes.getLength() != 1)
                                            new Exception("missing source name");

                                        String sourceName = sourceNameNodes.item(0).getTextContent();
                                        Short sourceId = sourceTable.get(sourceName);

                                        if(sourceId == null)
                                        {
                                            synchronized(sourceTable)
                                            {
                                                sourceId = sourceTable.get(sourceName);

                                                if(sourceId == null)
                                                {
                                                    sourceId = (short) (sourceTable.size() + 1);
                                                    sourceTable.put(sourceName, sourceId);
                                                    sources.add(sourceName);
                                                }
                                            }
                                        }

                                        res.listMain.add(new DataItemMain(id, sourceId, title));

                                        //--------------------------------------------------------------------------------

                                        NodeList descNodes = (NodeList) descriptionPath.evaluate(baseNode,
                                                XPathConstants.NODESET);


                                        StringBuffer descriptionBuilder = new StringBuffer();

                                        for(int i = 0; i < descNodes.getLength(); ++i)
                                        {
                                            Node e = descNodes.item(i);
                                            descriptionBuilder.append(e.getTextContent());
                                            descriptionBuilder.append('\n');
                                        }

                                        String description = descriptionBuilder.toString();

                                        if(!description.isEmpty())
                                            res.listExtra.add(new DataItemExtra(id, descriptionClassID, description));

                                        //--------------------------------------------------------------------------------

                                        NodeList protocolNodes = (NodeList) protocolPath.evaluate(baseNode,
                                                XPathConstants.NODESET);


                                        StringBuffer protocolBuilder = new StringBuffer();

                                        for(int i = 0; i < protocolNodes.getLength(); ++i)
                                        {
                                            Node e = protocolNodes.item(i);
                                            protocolBuilder.append(e.getTextContent());
                                            protocolBuilder.append('\n');
                                        }

                                        String protocol = protocolBuilder.toString();

                                        if(!protocol.isEmpty())
                                            res.listExtra.add(new DataItemExtra(id, protocolClassID, protocol));

                                        //--------------------------------------------------------------------------------

                                        NodeList commentNodes = (NodeList) commentPath.evaluate(baseNode,
                                                XPathConstants.NODESET);


                                        StringBuffer commentBuilder = new StringBuffer();

                                        for(int i = 0; i < commentNodes.getLength(); ++i)
                                        {
                                            Node e = commentNodes.item(i);
                                            commentBuilder.append(e.getTextContent());
                                            commentBuilder.append('\n');
                                        }

                                        String comment = commentBuilder.toString();

                                        if(!comment.isEmpty())
                                            res.listExtra.add(new DataItemExtra(id, commentClassID, comment));
                                    }

                                    zipStream.closeEntry();
                                }

                                zipStream.myClose();
                            }


                            res.listMain.sort(new Comparator<DataItemMain>()
                            {
                                @Override
                                public int compare(DataItemMain o1, DataItemMain o2)
                                {
                                    return Integer.compare(o1.bioassay, o2.bioassay);
                                }
                            });

                            res.listExtra.sort(new Comparator<DataItemExtra>()
                            {
                                @Override
                                public int compare(DataItemExtra o1, DataItemExtra o2)
                                {
                                    return Integer.compare(o1.bioassay, o2.bioassay);
                                }
                            });

                            resultAdd(f, res);
                        }
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error: " + file.getAbsolutePath());
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
            };

            threads[i].start();
        }

        for(int i = 0; i < threadCount; i++)
            threads[i].join();


        try (Connection connection = getConnection())
        {
            try (PreparedStatement insertStatement = connection
                    .prepareStatement("insert into source_bases (iri, title) values (?,?)"))
            {
                for(int i = 0; i < sources.size(); i++)
                {
                    System.out.println("store source " + (short) (offset + i + 1) + " : " + sources.get(i));

                    String sourceName = sources.get(i);
                    String sourceIri = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"
                            + sourceName.replaceAll("\\(.*\\)", "").replaceAll("[.&]", "").replace('/', '-')
                                    .replace(',', '_').replace(' ', '_').replaceAll("^([0-9])", "ID\\1");

                    System.out.println("add source \"" + sourceName + "\" as " + sourceIri);

                    insertStatement.setString(1, sourceIri);
                    insertStatement.setString(2, sourceName);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }
}
