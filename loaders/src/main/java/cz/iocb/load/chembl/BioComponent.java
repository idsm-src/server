package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
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
 * Loader of the ChEMBL biocomponents, the components of the biotherapeutic molecules, from the biocomponent file of the
 * release into the table {@code chembl.biocomponents}; the file also gives labels of taxonomies.
 */
class BioComponent extends Updater
{
    /**
     * IRI prefix of the biocomponents.
     */
    static final String prefix = ChEMBL.chembl + "biocomponent/CHEMBL_BC_";

    /**
     * Table of the biocomponents.
     */
    private static final EntityTable<Integer> biocomponents = new EntityTable<>("chembl.biocomponents", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("type"), varchar("description"), varchar("organism"),
            integer("taxonomy"), varchar("sequence"));


    /**
     * Not instantiable.
     */
    private BioComponent()
    {
    }


    /**
     * Loads the biocomponents from the biocomponent file of the release together with the labels of their taxonomies.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load biocomponents ...");

        Taxonomy.Forms taxonomies = new Taxonomy.Forms("biocomponent");

        try(InputStream stream = getTtlStream(ChEMBL.file("biocmpt")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(Taxonomy.isTaxonomy(subject))
                    {
                        Taxonomy.addLabel(subject, predicate, object);
                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "BioComponent");
                        case chemblId -> biocomponents.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_BC_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_BC_" + id);
                        case cco + "componentType" -> biocomponents.set(id, "type", getString(object));
                        case dcterms + "description" -> biocomponents.set(id, "description", getString(object));
                        case cco + "organismName" -> biocomponents.set(id, "organism", getString(object));
                        case cco + "taxonomy" -> biocomponents.set(id, "taxonomy", taxonomies.getTaxonomy(id, object));
                        case cco + "proteinSequence" -> biocomponents.set(id, "sequence", getString(object));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();

        System.out.println();
    }


    /**
     * Stores the collected biocomponents and reports the referenced ones that the file does not describe.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish biocomponents ...");

        biocomponents.store("biocomponent");

        System.out.println();
    }


    /**
     * Returns the id of a biocomponent and marks the biocomponent as referenced.
     *
     * @param node the IRI of the biocomponent
     * @return the id of the biocomponent
     * @throws IOException if the node is not an IRI of a biocomponent
     */
    static int getBioComponentID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        biocomponents.reference(id);
        return id;
    }
}
