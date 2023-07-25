package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
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
import org.apache.jena.rdf.model.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Bioassay extends Updater
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


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepBioassays = new IntSet();
    private static final IntSet newBioassays = new IntSet();
    private static final IntSet oldBioassays = new IntSet();


    private static String getMultiNodeValue(XPathExpression path, Node node) throws XPathExpressionException
    {
        NodeList nodes = (NodeList) path.evaluate(node, XPathConstants.NODESET);

        StringBuffer descriptionBuilder = new StringBuffer();

        for(int i = 0; i < nodes.getLength(); ++i)
        {
            if(i > 0)
                descriptionBuilder.append('\n');

            Node e = nodes.item(i);
            descriptionBuilder.append(e.getTextContent());
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


    private static String createSourceID(String sourceName)
    {
        if(sourceName.matches("[0-9]+"))
            return "ID" + sourceName;
        else
            return sourceName.replaceFirst("\\(.*", "").replaceAll("[- ,/.&]", "_");
    }


    private static void loadBioassays()
            throws SQLException, IOException, XPathException, ParserConfigurationException, SAXException
    {
        IntIntMap newSources = new IntIntMap();
        IntIntMap oldSources = new IntIntMap();

        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        IntStringMap newDescriptions = new IntStringMap();
        IntStringMap oldDescriptions = new IntStringMap();

        IntStringMap newProtocols = new IntStringMap();
        IntStringMap oldProtocols = new IntStringMap();

        IntStringMap newComments = new IntStringMap();
        IntStringMap oldComments = new IntStringMap();

        IntIntMap newAssays = new IntIntMap();
        IntIntMap oldAssays = new IntIntMap();

        IntIntMap newMechanisms = new IntIntMap();
        IntIntMap oldMechanisms = new IntIntMap();

        load("select id from pubchem.bioassay_bases", oldBioassays);
        load("select id,source from pubchem.bioassay_bases where source is not null", oldSources);
        load("select id,title from pubchem.bioassay_bases where title is not null", oldTitles);
        load("select bioassay,value from pubchem.bioassay_data where type_id = '136'::smallint", oldDescriptions);
        load("select bioassay,value from pubchem.bioassay_data where type_id = '1041'::smallint", oldProtocols);
        load("select bioassay,value from pubchem.bioassay_data where type_id = '1167'::smallint", oldComments);
        load("select bioassay,chembl_assay from pubchem.bioassay_chembl_assays", oldAssays);
        load("select bioassay,chembl_mechanism from pubchem.bioassay_chembl_mechanisms", oldMechanisms);

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

            XPathExpression trackingSourcePath = xPath.compile("./PC-AssayDescription_aid-source/PC-Source/"
                    + "PC-Source_db/PC-DBTracking/PC-DBTracking_source-id/Object-id/Object-id_str");


            InputStream fileStream = getZipStream(file);
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


                    Integer bioassayID = Integer.parseInt(getSingleNodeValue(idPath, baseNode));

                    synchronized(newBioassays)
                    {
                        oldBioassays.remove(bioassayID);
                        keepBioassays.add(bioassayID);
                    }


                    String sourceName = getSingleNodeValue(sourceNamePath, baseNode);
                    Integer sourceID = Source.registerSourceID(createSourceID(sourceName), sourceName);

                    synchronized(newSources)
                    {
                        if(!sourceID.equals(oldSources.remove(bioassayID)))
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
                        synchronized(Bioassay.class)
                        {
                            if(!description.equals(oldDescriptions.remove(bioassayID)))
                                newDescriptions.put(bioassayID, description);
                        }
                    }


                    String protocol = getMultiNodeValue(protocolPath, baseNode);

                    if(!protocol.isEmpty())
                    {
                        synchronized(Bioassay.class)
                        {
                            if(!protocol.equals(oldProtocols.remove(bioassayID)))
                                newProtocols.put(bioassayID, protocol);
                        }
                    }


                    String comment = getMultiNodeValue(commentPath, baseNode);

                    if(!comment.isEmpty())
                    {
                        synchronized(Bioassay.class)
                        {
                            if(!comment.equals(oldComments.remove(bioassayID)))
                                newComments.put(bioassayID, comment);
                        }
                    }


                    if(sourceName.equals("ChEMBL"))
                    {
                        String chemblSource = getSingleNodeValue(trackingSourcePath, baseNode);

                        if(chemblSource.startsWith("drug_mech_"))
                        {
                            Integer chemblID = Integer.parseInt(chemblSource.replaceFirst("^drug_mech_", ""));

                            synchronized(newMechanisms)
                            {
                                if(!chemblID.equals(oldMechanisms.remove(bioassayID)))
                                    newMechanisms.put(bioassayID, chemblID);
                            }
                        }
                        else if(chemblSource.startsWith("CHEMBL"))
                        {
                            Integer chemblID = Integer.parseInt(chemblSource.replaceFirst("^CHEMBL", ""));

                            synchronized(newAssays)
                            {
                                if(!chemblID.equals(oldAssays.remove(bioassayID)))
                                    newAssays.put(bioassayID, chemblID);
                            }
                        }
                        else
                        {
                            throw new IOException();
                        }
                    }
                }

                zipStream.closeEntry();
            }

            zipStream.myClose();
        });


        store("update pubchem.bioassay_bases set source=null where id=? and source=?", oldSources);
        store("insert into pubchem.bioassay_bases(id,source) values(?,?) "
                + "on conflict(id) do update set source=EXCLUDED.source", newSources);

        store("update pubchem.bioassay_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.bioassay_bases(id,title) values(?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);

        store("delete from pubchem.bioassay_data where type_id = '136'::smallint and bioassay=? and value=?",
                oldDescriptions);
        store("insert into pubchem.bioassay_data(type_id,bioassay,value) values(136,?,?)"
                + "on conflict(type_id,bioassay) do update set value=EXCLUDED.value", newDescriptions);

        store("delete from pubchem.bioassay_data where type_id = '1041'::smallint and bioassay=? and value=?",
                oldProtocols);
        store("insert into pubchem.bioassay_data(type_id,bioassay,value) values(1041,?,?)"
                + "on conflict(type_id,bioassay) do update set value=EXCLUDED.value", newProtocols);

        store("delete from pubchem.bioassay_data where type_id = '1167'::smallint and bioassay=? and value=?",
                oldComments);
        store("insert into pubchem.bioassay_data(type_id,bioassay,value) values(1167,?,?)"
                + "on conflict(type_id,bioassay) do update set value=EXCLUDED.value", newComments);

        store("delete from pubchem.bioassay_chembl_assays where bioassay=? and chembl_assay=?", oldAssays);
        store("insert into pubchem.bioassay_chembl_assays(bioassay,chembl_assay) values(?,?)"
                + "on conflict(bioassay) do update set chembl_assay=EXCLUDED.chembl_assay", newAssays);

        store("delete from pubchem.bioassay_chembl_mechanisms where bioassay=? and chembl_mechanism=?", oldMechanisms);
        store("insert into pubchem.bioassay_chembl_mechanisms(bioassay,chembl_mechanism) values(?,?)"
                + "on conflict(bioassay) do update set chembl_mechanism=EXCLUDED.chembl_mechanism", newMechanisms);
    }


    private static void loadStages(Model model) throws IOException, SQLException
    {
        IntIntMap keepStages = new IntIntMap();
        IntIntMap newStages = new IntIntMap();
        IntIntMap oldStages = new IntIntMap();

        load("select bioassay,stage from pubchem.bioassay_stages", oldStages);

        new QueryResultProcessor(patternQuery("?bioassay bao:BAO_0000210 ?stage"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bioassayID = getBioassayID(getIRI("bioassay"));
                Pair<Integer, Integer> stage = Ontology.getId(getIRI("stage"));

                if(stage.getOne() != Ontology.unitBAO)
                    throw new IOException();

                if(stage.getTwo().equals(oldStages.remove(bioassayID)))
                {
                    keepStages.put(bioassayID, stage.getTwo());
                }
                else
                {
                    Integer keep = keepStages.get(bioassayID);

                    if(stage.getTwo().equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newStages.put(bioassayID, stage.getTwo());

                    if(put != null && !stage.getTwo().equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("delete from pubchem.bioassay_stages where bioassay=? and stage=?", oldStages);
        store("insert into pubchem.bioassay_stages(bioassay,stage) values(?,?) "
                + "on conflict(bioassay) do update set stage=EXCLUDED.stage", newStages);
    }


    private static void loadConfirmatoryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,confirmatory_assay from pubchem.bioassay_confirmatory_assays", oldRelations);

        new QueryResultProcessor(patternQuery("?confirmatory bao:BAO_0000540 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bioassayID = getBioassayID(getIRI("bioassay"));
                Integer confirmatoryID = getBioassayID(getIRI("confirmatory"));

                Pair<Integer, Integer> pair = Pair.getPair(bioassayID, confirmatoryID);

                if(oldRelations.remove(pair))
                    keepRelations.add(pair);
                else if(!keepRelations.contains(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        store("delete from pubchem.bioassay_confirmatory_assays where bioassay=? and confirmatory_assay=?",
                oldRelations);
        store("insert into pubchem.bioassay_confirmatory_assays(bioassay,confirmatory_assay) values(?,?)",
                newRelations);
    }


    private static void loadPrimaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,primary_assay from pubchem.bioassay_primary_assays", oldRelations);

        new QueryResultProcessor(patternQuery("?primary bao:BAO_0001067 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bioassayID = getBioassayID(getIRI("bioassay"));
                Integer primaryID = getBioassayID(getIRI("primary"));

                Pair<Integer, Integer> pair = Pair.getPair(bioassayID, primaryID);

                if(oldRelations.remove(pair))
                    keepRelations.add(pair);
                else if(!keepRelations.contains(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        store("delete from pubchem.bioassay_primary_assays where bioassay=? and primary_assay=?", oldRelations);
        store("insert into pubchem.bioassay_primary_assays(bioassay,primary_assay) values(?,?)", newRelations);
    }


    private static void loadSummaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,summary_assay from pubchem.bioassay_summary_assays", oldRelations);

        new QueryResultProcessor(patternQuery("?summary bao:BAO_0001094 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bioassayID = getBioassayID(getIRI("bioassay"));
                Integer summaryID = getBioassayID(getIRI("summary"));

                Pair<Integer, Integer> pair = Pair.getPair(bioassayID, summaryID);

                if(oldRelations.remove(pair))
                    keepRelations.add(pair);
                else if(!keepRelations.contains(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        store("delete from pubchem.bioassay_summary_assays where bioassay=? and summary_assay=?", oldRelations);
        store("insert into pubchem.bioassay_summary_assays(bioassay,summary_assay) values(?,?)", newRelations);
    }


    private static void loadPatents(Model model) throws IOException, SQLException
    {
        IntPairSet keepPatents = new IntPairSet();
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select bioassay,patent from pubchem.bioassay_patent_references", oldPatents);

        new QueryResultProcessor(patternQuery("?bioassay cito:isDiscussedBy ?patent"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer bioassayID = getBioassayID(getIRI("bioassay"));
                Integer patentID = Patent.getPatentID(getIRI("patent"));

                Pair<Integer, Integer> pair = Pair.getPair(bioassayID, patentID);

                if(oldPatents.remove(pair))
                    keepPatents.add(pair);
                else if(!keepPatents.contains(pair))
                    newPatents.add(pair);
            }
        }.load(model);

        store("delete from pubchem.bioassay_patent_references where bioassay=? and patent=?", oldPatents);
        store("insert into pubchem.bioassay_patent_references(bioassay,patent) values(?,?)", newPatents);
    }


    static void load() throws XPathException, SQLException, IOException, ParserConfigurationException, SAXException
    {
        System.out.println("load bioassays ...");

        loadBioassays();

        Model model = getModel("pubchem/RDF/bioassay/pc_bioassay.ttl.gz");

        check(model, "pubchem/bioassay/check.sparql");

        loadStages(model);
        loadConfirmatoryAssays(model);
        loadPrimaryAssays(model);
        loadSummaryAssays(model);
        loadPatents(model);

        model.close();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish bioassays ...");

        store("delete from pubchem.bioassay_bases where id=?", oldBioassays);
        store("insert into pubchem.bioassay_bases(id) values(?)", newBioassays);

        System.out.println();
    }


    static void addBioassayID(Integer bioassayID)
    {
        synchronized(newBioassays)
        {
            if(!keepBioassays.contains(bioassayID) && !newBioassays.contains(bioassayID))
            {
                System.out.println("    add missing bioassay AID" + bioassayID);

                if(!oldBioassays.remove(bioassayID))
                    newBioassays.add(bioassayID);
                else
                    keepBioassays.add(bioassayID);
            }
        }
    }


    static Integer getBioassayID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer bioassayID = Integer.parseInt(value.substring(prefixLength));

        addBioassayID(bioassayID);

        return bioassayID;
    }


    public static int size()
    {
        return newBioassays.size() + keepBioassays.size();
    }
}
