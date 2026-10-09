package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.pubchem.PubChemRDF.bao;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.BufferedInputStream;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
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

    private static final EntityTable<Integer> bioassays = new EntityTable<>("pubchem.bioassays", intKey("id"), null,
            integer("source"), uniqueVarchar("title"));
    private static final MissingEntities<Integer> missingBioassays = new MissingEntities<>("bioassay", false);


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

        load("select bioassay,text from pubchem.bioassay_texts where type_id = '136'::smallint", oldDescriptions);
        load("select bioassay,text from pubchem.bioassay_texts where type_id = '1041'::smallint", oldProtocols);
        load("select bioassay,text from pubchem.bioassay_texts where type_id = '1167'::smallint", oldComments);
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
                try(InputStream gzipStream = new FilteredInputStream(new GZIPInputStream(zipStream)))
                {
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(gzipStream);

                    Node baseNode = (Node) basePath.evaluate(document.getDocumentElement(), XPathConstants.NODE);

                    if(baseNode == null)
                        throw new IOException("base node not found");


                    Integer bioassayID = Integer.parseInt(getSingleNodeValue(idPath, baseNode));

                    bioassays.reference(bioassayID);


                    String sourceName = getSingleNodeValue(sourceNamePath, baseNode);
                    Integer sourceID = Source.registerSourceID(createSourceID(sourceName), sourceName);

                    bioassays.set(bioassayID, "source", sourceID);


                    String title = getSingleNodeValue(titlePath, baseNode);

                    bioassays.set(bioassayID, "title", title);


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


        store("delete from pubchem.bioassay_texts where type_id = '136'::smallint and bioassay=? and text=?",
                oldDescriptions);
        store("insert into pubchem.bioassay_texts(type_id,bioassay,text) values(136,?,?)"
                + "on conflict(type_id,bioassay) do update set text=EXCLUDED.text", newDescriptions);

        store("delete from pubchem.bioassay_texts where type_id = '1041'::smallint and bioassay=? and text=?",
                oldProtocols);
        store("insert into pubchem.bioassay_texts(type_id,bioassay,text) values(1041,?,?)"
                + "on conflict(type_id,bioassay) do update set text=EXCLUDED.text", newProtocols);

        store("delete from pubchem.bioassay_texts where type_id = '1167'::smallint and bioassay=? and text=?",
                oldComments);
        store("insert into pubchem.bioassay_texts(type_id,bioassay,text) values(1167,?,?)"
                + "on conflict(type_id,bioassay) do update set text=EXCLUDED.text", newComments);

        store("delete from pubchem.bioassay_chembl_assays where bioassay=? and chembl_assay=?", oldAssays);
        store("insert into pubchem.bioassay_chembl_assays(bioassay,chembl_assay) values(?,?)"
                + "on conflict(bioassay) do update set chembl_assay=EXCLUDED.chembl_assay", newAssays);

        store("delete from pubchem.bioassay_chembl_mechanisms where bioassay=? and chembl_mechanism=?", oldMechanisms);
        store("insert into pubchem.bioassay_chembl_mechanisms(bioassay,chembl_mechanism) values(?,?)"
                + "on conflict(bioassay) do update set chembl_mechanism=EXCLUDED.chembl_mechanism", newMechanisms);
    }


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "title", dcterms + "source", bao + "BAO_0000209",
                bao + "BAO_0000540", bao + "BAO_0000210", bao + "BAO_0001067", bao + "BAO_0001094", rdf + "type",
                rdfs + "seeAlso", dcterms + "identifier");
        dispatcher.checkTypes(all(), vocab + "BioAssay", bao + "BAO_0000015");
        dispatcher.checkIdentifier(dcterms + "identifier", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
        dispatcher.checkPrefixes(all(), rdfs + "seeAlso", "http://rdf.ebi.ac.uk/resource/chembl/assay/CHEMBL",
                "http://rdf.ebi.ac.uk/resource/chembl/assay/drug_mech_");
    }


    private static void loadStages(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepStages = new IntIntMap();
        IntIntMap newStages = new IntIntMap();
        IntIntMap oldStages = new IntIntMap();

        load("select bioassay,stage_id from pubchem.bioassay_stages", oldStages);

        dispatcher.on(bao + "BAO_0000210", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            Pair<Integer, Integer> stage = Ontology.getId(object.getURI());

            if(stage.getOne() != OntologyResource.unitBAO)
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
        });

        dispatcher.after(() -> {
            store("delete from pubchem.bioassay_stages where bioassay=? and stage_id=?", oldStages);
            store("insert into pubchem.bioassay_stages(bioassay,stage_id) values(?,?) "
                    + "on conflict(bioassay) do update set stage_id=EXCLUDED.stage_id", newStages);
        });
    }


    private static void loadConfirmatoryAssays(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,confirmatory_assay from pubchem.bioassay_confirmatory_assays", oldRelations);

        dispatcher.on(bao + "BAO_0000540", (subject, object) -> {
            Integer bioassayID = getBioassayID(object.getURI());
            Integer confirmatoryID = getBioassayID(subject.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(bioassayID, confirmatoryID);

            if(oldRelations.remove(pair))
                keepRelations.add(pair);
            else if(!keepRelations.contains(pair))
                newRelations.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.bioassay_confirmatory_assays where bioassay=? and confirmatory_assay=?",
                    oldRelations);
            store("insert into pubchem.bioassay_confirmatory_assays(bioassay,confirmatory_assay) values(?,?)",
                    newRelations);
        });
    }


    private static void loadPrimaryAssays(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,primary_assay from pubchem.bioassay_primary_assays", oldRelations);

        dispatcher.on(bao + "BAO_0001067", (subject, object) -> {
            Integer bioassayID = getBioassayID(object.getURI());
            Integer primaryID = getBioassayID(subject.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(bioassayID, primaryID);

            if(oldRelations.remove(pair))
                keepRelations.add(pair);
            else if(!keepRelations.contains(pair))
                newRelations.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.bioassay_primary_assays where bioassay=? and primary_assay=?", oldRelations);
            store("insert into pubchem.bioassay_primary_assays(bioassay,primary_assay) values(?,?)", newRelations);
        });
    }


    private static void loadSummaryAssays(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select bioassay,summary_assay from pubchem.bioassay_summary_assays", oldRelations);

        dispatcher.on(bao + "BAO_0001094", (subject, object) -> {
            Integer bioassayID = getBioassayID(object.getURI());
            Integer summaryID = getBioassayID(subject.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(bioassayID, summaryID);

            if(oldRelations.remove(pair))
                keepRelations.add(pair);
            else if(!keepRelations.contains(pair))
                newRelations.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.bioassay_summary_assays where bioassay=? and summary_assay=?", oldRelations);
            store("insert into pubchem.bioassay_summary_assays(bioassay,summary_assay) values(?,?)", newRelations);
        });
    }


    static void load() throws XPathException, SQLException, IOException, ParserConfigurationException, SAXException
    {
        System.out.println("load bioassays ...");

        loadBioassays();

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadStages(dispatcher);
        loadConfirmatoryAssays(dispatcher);
        loadPrimaryAssays(dispatcher);
        loadSummaryAssays(dispatcher);

        dispatcher.load("pubchem/RDF/bioassay/pc_bioassay.ttl.gz");
        dispatcher.finish();

        bioassays.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish bioassays ...");

        bioassays.store();

        System.out.println();
    }


    static void addBioassayID(Integer bioassayID)
    {
        if(bioassays.reference(bioassayID))
            missingBioassays.referenced(bioassayID);
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
        return bioassays.size();
    }


    static class FilteredInputStream extends InputStream
    {
        private final InputStream inputStream;
        private final int[] buffer = new int[3];
        private int bufferPosition = 0;
        private int pushBackChar = -1;

        public FilteredInputStream(InputStream inputStream)
        {
            this.inputStream = new BufferedInputStream(inputStream);
        }

        @Override
        public int read() throws IOException
        {
            if(bufferPosition > 0)
                return buffer[--bufferPosition];

            int byteRead = pushBackChar != -1 ? pushBackChar : inputStream.read();

            if(byteRead <= 0b01111111)
                return byteRead;

            if(byteRead <= 0b10111111)
            {
                System.err.println("  wrong UTF-8 codding");
                return read();
            }

            int size = 0;

            if(byteRead <= 0b11011111)
                size = 0;
            else if(byteRead <= 0b11101111)
                size = 1;
            else if(byteRead <= 0b11110111)
                size = 2;

            for(int i = size; i >= 0; i--)
                if(!valid(buffer[i] = inputStream.read()))
                    return read();

            return byteRead;
        }

        private boolean valid(int i)
        {
            if(i > 0b01111111 && i <= 0b10111111)
            {
                bufferPosition++;
                return true;
            }
            else
            {
                bufferPosition = 0;
                pushBackChar = i;
                System.err.println("  wrong UTF-8 codding");
                return false;
            }
        }
    }
}
