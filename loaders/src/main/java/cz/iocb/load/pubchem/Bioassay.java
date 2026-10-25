package cz.iocb.load.pubchem;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBAO;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
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
import java.util.zip.ZipEntry;
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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the PubChem bioassays from their XML descriptions in {@code pubchem/Bioassay/XML} and from
 * {@code pubchem/RDF/bioassay/pc_bioassay.ttl.gz} into the table {@code pubchem.bioassays} and the tables of their
 * texts, ChEMBL links, stages and related assays.
 */
class Bioassay extends Updater
{
    /**
     * A ZIP stream that stays open when the reader of an entry closes it.
     */
    private static class MyZipInputStream extends ZipInputStream
    {
        /**
         * Creates the stream.
         *
         * @param in the stream of the ZIP file
         */
        public MyZipInputStream(InputStream in)
        {
            super(in);
        }

        @Override
        public void close() throws IOException
        {
            //super.close();
        }

        /**
         * Closes the stream.
         *
         * @throws IOException if an I/O error occurs
         */
        public void myClose() throws IOException
        {
            super.close();
        }
    }


    /**
     * IRI prefix of the bioassays.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the bioassays.
     */
    private static final EntityTable<Integer> bioassays = new EntityTable<>("pubchem.bioassays", intKey("id"), null,
            integer("source"), uniqueVarchar("title"));

    /**
     * Bioassays referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingBioassays = new MissingEntities<>("bioassay", false);


    /**
     * Not instantiable.
     */
    private Bioassay()
    {
    }


    /**
     * Returns the text contents of the elements given by the path, one per line.
     *
     * @param path the path of the elements
     * @param node the node the path starts from
     * @return the text contents of the elements, empty if there is none
     * @throws XPathExpressionException if the path cannot be evaluated
     */
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


    /**
     * Returns the value of the single element given by the path; no element or several elements of the given name are
     * refused.
     *
     * @param path the path of the element
     * @param node the node the path starts from
     * @param name the name of the element shown in the error
     * @return the text content of the element
     * @throws XPathExpressionException if the path cannot be evaluated
     * @throws DataException if there is no such element or there are several
     */
    private static String getSingleNodeValue(XPathExpression path, Node node, String name)
            throws XPathExpressionException, DataException
    {
        NodeList nodes = (NodeList) path.evaluate(node, XPathConstants.NODESET);

        if(nodes.getLength() != 1)
            throw new DataException("not a single element " + name, nodes.getLength() + " elements");

        return nodes.item(0).getTextContent();
    }


    /**
     * Returns the identifier of the source IRI for the name of a source in the XML description: {@code ID} followed by
     * a numeric name, otherwise the name cut at its first parenthesis with the characters {@code -,/.&} and spaces
     * replaced by underscores.
     *
     * @param sourceName the name of the source
     * @return the identifier of the source IRI
     */
    private static String createSourceID(String sourceName)
    {
        if(sourceName.matches("[0-9]+"))
            return "ID" + sourceName;
        else
            return sourceName.replaceFirst("\\(.*", "").replaceAll("[- ,/.&]", "_");
    }


    /**
     * Loads the bioassays described by the XML; the ChEMBL assays and mechanisms of all of them are also put into the
     * given maps, against which pc_bioassay is checked. The texts and the ChEMBL links are stored at once.
     *
     * @param chemblAssays the map receiving the ChEMBL assay of each bioassay
     * @param chemblMechanisms the map receiving the ChEMBL mechanism of each bioassay
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     * @throws XPathException if an XPath expression cannot be compiled or evaluated
     * @throws ParserConfigurationException if no XML parser can be created
     * @throws SAXException if an XML document cannot be parsed
     */
    private static void loadBioassays(IntIntMap chemblAssays, IntIntMap chemblMechanisms)
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

            ZipEntry entry;

            while((entry = zipStream.getNextEntry()) != null)
            {
                String name = file + "/" + entry.getName();

                try(InputStream gzipStream = new FilteredInputStream(new GZIPInputStream(zipStream), name))
                {
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(gzipStream);

                    Node baseNode = (Node) basePath.evaluate(document.getDocumentElement(), XPathConstants.NODE);

                    if(baseNode == null)
                        throw new DataException("no element PC-AssayDescription");


                    Integer bioassayID = Integer.parseInt(getSingleNodeValue(idPath, baseNode, "PC-ID_id"));

                    bioassays.reference(bioassayID);


                    String sourceName = getSingleNodeValue(sourceNamePath, baseNode, "PC-DBTracking_name");
                    Integer sourceID = Source.registerSourceID(createSourceID(sourceName), sourceName);

                    bioassays.set(bioassayID, "source", sourceID);


                    String title = getSingleNodeValue(titlePath, baseNode, "PC-AssayDescription_name");

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
                        String chemblSource = getSingleNodeValue(trackingSourcePath, baseNode, "Object-id_str");

                        if(chemblSource.startsWith("drug_mech_"))
                        {
                            Integer chemblID = Integer.parseInt(chemblSource.replaceFirst("^drug_mech_", ""));

                            synchronized(newMechanisms)
                            {
                                if(!chemblID.equals(oldMechanisms.remove(bioassayID)))
                                    newMechanisms.put(bioassayID, chemblID);

                                chemblMechanisms.put(bioassayID, chemblID);
                            }
                        }
                        else if(chemblSource.startsWith("CHEMBL"))
                        {
                            Integer chemblID = Integer.parseInt(chemblSource.replaceFirst("^CHEMBL", ""));

                            synchronized(newAssays)
                            {
                                if(!chemblID.equals(oldAssays.remove(bioassayID)))
                                    newAssays.put(bioassayID, chemblID);

                                chemblAssays.put(bioassayID, chemblID);
                            }
                        }
                        else
                        {
                            throw new DataException("unexpected ChEMBL source", chemblSource);
                        }
                    }
                }
                catch(DataException e)
                {
                    Problems.error(e.getKind(), name + (e.getDetail() == null ? "" : ": " + e.getDetail()));
                }
                catch(NumberFormatException e)
                {
                    Problems.error("malformed number", name + ": " + e.getMessage());
                }
                catch(SAXException e)
                {
                    Problems.error("malformed XML", name + ": " + e.getMessage());
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


    /**
     * Registers the checks of the predicates, types and values of the bioassay file.
     *
     * @param dispatcher the dispatcher of the triples
     */
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


    /**
     * Checks that the title, the source and the ChEMBL assay or mechanism of a bioassay in pc_bioassay are those of its
     * XML description, which are published. PubChem escapes some titles in pc_bioassay once more: every backslash and
     * quotation mark of the XML title is preceded by a backslash. The XML is newer than pc_bioassay, so it describes
     * also the bioassays added since and its values are not checked against pc_bioassay; a title edited since is only a
     * warning.
     *
     * @param dispatcher the dispatcher of the triples
     * @param chemblAssays the ChEMBL assays of the bioassays by the XML descriptions
     * @param chemblMechanisms the ChEMBL mechanisms of the bioassays by the XML descriptions
     */
    private static void checkDescriptions(TripleDispatcher dispatcher, IntIntMap chemblAssays,
            IntIntMap chemblMechanisms)
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            String title = (String) bioassays.get(bioassayID, "title");
            String value = getString(object);

            if(title == null)
                throw new DataException("bioassay without its XML description");

            if(!value.equals(title) && !value.equals(title.replace("\\", "\\\\").replace("\"", "\\\"")))
                Problems.warning("value different from the XML in " + dcterms + "title",
                        text(subject) + " " + text(object) + " (" + title + ")");
        });

        dispatcher.on(dcterms + "source", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            Integer sourceID = Source.getSourceID(object.getURI());

            if(bioassays.get(bioassayID, "title") == null)
                throw new DataException("bioassay without its XML description");

            if(!sourceID.equals(bioassays.get(bioassayID, "source")))
                throw new DataException("value different from the XML");
        });

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            Integer chemblID;
            Integer described;

            if(startsWith(object, "http://rdf.ebi.ac.uk/resource/chembl/assay/CHEMBL"))
            {
                chemblID = getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/assay/CHEMBL");
                described = chemblAssays.get(bioassayID);
            }
            else if(startsWith(object, "http://rdf.ebi.ac.uk/resource/chembl/assay/drug_mech_"))
            {
                chemblID = getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/assay/drug_mech_");
                described = chemblMechanisms.get(bioassayID);
            }
            else
            {
                return;
            }

            if(bioassays.get(bioassayID, "title") == null)
                throw new DataException("bioassay without its XML description");

            if(!chemblID.equals(described))
                throw new DataException("value different from the XML");
        });
    }


    /**
     * Registers the measuregroups of the bioassays. A measuregroup is published as one of the bioassay in its IRI, so
     * this has to be the bioassay that the measuregroup is stated for.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void loadMeasuregroups(TripleDispatcher dispatcher)
    {
        dispatcher.on(bao + "BAO_0000209", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            Pair<Integer, Integer> measuregroup = Measuregroup.parseMeasuregroup(object);

            if(!measuregroup.getOne().equals(bioassayID))
                Problems.error("value of " + bao + "BAO_0000209 not matching the IRI",
                        text(subject) + " " + text(object));
        });
    }


    /**
     * Registers the handler of the stages of the bioassays, at most one per bioassay, and the action that stores their
     * changes in {@code pubchem.bioassay_stages}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadStages(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntMap keepStages = new IntIntMap();
        IntIntMap newStages = new IntIntMap();
        IntIntMap oldStages = new IntIntMap();

        load("select bioassay,stage_id from pubchem.bioassay_stages", oldStages);

        dispatcher.on(bao + "BAO_0000210", (subject, object) -> {
            Integer bioassayID = getBioassayID(subject.getURI());
            Pair<Integer, Integer> stage = Ontology.getResourceId(object, unitBAO);

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
                    throw new DataException("multiple values of pubchem.bioassay_stages.stage_id",
                            bioassayID + ": " + keep + ", " + stage.getTwo());

                Integer put = newStages.put(bioassayID, stage.getTwo());

                if(put != null && !stage.getTwo().equals(put))
                    throw new DataException("multiple values of pubchem.bioassay_stages.stage_id",
                            bioassayID + ": " + put + ", " + stage.getTwo());
            }
        });

        dispatcher.after(() -> {
            store("delete from pubchem.bioassay_stages where bioassay=? and stage_id=?", oldStages);
            store("insert into pubchem.bioassay_stages(bioassay,stage_id) values(?,?) "
                    + "on conflict(bioassay) do update set stage_id=EXCLUDED.stage_id", newStages);
        });
    }


    /**
     * Registers the handler of the confirmatory assays of the bioassays and the action that stores their changes in
     * {@code pubchem.bioassay_confirmatory_assays}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the primary assays of the bioassays and the action that stores their changes in
     * {@code pubchem.bioassay_primary_assays}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the summary assays of the bioassays and the action that stores their changes in
     * {@code pubchem.bioassay_summary_assays}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Loads the bioassays from their XML descriptions and from {@code pubchem/RDF/bioassay/pc_bioassay.ttl.gz} into the
     * tables and flushes the table of the bioassays.
     *
     * @throws XPathException if an XPath expression cannot be compiled or evaluated
     * @throws SQLException if a database error occurs
     * @throws IOException if an I/O error occurs
     * @throws ParserConfigurationException if no XML parser can be created
     * @throws SAXException if an XML document cannot be parsed
     */
    static void load() throws XPathException, SQLException, IOException, ParserConfigurationException, SAXException
    {
        System.out.println("load bioassays ...");

        IntIntMap chemblAssays = new IntIntMap();
        IntIntMap chemblMechanisms = new IntIntMap();

        loadBioassays(chemblAssays, chemblMechanisms);

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        checkDescriptions(dispatcher, chemblAssays, chemblMechanisms);
        loadStages(dispatcher);
        loadConfirmatoryAssays(dispatcher);
        loadPrimaryAssays(dispatcher);
        loadSummaryAssays(dispatcher);
        loadMeasuregroups(dispatcher);

        dispatcher.load("pubchem/RDF/bioassay/pc_bioassay.ttl.gz");
        dispatcher.finish();

        bioassays.flush();

        System.out.println();
    }


    /**
     * Stores the bioassays, including those referenced by the parts loaded later, and deletes the stored bioassays that
     * are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish bioassays ...");

        bioassays.store();

        System.out.println();
    }


    /**
     * Ensures that the bioassay of an id has a row; a bioassay without an XML description is counted as missing.
     *
     * @param bioassayID the id of the bioassay
     */
    static void addBioassayID(Integer bioassayID)
    {
        if(bioassays.reference(bioassayID))
            missingBioassays.referenced(bioassayID);
    }


    /**
     * Returns the id of the bioassay of an IRI and ensures that the bioassay has a row; a bioassay without an XML
     * description is counted as missing.
     *
     * @param value the IRI of the bioassay
     * @return the id of the bioassay
     * @throws IOException if the IRI is not an IRI of a bioassay
     */
    static Integer getBioassayID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer bioassayID = Integer.parseInt(value.substring(prefixLength));

        addBioassayID(bioassayID);

        return bioassayID;
    }


    /**
     * Returns the number of the bioassays.
     *
     * @return the number of the bioassays
     */
    public static int size()
    {
        return bioassays.size();
    }


    /**
     * An input stream that skips the invalid UTF-8 sequences of a bioassay description with a warning.
     */
    static class FilteredInputStream extends InputStream
    {
        /**
         * The filtered stream.
         */
        private final InputStream inputStream;

        /**
         * The name of the file shown in the warnings.
         */
        private final String name;

        /**
         * The continuation bytes of the last sequence read ahead, in reverse order.
         */
        private final int[] buffer = new int[3];

        /**
         * The number of the bytes in {@link #buffer} not returned yet.
         */
        private int bufferPosition = 0;

        /**
         * The byte that has ended an invalid sequence and is read again, or -1.
         */
        private int pushBackChar = -1;

        /**
         * Creates the stream.
         *
         * @param inputStream the filtered stream
         * @param name the name of the file shown in the warnings
         */
        public FilteredInputStream(InputStream inputStream, String name)
        {
            this.inputStream = new BufferedInputStream(inputStream);
            this.name = name;
        }

        /*
         * Returns the next byte of a valid UTF-8 sequence; the bytes of an invalid sequence are skipped with a
         * warning, and the byte that has ended it prematurely is read again.
         */
        @Override
        public int read() throws IOException
        {
            while(true)
            {
                if(bufferPosition > 0)
                    return buffer[--bufferPosition];

                int byteRead = pushBackChar;

                if(byteRead != -1)
                    pushBackChar = -1;
                else
                    byteRead = inputStream.read();

                if(byteRead <= 0b01111111)
                    return byteRead;

                if(byteRead <= 0b10111111 || byteRead > 0b11110111)
                {
                    Problems.warning("invalid UTF-8 sequence skipped", name);
                    continue;
                }

                int size = byteRead <= 0b11011111 ? 0 : byteRead <= 0b11101111 ? 1 : 2;
                boolean complete = true;

                for(int i = size; i >= 0 && complete; i--)
                    complete = valid(buffer[i] = inputStream.read());

                if(complete)
                    return byteRead;
            }
        }

        /**
         * Tests whether a byte read ahead continues the current sequence and counts it in {@link #buffer}; otherwise
         * the sequence is skipped with a warning and the byte is pushed back.
         *
         * @param i the byte read ahead
         * @return whether the byte is a continuation byte
         */
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
                Problems.warning("invalid UTF-8 sequence skipped", name);
                return false;
            }
        }
    }
}
