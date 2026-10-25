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
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the ChEMBL target components from the target component file of the release into the table
 * {@code chembl.components}, together with their alternative names, their references to other databases with the
 * labels of the references, and their UniProt accessions from the UniProt linkset; the file also gives labels of
 * taxonomies.
 */
class TargetComponent extends Updater
{
    /**
     * A type of the references of the target components.
     *
     * @param type the stored type of the reference
     * @param prefix the IRI prefix of the references of the type
     * @param pattern the pattern of the stored value, the rest of the IRI after the prefix
     */
    private static record Reference(String type, String prefix, Pattern pattern)
    {
        /**
         * Creates the type of the references.
         *
         * @param type the stored type of the reference
         * @param prefix the IRI prefix of the references of the type
         * @param pattern the regular expression of the stored value, the rest of the IRI after the prefix
         */
        Reference(String type, String prefix, String pattern)
        {
            this(type, prefix, Pattern.compile(pattern));
        }
    }


    /**
     * IRI prefix of the target components.
     */
    static final String prefix = ChEMBL.chembl + "targetcomponent/CHEMBL_TC_";

    /**
     * IRI prefix of the UniProt proteins.
     */
    static final String uniprotPrefix = "http://purl.uniprot.org/uniprot/";

    /**
     * Reference types by the local name of the {@code rdf:type} of the reference: the stored type, and the IRI prefix
     * and pattern of the stored value.
     */
    private static final Map<String, Reference> referenceTypes = new HashMap<>();

    static
    {
        referenceTypes.put("GoProcessRef",
                new Reference("GO PROCESS", "http://identifiers.org/obo.go/", "GO:[0-9]{7}"));
        referenceTypes.put("GoFunctionRef",
                new Reference("GO FUNCTION", "http://identifiers.org/obo.go/", "GO:[0-9]{7}"));
        referenceTypes.put("GoComponentRef",
                new Reference("GO COMPONENT", "http://identifiers.org/obo.go/", "GO:[0-9]{7}"));
        referenceTypes.put("ProteinDataBankRef",
                new Reference("PDB", "http://identifiers.org/pdb/", "[0-9][A-Z0-9]{3}"));
        referenceTypes.put("InterproRef", new Reference("INTERPRO", "http://identifiers.org/interpro/", "IPR[0-9]{6}"));
        referenceTypes.put("ReactomeRef",
                new Reference("REACTOME", "http://identifiers.org/reactome/", "R-[A-Z]{3}-[1-9][0-9]*"));
        referenceTypes.put("PfamRef", new Reference("PFAM", "http://identifiers.org/pfam/", "PF[0-9]{5}"));
        referenceTypes.put("EnzymeClassRef", new Reference("ENZYME CLASS", "http://identifiers.org/ec-code/",
                "((-|[1-9][0-9]*)\\.){3}(-|n?[1-9][0-9]*)"));
        referenceTypes.put("IntactRef", new Reference("INTACT", "http://identifiers.org/intact/", "[A-Z0-9]*"));
        referenceTypes.put("UniprotRef", new Reference("UNIPROT", uniprotPrefix, "[A-Z0-9]+"));
        referenceTypes.put("PharmgkbRef", new Reference("PHARMGKB", "http://www.pharmgkb.org/gene/", "PA[1-9][0-9]*"));
        referenceTypes.put("TimbalRef",
                new Reference("TIMBAL", "http://mordred.bioc.cam.ac.uk/timbal/", "[A-Za-z0-9%()-]+"));
        referenceTypes.put("CGDRef", new Reference("CGD", "http://research.nhgri.nih.gov/CGD/view/?g=", "[A-Z0-9-]+"));
    }

    /**
     * Table of the target components.
     */
    private static final EntityTable<Integer> components = new EntityTable<>("chembl.components", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("type"), varchar("description"), varchar("organism"),
            integer("taxonomy"), varchar("sequence"), varchar("accession"));

    /**
     * Table of the alternative names of the target components.
     */
    private static final ValueTable alternatives = new ValueTable("chembl.component_alternatives", column("component"),
            column("alternative"));

    /**
     * Table of the references of the target components.
     */
    private static final ValueTable references = new ValueTable("chembl.component_references", column("component"),
            enumeration("type", "chembl.component_reference_type"), column("reference"));

    /**
     * Table of the labels of the reference IRIs, which carry one label per component referencing them.
     */
    private static final ValueTable referenceLabels = new ValueTable("chembl.component_reference_labels",
            enumeration("type", "chembl.component_reference_type"), column("reference"), column("label"));


    /**
     * Not instantiable.
     */
    private TargetComponent()
    {
    }


    /**
     * Loads the target components, their alternative names and references, and the labels of the references from the
     * target component file of the release, and checks that the references with an {@code rdf:type} are exactly the
     * referenced ones, because the mapping produces their {@code rdf:type} from the stored type and value.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadComponents() throws IOException, SQLException
    {
        Taxonomy.Forms taxonomies = new Taxonomy.Forms("target component");
        Set<Pair<Integer, String>> xrefs = new LinkedHashSet<>();
        Map<String, String> xrefTypes = new HashMap<>();
        Map<String, Set<String>> xrefLabels = new HashMap<>();

        try(InputStream stream = getTtlStream(ChEMBL.file("targetcmpt")))
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

                    if(!subject.getURI().startsWith(prefix))
                    {
                        // a reference
                        if(predicate.getURI().equals(rdfType))
                        {
                            String type = xrefTypes.put(subject.getURI(), getStringID(object, cco));

                            if(type != null && !type.equals(xrefTypes.get(subject.getURI())))
                                throw new DataException("multiple types of reference", subject.getURI());
                        }
                        else if(predicate.getURI().equals(rdfsLabel))
                        {
                            xrefLabels.computeIfAbsent(subject.getURI(), k -> new HashSet<>()).add(getString(object));
                        }
                        else
                        {
                            unexpected(subject, predicate, object);
                        }

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, cco + "TargetComponent");
                        case chemblId -> components.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL_TC_" + id));
                        case rdfsLabel -> ChEMBL.checkValue(subject, predicate, object, "CHEMBL_TC_" + id);
                        case cco + "componentType" -> components.set(id, "type", getString(object));
                        case dcterms + "description" -> components.set(id, "description", getString(object));
                        case cco + "organismName" -> components.set(id, "organism", getString(object));
                        case cco + "taxonomy" -> components.set(id, "taxonomy", taxonomies.getTaxonomy(id, object));
                        case cco + "proteinSequence" -> components.set(id, "sequence", getString(object));
                        case skos + "altLabel" -> alternatives.add(id, getString(object));
                        case cco + "targetCmptXref" -> xrefs.add(Pair.getPair(id, object.getURI()));
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();

        // the mapping produces rdf:type of the references from the stored type and value
        for(Pair<Integer, String> xref : xrefs)
        {
            Pair<Reference, String> reference = getReference(xref.getTwo(), xrefTypes);

            if(reference != null)
                references.add(xref.getOne(), reference.getOne().type, reference.getTwo());
        }

        for(Entry<String, Set<String>> entry : xrefLabels.entrySet())
        {
            if(!xrefTypes.containsKey(entry.getKey()))
            {
                Problems.error("reference label without rdf:type", entry.getKey());
                continue;
            }

            Pair<Reference, String> reference = getReference(entry.getKey(), xrefTypes);

            if(reference != null)
                for(String label : entry.getValue())
                    referenceLabels.add(reference.getOne().type, reference.getTwo(), label);
        }

        Set<String> referenced = new HashSet<>();

        for(Pair<Integer, String> xref : xrefs)
            referenced.add(xref.getTwo());

        for(String iri : xrefTypes.keySet())
            if(!referenced.contains(iri))
                Problems.error("unreferenced reference", iri);
    }


    /**
     * Returns the reference type and the stored value of a reference IRI with the given rdf:type values; a reference
     * of an unknown type or with an unexpected IRI is reported and null is returned.
     *
     * @param iri the reference IRI
     * @param types the local names of the {@code rdf:type} values of the reference IRIs
     * @return the reference type and the stored value, or null
     */
    private static Pair<Reference, String> getReference(String iri, Map<String, String> types)
    {
        String type = types.get(iri);
        Reference reference = type == null ? null : referenceTypes.get(type);

        if(reference == null)
        {
            Problems.error("unknown type of reference", iri + " " + type);
            return null;
        }

        String value = iri.startsWith(reference.prefix) ? iri.substring(reference.prefix.length()) : null;

        if(value == null || !reference.pattern.matcher(value).matches())
        {
            Problems.error("unexpected reference", iri + " of type " + type);
            return null;
        }

        return Pair.getPair(reference, value);
    }


    /**
     * Loads the UniProt accessions of the target components from the UniProt linkset file of the release.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadAccessions() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream(ChEMBL.file("targetcmpt_uniprot_ls")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(predicate.getURI().equals(voidInDataset))
                        return;

                    if(predicate.getURI().equals(skos + "exactMatch"))
                        components.set(getComponentID(subject), "accession", getStringID(object, uniprotPrefix));
                    else
                        unexpected(subject, predicate, object);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the target components with their alternative names, references and UniProt accessions from the files of
     * the release.
     *
     * @throws IOException if a file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load target components ...");

        loadComponents();
        loadAccessions();

        System.out.println();
    }


    /**
     * Stores the collected target components, reporting the referenced ones that the files do not describe, their
     * alternative names, their references and the labels of the references.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish target components ...");

        components.store("target component");
        alternatives.store();
        references.store();
        referenceLabels.store();

        System.out.println();
    }


    /**
     * Returns the id of a target component and marks the target component as referenced.
     *
     * @param node the IRI of the target component
     * @return the id of the target component
     * @throws IOException if the node is not an IRI of a target component
     */
    static int getComponentID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        components.reference(id);
        return id;
    }
}
