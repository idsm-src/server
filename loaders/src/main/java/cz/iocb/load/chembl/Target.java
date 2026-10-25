package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.chembl.ChEMBL.skos;
import static cz.iocb.load.chembl.ChEMBL.voidInDataset;
import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.chembl.ValueTable.enumeration;
import static cz.iocb.load.common.EntityTable.bool;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the ChEMBL targets from the target file of the release into the table {@code chembl.targets}, together
 * with their components, the target component matches of the linksets and the relations between the targets.
 */
class Target extends Updater
{
    /**
     * IRI prefix of the targets.
     */
    static final String prefix = ChEMBL.chembl + "target/CHEMBL";

    /**
     * Local names of the {@code cco} classes of the targets by their {@code cco:targetType}, which determines the
     * {@code rdf:type} of a target.
     */
    private static final Map<String, String> types = new HashMap<>();

    static
    {
        types.put("SINGLE PROTEIN", "SingleProtein");
        types.put("ORGANISM", "Organism");
        types.put("CELL-LINE", "CellLineTarget");
        types.put("PROTEIN COMPLEX", "ProteinComplex");
        types.put("PROTEIN FAMILY", "ProteinFamily");
        types.put("TISSUE", "Tissue");
        types.put("SELECTIVITY GROUP", "ProteinSelectivityGroup");
        types.put("PROTEIN-PROTEIN INTERACTION", "ProteinProteinInteraction");
        types.put("PROTEIN COMPLEX GROUP", "ProteinComplexGroup");
        types.put("NUCLEIC-ACID", "NucleicAcid");
        types.put("SMALL MOLECULE", "SmallMoleculeTarget");
        types.put("UNKNOWN", "UnknownTarget");
        types.put("CHIMERIC PROTEIN", "ChimericProtein");
        types.put("MACROMOLECULE", "Macromolecule");
        types.put("SUBCELLULAR", "SubCellular");
        types.put("OLIGOSACCHARIDE", "OligosaccharideTarget");
        types.put("METAL", "Metal");
        types.put("PROTEIN NUCLEIC-ACID COMPLEX", "ProteinNucleicAcidComplex");
        types.put("PHENOTYPE", "Phenotype");
        types.put("NON-MOLECULAR", "NonMolecular");
        types.put("ADMET", "ADMET");
        types.put("LIPID", "UnclassifiedTarget");
        types.put("3D CELL CULTURE", "UnclassifiedTarget");
        types.put("UNCHECKED", "UnclassifiedTarget");
        types.put("NO TARGET", "UnclassifiedTarget");
    }

    /**
     * Table of the targets.
     */
    private static final EntityTable<Integer> targets = new EntityTable<>("chembl.targets", intKey("id"), "chembl_id",
            uniqueVarchar("chembl_id"), varchar("type"), varchar("label"), varchar("organism"), integer("taxonomy"),
            integer("cell_line"), bool("species_group"));

    /**
     * Table of the components of the targets.
     */
    private static final ValueTable components = new ValueTable("chembl.target_components", column("target"),
            column("component"));

    /**
     * Table of the {@code skos:exactMatch} links of the single protein targets to their components.
     */
    private static final ValueTable exactMatches = new ValueTable("chembl.target_exact_matches", column("target"),
            column("component"));

    /**
     * Table of the {@code skos:relatedMatch} links of the complex and group targets to their components.
     */
    private static final ValueTable relatedMatches = new ValueTable("chembl.target_related_matches", column("target"),
            column("component"));

    /**
     * Table of the relations between the targets.
     */
    private static final ValueTable relations = new ValueTable("chembl.target_relations", column("target"),
            enumeration("relationship", "chembl.target_relationship_type"), column("related"));


    /**
     * Not instantiable.
     */
    private Target()
    {
    }


    /**
     * Loads the targets and their components from the target file of the release and checks the triples of the target
     * components and cell lines linking them to the targets against the links of the targets, and the {@code rdf:type}
     * and {@code dcterms:title} values, which the mapping produces from {@code cco:targetType} and {@code rdfs:label}.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadTargets() throws IOException, SQLException
    {
        Taxonomy.Forms taxonomies = new Taxonomy.Forms("target");
        InverseCheck targetComponents = new InverseCheck("cco:hasTargetComponent");
        InverseCheck cellLines = new InverseCheck("cco:isTargetForCellLine");
        HashMap<Integer, String> classes = new HashMap<>();
        HashMap<Integer, String> titles = new HashMap<>();

        try(InputStream stream = getTtlStream(ChEMBL.file("target")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(subject.getURI().startsWith(TargetComponent.prefix))
                    {
                        if(predicate.getURI().equals(cco + "hasTarget"))
                            targetComponents.inverse(getTargetID(object), TargetComponent.getComponentID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    if(subject.getURI().startsWith(CellLine.prefix))
                    {
                        if(predicate.getURI().equals(cco + "isCellLineForTarget"))
                            cellLines.inverse(getTargetID(object), CellLine.getCellLineID(subject));
                        else
                            unexpected(subject, predicate, object);

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType ->
                        {
                            if(classes.put(id, object.getURI()) != null)
                                Problems.error("multiple rdf:type values", subject.getURI());
                        }
                        case chemblId -> targets.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel -> targets.set(id, "label", getString(object));
                        case dcterms + "title" -> titles.put(id, getString(object));
                        case cco + "targetType" -> targets.set(id, "type", getString(object));
                        case cco + "organismName" -> targets.set(id, "organism", getString(object));
                        case cco + "taxonomy" -> targets.set(id, "taxonomy", taxonomies.getTaxonomy(id, object));
                        case cco + "isSpeciesGroup" -> targets.set(id, "species_group", getBoolean(object));
                        case cco + "isTargetForCellLine" ->
                        {
                            int cellLineID = CellLine.getCellLineID(object);
                            targets.set(id, "cell_line", cellLineID);
                            cellLines.forward(id, cellLineID);
                        }
                        case cco + "hasTargetComponent" ->
                        {
                            int componentID = TargetComponent.getComponentID(object);
                            components.add(id, componentID);
                            targetComponents.forward(id, componentID);
                        }
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();
        targetComponents.check();
        cellLines.check();

        // the mapping produces rdf:type from cco:targetType and dcterms:title from rdfs:label
        int typeIndex = targets.columnIndex("type");
        int labelIndex = targets.columnIndex("label");

        for(Entry<Integer, Object[]> entry : targets.rows())
        {
            String type = (String) entry.getValue()[typeIndex];
            String label = (String) entry.getValue()[labelIndex];
            String expectedClass = type == null || !types.containsKey(type) ? null : cco + types.get(type);

            if(type != null && expectedClass == null)
                Problems.error("unknown cco:targetType", "CHEMBL" + entry.getKey() + " " + type);
            else if(expectedClass != null && !expectedClass.equals(classes.remove(entry.getKey())))
                Problems.error("rdf:type not corresponding to cco:targetType", "CHEMBL" + entry.getKey());

            if(label != null && !label.equals(titles.remove(entry.getKey())))
                Problems.error("dcterms:title different from rdfs:label", "CHEMBL" + entry.getKey());
        }

        for(Integer id : classes.keySet())
            Problems.error("rdf:type without cco:targetType", "CHEMBL" + id);

        for(Integer id : titles.keySet())
            Problems.error("dcterms:title without rdfs:label", "CHEMBL" + id);
    }


    /**
     * Loads the links of the targets to their components from a linkset file of the release.
     *
     * @param part the name of the part of the linkset
     * @param property the IRI of the linking property
     * @param table the table of the links
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadLinkset(String part, String property, ValueTable table) throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream(ChEMBL.file(part)))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(predicate.getURI().equals(voidInDataset))
                        return;

                    if(predicate.getURI().equals(property))
                        table.add(getTargetID(subject), TargetComponent.getComponentID(object));
                    else
                        unexpected(subject, predicate, object);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the relations between the targets from the target relation file of the release.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadRelations() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream(ChEMBL.file("targetrel")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    String relationship = switch(predicate.getURI())
                    {
                        case cco + "relEquivalentTo" -> "EQUIVALENT TO";
                        case cco + "relOverlapsWith" -> "OVERLAPS WITH";
                        case cco + "relSubsetOf" -> "SUBSET OF";
                        case cco + "relHasSubset" -> "SUPERSET OF";
                        default -> null;
                    };

                    if(relationship != null)
                        relations.add(getTargetID(subject), relationship, getTargetID(object));
                    else
                        unexpected(subject, predicate, object);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the targets, their components, the linksets of the target components and the relations between the
     * targets from the files of the release.
     *
     * @throws IOException if a file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load targets ...");

        loadTargets();
        loadLinkset("singletarget_targetcmpt_ls", skos + "exactMatch", exactMatches);
        loadLinkset("complextarget_targetcmpt_ls", skos + "relatedMatch", relatedMatches);
        loadLinkset("grouptarget_targetcmpt_ls", skos + "relatedMatch", relatedMatches);
        loadRelations();

        System.out.println();
    }


    /**
     * Stores the collected targets, reporting the referenced ones that the files do not describe, their components,
     * the linksets and the relations.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish targets ...");

        targets.store("target");
        components.store();
        exactMatches.store();
        relatedMatches.store();
        relations.store();

        System.out.println();
    }


    /**
     * Returns the id of a target and marks the target as referenced.
     *
     * @param node the IRI of the target
     * @return the id of the target
     * @throws IOException if the node is not an IRI of a target
     */
    static int getTargetID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        targets.reference(id);
        return id;
    }
}
