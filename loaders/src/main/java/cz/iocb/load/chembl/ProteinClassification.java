package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfs;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.chembl.ChEMBL.skos;
import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class ProteinClassification extends Updater
{
    static final String prefix = ChEMBL.chembl + "protclass/CHEMBL_PC_";

    private static final EntityTable<Integer> classes = new EntityTable<>("chembl.protein_classes", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("label"), varchar("level"), varchar("path"),
            integer("parent"));

    private static final ValueTable componentDescendants = new ValueTable("chembl.protein_class_component_descendants",
            column("protein_class"), column("component"));

    private static final ValueTable targetDescendants = new ValueTable("chembl.protein_class_target_descendants",
            column("protein_class"), column("target"));


    static void load() throws IOException, SQLException
    {
        System.out.println("load protein classifications ...");

        InverseCheck narrowers = new InverseCheck("rdfs:subClassOf");
        InverseCheck components = new InverseCheck("cco:hasTargetComponentDescendant");
        InverseCheck targets = new InverseCheck("cco:hasTargetDescendant");
        HashMap<Integer, String> prefLabels = new HashMap<>();
        HashMap<Integer, Integer> broaders = new HashMap<>();

        try(InputStream stream = getTtlStream(ChEMBL.file("protclass")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(subject.getURI().startsWith(Target.prefix))
                    {
                        if(predicate.getURI().equals(cco + "hasProteinClassification"))
                            targets.inverse(getClassID(object), Target.getTargetID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    if(subject.getURI().startsWith(TargetComponent.prefix))
                    {
                        if(predicate.getURI().equals(cco + "hasProteinClassification"))
                            components.inverse(getClassID(object), TargetComponent.getComponentID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "ProteinClassification");
                        case chemblId -> classes.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_PC_" + id));
                        case rdfsLabel -> classes.set(id, "label", getString(object));
                        case skos + "prefLabel" -> prefLabels.put(id, getString(object));
                        case cco + "classLevel" -> classes.set(id, "level", getString(object));
                        case cco + "classPath" -> classes.set(id, "path", getString(object));
                        case rdfs + "subClassOf" ->
                        {
                            int parentID = getClassID(object);
                            classes.set(id, "parent", parentID);
                            narrowers.forward(id, parentID);
                        }
                        case skos + "broader" -> broaders.put(id, getClassID(object));
                        case skos + "narrower" -> narrowers.inverse(getClassID(object), id);
                        case cco + "hasTargetComponentDescendant" ->
                        {
                            int componentID = TargetComponent.getComponentID(object);
                            componentDescendants.add(id, componentID);
                            components.forward(id, componentID);
                        }
                        case cco + "hasTargetDescendant" ->
                        {
                            int targetID = Target.getTargetID(object);
                            targetDescendants.add(id, targetID);
                            targets.forward(id, targetID);
                        }
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        narrowers.check();
        components.check();
        targets.check();

        // the mapping produces skos:prefLabel from rdfs:label and skos:broader from rdfs:subClassOf
        int labelIndex = classes.columnIndex("label");
        int parentIndex = classes.columnIndex("parent");

        for(Entry<Integer, Object[]> entry : classes.rows())
        {
            if(!Objects.equals(entry.getValue()[labelIndex], prefLabels.remove(entry.getKey())))
                Problems.error("skos:prefLabel different from rdfs:label", "CHEMBL_PC_" + entry.getKey());

            if(!Objects.equals(entry.getValue()[parentIndex], broaders.remove(entry.getKey())))
                Problems.error("skos:broader different from rdfs:subClassOf", "CHEMBL_PC_" + entry.getKey());
        }

        for(Integer id : prefLabels.keySet())
            Problems.error("skos:prefLabel different from rdfs:label", "CHEMBL_PC_" + id);

        for(Integer id : broaders.keySet())
            Problems.error("skos:broader different from rdfs:subClassOf", "CHEMBL_PC_" + id);

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish protein classifications ...");

        classes.store("protein classification");
        componentDescendants.store();
        targetDescendants.store();

        System.out.println();
    }


    static int getClassID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        classes.reference(id);
        return id;
    }
}
