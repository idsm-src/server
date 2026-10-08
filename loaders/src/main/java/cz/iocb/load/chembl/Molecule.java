package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.foaf;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.chembl.ChEMBL.sio;
import static cz.iocb.load.chembl.ChEMBL.skos;
import static cz.iocb.load.common.EntityTable.bool;
import static cz.iocb.load.common.EntityTable.float8;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.real;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.chembl.ValueTable.enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.jena.graph.Node;
import cz.iocb.load.chembl.ValueTable.Value;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Molecule extends Updater
{
    // a descriptor node <molecule#name>: the column of its value, its rdf:type, and whether the value is numeric
    private static record Descriptor(EntityTable<Integer> table, String column, String type, boolean numeric)
    {
    }


    // a reference of the unichem file: its rdf:type, the stored type, the IRI prefix, pattern of the stored value and
    // the IRI suffix
    private static record Reference(String rdfType, String type, String prefix, Pattern pattern, String suffix)
    {
        Reference(String rdfType, String type, String prefix, String pattern)
        {
            this(rdfType, type, prefix, Pattern.compile(pattern), "");
        }
    }


    static final String prefix = ChEMBL.chembl + "molecule/CHEMBL";
    static final String imagePrefix = "https://www.ebi.ac.uk/chembl/api/data/image/CHEMBL";
    static final String imageSuffix = ".svg";

    private static final EntityTable<Integer> molecules = new EntityTable<>("chembl.molecule_bases", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("type"), varchar("label"), real("phase"),
            bool("biotherapeutic"), varchar("helm_notation"), varchar("description"), varchar("hrac_classification"),
            varchar("irac_classification"), varchar("frac_classification"), integer("parent"));

    private static final EntityTable<Integer> descriptors = new EntityTable<>("chembl.molecule_descriptors",
            intKey("molecule"), null, float8("alogp"), float8("aromatic_rings"), float8("hba"), float8("hbd"),
            float8("heavy_atoms"), float8("num_ro5_violations"), float8("psa"), float8("qed_weighted"), float8("rtb"),
            float8("mw_freebase"), float8("full_mwt"), varchar("ro3_pass"), varchar("full_molformula"));

    private static final EntityTable<Integer> structures = new EntityTable<>("chembl.molecule_structures",
            intKey("molecule"), null, uniqueVarchar("standard_inchi"), uniqueVarchar("standard_inchi_key"),
            uniqueVarchar("canonical_smiles"));

    // the labels of the descriptor nodes, named after the nodes, and of the image
    private static final EntityTable<Integer> labels = new EntityTable<>("chembl.molecule_labels", intKey("molecule"),
            null, uniqueVarchar("alogp"), uniqueVarchar("aromatic_rings"), uniqueVarchar("hba"), uniqueVarchar("hbd"),
            uniqueVarchar("heavy_atoms"), uniqueVarchar("num_ro5_violations"), uniqueVarchar("psa"),
            uniqueVarchar("qed_weighted"), uniqueVarchar("rtb"), uniqueVarchar("mw_freebase"),
            uniqueVarchar("full_mwt"), uniqueVarchar("ro3_pass"), uniqueVarchar("full_molformula"),
            uniqueVarchar("standard_inchi"), uniqueVarchar("standard_inchi_key"), uniqueVarchar("canonical_smiles"),
            uniqueVarchar("image"));

    private static final ValueTable alternatives = new ValueTable("chembl.molecule_alternatives", column("molecule"),
            column("alternative"));

    private static final ValueTable atcClassifications = new ValueTable("chembl.molecule_atc_classifications",
            column("molecule"), column("classification"));

    private static final ValueTable documents = new ValueTable("chembl.molecule_documents", column("molecule"),
            column("document"));

    private static final ValueTable biocomponents = new ValueTable("chembl.molecule_biocomponents", column("molecule"),
            column("biocomponent"));

    private static final ValueTable references = new ValueTable("chembl.molecule_references", column("molecule"),
            enumeration("type", "chembl.molecule_reference_type"), column("reference"));

    // the labels of the reference IRIs, which carry one label per molecule referencing them
    private static final ValueTable referenceLabels = new ValueTable("chembl.molecule_reference_labels",
            enumeration("type", "chembl.molecule_reference_type"), column("reference"), column("label"));

    private static final ValueTable pubchemReferences = new ValueTable("chembl.molecule_pubchem_references",
            column("molecule"), column("compound"));

    private static final ValueTable chebiReferences = new ValueTable("chembl.molecule_chebi_references",
            column("molecule"), column("chebi"));

    // rdf:type of a molecule is determined by its cco:substanceType
    private static final Map<String, String> types = new HashMap<>();

    private static final Map<String, Descriptor> descriptorTypes = new LinkedHashMap<>();

    private static final List<Reference> referenceTypes = new ArrayList<>();

    static
    {
        types.put("Small molecule", "SmallMolecule");
        types.put("Unknown", "UnknownSubstance");
        types.put("Protein", "ProteinMolecule");
        types.put("Antibody", "Antibody");
        types.put("Oligonucleotide", "Oligonucleotide");
        types.put("Gene", "UnclassifiedMolecule");
        types.put("Enzyme", "Enzyme");
        types.put("Antibody drug conjugate", "UnclassifiedMolecule");
        types.put("Vaccine component", "UnclassifiedMolecule");
        types.put("Cell", "CellTherapy");
        types.put("Oligosaccharide", "Oligosaccharide");

        descriptorTypes.put("alogp", new Descriptor(descriptors, "alogp", "CHEMINF_000251", true));
        descriptorTypes.put("aromatic_rings", new Descriptor(descriptors, "aromatic_rings", "CHEMINF_000381", true));
        descriptorTypes.put("hba", new Descriptor(descriptors, "hba", "CHEMINF_000245", true));
        descriptorTypes.put("hbd", new Descriptor(descriptors, "hbd", "CHEMINF_000244", true));
        descriptorTypes.put("heavy_atoms", new Descriptor(descriptors, "heavy_atoms", "CHEMINF_000300", true));
        descriptorTypes.put("num_ro5_violations",
                new Descriptor(descriptors, "num_ro5_violations", "CHEMINF_000312", true));
        descriptorTypes.put("psa", new Descriptor(descriptors, "psa", "CHEMINF_000307", true));
        descriptorTypes.put("qed_weighted", new Descriptor(descriptors, "qed_weighted", "CHEMINF_000431", true));
        descriptorTypes.put("rtb", new Descriptor(descriptors, "rtb", "CHEMINF_000254", true));
        descriptorTypes.put("mw_freebase", new Descriptor(descriptors, "mw_freebase", "CHEMINF_000350", true));
        descriptorTypes.put("full_mwt", new Descriptor(descriptors, "full_mwt", "CHEMINF_000216", true));
        descriptorTypes.put("ro3_pass", new Descriptor(descriptors, "ro3_pass", "CHEMINF_000315", false));
        descriptorTypes.put("full_molformula", new Descriptor(descriptors, "full_molformula", "CHEMINF_000042", false));
        descriptorTypes.put("standard_inchi_key",
                new Descriptor(structures, "standard_inchi_key", "CHEMINF_000059", false));
        descriptorTypes.put("standard_inchi", new Descriptor(structures, "standard_inchi", "CHEMINF_000113", false));
        descriptorTypes.put("canonical_smiles",
                new Descriptor(structures, "canonical_smiles", "CHEMINF_000018", false));

        referenceTypes.add(new Reference("BindingDbRef", "BINDING DB",
                "https://www.bindingdb.org/rwd/bind/chemsearch/marvin/SDFdownload.jsp?"
                        + "download_file=/rwd/bind/downloads/BindingDB_All_202601_tsv.zip",
                "[1-9][0-9]*"));
        referenceTypes.add(new Reference("BrendaRef", "BRENDA",
                "https://www.brenda-enzymes.org/ligand.php?brenda_ligand_id=", "[1-9][0-9]*"));
        referenceTypes.add(
                new Reference("CcdcRef", "CCDC", "https://www.ccdc.cam.ac.uk/structures/search?sid=UNICHEM&pid=csd:",
                        "[A-Z]{6}(-UNICHEM-[1-9][0-9]*)?"));
        referenceTypes.add(new Reference("ChebiRef", "CHEBI", "https://www.ebi.ac.uk/chebi/CHEBI%3A", "[1-9][0-9]*"));
        referenceTypes.add(new Reference("ClinicalTrialsRef", "CLINICAL TRIALS", "https://clinicaltrials.gov/study/NCT",
                "[0-9]{8}(-UNICHEM-[1-9][0-9]*)?"));
        referenceTypes.add(new Reference("CompToxRef", "COMPTOX",
                "https://comptox.epa.gov/dashboard/chemical/details/DTXSID", "[0-9]+"));
        referenceTypes.add(new Reference("DrugCentralRef", "DRUG CENTRAL", "https://drugcentral.org/drugcard/",
                "[A-Za-z0-9+%-]+"));
        referenceTypes.add(new Reference("DrugbankRef", "DRUGBANK", "https://go.drugbank.com/drugs/DB", "[0-9]{5}"));
        referenceTypes.add(new Reference("FdaSrsRef", "FDA SRS",
                "https://d20b1koi85gdl2.cloudfront.net/uniisearch/srs/unii/", "[A-Z0-9]{10}"));
        referenceTypes.add(new Reference("FooDbRef", "FOO DB", "https://foodb.ca/compounds/FDB", "[0-9]{6}"));
        referenceTypes.add(new Reference("HmdbRef", "HMDB", "https://www.hmdb.ca/metabolites/HMDB", "[0-9]{7}"));
        referenceTypes.add(new Reference("IupharRef", "IUPHAR",
                "https://www.guidetopharmacology.org/GRAC/LigandDisplayForward?ligandId=", "[1-9][0-9]*"));
        referenceTypes.add(new Reference("LipidMapsRef", "LIPID MAPS",
                "https://www.lipidmaps.org/data/LMSDRecord.php?LMID=LM", "[A-Z0-9]+"));
        referenceTypes.add(new Reference("MolportRef", "MOLPORT", "https://www.molport.com/shop/compound/Molport-",
                "[0-9]{3}(-[0-9]{3}){2}"));
        referenceTypes.add(new Reference("NmrShiftDb2Ref", "NMR SHIFT DB2",
                "https://nmrshiftdb.nmr.uni-koeln.de/molecule/", "[1-9][0-9]*"));
        referenceTypes
                .add(new Reference("PdbeRef", "PDBE", "https://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/",
                        Pattern.compile("[A-Z0-9_]*"), "+-+Ideal+conformer"));
        referenceTypes.add(new Reference("ProbesAndDrugsRef", "PROBES AND DRUGS",
                "https://www.probes-drugs.org/compounds/PD", "[0-9]{6}"));
        referenceTypes.add(
                new Reference("PubchemRef", "PUBCHEM", "https://pubchem.ncbi.nlm.nih.gov/compound/", "[1-9][0-9]*"));
        referenceTypes.add(new Reference("RcsbPdbRef", "RCSB PDB", "https://www.rcsb.org/ligand/", "[A-Z0-9]+"));
        referenceTypes.add(
                new Reference("RheaRef", "RHEA CHEBI", "https://www.rhea-db.org/rhea?query=CHEBI%3A", "[1-9][0-9]*"));
        referenceTypes.add(new Reference("RheaRef", "RHEA POLYMER", "https://www.rhea-db.org/rhea?query=POLYMER%3A",
                "[1-9][0-9]*"));
        referenceTypes.add(
                new Reference("SureChemblRef", "SURE CHEMBL", "https://www.surechembl.org/chemical/", "[1-9][0-9]*"));
        referenceTypes.add(new Reference("SwissLipidsRef", "SWISS LIPIDS",
                "https://www.swisslipids.org/#/entity/SLM%3A", "[0-9]{9}"));
    }


    private static void loadMolecules() throws IOException, SQLException
    {
        InverseCheck documentInverses = new InverseCheck("cco:hasDocument");
        InverseCheck biocomponentInverses = new InverseCheck("cco:hasBioComponent");
        HashMap<Integer, String> pendingClasses = new HashMap<>();
        HashMap<Integer, String> pendingLabels = new HashMap<>();
        BitSet typed = new BitSet();
        BitSet depicted = new BitSet();
        BitSet images = new BitSet();
        Map<String, BitSet> nodes = new HashMap<>();
        Map<String, BitSet> links = new HashMap<>();

        try(InputStream stream = getTtlStream(ChEMBL.file("molecule")))
        {
            new TripleStreamProcessor()
            {
                // an unknown substance type is reported once, when it is read
                private void checkClass(int id, String type, String declared)
                {
                    if(types.containsKey(type) && !(cco + types.get(type)).equals(declared))
                        ChEMBL.warning("rdf:type not corresponding to cco:substanceType", "CHEMBL" + id);
                }


                private void parseNode(Node subject, Node predicate, Node object) throws IOException
                {
                    String iri = subject.getURI();
                    int separator = iri.indexOf('#');
                    int id = Integer.parseInt(iri.substring(prefix.length(), separator));
                    String name = iri.substring(separator + 1);
                    Descriptor descriptor = descriptorTypes.get(name);

                    if(descriptor == null)
                    {
                        ChEMBL.warning("unexpected descriptor", iri);
                        return;
                    }

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, sio + descriptor.type);
                        case sio + "SIO_000300" ->
                        {
                            descriptor.table.set(id, descriptor.column,
                                    descriptor.numeric ? (Object) getDouble(object) : getString(object));
                            nodes.computeIfAbsent(name, k -> new BitSet()).set(id);
                        }
                        case rdfsLabel -> labels.set(id, name, getString(object));
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }


                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    String iri = subject.getURI();

                    if(iri.startsWith(imagePrefix))
                    {
                        int id = getIntID(subject, imagePrefix, imageSuffix);
                        images.set(id);

                        switch(predicate.getURI())
                        {
                            case rdfType -> ChEMBL.checkType(subject, object, foaf + "Image");
                            case rdfsLabel -> labels.set(id, "image", getString(object));
                            default -> ChEMBL.unexpected(subject, predicate, object);
                        }

                        return;
                    }

                    if(iri.startsWith(Document.prefix) && predicate.getURI().equals(cco + "hasMolecule"))
                    {
                        documentInverses.inverse(getMoleculeID(object), Document.getDocumentID(subject));
                        return;
                    }

                    if(iri.startsWith(BioComponent.prefix) && predicate.getURI().equals(cco + "hasMolecule"))
                    {
                        biocomponentInverses.inverse(getMoleculeID(object), BioComponent.getBioComponentID(subject));
                        return;
                    }

                    if(iri.startsWith(prefix) && iri.indexOf('#') > 0)
                    {
                        parseNode(subject, predicate, object);
                        return;
                    }

                    int id = getIntID(subject, prefix);

                    switch(predicate.getURI())
                    {
                        case rdfType ->
                        {
                            String type = (String) molecules.get(id, "type");
                            typed.set(id);

                            if(type != null)
                                checkClass(id, type, object.getURI());
                            else if(pendingClasses.put(id, object.getURI()) != null)
                                ChEMBL.warning("multiple rdf:type values", iri);
                        }
                        case chemblId -> molecules.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel ->
                        {
                            molecules.set(id, "label", getString(object));
                            String prefLabel = pendingLabels.remove(id);

                            if(prefLabel != null && !prefLabel.equals(getString(object)))
                                ChEMBL.warning("skos:prefLabel different from rdfs:label", iri);
                        }
                        case skos + "prefLabel" ->
                        {
                            String label = (String) molecules.get(id, "label");

                            if(label == null)
                                pendingLabels.put(id, getString(object));
                            else if(!label.equals(getString(object)))
                                ChEMBL.warning("skos:prefLabel different from rdfs:label", iri);
                        }
                        case cco + "substanceType" ->
                        {
                            String type = getString(object);
                            molecules.set(id, "type", type);
                            String declared = pendingClasses.remove(id);

                            if(!types.containsKey(type))
                                ChEMBL.warning("unknown cco:substanceType", "CHEMBL" + id + " " + type);
                            else if(declared != null)
                                checkClass(id, type, declared);
                        }
                        case cco + "highestDevelopmentPhase" -> molecules.set(id, "phase", getFloat(object));
                        case cco + "isBiotherapeutic" -> molecules.set(id, "biotherapeutic", getBoolean(object));
                        case cco + "helmNotation" -> molecules.set(id, "helm_notation", getString(object));
                        case dcterms + "description" -> molecules.set(id, "description", getString(object));
                        case cco + "HRACClassification" -> molecules.set(id, "hrac_classification", getString(object));
                        case cco + "IRACClassification" -> molecules.set(id, "irac_classification", getString(object));
                        case cco + "FRACClassification" -> molecules.set(id, "frac_classification", getString(object));
                        case skos + "altLabel" -> alternatives.add(id, getString(object));
                        case cco + "atcClassification" -> atcClassifications.add(id, getString(object));
                        case cco + "hasDocument" ->
                        {
                            int documentID = Document.getDocumentID(object);
                            documents.add(id, documentID);
                            documentInverses.forward(id, documentID);
                        }
                        case cco + "hasBioComponent" ->
                        {
                            int biocomponentID = BioComponent.getBioComponentID(object);
                            biocomponents.add(id, biocomponentID);
                            biocomponentInverses.forward(id, biocomponentID);
                        }
                        case foaf + "depiction" ->
                        {
                            if(getIntID(object, imagePrefix, imageSuffix) != id)
                                ChEMBL.warning("unexpected foaf:depiction", iri + " " + object.getURI());

                            depicted.set(id);
                        }
                        case sio + "SIO_000008" ->
                        {
                            String node = object.getURI();
                            String name = node.startsWith(iri + "#") ? node.substring(iri.length() + 1) : null;

                            if(name != null && descriptorTypes.containsKey(name))
                                links.computeIfAbsent(name, k -> new BitSet()).set(id);
                            else
                                ChEMBL.warning("unexpected descriptor", iri + " " + node);
                        }
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        documentInverses.check();
        biocomponentInverses.check();

        for(Integer id : pendingClasses.keySet())
            ChEMBL.warning("rdf:type without cco:substanceType", "CHEMBL" + id);

        for(Integer id : pendingLabels.keySet())
            ChEMBL.warning("skos:prefLabel different from rdfs:label", "CHEMBL" + id);

        // the mapping produces rdf:type, foaf:depiction and the image for the described molecules
        int typeIndex = molecules.columnIndex("type");
        int chemblIdIndex = molecules.columnIndex("chembl_id");
        BitSet described = new BitSet();

        for(Entry<Integer, Object[]> entry : molecules.rows())
        {
            if(entry.getValue()[chemblIdIndex] != null)
                described.set(entry.getKey());

            if(entry.getValue()[typeIndex] != null && !typed.get(entry.getKey()))
                ChEMBL.warning("cco:substanceType without rdf:type", "CHEMBL" + entry.getKey());
        }

        compare(described, depicted, "described molecule", "foaf:depiction");
        compare(described, images, "described molecule", "image");

        // the mapping produces the links to the descriptor nodes for the stored values
        for(String name : descriptorTypes.keySet())
            compare(nodes.getOrDefault(name, new BitSet()), links.getOrDefault(name, new BitSet()),
                    "descriptor " + name, "sio:SIO_000008");
    }


    private static void compare(BitSet set, BitSet expected, String setName, String expectedName)
    {
        BitSet difference = (BitSet) set.clone();
        difference.xor(expected);

        for(int id = difference.nextSetBit(0); id >= 0; id = difference.nextSetBit(id + 1))
            if(set.get(id))
                ChEMBL.warning(setName + " without " + expectedName, "CHEMBL" + id);
            else
                ChEMBL.warning(expectedName + " without " + setName, "CHEMBL" + id);
    }


    private static void loadHierarchy() throws IOException, SQLException
    {
        InverseCheck parents = new InverseCheck("cco:hasParentMolecule");

        try(InputStream stream = getTtlStream(ChEMBL.file("molhierarchy")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    int id = getMoleculeID(subject);

                    switch(predicate.getURI())
                    {
                        case cco + "hasParentMolecule" ->
                        {
                            int parentID = getMoleculeID(object);
                            molecules.set(id, "parent", parentID);
                            parents.forward(id, parentID);
                        }
                        case cco + "hasChildMolecule" -> parents.inverse(getMoleculeID(object), id);
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        parents.check();
    }


    private static void loadReferences() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream(ChEMBL.file("unichem")))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    String iri = subject.getURI();

                    if(iri.startsWith(prefix))
                    {
                        if(!predicate.getURI().equals(cco + "moleculeXref"))
                        {
                            ChEMBL.unexpected(subject, predicate, object);
                            return;
                        }

                        int id = getMoleculeID(subject);
                        Reference reference = getReference(object.getURI());
                        String value = getValue(reference, object.getURI());

                        references.add(id, reference.type, value);

                        if(reference.type.equals("PUBCHEM"))
                            pubchemReferences.add(id, Integer.parseInt(value));
                        else if(reference.type.equals("CHEBI"))
                            chebiReferences.add(id, Integer.parseInt(value));

                        return;
                    }

                    Reference reference = getReference(iri);

                    switch(predicate.getURI())
                    {
                        case rdfType -> ChEMBL.checkType(subject, object, cco + reference.rdfType);
                        case rdfsLabel -> referenceLabels.add(reference.type, getValue(reference, iri),
                                getString(object));
                        default -> ChEMBL.unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        // the mapping produces rdf:type of the references from the references of the molecules
        Set<Value> referenced = new HashSet<>();

        for(Value value : references.values())
            referenced.add(new Value(new Object[] { value.get(1), value.get(2) }));

        for(Value value : referenceLabels.values())
            if(!referenced.contains(new Value(new Object[] { value.get(0), value.get(1) })))
                ChEMBL.warning("reference label without cco:moleculeXref", value.get(0) + " " + value.get(1));
    }


    private static Reference getReference(String iri) throws IOException
    {
        for(Reference reference : referenceTypes)
            if(iri.startsWith(reference.prefix) && iri.endsWith(reference.suffix))
                return reference;

        throw new IOException("unexpected reference " + iri);
    }


    private static String getValue(Reference reference, String iri) throws IOException
    {
        String value = iri.substring(reference.prefix.length(), iri.length() - reference.suffix.length());

        if(!reference.pattern.matcher(value).matches())
            throw new IOException("unexpected reference " + iri);

        return value;
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load molecules ...");

        loadMolecules();
        loadHierarchy();
        loadReferences();

        ChEMBL.finishLoad();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish molecules ...");

        molecules.store("molecule");
        descriptors.store("molecule descriptor");
        structures.store("molecule structure");
        labels.store("molecule label");
        alternatives.store();
        atcClassifications.store();
        documents.store();
        biocomponents.store();
        references.store();
        referenceLabels.store();
        pubchemReferences.store();
        chebiReferences.store();

        ChEMBL.finishLoad();
    }


    static int getMoleculeID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        molecules.reference(id);
        return id;
    }


    static int size()
    {
        return molecules.size();
    }
}
