package cz.iocb.load.chembl;

import static cz.iocb.load.chembl.ChEMBL.cco;
import static cz.iocb.load.chembl.ChEMBL.chemblId;
import static cz.iocb.load.chembl.ChEMBL.dcterms;
import static cz.iocb.load.chembl.ChEMBL.foaf;
import static cz.iocb.load.chembl.ChEMBL.rdfType;
import static cz.iocb.load.chembl.ChEMBL.rdfsLabel;
import static cz.iocb.load.chembl.ChEMBL.sio;
import static cz.iocb.load.chembl.ChEMBL.skos;
import static cz.iocb.load.chembl.ValueTable.column;
import static cz.iocb.load.chembl.ValueTable.enumeration;
import static cz.iocb.load.common.EntityTable.bool;
import static cz.iocb.load.common.EntityTable.float8;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.real;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.BufferedReader;
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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the ChEMBL molecules into the table {@code chembl.molecules} and the related tables: their descriptors,
 * structures, labels, alternative names, ATC classifications, documents and biocomponents from the molecule file, their
 * parents from the molecule hierarchy file, their references to other databases from the UniChem file, and their
 * molfiles from the SDF files of the release.
 */
class Molecule extends Updater
{
    /**
     * A descriptor node {@code <molecule#name>}.
     *
     * @param table the table of the value of the descriptor
     * @param column the column of the value of the descriptor
     * @param type the local name of the {@code sio} class of the node
     * @param numeric whether the value is numeric
     */
    private static record Descriptor(EntityTable<Integer> table, String column, String type, boolean numeric)
    {
    }


    /**
     * A type of the references of the UniChem file.
     *
     * @param rdfType the local name of the {@code cco} class of the reference
     * @param type the stored type of the reference
     * @param prefix the IRI prefix of the references of the type
     * @param pattern the pattern of the stored value, the part of the IRI between the prefix and the suffix
     * @param suffix the IRI suffix of the references of the type
     */
    private static record Reference(String rdfType, String type, String prefix, Pattern pattern, String suffix)
    {
        /**
         * Creates a type of the references whose IRIs have no suffix.
         *
         * @param rdfType the local name of the {@code cco} class of the reference
         * @param type the stored type of the reference
         * @param prefix the IRI prefix of the references of the type
         * @param pattern the regular expression of the stored value, the rest of the IRI after the prefix
         */
        Reference(String rdfType, String type, String prefix, String pattern)
        {
            this(rdfType, type, prefix, Pattern.compile(pattern), "");
        }
    }


    /**
     * IRI prefix of the molecules.
     */
    static final String prefix = ChEMBL.chembl + "molecule/CHEMBL";

    /**
     * IRI prefix of the images of the molecules.
     */
    static final String imagePrefix = "https://www.ebi.ac.uk/chembl/api/data/image/CHEMBL";

    /**
     * IRI suffix of the images of the molecules.
     */
    static final String imageSuffix = ".svg";

    /**
     * Table of the molecules.
     */
    private static final EntityTable<Integer> molecules = new EntityTable<>("chembl.molecules", intKey("id"),
            "chembl_id", uniqueVarchar("chembl_id"), varchar("type"), varchar("label"), real("phase"),
            bool("biotherapeutic"), varchar("helm_notation"), varchar("description"), varchar("hrac_classification"),
            varchar("irac_classification"), varchar("frac_classification"), integer("parent"));

    /**
     * Table of the values of the descriptors of the molecules.
     */
    private static final EntityTable<Integer> descriptors = new EntityTable<>("chembl.molecule_descriptors",
            intKey("molecule"), null, float8("alogp"), float8("aromatic_rings"), float8("hba"), float8("hbd"),
            float8("heavy_atoms"), float8("num_ro5_violations"), float8("psa"), float8("qed_weighted"), float8("rtb"),
            float8("mw_freebase"), float8("full_mwt"), varchar("ro3_pass"), varchar("full_molformula"));

    /**
     * Table of the structure descriptors of the molecules: the InChI, the InChIKey and the SMILES.
     */
    private static final EntityTable<Integer> structures = new EntityTable<>("chembl.molecule_structures",
            intKey("molecule"), null, uniqueVarchar("standard_inchi"), uniqueVarchar("standard_inchi_key"),
            uniqueVarchar("canonical_smiles"));

    /**
     * Table of the labels of the descriptor nodes, named after the nodes, and of the image of the molecules.
     */
    private static final EntityTable<Integer> labels = new EntityTable<>("chembl.molecule_labels", intKey("molecule"),
            null, uniqueVarchar("alogp"), uniqueVarchar("aromatic_rings"), uniqueVarchar("hba"), uniqueVarchar("hbd"),
            uniqueVarchar("heavy_atoms"), uniqueVarchar("num_ro5_violations"), uniqueVarchar("psa"),
            uniqueVarchar("qed_weighted"), uniqueVarchar("rtb"), uniqueVarchar("mw_freebase"),
            uniqueVarchar("full_mwt"), uniqueVarchar("ro3_pass"), uniqueVarchar("full_molformula"),
            uniqueVarchar("standard_inchi"), uniqueVarchar("standard_inchi_key"), uniqueVarchar("canonical_smiles"),
            uniqueVarchar("image"));

    /**
     * Table of the alternative names of the molecules.
     */
    private static final ValueTable alternatives = new ValueTable("chembl.molecule_alternatives", column("molecule"),
            column("alternative"));

    /**
     * Table of the ATC classifications of the molecules.
     */
    private static final ValueTable atcClassifications = new ValueTable("chembl.molecule_atc_classifications",
            column("molecule"), column("classification"));

    /**
     * Table of the documents of the molecules.
     */
    private static final ValueTable documents = new ValueTable("chembl.molecule_documents", column("molecule"),
            column("document"));

    /**
     * Table of the biocomponents of the molecules.
     */
    private static final ValueTable biocomponents = new ValueTable("chembl.molecule_biocomponents", column("molecule"),
            column("biocomponent"));

    /**
     * Table of the references of the molecules to other databases.
     */
    private static final ValueTable references = new ValueTable("chembl.molecule_references", column("molecule"),
            enumeration("type", "chembl.molecule_reference_type"), column("reference"));

    /**
     * Table of the labels of the reference IRIs, which carry one label per molecule referencing them.
     */
    private static final ValueTable referenceLabels = new ValueTable("chembl.molecule_reference_labels",
            enumeration("type", "chembl.molecule_reference_type"), column("reference"), column("label"));

    /**
     * Table of the PubChem compounds referenced by the molecules.
     */
    private static final ValueTable pubchemReferences = new ValueTable("chembl.molecule_pubchem_references",
            column("molecule"), column("compound"));

    /**
     * Table of the ChEBI entities referenced by the molecules.
     */
    private static final ValueTable chebiReferences = new ValueTable("chembl.molecule_chebi_references",
            column("molecule"), column("chebi"));

    /**
     * Local names of the {@code cco} classes of the molecules by their {@code cco:substanceType}, which determines the
     * {@code rdf:type} of a molecule.
     */
    private static final Map<String, String> types = new HashMap<>();

    /**
     * The descriptor nodes by their names.
     */
    private static final Map<String, Descriptor> descriptorTypes = new LinkedHashMap<>();

    /**
     * The types of the references of the UniChem file.
     */
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


    /**
     * Not instantiable.
     */
    private Molecule()
    {
    }


    /**
     * Loads the molecules with their descriptors, labels, alternative names, ATC classifications, documents and
     * biocomponents from the molecule file of the release, and checks the inverse triples of their links and the
     * values the mapping produces from the stored ones: {@code skos:prefLabel}, {@code rdf:type}, the images and the
     * links to the descriptor nodes.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadMolecules() throws IOException, SQLException
    {
        InverseCheck documentInverses = new InverseCheck("cco:hasDocument");
        InverseCheck biocomponentInverses = new InverseCheck("cco:hasBioComponent");
        HashMap<Integer, String> pendingClasses = new HashMap<>();
        HashMap<Integer, String> pendingLabels = new HashMap<>();
        BitSet labelled = new BitSet();
        BitSet prefLabelled = new BitSet();
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
                        Problems.error("rdf:type not corresponding to cco:substanceType", "CHEMBL" + id);
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
                        Problems.error("unexpected descriptor", iri);
                        return;
                    }

                    switch(predicate.getURI())
                    {
                        case rdfType -> checkType(subject, object, sio + descriptor.type);
                        case sio + "SIO_000300" ->
                        {
                            descriptor.table.set(id, descriptor.column,
                                    descriptor.numeric ? (Object) getDouble(object) : getString(object));
                            nodes.computeIfAbsent(name, k -> new BitSet()).set(id);
                        }
                        case rdfsLabel -> labels.set(id, name, getString(object));
                        default -> unexpected(subject, predicate, object);
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
                            case rdfType -> checkType(subject, object, foaf + "Image");
                            case rdfsLabel -> labels.set(id, "image", getString(object));
                            default -> unexpected(subject, predicate, object);
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
                                Problems.error("multiple rdf:type values", iri);
                        }
                        case chemblId -> molecules.set(id, "chembl_id",
                                ChEMBL.getChemblId(subject, predicate, object, "CHEMBL" + id));
                        case rdfsLabel ->
                        {
                            labelled.set(id);
                            molecules.set(id, "label", getString(object));
                            String prefLabel = pendingLabels.remove(id);

                            if(prefLabel != null && !prefLabel.equals(getString(object)))
                                Problems.error("skos:prefLabel different from rdfs:label", iri);
                        }
                        case skos + "prefLabel" ->
                        {
                            prefLabelled.set(id);
                            String label = (String) molecules.get(id, "label");
                            String prefLabel = getString(object);

                            if(label == null)
                            {
                                String pending = pendingLabels.put(id, prefLabel);

                                if(pending != null && !pending.equals(prefLabel))
                                    Problems.error("skos:prefLabel different from rdfs:label", iri);
                            }
                            else if(!label.equals(prefLabel))
                            {
                                Problems.error("skos:prefLabel different from rdfs:label", iri);
                            }
                        }
                        case cco + "substanceType" ->
                        {
                            String type = getString(object);
                            molecules.set(id, "type", type);
                            String declared = pendingClasses.remove(id);

                            if(!types.containsKey(type))
                                Problems.error("unknown cco:substanceType", "CHEMBL" + id + " " + type);
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
                                Problems.error("unexpected foaf:depiction", iri + " " + object.getURI());

                            depicted.set(id);
                        }
                        case sio + "SIO_000008" ->
                        {
                            String node = object.getURI();
                            String name = node.startsWith(iri + "#") ? node.substring(iri.length() + 1) : null;

                            if(name != null && descriptorTypes.containsKey(name))
                                links.computeIfAbsent(name, k -> new BitSet()).set(id);
                            else
                                Problems.error("unexpected descriptor", iri + " " + node);
                        }
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        documentInverses.check();
        biocomponentInverses.check();

        for(Integer id : pendingClasses.keySet())
            Problems.error("rdf:type without cco:substanceType", "CHEMBL" + id);

        // the mapping produces skos:prefLabel from rdfs:label
        compare(labelled, prefLabelled, "rdfs:label", "skos:prefLabel");

        // the mapping produces rdf:type, foaf:depiction and the image for the described molecules
        int typeIndex = molecules.columnIndex("type");
        int chemblIdIndex = molecules.columnIndex("chembl_id");
        BitSet described = new BitSet();

        for(Entry<Integer, Object[]> entry : molecules.rows())
        {
            if(entry.getValue()[chemblIdIndex] != null)
                described.set(entry.getKey());

            if(entry.getValue()[typeIndex] != null && !typed.get(entry.getKey()))
                Problems.error("cco:substanceType without rdf:type", "CHEMBL" + entry.getKey());
        }

        compare(described, depicted, "described molecule", "foaf:depiction");
        compare(described, images, "described molecule", "image");

        // the mapping produces the links to the descriptor nodes for the stored values
        for(String name : descriptorTypes.keySet())
            compare(nodes.getOrDefault(name, new BitSet()), links.getOrDefault(name, new BitSet()),
                    "descriptor " + name, "sio:SIO_000008");
    }


    /**
     * Reports an error for every molecule that is in exactly one of two sets.
     *
     * @param set the ids of the molecules of the first set
     * @param expected the ids of the molecules of the second set
     * @param setName the name of the first set, used in the reported errors
     * @param expectedName the name of the second set, used in the reported errors
     */
    private static void compare(BitSet set, BitSet expected, String setName, String expectedName)
    {
        BitSet difference = (BitSet) set.clone();
        difference.xor(expected);

        for(int id = difference.nextSetBit(0); id >= 0; id = difference.nextSetBit(id + 1))
            if(set.get(id))
                Problems.error(setName + " without " + expectedName, "CHEMBL" + id);
            else
                Problems.error(expectedName + " without " + setName, "CHEMBL" + id);
    }


    /**
     * Loads the parents of the molecules from the molecule hierarchy file of the release.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
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
                        default -> unexpected(subject, predicate, object);
                    }
                }
            }.load(stream);
        }

        parents.check();
    }


    /**
     * Loads the references of the molecules and their labels from the UniChem file of the release, and checks that
     * every labelled reference is referenced, because the mapping produces {@code rdf:type} of the references from the
     * references of the molecules.
     *
     * @throws IOException if the file cannot be read
     * @throws SQLException if a database error occurs
     */
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
                            unexpected(subject, predicate, object);
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
                        case rdfType -> checkType(subject, object, cco + reference.rdfType);
                        case rdfsLabel -> referenceLabels.add(reference.type, getValue(reference, iri),
                                getString(object));
                        default -> unexpected(subject, predicate, object);
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
                Problems.error("reference label without cco:moleculeXref", value.get(0) + " " + value.get(1));
    }


    /**
     * Returns the type of a reference IRI.
     *
     * @param iri the reference IRI
     * @return the type of the reference
     * @throws IOException if the IRI is of no known type
     */
    private static Reference getReference(String iri) throws IOException
    {
        for(Reference reference : referenceTypes)
            if(iri.startsWith(reference.prefix) && iri.endsWith(reference.suffix))
                return reference;

        throw new DataException("unexpected reference", iri);
    }


    /**
     * Returns the stored value of a reference IRI.
     *
     * @param reference the type of the reference
     * @param iri the reference IRI
     * @return the stored value of the reference
     * @throws IOException if the value does not match the pattern of the type
     */
    private static String getValue(Reference reference, String iri) throws IOException
    {
        String value = iri.substring(reference.prefix.length(), iri.length() - reference.suffix.length());

        if(!reference.pattern.matcher(value).matches())
            throw new DataException("unexpected reference", iri);

        return value;
    }


    /**
     * Loads the structures of the molecules from the SDF files of the release; a molecule known only from the files
     * gets its row as well.
     *
     * @throws IOException if a file cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadMolfiles() throws IOException, SQLException
    {
        StructureTable molfiles = new StructureTable("chembl.molecule_molfiles", "molecule", "molfile");
        molfiles.load();

        processFiles("chembl/sdf", ".*\\.sdf\\.gz", file -> {
            try(BufferedReader reader = getReader(file))
            {
                SdfReader.read(file, reader, "chembl_id", (id, molfile) -> {
                    if(!id.startsWith("CHEMBL"))
                        throw new DataException("unexpected molecule", id);

                    int moleculeID = Integer.parseInt(id.substring(6));

                    molecules.reference(moleculeID);
                    molfiles.put(moleculeID, molfile);
                });
            }
        });

        molfiles.store();
    }


    /**
     * Loads the molecules with their related data from the molecule, molecule hierarchy, UniChem and SDF files of the
     * release.
     *
     * @throws IOException if a file cannot be read
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load molecules ...");

        loadMolecules();
        loadHierarchy();
        loadReferences();
        loadMolfiles();

        System.out.println();
    }


    /**
     * Stores the collected molecules, reporting the referenced ones that the files do not describe, and their related
     * data.
     *
     * @throws SQLException if a database error occurs
     */
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

        System.out.println();
    }


    /**
     * Returns the id of a molecule and marks the molecule as referenced.
     *
     * @param node the IRI of the molecule
     * @return the id of the molecule
     * @throws IOException if the node is not an IRI of a molecule
     */
    static int getMoleculeID(Node node) throws IOException
    {
        int id = TripleStreamProcessor.getIntID(node, prefix);
        molecules.reference(id);
        return id;
    }


    /**
     * Returns the number of the molecules described by the molecule file.
     *
     * @return the number of the described molecules
     */
    static int size()
    {
        return molecules.size();
    }
}
