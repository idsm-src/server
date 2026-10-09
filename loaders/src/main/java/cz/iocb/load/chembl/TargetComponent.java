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
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class TargetComponent extends Updater
{
    private static record Reference(String type, String prefix, Pattern pattern)
    {
        Reference(String type, String prefix, String pattern)
        {
            this(type, prefix, Pattern.compile(pattern));
        }
    }


    static final String prefix = ChEMBL.chembl + "targetcomponent/CHEMBL_TC_";
    static final String uniprotPrefix = "http://purl.uniprot.org/uniprot/";

    // reference types by the rdf:type of the reference: the stored type, and the IRI prefix and pattern of the stored
    // value
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

    private static final EntityTable<Integer> components = new EntityTable<>("chembl.components", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("type"), varchar("description"), varchar("organism"),
            integer("taxonomy"), varchar("sequence"), varchar("accession"));

    private static final ValueTable alternatives = new ValueTable("chembl.component_alternatives", column("component"),
            column("alternative"));

    private static final ValueTable references = new ValueTable("chembl.component_references", column("component"),
            enumeration("type", "chembl.component_reference_type"), column("reference"));

    // the labels of the reference IRIs, which carry one label per component referencing them
    private static final ValueTable referenceLabels = new ValueTable("chembl.component_reference_labels",
            enumeration("type", "chembl.component_reference_type"), column("reference"), column("label"));


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
                                throw new IOException("multiple types of reference " + subject.getURI());
                        }
                        else if(predicate.getURI().equals(rdfsLabel))
                        {
                            xrefLabels.computeIfAbsent(subject.getURI(), k -> new HashSet<>()).add(getString(object));
                        }
                        else
                        {
                            ChEMBL.unexpected(subject, predicate, object);
                        }

                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + "TargetComponent");
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
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        taxonomies.check();

        // the mapping produces rdf:type of the references from the stored type and value
        for(Pair<Integer, String> xref : xrefs)
        {
            Pair<Reference, String> reference = getReference(xref.getTwo(), xrefTypes);
            references.add(xref.getOne(), reference.getOne().type, reference.getTwo());
        }

        for(Entry<String, Set<String>> entry : xrefLabels.entrySet())
        {
            if(!xrefTypes.containsKey(entry.getKey()))
            {
                ChEMBL.warning("reference label without rdf:type", entry.getKey());
                continue;
            }

            Pair<Reference, String> reference = getReference(entry.getKey(), xrefTypes);

            for(String label : entry.getValue())
                referenceLabels.add(reference.getOne().type, reference.getTwo(), label);
        }

        Set<String> referenced = new HashSet<>();

        for(Pair<Integer, String> xref : xrefs)
            referenced.add(xref.getTwo());

        for(String iri : xrefTypes.keySet())
            if(!referenced.contains(iri))
                ChEMBL.warning("unreferenced reference", iri);
    }


    /*
     * Returns the reference type and the stored value of a reference IRI with the given rdf:type values.
     */
    private static Pair<Reference, String> getReference(String iri, Map<String, String> types) throws IOException
    {
        String type = types.get(iri);
        Reference reference = type == null ? null : referenceTypes.get(type);

        if(reference == null)
            throw new IOException("unknown type of reference " + iri);

        String value = iri.startsWith(reference.prefix) ? iri.substring(reference.prefix.length()) : null;

        if(value == null || !reference.pattern.matcher(value).matches())
            throw new IOException("unexpected reference " + iri + " of type " + type);

        return Pair.getPair(reference, value);
    }


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
                        ChEMBL.unexpected(subject, predicate, object);
                }
            }.load(stream);
        }
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load target components ...");

        loadComponents();
        loadAccessions();

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish target components ...");

        components.store("target component");
        alternatives.store();
        references.store();
        referenceLabels.store();

        ChEMBL.finishLoad();
    }


    static int getComponentID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        components.reference(id);
        return id;
    }
}
