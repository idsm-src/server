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
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class DrugIndication extends Updater
{
    static final String prefix = ChEMBL.chembl + "drug_indication/CHEMBL_IND_";
    static final String meshPrefix = "http://identifiers.org/mesh/";

    private static final EntityTable<Integer> indications = new EntityTable<>("chembl.drug_indication_bases",
            intKey("id"), "chembl_id", uniqueVarchar("chembl_id"), integer("molecule"), varchar("mesh"),
            varchar("mesh_heading"), integer("efo_unit"), integer("efo_id"), varchar("efo_name"), integer("phase"));


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
                            ChEMBL.unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "DrugIndication");
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
                            Pair<Integer, Integer> efo = ChEMBL.getOntologyId(subject, predicate, object, null);

                            if(efo != null)
                            {
                                indications.set(id, "efo_unit", efo.getOne());
                                indications.set(id, "efo_id", efo.getTwo());
                            }
                        }
                        case cco + "hasEFOName" -> indications.set(id, "efo_name", getString(object));
                        case cco + "highestDevelopmentPhase" -> indications.set(id, "phase", getInt(object));
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        molecules.check();

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish drug indications ...");

        indications.store("drug indication");

        ChEMBL.finishLoad();
    }


    static int getIndicationID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        indications.reference(id);
        return id;
    }
}
