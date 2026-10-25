package cz.iocb.load.chembl;

import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitBAO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUO;
import static cz.iocb.chemweb.server.sparql.config.ontology.OntologyResource.unitUncategorized;
import static cz.iocb.load.chembl.ChEMBL.bao;
import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.common.EntityTable.bool;
import static cz.iocb.load.common.EntityTable.float8;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the ChEMBL activities, the measured results of the assays, from the activity file of the release into the
 * table {@code chembl.activities}.
 */
class Activity extends Updater
{
    /**
     * IRI prefix of the activities.
     */
    static final String prefix = ChEMBL.chembl + "activity/CHEMBL_ACT_";

    /**
     * Table of the activities.
     */
    private static final EntityTable<Integer> activities = new EntityTable<>("chembl.activities", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), integer("assay"), integer("molecule"), integer("document"),
            integer("endpoint_id"), integer("unit_id"), integer("qudt_id"), varchar("type"), varchar("relation"),
            float8("value"), varchar("units"), varchar("standard_type"), varchar("standard_relation"),
            float8("standard_value"), varchar("standard_units"), float8("pchembl"), varchar("comment"),
            varchar("validity_comment"), bool("potential_duplicate"));


    /**
     * Not instantiable.
     */
    private Activity()
    {
    }


    /**
     * Loads the activities from the activity file of the release, checks the {@code cco:hasActivity} triples of the
     * documents and molecules against the links of the activities, and checks that the activities with a
     * {@code cco:dataValidityComment} are exactly those with {@code cco:dataValidityIssue}.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load activities ...");

        InverseCheck documents = new InverseCheck("cco:hasDocument");
        InverseCheck molecules = new InverseCheck("cco:hasMolecule");
        BitSet validityIssues = new BitSet();

        try(InputStream stream = getTtlStream(ChEMBL.file("activity")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(!subject.getURI().startsWith(prefix))
                    {
                        if(subject.getURI().startsWith(Document.prefix)
                                && predicate.getURI().equals(cco + "hasActivity"))
                            documents.inverse(getActivityID(object), Document.getDocumentID(subject));
                        else if(subject.getURI().startsWith(Molecule.prefix)
                                && predicate.getURI().equals(cco + "hasActivity"))
                            molecules.inverse(getActivityID(object), Molecule.getMoleculeID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "Activity");
                        case chemblId -> activities.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_ACT_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_ACT_" + id);
                        case cco + "hasMolecule" ->
                        {
                            int moleculeID = Molecule.getMoleculeID(object);
                            activities.set(id, "molecule", moleculeID);
                            molecules.forward(id, moleculeID);
                        }
                        case cco + "hasDocument" ->
                        {
                            int documentID = Document.getDocumentID(object);
                            activities.set(id, "document", documentID);
                            documents.forward(id, documentID);
                        }
                        case bao + "BAO_0000208" -> activities.set(id, "endpoint_id",
                                Ontology.getResourceId(object, unitBAO).getTwo());
                        case cco + "hasUnitOnto" -> activities.set(id, "unit_id",
                                Ontology.getResourceId(object, unitUO).getTwo());
                        case cco + "hasQUDT" -> activities.set(id, "qudt_id",
                                Ontology.getResourceId(object, unitUncategorized).getTwo());
                        case cco + "type" -> activities.set(id, "type", getString(object));
                        case cco + "relation" -> activities.set(id, "relation", getString(object));
                        case cco + "value" -> activities.set(id, "value", getDouble(object));
                        case cco + "units" -> activities.set(id, "units", getString(object));
                        case cco + "standardType" -> activities.set(id, "standard_type", getString(object));
                        case cco + "standardRelation" -> activities.set(id, "standard_relation", getString(object));
                        case cco + "standardValue" -> activities.set(id, "standard_value", getDouble(object));
                        case cco + "standardUnits" -> activities.set(id, "standard_units", getString(object));
                        case cco + "pChembl" -> activities.set(id, "pchembl", getDouble(object));
                        case cco + "activityComment" -> activities.set(id, "comment", getString(object));
                        case cco + "dataValidityComment" -> activities.set(id, "validity_comment", getString(object));
                        case cco + "dataValidityIssue" ->
                        {
                            if(getBoolean(object))
                                validityIssues.set(id);
                            else
                                Problems.error("false cco:dataValidityIssue", subject.getURI());
                        }
                        case cco + "potentialDuplicate" -> activities.set(id, "potential_duplicate",
                                getBoolean(object));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        documents.check();
        molecules.check();

        // the mapping produces cco:dataValidityIssue true for the activities with cco:dataValidityComment
        int commentIndex = activities.columnIndex("validity_comment");

        for(Entry<Integer, Object[]> entry : activities.rows())
            if((entry.getValue()[commentIndex] != null) != validityIssues.get(entry.getKey()))
                Problems.error("cco:dataValidityIssue not corresponding to cco:dataValidityComment",
                        "CHEMBL_ACT_" + entry.getKey());

        System.out.println();
    }


    /**
     * Stores the collected activities and reports the referenced ones that the file does not describe.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish activities ...");

        activities.store("activity");

        System.out.println();
    }


    /**
     * Returns the id of an activity and marks the activity as referenced.
     *
     * @param node the IRI of the activity
     * @return the id of the activity
     * @throws IOException if the node is not an IRI of an activity
     */
    static int getActivityID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        activities.reference(id);
        return id;
    }


    /**
     * Sets the assay of an activity, which the assay file gives.
     *
     * @param activityID the id of the activity
     * @param assayID the id of the assay
     * @throws IOException if the activity already has another assay
     */
    static void setAssay(int activityID, int assayID) throws IOException
    {
        activities.set(activityID, "assay", assayID);
    }
}
