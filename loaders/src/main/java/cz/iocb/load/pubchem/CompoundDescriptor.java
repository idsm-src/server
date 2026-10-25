package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.float8;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.TripleDispatcher.is;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem compound descriptors from the files of {@code pubchem/RDF/descriptor/compound} into the table
 * {@code pubchem.compound_descriptors} and the tables of the string descriptors; the descriptor values that the files
 * of {@code pubchem/RDF/compound/general} repeat are checked against them.
 */
class CompoundDescriptor extends Updater
{
    /**
     * IRI prefix of the compound descriptors.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the numeric descriptors of the compounds.
     */
    private static final EntityTable<Integer> descriptors = new EntityTable<>("pubchem.compound_descriptors",
            intKey("compound"), null, integer("hydrogen_bond_acceptor_count"), integer("defined_atom_stereo_count"),
            integer("defined_bond_stereo_count"), integer("undefined_bond_stereo_count"), integer("isotope_atom_count"),
            integer("covalent_unit_count"), integer("hydrogen_bond_donor_count"), integer("non_hydrogen_atom_count"),
            integer("rotatable_bond_count"), integer("undefined_atom_stereo_count"), integer("total_formal_charge"),
            float8("structure_complexity"), float8("mono_isotopic_weight"), float8("xlogp3_aa"), float8("xlogp3"),
            float8("exact_mass"), float8("molecular_weight"), float8("tpsa"));


    /**
     * Not instantiable.
     */
    private CompoundDescriptor()
    {
    }


    /**
     * Loads an integer descriptor of the compounds into a column of {@link #descriptors} and checks the values of the
     * property of the same name in {@code pubchem/RDF/compound/general} against it.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @param field the column of the descriptor, also the local name of the property
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadIntegerField(String name, String suffix, String field) throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        Integer value = getIntFromInteger(object);

                        descriptors.set(id, field, value);
                    }
                }.load(stream);
            }
        });

        processFiles("pubchem/RDF/compound/general", "pc_compound2" + field + "_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    String property = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#" + field;

                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, property))
                            return;

                        Integer id = Compound.getCompoundID(subject.getURI());
                        Integer value = getIntFromInteger(object);

                        if(!value.equals(descriptors.get(id, field)))
                            throw new DataException("value different from the descriptor",
                                    "descriptor " + descriptors.get(id, field));
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads a decimal descriptor of the compounds into a column of {@link #descriptors} and checks the values of the
     * property of the same name in {@code pubchem/RDF/compound/general} against it.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @param field the column of the descriptor, also the local name of the property
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadFloatField(String name, String suffix, String field) throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        Double value = getDecimalValue(object);

                        descriptors.set(id, field, value);
                    }
                }.load(stream);
            }
        });

        processFiles("pubchem/RDF/compound/general", "pc_compound2" + field + "_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    String property = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#" + field;

                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, property))
                            return;

                        Integer id = Compound.getCompoundID(subject.getURI());
                        Double value = getDecimalValue(object);

                        if(!value.equals(descriptors.get(id, field)))
                            throw new DataException("value different from the descriptor",
                                    "descriptor " + descriptors.get(id, field));
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the XLogP3 and XLogP3-AA descriptors of the compounds into {@link #descriptors}, which share their files,
     * and checks the values of {@code vocab:xlogp3} against them.
     *
     * @param name the name of the descriptors in the names of their files
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadXLogP3Field(String name) throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        Double value = getDecimalValue(object);

                        if(subject.getURI().endsWith("-AA"))
                        {
                            Integer id = getDescriptorID(subject.getURI(), "_XLogP3-AA");

                            descriptors.set(id, "xlogp3_aa", value);
                        }
                        else
                        {
                            Integer id = getDescriptorID(subject.getURI(), "_XLogP3");

                            descriptors.set(id, "xlogp3", value);
                        }
                    }
                }.load(stream);
            }
        });

        processFiles("pubchem/RDF/compound/general", "pc_compound2xlogp3_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#xlogp3"))
                            return;

                        Integer id = Compound.getCompoundID(subject.getURI());
                        Double value = getDecimalValue(object);

                        Double keepAA = (Double) descriptors.get(id, "xlogp3_aa");
                        Double keep = (Double) descriptors.get(id, "xlogp3");

                        if(keepAA == null && keep == null)
                            throw new DataException("value without a descriptor");

                        if(keepAA != null && !value.equals(keepAA))
                            throw new DataException("value different from the descriptor", "descriptor " + keepAA);

                        if(keep != null && !value.equals(keep))
                            throw new DataException("value different from the descriptor", "descriptor " + keep);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads a string descriptor of the compounds into the table {@code "pubchem.compound_" + table} and checks the
     * values of the property of the same name in {@code pubchem/RDF/compound/general} against it.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @param table the name of the table without the {@code compound_} prefix
     * @param field the column of the descriptor, also the local name of the property
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadStringField(String name, String suffix, String table, String field)
            throws IOException, SQLException
    {
        EntityTable<Integer> values = new EntityTable<>("pubchem.compound_" + table, intKey("compound"), null,
                uniqueVarchar(field));

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        String value = getString(object);

                        values.set(id, field, value);
                    }
                }.load(stream);
            }
        });

        processFiles("pubchem/RDF/compound/general", "pc_compound2" + field + "_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    String property = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#" + field;

                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, property))
                            return;

                        Integer id = Compound.getCompoundID(subject.getURI());
                        String value = getString(object);

                        if(!value.equals(values.get(id, field)))
                            throw new DataException("value different from the descriptor",
                                    "descriptor " + values.get(id, field));
                    }
                }.load(stream);
            }
        });

        values.store();
    }


    /**
     * Checks that the unit files of a descriptor give the expected unit.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @param unitName the local name of the expected unit in the OBO namespace
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void checkUnit(String name, String suffix, String unitName) throws IOException, SQLException
    {
        final String unit = "http://purl.obolibrary.org/obo/" + unitName;

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_unit_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);

                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000221"))
                            return;

                        if(!is(object, unit))
                            unexpectedValue(subject, predicate, object);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Checks that the values of the compound identifier descriptors match the ids in their IRIs.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void checkIdentifier(String name, String suffix) throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                        String value = getString(object);

                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        if(!value.equals(Integer.toString(id)))
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Checks that the type files of a descriptor give the expected types.
     *
     * @param name the name of the descriptor in the names of its files
     * @param suffix the suffix of the IRIs of the descriptor
     * @param typeName the local name of the expected type in the SIO namespace
     * @param vocabName the local name of the expected type in the PubChem vocabulary
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void checkType(String name, String suffix, String typeName, String vocabName)
            throws IOException, SQLException
    {
        final String type = "http://semanticscience.org/resource/" + typeName;
        final String vocab = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#" + vocabName;

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);

                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        checkType(subject, object, type, vocab);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Checks that the type files of the XLogP3 and XLogP3-AA descriptors give the expected types.
     *
     * @param name the name of the descriptors in the names of their files
     * @param typeName the local name of the expected type in the SIO namespace
     * @param vocabName the local name of the expected type in the PubChem vocabulary
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void checkXLogP3Type(String name, String typeName, String vocabName) throws IOException, SQLException
    {
        final String type = "http://semanticscience.org/resource/" + typeName;
        final String vocab = "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#" + vocabName;

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(subject.getURI().endsWith("-AA"))
                            getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", "_XLogP3-AA");
                        else
                            getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", "_XLogP3");

                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        checkType(subject, object, type, vocab);
                    }
                }.load(stream);
            }
        });
    }


    /**
     * Loads the compound descriptors into the tables and flushes the table of the numeric descriptors.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load compound descriptors ...");


        loadIntegerField("CovalentUnitCount", "_Covalent_Unit_Count", "covalent_unit_count");
        loadIntegerField("DefinedAtomStereoCount", "_Defined_Atom_Stereo_Count", "defined_atom_stereo_count");
        loadIntegerField("DefinedBondStereoCount", "_Defined_Bond_Stereo_Count", "defined_bond_stereo_count");
        loadIntegerField("TotalFormalCharge", "_Total_Formal_Charge", "total_formal_charge");
        loadIntegerField("HydrogenBondAcceptorCount", "_Hydrogen_Bond_Acceptor_Count", "hydrogen_bond_acceptor_count");
        loadIntegerField("HydrogenBondDonorCount", "_Hydrogen_Bond_Donor_Count", "hydrogen_bond_donor_count");
        loadIntegerField("NonHydrogenAtomCount", "_Non-Hydrogen_Atom_Count", "non_hydrogen_atom_count");
        loadIntegerField("IsotopeAtomCount", "_Isotope_Atom_Count", "isotope_atom_count");
        loadIntegerField("RotatableBondCount", "_Rotatable_Bond_Count", "rotatable_bond_count");
        loadIntegerField("UndefinedAtomStereoCount", "_Undefined_Atom_Stereo_Count", "undefined_atom_stereo_count");
        loadIntegerField("UndefinedBondStereoCount", "_Undefined_Bond_Stereo_Count", "undefined_bond_stereo_count");

        loadFloatField("StructureComplexity", "_Structure_Complexity", "structure_complexity");
        loadFloatField("ExactMass", "_Exact_Mass", "exact_mass");
        loadFloatField("MolecularWeight", "_Molecular_Weight", "molecular_weight");
        loadFloatField("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "mono_isotopic_weight");
        loadFloatField("TPSA", "_TPSA", "tpsa");

        loadXLogP3Field("XLogP3");

        descriptors.flush();

        loadStringField("MolecularFormula", "_Molecular_Formula", "molecular_formulas", "molecular_formula");
        loadStringField("SMILES", "_SMILES", "smileses", "smiles");
        loadStringField("ConnectivitySMILES", "_Connectivity_SMILES", "connectivity_smileses", "connectivity_smiles");
        loadStringField("IUPACInChI", "_IUPAC_InChI", "iupac_inchis", "iupac_inchi");
        loadStringField("PreferredIUPACName", "_Preferred_IUPAC_Name", "preferred_iupac_names", "preferred_iupac_name");

        checkUnit("ExactMass", "_Exact_Mass", "UO_0000055");
        checkUnit("MolecularWeight", "_Molecular_Weight", "UO_0000055");
        checkUnit("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "UO_0000055");
        checkUnit("TPSA", "_TPSA", "UO_0000324");

        checkIdentifier("CompoundIdentifier", "_Compound_Identifier");

        checkType("StructureComplexity", "_Structure_Complexity", "CHEMINF_000390", "StructureComplexity");
        checkType("CovalentUnitCount", "_Covalent_Unit_Count", "CHEMINF_000369", "CovalentUnitCount");
        checkType("DefinedAtomStereoCount", "_Defined_Atom_Stereo_Count", "CHEMINF_000370", "DefinedAtomStereoCount");
        checkType("DefinedBondStereoCount", "_Defined_Bond_Stereo_Count", "CHEMINF_000371", "DefinedBondStereoCount");
        checkType("ExactMass", "_Exact_Mass", "CHEMINF_000338", "ExactMass");
        checkType("TotalFormalCharge", "_Total_Formal_Charge", "CHEMINF_000336", "TotalFormalCharge");
        checkType("HydrogenBondAcceptorCount", "_Hydrogen_Bond_Acceptor_Count", "CHEMINF_000388",
                "HydrogenBondAcceptorCount");
        checkType("HydrogenBondDonorCount", "_Hydrogen_Bond_Donor_Count", "CHEMINF_000387", "HydrogenBondDonorCount");
        checkType("NonHydrogenAtomCount", "_Non-Hydrogen_Atom_Count", "CHEMINF_000373", "NonHydrogenAtomCount");
        checkType("IsotopeAtomCount", "_Isotope_Atom_Count", "CHEMINF_000372", "IsotopeAtomCount");
        checkType("MolecularFormula", "_Molecular_Formula", "CHEMINF_000335", "MolecularFormula");
        checkType("MolecularWeight", "_Molecular_Weight", "CHEMINF_000334", "MolecularWeight");
        checkType("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "CHEMINF_000337", "MonoIsotopicWeight");
        checkType("RotatableBondCount", "_Rotatable_Bond_Count", "CHEMINF_000389", "RotatableBondCount");
        checkType("TPSA", "_TPSA", "CHEMINF_000392", "TPSA");
        checkType("UndefinedAtomStereoCount", "_Undefined_Atom_Stereo_Count", "CHEMINF_000374",
                "UndefinedAtomStereoCount");
        checkType("UndefinedBondStereoCount", "_Undefined_Bond_Stereo_Count", "CHEMINF_000375",
                "UndefinedBondStereoCount");
        checkXLogP3Type("XLogP3", "CHEMINF_000395", "XLogP3");
        checkType("ConnectivitySMILES", "_Connectivity_SMILES", "CHEMINF_000376", "ConnectivitySMILES");
        checkType("SMILES", "_SMILES", "CHEMINF_000379", "SMILES");
        checkType("IUPACInChI", "_IUPAC_InChI", "CHEMINF_000396", "IUPACInChI");
        checkType("PreferredIUPACName", "_Preferred_IUPAC_Name", "CHEMINF_000382", "PreferredIUPACName");
        checkType("CompoundIdentifier", "_Compound_Identifier", "CHEMINF_000140", "CompoundIdentifier");

        System.out.println();
    }


    /**
     * Stores the table of the numeric descriptors.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        descriptors.store();
    }


    /**
     * Returns the value of a decimal descriptor as a double, which holds every decimal of at most 15 significant digits
     * exactly; the database turns it back into the same decimal, because a double is cast to numeric with 15 digits.
     *
     * @param node the literal of the descriptor
     * @return the value as a double
     * @throws IOException if the literal is not a decimal or has more than 15 significant digits
     */
    private static Double getDecimalValue(Node node) throws IOException
    {
        BigDecimal value = getDecimal(node);

        if(value.stripTrailingZeros().precision() > 15)
            throw new DataException("decimal value of more than 15 significant digits", text(node));

        return value.doubleValue();
    }


    /**
     * Returns the id of the compound of a descriptor IRI and adds the compound.
     *
     * @param value the IRI of the descriptor
     * @param suffix the suffix of the IRIs of the descriptor
     * @return the id of the compound
     * @throws IOException if the IRI is not a descriptor IRI with the suffix
     */
    private static Integer getDescriptorID(String value, String suffix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        if(!value.endsWith(suffix))
            throw new DataException("unexpected IRI", value);

        Integer id = Integer.parseInt(value.substring(prefixLength, value.length() - suffix.length()));

        Compound.addCompoundID(id);

        return id;
    }
}
