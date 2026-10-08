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
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Activity extends Updater
{
    static final String prefix = ChEMBL.chembl + "activity/CHEMBL_ACT_";

    private static final EntityTable<Integer> activities = new EntityTable<>("chembl.activity_bases", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), integer("assay"), integer("molecule"), integer("document"),
            integer("endpoint_id"), integer("unit_id"), integer("qudt_id"), varchar("type"), varchar("relation"),
            float8("value"), varchar("units"), varchar("standard_type"), varchar("standard_relation"),
            float8("standard_value"), varchar("standard_units"), float8("pchembl"), varchar("comment"),
            varchar("validity_comment"), bool("potential_duplicate"));


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
                            ChEMBL.unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "Activity");
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
                        case bao + "BAO_0000208" ->
                        {
                            Pair<Integer, Integer> endpoint = ChEMBL.getOntologyId(subject, predicate, object, unitBAO);

                            if(endpoint != null)
                                activities.set(id, "endpoint_id", endpoint.getTwo());
                        }
                        case cco + "hasUnitOnto" ->
                        {
                            Pair<Integer, Integer> unit = ChEMBL.getOntologyId(subject, predicate, object, unitUO);

                            if(unit != null)
                                activities.set(id, "unit_id", unit.getTwo());
                        }
                        case cco + "hasQUDT" ->
                        {
                            Pair<Integer, Integer> unit = ChEMBL.getOntologyId(subject, predicate, object,
                                    unitUncategorized);

                            if(unit != null)
                                activities.set(id, "qudt_id", unit.getTwo());
                        }
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
                                ChEMBL.warning("false cco:dataValidityIssue", subject.getURI());
                        }
                        case cco + "potentialDuplicate" -> activities.set(id, "potential_duplicate",
                                getBoolean(object));
                        default -> ChEMBL.unexpected(subject, predicate, object);
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
                ChEMBL.warning("cco:dataValidityIssue not corresponding to cco:dataValidityComment",
                        "CHEMBL_ACT_" + entry.getKey());

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish activities ...");

        activities.store("activity");

        ChEMBL.finishLoad();
    }


    static int getActivityID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        activities.reference(id);
        return id;
    }


    static void setAssay(int activityID, int assayID) throws IOException
    {
        activities.set(activityID, "assay", assayID);
    }
}
