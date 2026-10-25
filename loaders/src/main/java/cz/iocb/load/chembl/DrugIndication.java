package cz.iocb.load.chembl;

import static cz.iocb.chemweb.server.sparql.config.chembl.DrugIndication.efoUnits;
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
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



/**
 * Loader of the ChEMBL drug indications, the diseases the molecules are developed for, from the indication file of the
 * release into the table {@code chembl.drug_indications}.
 */
class DrugIndication extends Updater
{
    /**
     * IRI prefix of the drug indications.
     */
    static final String prefix = ChEMBL.chembl + "drug_indication/CHEMBL_IND_";

    /**
     * IRI prefix of the MeSH identifiers of the indications.
     */
    static final String meshPrefix = "http://identifiers.org/mesh/";

    /**
     * Table of the drug indications.
     */
    private static final EntityTable<Integer> indications = new EntityTable<>("chembl.drug_indications", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), integer("molecule"), varchar("mesh"), varchar("mesh_heading"),
            integer("efo_unit"), integer("efo_id"), varchar("efo_name"), integer("phase"));


    /**
     * Not instantiable.
     */
    private DrugIndication()
    {
    }


    /**
     * Loads the drug indications from the indication file of the release and checks the {@code cco:hasDrugIndication}
     * triples of the molecules against the links of the indications.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load drug indications ...");

        InverseCheck molecules = new InverseCheck("cco:hasMolecule");

        try(InputStream stream = getTtlStream(ChEMBL.file("indication")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(subject.getURI().startsWith(Molecule.prefix))
                    {
                        if(predicate.getURI().equals(cco + "hasDrugIndication"))
                            molecules.inverse(getIndicationID(object), Molecule.getMoleculeID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "DrugIndication");
                        case chemblId -> indications.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_IND_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_IND_" + id);
                        case cco + "hasMolecule" ->
                        {
                            int moleculeID = Molecule.getMoleculeID(object);
                            indications.set(id, "molecule", moleculeID);
                            molecules.forward(id, moleculeID);
                        }
                        case cco + "hasMesh" -> indications.set(id, "mesh", getStringID(object, meshPrefix));
                        case cco + "hasMeshHeading" -> indications.set(id, "mesh_heading", getString(object));
                        case cco + "hasEFO" ->
                        {
                            Pair<Integer, Integer> efo = Ontology.getResourceId(object, efoUnits);
                            indications.set(id, "efo_unit", efo.getOne());
                            indications.set(id, "efo_id", efo.getTwo());
                        }
                        case cco + "hasEFOName" -> indications.set(id, "efo_name", getString(object));
                        case cco + "highestDevelopmentPhase" -> indications.set(id, "phase", getInt(object));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        molecules.check();

        System.out.println();
    }


    /**
     * Stores the collected drug indications and reports the referenced ones that the file does not describe.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish drug indications ...");

        indications.store("drug indication");

        System.out.println();
    }


    /**
     * Returns the id of a drug indication and marks the drug indication as referenced.
     *
     * @param node the IRI of the drug indication
     * @return the id of the drug indication
     * @throws IOException if the node is not an IRI of a drug indication
     */
    static int getIndicationID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        indications.reference(id);
        return id;
    }
}
