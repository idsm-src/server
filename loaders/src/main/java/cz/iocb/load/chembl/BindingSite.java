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
 * Loader of the ChEMBL binding sites of the targets from the binding site file of the release into the table
 * {@code chembl.binding_sites}.
 */
class BindingSite extends Updater
{
    /**
     * IRI prefix of the binding sites.
     */
    static final String prefix = ChEMBL.chembl + "binding_site/CHEMBL_BS_";

    /**
     * Table of the binding sites.
     */
    private static final EntityTable<Integer> sites = new EntityTable<>("chembl.binding_sites", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("name"), integer("target"));


    /**
     * Not instantiable.
     */
    private BindingSite()
    {
    }


    /**
     * Loads the binding sites from the binding site file of the release and checks the {@code cco:hasBindingSite}
     * triples of the targets against their {@code cco:hasTarget} links.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load binding sites ...");

        InverseCheck targets = new InverseCheck("cco:hasTarget");

        try(InputStream stream = getTtlStream(ChEMBL.file("bindingsite")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(subject.getURI().startsWith(Target.prefix))
                    {
                        if(predicate.getURI().equals(cco + "hasBindingSite"))
                            targets.inverse(getSiteID(object), Target.getTargetID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "BindingSite");
                        case chemblId -> sites.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_BS_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_BS_" + id);
                        case cco + "bindingSiteName" -> sites.set(id, "name", getString(object));
                        case cco + "hasTarget" ->
                        {
                            int targetID = Target.getTargetID(object);
                            sites.set(id, "target", targetID);
                            targets.forward(id, targetID);
                        }
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        targets.check();

        System.out.println();
    }


    /**
     * Stores the collected binding sites and reports the referenced ones that the file does not describe.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish binding sites ...");

        sites.store("binding site");

        System.out.println();
    }


    /**
     * Returns the id of a binding site and marks the binding site as referenced.
     *
     * @param node the IRI of the binding site
     * @return the id of the binding site
     * @throws IOException if the node is not an IRI of a binding site
     */
    static int getSiteID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        sites.reference(id);
        return id;
    }
}
