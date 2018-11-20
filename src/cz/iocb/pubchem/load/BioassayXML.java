package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import cz.iocb.pubchem.load.Ontology.Identifier;
import cz.iocb.pubchem.load.common.Loader;



public class BioassayXML extends Loader
{
    private static class MyZipInputStream extends ZipInputStream
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


    private static Map<String, Short> getSources() throws SQLException, IOException
    {
        Hashtable<String, Short> sourceTable = new Hashtable<String, Short>();

        try(Connection connection = getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement("select id, iri, title from source_bases"))
            {
                try(ResultSet result = statement.executeQuery())
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
                        }

                        sourceTable.put(title, id);
                    }
                }
            }
        }

        return sourceTable;
    }


    private static String getMultiNodeValue(XPathExpression path, Node node) throws XPathExpressionException
    {
        NodeList nodes = (NodeList) path.evaluate(node, XPathConstants.NODESET);

        StringBuffer descriptionBuilder = new StringBuffer();

        for(int i = 0; i < nodes.getLength(); ++i)
        {
            Node e = nodes.item(i);
            descriptionBuilder.append(e.getTextContent());
            descriptionBuilder.append('\n');
        }

        return descriptionBuilder.toString();
    }


    private static String getSingleNodeValue(XPathExpression path, Node node) throws XPathExpressionException
    {
        NodeList nodes = (NodeList) path.evaluate(node, XPathConstants.NODESET);

        if(nodes.getLength() != 1)
            new IOException("missing path value");

        return nodes.item(0).getTextContent();
    }


    public static void loadDirectory(String path) throws SQLException, IOException
    {
        final Identifier descriptionClass = Ontology.getId("http://semanticscience.org/resource/SIO_000136");
        final Identifier protocolClass = Ontology.getId("http://semanticscience.org/resource/SIO_001041");
        final Identifier commentClass = Ontology.getId("http://semanticscience.org/resource/SIO_001167");

        if(descriptionClass.unit != Ontology.unitSIO || protocolClass.unit != Ontology.unitSIO
                || protocolClass.unit != Ontology.unitSIO)
            throw new IOException();


        Map<String, Short> sourceTable = getSources();
        ArrayList<String> newSources = new ArrayList<String>();
        AtomicInteger dataID = new AtomicInteger();
        int newSourceOffset = sourceTable.size();

        File dir = new File(getPubchemDirectory() + File.separatorChar + path);


        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).filter(n -> n.endsWith(".zip"))
                .forEach(name -> {
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



                        try(Connection connection = getConnection())
                        {
                            try(PreparedStatement insertStatement = connection
                                    .prepareStatement("insert into bioassay_bases (id, source, title) values (?,?,?)"))
                            {
                                try(PreparedStatement insertExtraStatement = connection.prepareStatement(
                                        "insert into bioassay_data (__, bioassay, type_id, value) values (?,?,?,?)"))
                                {
                                    InputStream fileStream = getStream(path + File.separatorChar + name, false);
                                    MyZipInputStream zipStream = new MyZipInputStream(fileStream);

                                    while(zipStream.getNextEntry() != null)
                                    {
                                        try(InputStream gzipStream = new GZIPInputStream(zipStream))
                                        {
                                            DocumentBuilder db = dbf.newDocumentBuilder();
                                            Document document = db.parse(gzipStream);


                                            Node baseNode = (Node) basePath.evaluate(document.getDocumentElement(),
                                                    XPathConstants.NODE);

                                            if(baseNode == null)
                                                new IOException("base node not found");


                                            int id = Integer.parseInt(getSingleNodeValue(idPath, baseNode));
                                            String title = getSingleNodeValue(titlePath, baseNode);
                                            String sourceName = getSingleNodeValue(sourceNamePath, baseNode);
                                            Short sourceId = sourceTable.get(sourceName);

                                            if(sourceId == null)
                                            {
                                                synchronized(sourceTable)
                                                {
                                                    sourceId = sourceTable.get(sourceName);

                                                    if(sourceId == null)
                                                    {
                                                        sourceId = (short) sourceTable.size();
                                                        sourceTable.put(sourceName, sourceId);
                                                        newSources.add(sourceName);
                                                    }
                                                }
                                            }

                                            insertStatement.setInt(1, id);
                                            insertStatement.setShort(2, sourceId);
                                            insertStatement.setString(3, title);
                                            insertStatement.addBatch();


                                            String description = getMultiNodeValue(descriptionPath, baseNode);

                                            if(!description.isEmpty())
                                            {
                                                insertExtraStatement.setInt(1, dataID.getAndIncrement());
                                                insertExtraStatement.setInt(2, id);
                                                insertExtraStatement.setInt(3, descriptionClass.id);
                                                insertExtraStatement.setString(4, description);
                                                insertExtraStatement.addBatch();
                                            }


                                            String protocol = getMultiNodeValue(protocolPath, baseNode);

                                            if(!protocol.isEmpty())
                                            {
                                                insertExtraStatement.setInt(1, dataID.getAndIncrement());
                                                insertExtraStatement.setInt(2, id);
                                                insertExtraStatement.setInt(3, protocolClass.id);
                                                insertExtraStatement.setString(4, protocol);
                                                insertExtraStatement.addBatch();
                                            }


                                            String comment = getMultiNodeValue(commentPath, baseNode);

                                            if(!comment.isEmpty())
                                            {
                                                insertExtraStatement.setInt(1, dataID.getAndIncrement());
                                                insertExtraStatement.setInt(2, id);
                                                insertExtraStatement.setInt(3, commentClass.id);
                                                insertExtraStatement.setString(4, comment);
                                                insertExtraStatement.addBatch();
                                            }
                                        }

                                        zipStream.closeEntry();
                                    }

                                    zipStream.myClose();
                                    insertExtraStatement.executeBatch();
                                }

                                insertStatement.executeBatch();
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("exception for " + name);
                        e.printStackTrace();
                        System.exit(1);
                    }
                });



        try(Connection connection = getConnection())
        {
            try(PreparedStatement insertStatement = connection
                    .prepareStatement("insert into source_bases (id, iri, title) values (?,?,?)"))
            {
                for(int i = 0; i < newSources.size(); i++)
                {
                    short sourceID = (short) (newSourceOffset + i);
                    String sourceName = newSources.get(i);
                    String sourceIri = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"
                            + sourceName.replaceAll("\\(.*\\)", "").replaceAll("[.&]", "").replace('/', '-')
                                    .replace(',', '_').replace(' ', '_').replaceAll("^([0-9])", "ID\\1");

                    System.out.println("  add missing source: " + sourceIri + " (" + sourceName + ")");

                    insertStatement.setShort(1, sourceID);
                    insertStatement.setString(2, sourceIri);
                    insertStatement.setString(3, sourceName);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("Bioassay/XML");
    }
}
