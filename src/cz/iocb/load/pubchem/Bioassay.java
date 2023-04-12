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
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



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


    private static IntHashSet usedBioassays;
    private static IntHashSet newBioassays;
    private static IntHashSet oldBioassays;


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
        usedBioassays = new IntHashSet();
        newBioassays = new IntHashSet();
        oldBioassays = getIntSet("select id from pubchem.bioassay_bases");

        IntIntHashMap newSources = new IntIntHashMap();
        IntIntHashMap oldSources = getIntIntMap("select id, source from pubchem.bioassay_bases");

        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.bioassay_bases");

        IntStringMap newDescriptions = new IntStringMap();
        IntStringMap oldDescriptions = getIntStringMap(
                "select bioassay, value from pubchem.bioassay_data where type_id = '136'::smallint");

        IntStringMap newProtocols = new IntStringMap();
        IntStringMap oldProtocols = getIntStringMap(
                "select bioassay, value from pubchem.bioassay_data where type_id = '1041'::smallint");

        IntStringMap newComments = new IntStringMap();
        IntStringMap oldComments = getIntStringMap(
                "select bioassay, value from pubchem.bioassay_data where type_id = '1167'::smallint");

        IntIntHashMap newAssays = new IntIntHashMap();
        IntIntHashMap oldAssays = getIntIntMap("select bioassay, chembl_assay from pubchem.bioassay_chembl_assays");

        IntIntHashMap newMechanisms = new IntIntHashMap();
        IntIntHashMap oldMechanisms = getIntIntMap(
                "select bioassay, chembl_mechanism from pubchem.bioassay_chembl_mechanisms");


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


                    int bioassayID = Integer.parseInt(getSingleNodeValue(idPath, baseNode));

                    synchronized(newBioassays)
                    {
                        usedBioassays.add(bioassayID);

                        if(!oldBioassays.remove(bioassayID))
                            newBioassays.add(bioassayID);
                    }


                    String sourceName = getSingleNodeValue(sourceNamePath, baseNode);
                    int sourceID = Source.getSourceID(createSourceID(sourceName), sourceName);

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
                            int chemblID = Integer.parseInt(chemblSource.replaceFirst("^drug_mech_", ""));

                            synchronized(newMechanisms)
                            {
                                if(oldMechanisms.removeKeyIfAbsent(bioassayID, NO_VALUE) != chemblID)
                                    newMechanisms.put(bioassayID, chemblID);
                            }
                        }
                        else if(chemblSource.startsWith("CHEMBL"))
                        {
                            int chemblID = Integer.parseInt(chemblSource.replaceFirst("^CHEMBL", ""));

                            synchronized(newAssays)
                            {
                                if(oldAssays.removeKeyIfAbsent(bioassayID, NO_VALUE) != chemblID)
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


        batch("insert into pubchem.bioassay_bases(id) values (?)", newBioassays);
        newBioassays.clear();

        batch("update pubchem.bioassay_bases set source = null where id = ?", oldSources.keySet());
        batch("insert into pubchem.bioassay_bases(id, source) values (?,?) "
                + "on conflict (id) do update set source=EXCLUDED.source", newSources);

        batch("update pubchem.bioassay_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.bioassay_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);

        batch("delete from pubchem.bioassay_data where bioassay = ? and type_id = '136'::smallint",
                oldDescriptions.keySet());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value) values (136,?,?)"
                + "on conflict (type_id, bioassay) do update set value=EXCLUDED.value", newDescriptions);

        batch("delete from pubchem.bioassay_data where bioassay = ? and type_id = '1041'::smallint",
                oldProtocols.keySet());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value) values (1041,?,?)"
                + "on conflict (type_id, bioassay) do update set value=EXCLUDED.value", newProtocols);

        batch("delete from pubchem.bioassay_data where bioassay = ? and type_id = '1167'::smallint",
                oldComments.keySet());
        batch("insert into pubchem.bioassay_data(type_id, bioassay, value) values (1167,?,?)"
                + "on conflict (type_id, bioassay) do update set value=EXCLUDED.value", newComments);

        batch("delete from pubchem.bioassay_chembl_assays where bioassay = ?", oldAssays.keySet());
        batch("insert into pubchem.bioassay_chembl_assays(bioassay, chembl_assay) values (?,?)"
                + "on conflict (bioassay) do update set chembl_assay=EXCLUDED.chembl_assay", newAssays);

        batch("delete from pubchem.bioassay_chembl_mechanisms where bioassay = ?", oldMechanisms.keySet());
        batch("insert into pubchem.bioassay_chembl_mechanisms(bioassay, chembl_mechanism) values (?,?)"
                + "on conflict (bioassay) do update set chembl_mechanism=EXCLUDED.chembl_mechanism", newMechanisms);
    }


    private static void loadStages(Model model) throws IOException, SQLException
    {
        IntIntHashMap newStages = new IntIntHashMap();
        IntIntHashMap oldStages = getIntIntMap("select bioassay, stage from pubchem.bioassay_stages");

        new QueryResultProcessor(patternQuery("?bioassay bao:BAO_0000210 ?stage"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                Identifier stage = Ontology.getId(getIRI("stage"));

                if(stage.unit != Ontology.unitBAO)
                    throw new IOException();

                Bioassay.addBioassayID(bioassayID);

                if(stage.id != oldStages.removeKeyIfAbsent(bioassayID, NO_VALUE))
                    newStages.put(bioassayID, stage.id);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_stages where bioassay = ?", oldStages.keySet());
        batch("insert into pubchem.bioassay_stages(bioassay, stage) values (?,?) "
                + "on conflict (bioassay) do update set stage=EXCLUDED.stage", newStages);
    }


    private static void loadConfirmatoryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = getIntPairSet(
                "select bioassay, confirmatory_assay from pubchem.bioassay_confirmatory_assays");

        new QueryResultProcessor(patternQuery("?confirmatory bao:BAO_0000540 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int confirmatoryID = getIntID("confirmatory", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, confirmatoryID);
                Bioassay.addBioassayID(bioassayID);
                Bioassay.addBioassayID(confirmatoryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_confirmatory_assays where bioassay = ? and confirmatory_assay = ?",
                oldRelations);
        batch("insert into pubchem.bioassay_confirmatory_assays(bioassay, confirmatory_assay) values (?,?)",
                newRelations);
    }


    private static void loadPrimaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = getIntPairSet("select bioassay, primary_assay from pubchem.bioassay_primary_assays");

        new QueryResultProcessor(patternQuery("?primary bao:BAO_0001067 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int primaryID = getIntID("primary", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, primaryID);
                Bioassay.addBioassayID(bioassayID);
                Bioassay.addBioassayID(primaryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_primary_assays where bioassay = ? and primary_assay = ?", oldRelations);
        batch("insert into pubchem.bioassay_primary_assays(bioassay, primary_assay) values (?,?)", newRelations);
    }


    private static void loadSummaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = getIntPairSet("select bioassay, summary_assay from pubchem.bioassay_summary_assays");

        new QueryResultProcessor(patternQuery("?summary bao:BAO_0001094 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int summaryID = getIntID("summary", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, summaryID);
                Bioassay.addBioassayID(bioassayID);
                Bioassay.addBioassayID(summaryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_summary_assays where bioassay = ? and summary_assay = ?", oldRelations);
        batch("insert into pubchem.bioassay_summary_assays(bioassay, summary_assay) values (?,?)", newRelations);
    }


    private static void loadPatents(Model model) throws IOException, SQLException
    {
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = getIntPairSet("select bioassay, patent from pubchem.bioassay_patent_references");

        new QueryResultProcessor(patternQuery("?bioassay cito:isDiscussedBy ?patent"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int patentID = Patent.getPatentID(getStringID("patent", "http://rdf.ncbi.nlm.nih.gov/pubchem/patent/"));

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, patentID);
                Bioassay.addBioassayID(bioassayID);

                if(!oldPatents.remove(pair))
                    newPatents.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_patent_references where bioassay = ? and patent = ?", oldPatents);
        batch("insert into pubchem.bioassay_patent_references(bioassay, patent) values (?,?)", newPatents);
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

        batch("delete from pubchem.bioassay_bases where id = ?", oldBioassays);
        batch("insert into pubchem.bioassay_bases(id) values (?)" + " on conflict do nothing", newBioassays);

        usedBioassays = null;
        newBioassays = null;
        oldBioassays = null;

        System.out.println();
    }


    static void addBioassayID(int bioassayID)
    {
        synchronized(newBioassays)
        {
            if(usedBioassays.add(bioassayID))
            {
                System.out.println("    add missing bioassay AID" + bioassayID);

                if(!oldBioassays.remove(bioassayID))
                    newBioassays.add(bioassayID);
            }
        }
    }
}
