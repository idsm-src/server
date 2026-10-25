package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the ChEMBL mechanisms of action of the drugs from the mechanism file of the release into the table
 * {@code chembl.mechanisms}.
 */
class Mechanism extends Updater
{
    /**
     * IRI prefix of the mechanisms.
     */
    static final String prefix = ChEMBL.chembl + "drug_mechanism/CHEMBL_MEC_";

    /**
     * Table of the mechanisms.
     */
    private static final EntityTable<Integer> mechanisms = new EntityTable<>("chembl.mechanisms", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), integer("molecule"), integer("target"), integer("binding_site"),
            varchar("description"), varchar("action_type"));


    /**
     * Not instantiable.
     */
    private Mechanism()
    {
    }


    /**
     * Loads the mechanisms from the mechanism file of the release and checks the triples of the molecules, targets and
     * binding sites linking them to the mechanisms against the links of the mechanisms.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load mechanisms ...");

        InverseCheck molecules = new InverseCheck("cco:hasMolecule");
        InverseCheck targets = new InverseCheck("cco:hasTarget");
        InverseCheck sites = new InverseCheck("cco:hasBindingSite");

        try(InputStream stream = getTtlStream(ChEMBL.file("moa")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(!subject.getURI().startsWith(prefix))
                    {
                        if(subject.getURI().startsWith(Molecule.prefix)
                                && predicate.getURI().equals(cco + "hasMechanism"))
                            molecules.inverse(getMechanismID(object), Molecule.getMoleculeID(subject));
                        else if(subject.getURI().startsWith(Target.prefix)
                                && predicate.getURI().equals(cco + "isTargetForMechanism"))
                            targets.inverse(getMechanismID(object), Target.getTargetID(subject));
                        else if(subject.getURI().startsWith(BindingSite.prefix)
                                && predicate.getURI().equals(cco + "isBindingSiteForMechanism"))
                            sites.inverse(getMechanismID(object), BindingSite.getSiteID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "Mechanism");
                        case chemblId -> mechanisms.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_MEC_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_MEC_" + id);
                        case cco + "hasMolecule" ->
                        {
                            int moleculeID = Molecule.getMoleculeID(object);
                            mechanisms.set(id, "molecule", moleculeID);
                            molecules.forward(id, moleculeID);
                        }
                        case cco + "hasTarget" ->
                        {
                            int targetID = Target.getTargetID(object);
                            mechanisms.set(id, "target", targetID);
                            targets.forward(id, targetID);
                        }
                        case cco + "hasBindingSite" ->
                        {
                            int siteID = BindingSite.getSiteID(object);
                            mechanisms.set(id, "binding_site", siteID);
                            sites.forward(id, siteID);
                        }
                        case cco + "mechanismDescription" -> mechanisms.set(id, "description", getString(object));
                        case cco + "mechanismActionType" -> mechanisms.set(id, "action_type", getString(object));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        molecules.check();
        targets.check();
        sites.check();

        System.out.println();
    }


    /**
     * Stores the collected mechanisms and reports the referenced ones that the file does not describe.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish mechanisms ...");

        mechanisms.store("mechanism");

        System.out.println();
    }


    /**
     * Returns the id of a mechanism and marks the mechanism as referenced.
     *
     * @param node the IRI of the mechanism
     * @return the id of the mechanism
     * @throws IOException if the node is not an IRI of a mechanism
     */
    static int getMechanismID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        mechanisms.reference(id);
        return id;
    }
}
