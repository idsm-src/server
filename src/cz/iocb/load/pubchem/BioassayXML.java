package cz.iocb.load.pubchem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cz.iocb.load.common.Updater;



class BioassayXML extends Updater
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


    private static int nextDataID;


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


    private static String getSourceIRI(String sourceName)
    {
        String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/source/";

        if(sourceName.matches("[0-9]+"))
            return prefix + "ID" + sourceName;
        else
            return prefix + sourceName.replaceFirst("\\(.*", "").replaceAll("[- ,/.&]", "_");
    }


    static void loadBioassays()
            throws SQLException, IOException, XPathException, ParserConfigurationException, SAXException
    {
        HashMap<Integer, Integer> assayMapping = new HashMap<Integer, Integer>();

        try(BufferedReader reader = new BufferedReader(new FileReader(baseDirectory + "chembl/assay-mapping.txt")))
        {
            for(String line = reader.readLine(); line != null; line = reader.readLine())
            {
                int id = Integer.parseInt(line.replaceFirst("\t.*", ""));
                int chembl = Integer.parseInt(line.replaceFirst(".*\tCHEMBL", ""));

                assayMapping.put(id, chembl);
            }
        }


        IntHashSet newBioassays = new IntHashSet(1000000);
        IntHashSet oldBioassays = getIntSet("select id from pubchem.bioassay_bases", 1000000);

        IntIntHashMap newSources = new IntIntHashMap(2000000);
        IntIntHashMap oldSources = getIntIntMap("select id, source from pubchem.bioassay_bases", 2000000);

        IntStringMap newTitles = new IntStringMap(2000000);
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.bioassay_bases", 2000000);

        IntStringPairIntMap newDescriptions = new IntStringPairIntMap(2000000);
        IntStringPairIntMap oldDescriptions = getIntStringPairIntMap(
                "select bioassay, value, __ from pubchem.bioassay_data where type_id = '136'::smallint", 2000000);

        IntStringPairIntMap newProtocols = new IntStringPairIntMap(2000000);
        IntStringPairIntMap oldProtocols = getIntStringPairIntMap(
                "select bioassay, value, __ from pubchem.bioassay_data where type_id = '1041'::smallint", 2000000);

        IntStringPairIntMap newComments = new IntStringPairIntMap(2000000);
        IntStringPairIntMap oldComments = getIntStringPairIntMap(
                "select bioassay, value, __ from pubchem.bioassay_data where type_id = '1167'::smallint", 2000000);

        IntIntHashMap newAssays = new IntIntHashMap(2000000);
        IntIntHashMap oldAssays = getIntIntMap("select bioassay, chembl_assay from pubchem.bioassay_chembl_assays",
                2000000);

        IntIntHashMap newMechanisms = new IntIntHashMap(1000);
        IntIntHashMap oldMechanisms = getIntIntMap(
                "select bioassay, chembl_mechanism from pubchem.bioassay_chembl_mechanisms", 1000);

        nextDataID = getIntValue("select coalesce(max(__)+1,0) from pubchem.bioassay_data");


        processXmlFiles("pubchem/Bioassay/XML", "[0-9]+_[0-9]+\\.zip", file -> {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression basePath = xPath
                    .compile("/PC-AssaySubmit/PC-AssaySubmit_assay/PC-AssaySubmit_assay_descr/PC-AssayDescription");

            XPathExpression titlePath = xPath.compile("./PC-AssayDescription_name");

            XPathExpression sourceNamePath = xPath.compile(
                    "./PC-AssayDescription_aid-source/PC-Source/PC-Source_db/PC-DBTracking/PC-DBTracking_name");

            XPathExpression idPath = xPath.compile("./PC-AssayDescription_aid/PC-ID/PC-ID_id");
            XPathExpression descriptionPath = xPath
                    .compile("./PC-AssayDescription_description/PC-AssayDescription_description_E");
            XPathExpression protocolPath = xPath
                    .compile("./PC-AssayDescription_protocol/PC-AssayDescription_protocol_E");
            XPathExpression commentPath = xPath.compile("./PC-AssayDescription_comment/PC-AssayDescription_comment_E");

            XPathExpression trackingSourcePath = xPath.compile(
                    "./PC-AssayDescription_aid-source/PC-Source/PC-Source_db/PC-DBTracking/PC-DBTracking_source-id/Object-id/Object-id_str");


            InputStream fileStream = getStream(file, false);
            MyZipInputStream zipStream = new MyZipInputStream(fileStream);

            while(zipStream.getNextEntry() != null)
            {
                try(InputStream gzipStream = new GZIPInputStream(zipStream))
                {
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(gzipStream);

                    Node baseNode = (Node) basePath.evaluate(document.getDocumentElement(), XPathConstants.NODE);

                    if(baseNode == null)
                        throw new IOException("base node not found");


                    int bioassayID = Integer.parseInt(getSingleNodeValue(idPath, baseNode));

                    synchronized(newBioassays)
                    {
                        if(!oldBioassays.remove(bioassayID))
                            newBioassays.add(bioassayID);
                    }


                    String sourceName = getSingleNodeValue(sourceNamePath, baseNode);
                    int sourceID = Source.getSourceID(getSourceIRI(sourceName), sourceName);

                    synchronized(newSources)
                    {
                        if(oldSources.removeKeyIfAbsent(bioassayID, NO_VALUE) != sourceID)
                            newSources.put(bioassayID, sourceID);
                    }


                    String title = getSingleNodeValue(titlePath, baseNode);

                    synchronized(newTitles)
                    {
                        if(!title.equals(oldTitles.remove(bioassayID)))
                            newTitles.put(bioassayID, title);
                    }


                    String description = getMultiNodeValue(descriptionPath, baseNode);

                    if(!description.isEmpty())
                    {
                        IntObjectPair<String> pair = PrimitiveTuples.pair(bioassayID, description);

                        synchronized(BioassayXML.class)
                        {
                            if(oldDescriptions.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                                newDescriptions.put(pair, nextDataID++);
                        }
                    }


                    String protocol = getMultiNodeValue(protocolPath, baseNode);

                    if(!protocol.isEmpty())
                    {
                        IntObjectPair<String> pair = PrimitiveTuples.pair(bioassayID, protocol);

                        synchronized(BioassayXML.class)
                        {
                            if(oldProtocols.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                                newProtocols.put(pair, nextDataID++);
                        }
                    }


                    String comment = getMultiNodeValue(commentPath, baseNode);

                    if(!comment.isEmpty())
                    {
                        IntObjectPair<String> pair = PrimitiveTuples.pair(bioassayID, comment);

                        synchronized(BioassayXML.class)
                        {
                            if(oldComments.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                                newComments.put(pair, nextDataID++);
                        }
                    }


                    if(sourceName.equals("ChEMBL"))
                    {
                        String chemblSource = getSingleNodeValue(trackingSourcePath, baseNode);

                        if(chemblSource.startsWith("drug_mech_"))
                        {
                            int chemblID = Integer.parseInt(chemblSource.replaceFirst("^drug_mech_", ""));

                            synchronized(newMechanisms)
                            {
                                if(oldMechanisms.removeKeyIfAbsent(bioassayID, NO_VALUE) != chemblID)
                                    newMechanisms.put(bioassayID, chemblID);
                            }
                        }
                        else
                        {
                            Integer chemblID = assayMapping.get(Integer.parseInt(chemblSource));

                            synchronized(newAssays)
                            {
                                if(chemblID == null)
                                    System.err.println("lost assay mapping for " + chemblSource + " in " + bioassayID);
                                else if(oldAssays.removeKeyIfAbsent(bioassayID, NO_VALUE) != chemblID)
                                    newAssays.put(bioassayID, chemblID);
                            }
                        }
                    }
                }

                zipStream.closeEntry();
            }

            zipStream.myClose();
        });


        batch("delete from pubchem.bioassay_bases where id = ?", oldBioassays);
        batch("insert into pubchem.bioassay_bases(id, source, title) values (?,?,?)", newBioassays,
                (PreparedStatement statement, int bioassay) -> {
                    statement.setInt(1, bioassay);
                    statement.setInt(2, newSources.getOrThrow(bioassay));
                    statement.setString(3, newTitles.remove(bioassay));
                    newSources.remove(bioassay);
                });

        batch("update pubchem.bioassay_bases set source = ? where id = ?", newSources, Direction.REVERSE);
        batch("update pubchem.bioassay_bases set title = ? where id = ?", newTitles, Direction.REVERSE);

        batch("delete from pubchem.bioassay_data where __ = ?", oldDescriptions.values());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value, __) values (136,?,?,?)", newDescriptions);

        batch("delete from pubchem.bioassay_data where __ = ?", oldProtocols.values());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value, __) values (1041,?,?,?)", newProtocols);

        batch("delete from pubchem.bioassay_data where __ = ?", oldComments.values());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value, __) values (1167,?,?,?)", newComments);

        batch("delete from pubchem.bioassay_chembl_assays where bioassay = ?", oldAssays.keySet());
        batch("insert into pubchem.bioassay_chembl_assays(bioassay, chembl_assay) values (?,?)", newAssays);

        batch("delete from pubchem.bioassay_chembl_mechanisms where bioassay = ?", oldMechanisms.keySet());
        batch("insert into pubchem.bioassay_chembl_mechanisms(bioassay, chembl_mechanism) values (?,?)", newMechanisms);
    }


    static void load() throws XPathException, SQLException, IOException, ParserConfigurationException, SAXException
    {
        System.out.println("load bioassays ...");

        loadBioassays();

        System.out.println();
    }
}
