package cz.iocb.load.chembl;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBAO;
import static cz.iocb.load.chembl.ChEMBL.bao;
import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



class Assay extends Updater
{
    static final String prefix = ChEMBL.chembl + "assay/CHEMBL";
    static final String pubchemPrefix = "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=";
    static final String panelMember = "Panel member name:";

    private static final EntityTable<Integer> assays = new EntityTable<>("chembl.assays", intKey("id"), "chembl_id",
            uniqueVarchar("chembl_id"), varchar("type"), varchar("description"), integer("document"), integer("target"),
            integer("source"), integer("cell_line"), integer("format_id"), varchar("organism"), integer("taxonomy"),
            varchar("category"), varchar("cell_type"), varchar("strain"), varchar("tissue"),
            varchar("subcellular_fraction"), varchar("test_type"), varchar("relationship_type"),
            varchar("relationship_desc"), integer("confidence_score"), varchar("confidence_desc"),
            integer("pubchem_assay"), integer("pubchem_bioassay"));

    // the labels of the PubChem assay IRIs, which carry one label per assay referencing them
    private static final ValueTable referenceLabels = new ValueTable("chembl.assay_reference_labels",
            column("reference"), column("label"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load assays ...");

        Taxonomy.Forms taxonomies = new Taxonomy.Forms("assay");
        InverseCheck activities = new InverseCheck("cco:hasAssay");
        InverseCheck documents = new InverseCheck("cco:hasDocument");
        InverseCheck targets = new InverseCheck("cco:hasTarget");
        InverseCheck sources = new InverseCheck("cco:hasSource");
        InverseCheck cellLines = new InverseCheck("cco:hasCellLine");
        Set<Integer> xrefTypes = new HashSet<>();

        try(InputStream stream = getTtlStream(ChEMBL.file("assay")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    String iri = subject.getURI();

                    if(!iri.startsWith(prefix))
                    {
                        if(iri.startsWith(Activity.prefix) && predicate.getURI().equals(cco + "hasAssay"))
                        {
                            int activityID = Activity.getActivityID(subject);
                            int assayID = getAssayID(object);
                            Activity.setAssay(activityID, assayID);
                            activities.forward(activityID, assayID);
                        }
                        else if(iri.startsWith(Document.prefix) && predicate.getURI().equals(cco + "hasAssay"))
                            documents.inverse(getAssayID(object), Document.getDocumentID(subject));
                        else if(iri.startsWith(Target.prefix) && predicate.getURI().equals(cco + "hasAssay"))
                            targets.inverse(getAssayID(object), Target.getTargetID(subject));
                        else if(iri.startsWith(Source.prefix) && predicate.getURI().equals(cco + "hasAssay"))
                            sources.inverse(getAssayID(object), Source.getSourceID(subject));
                        else if(iri.startsWith(CellLine.prefix)
                                && predicate.getURI().equals(cco + "isCellLineForAssay"))
                            cellLines.inverse(getAssayID(object), CellLine.getCellLineID(subject));
                        else if(iri.startsWith(pubchemPrefix) && predicate.getURI().equals(rdfType))
                        {
                            ChEMBL.checkType(subject, object, cco + "PubchemBioassayRef");
                            xrefTypes.add(getIntID(subject, pubchemPrefix));
                        }
                        else if(iri.startsWith(pubchemPrefix) && predicate.getURI().equals(rdfsLabel))
                            referenceLabels.add(getIntID(subject, pubchemPrefix), getString(object));
                        else
                            ChEMBL.unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "Assay");
                        case chemblId -> assays.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL" + id);
                        case cco + "assayType" -> assays.set(id, "type", getString(object));
                        case dcterms + "description" -> assays.set(id, "description", getString(object));
                        case cco + "hasDocument" ->
                        {
                            int documentID = Document.getDocumentID(object);
                            assays.set(id, "document", documentID);
                            documents.forward(id, documentID);
                        }
                        case cco + "hasTarget" ->
                        {
                            int targetID = Target.getTargetID(object);
                            assays.set(id, "target", targetID);
                            targets.forward(id, targetID);
                        }
                        case cco + "hasSource" ->
                        {
                            int sourceID = Source.getSourceID(object);
                            assays.set(id, "source", sourceID);
                            sources.forward(id, sourceID);
                        }
                        case cco + "hasCellLine" ->
                        {
                            int cellLineID = CellLine.getCellLineID(object);
                            assays.set(id, "cell_line", cellLineID);
                            cellLines.forward(id, cellLineID);
                        }
                        case bao + "BAO_0000205" -> assays.set(id, "format_id",
                                Ontology.getResourceId(object, unitBAO).getTwo());
                        case cco + "organismName" -> assays.set(id, "organism", getString(object));
                        case cco + "taxonomy" -> assays.set(id, "taxonomy", taxonomies.getTaxonomy(id, object));
                        case cco + "assayCategory" -> assays.set(id, "category", getString(object));
                        case cco + "assayCellType" -> assays.set(id, "cell_type", getString(object));
                        case cco + "assayStrain" -> assays.set(id, "strain", getString(object));
                        case cco + "assayTissue" -> assays.set(id, "tissue", getString(object));
                        case cco + "assaySubCellFrac" -> assays.set(id, "subcellular_fraction", getString(object));
                        case cco + "assayTestType" -> assays.set(id, "test_type", getString(object));
                        case cco + "targetRelType" -> assays.set(id, "relationship_type", getString(object));
                        case cco + "targetRelDesc" -> assays.set(id, "relationship_desc", getString(object));
                        case cco + "targetConfScore" -> assays.set(id, "confidence_score", getInt(object));
                        case cco + "targetConfDesc" -> assays.set(id, "confidence_desc", getString(object));
                        case cco + "assayXref" -> assays.set(id, "pubchem_assay", getIntID(object, pubchemPrefix));
                        case cco + "hasActivity" -> activities.inverse(Activity.getActivityID(object), id);
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();
        activities.check();
        documents.check();
        targets.check();
        sources.check();
        cellLines.check();

        checkXrefs(xrefTypes);
        setPubchemBioassays();

        System.out.println();
    }


    /*
     * The mapping produces rdf:type of the PubChem assay references from the stored values.
     */
    private static void checkXrefs(Set<Integer> types)
    {
        int xrefIndex = assays.columnIndex("pubchem_assay");
        Set<Integer> xrefs = new HashSet<>();

        for(Entry<Integer, Object[]> entry : assays.rows())
            if(entry.getValue()[xrefIndex] != null)
                xrefs.add((Integer) entry.getValue()[xrefIndex]);

        for(Integer xref : xrefs)
            if(!types.contains(xref))
                Problems.error("reference without rdf:type", pubchemPrefix + xref);

        for(Integer xref : types)
            if(!xrefs.contains(xref))
                Problems.error("unreferenced reference", pubchemPrefix + xref);
    }


    /*
     * The assays of PubChem panel assays reference the AID of the panel concatenated with the number of the panel
     * member (AID 493040 member 169 gives aid=493040169). The extension link to the PubChem assay uses the AID of the
     * panel: the panel members are recognized by their descriptions, which are equal up to the name of the member, and
     * the AID of the panel is the common prefix of the references of its members.
     */
    private static void setPubchemBioassays() throws IOException
    {
        int descriptionIndex = assays.columnIndex("description");
        int xrefIndex = assays.columnIndex("pubchem_assay");
        Map<String, List<Integer>> panels = new HashMap<>();

        for(Entry<Integer, Object[]> entry : assays.rows())
        {
            Integer xref = (Integer) entry.getValue()[xrefIndex];
            String description = (String) entry.getValue()[descriptionIndex];

            if(xref == null)
                continue;

            int index = description == null ? -1 : description.indexOf(panelMember);

            if(index < 0)
                assays.set(entry.getKey(), "pubchem_bioassay", xref);
            else
                panels.computeIfAbsent(description.substring(0, index), k -> new ArrayList<>()).add(entry.getKey());
        }

        int resolved = 0;

        for(List<Integer> panel : panels.values())
        {
            List<String> xrefs = panel.stream().map(id -> assays.get(id, "pubchem_assay").toString()).toList();
            String base = xrefs.get(0);

            for(String xref : xrefs)
                while(!xref.startsWith(base))
                    base = base.substring(0, base.length() - 1);

            Set<String> members = new HashSet<>();
            boolean valid = panel.size() > 1 && !base.isEmpty();

            for(String xref : xrefs)
            {
                String member = xref.substring(base.length());
                valid &= member.matches("[1-9][0-9]*") && members.add(member);
            }

            if(valid && members.contains("1"))
            {
                for(Integer id : panel)
                    assays.set(id, "pubchem_bioassay", Integer.parseInt(base));

                resolved += panel.size();
            }
            else
            {
                for(Integer id : panel)
                    Problems.warning("unresolved PubChem panel assay",
                            "CHEMBL" + id + " " + pubchemPrefix + assays.get(id, "pubchem_assay"));
            }
        }

        System.out.println("    resolved " + resolved + " members of " + panels.size() + " PubChem panel assays");
    }


    static void finish() throws SQLException
    {
        System.out.println("finish assays ...");

        assays.store("assay");
        referenceLabels.store();

        System.out.println();
    }


    static int getAssayID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        assays.reference(id);
        return id;
    }


    static int size()
    {
        return assays.size();
    }
}
