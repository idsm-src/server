package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.impl.map.mutable.primitive.IntFloatHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class CompoundDescriptor extends Updater
{
    private static IntHashSet oldDescriptors;


    private static void loadIntegerField(String name, String suffix, String field) throws IOException, SQLException
    {
        IntIntHashMap newValues = new IntIntHashMap(200000000);
        IntIntHashMap oldValues = getIntIntMap(
                "select compound, " + field + " from pubchem.descriptor_compound_bases where " + field + " is not null",
                200000000);

        processFiles("pubchem/RDF/descriptor/compound", name, file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                            throw new IOException();

                        int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                        int value = getInteger(object);

                        Compound.addCompoundID(id);

                        synchronized(newValues)
                        {
                            oldDescriptors.remove(id);

                            if(value != oldValues.removeKeyIfAbsent(id, NO_VALUE))
                                newValues.put(id, value);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.descriptor_compound_bases set " + field + " = null where compound = ?",
                oldValues.keySet());
        batch("insert into pubchem.descriptor_compound_bases (compound, " + field + ") values (?,?) "
                + "on conflict (compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


    private static void loadFloatField(String name, String suffix, String field) throws IOException, SQLException
    {
        IntFloatHashMap newValues = new IntFloatHashMap(200000000);
        IntFloatHashMap oldValues = getIntFloatMap(
                "select compound, " + field + " from pubchem.descriptor_compound_bases where " + field + " is not null",
                200000000);

        processFiles("pubchem/RDF/descriptor/compound", name, file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                            throw new IOException();

                        int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                        float value = getFloat(object);

                        Compound.addCompoundID(id);

                        synchronized(newValues)
                        {
                            oldDescriptors.remove(id);

                            if(value != oldValues.removeKeyIfAbsent(id, Float.NaN) || value == Float.NaN)
                                newValues.put(id, value);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.descriptor_compound_bases set " + field + " = null where compound = ?",
                oldValues.keySet());
        batch("insert into pubchem.descriptor_compound_bases (compound, " + field + ") values (?,?) "
                + "on conflict (compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


    private static void loadXLogP3Field(String name) throws IOException, SQLException
    {
        IntFloatHashMap newAAValues = new IntFloatHashMap(200000000);
        IntFloatHashMap oldAAValues = getIntFloatMap(
                "select compound, xlogp3_aa from pubchem.descriptor_compound_bases where xlogp3_aa is not null",
                200000000);

        IntFloatHashMap newValues = new IntFloatHashMap(4000000);
        IntFloatHashMap oldValues = getIntFloatMap(
                "select compound, xlogp3 from pubchem.descriptor_compound_bases where xlogp3 is not null", 4000000);

        processFiles("pubchem/RDF/descriptor/compound", name, file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                            throw new IOException();

                        float value = getFloat(object);

                        if(subject.getURI().endsWith("-AA"))
                        {
                            int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID",
                                    "_XLogP3-AA");

                            Compound.addCompoundID(id);

                            synchronized(newAAValues)
                            {
                                oldDescriptors.remove(id);

                                if(value != oldAAValues.removeKeyIfAbsent(id, Float.NaN) || value == Float.NaN)
                                    newAAValues.put(id, value);
                            }
                        }
                        else
                        {
                            int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", "_XLogP3");

                            Compound.addCompoundID(id);

                            synchronized(newAAValues)
                            {
                                oldDescriptors.remove(id);

                                if(value != oldValues.removeKeyIfAbsent(id, Float.NaN) || value == Float.NaN)
                                    newValues.put(id, value);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        batch("update pubchem.descriptor_compound_bases set xlogp3_aa = null where compound = ?", oldAAValues.keySet());
        batch("insert into pubchem.descriptor_compound_bases (compound, xlogp3_aa) values (?,?) "
                + "on conflict (compound) do update set xlogp3_aa=EXCLUDED.xlogp3_aa", newAAValues);

        batch("update pubchem.descriptor_compound_bases set xlogp3 = null where compound = ?", oldValues.keySet());
        batch("insert into pubchem.descriptor_compound_bases (compound, xlogp3) values (?,?) "
                + "on conflict (compound) do update set xlogp3=EXCLUDED.xlogp3", newValues);
    }


    private static void loadStringField(String name, String suffix, String table, String field)
            throws IOException, SQLException
    {
        IntStringMap newValues = new IntStringMap(200000000);
        IntStringMap oldValues = getIntStringMap(
                "select compound, " + field + " from pubchem." + table + " where " + field + " is not null", 200000000);

        processFiles("pubchem/RDF/descriptor/compound", name, file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                            throw new IOException();

                        int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                        String value = getString(object);

                        Compound.addCompoundID(id);

                        synchronized(newValues)
                        {
                            if(!value.equals(oldValues.remove(id)))
                                newValues.put(id, value);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from pubchem." + table + " where compound = ?", oldValues.keySet());
        batch("insert into pubchem." + table + " (compound, " + field + ") values (?,?) "
                + "on conflict (compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


    private static void loadUnitField(String name, String suffix, String unit) throws IOException, SQLException
    {
        processFiles("pubchem/RDF/descriptor/compound", name, file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!object.getURI().equals(unit))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load compound descriptors ...");

        oldDescriptors = getIntSet("select compound from pubchem.descriptor_compound_bases", 200000000);

        loadIntegerField("pc_descr_CovalentUnitCount_value_[0-9]+\\.ttl\\.gz", "_Covalent_Unit_Count",
                "covalent_unit_count");
        loadIntegerField("pc_descr_DefinedAtomStereoCount_value_[0-9]+\\.ttl\\.gz", "_Defined_Atom_Stereo_Count",
                "defined_atom_stereo_count");
        loadIntegerField("pc_descr_DefinedBondStereoCount_value_[0-9]+\\.ttl\\.gz", "_Defined_Bond_Stereo_Count",
                "defined_bond_stereo_count");
        loadIntegerField("pc_descr_FormalCharge_value_[0-9]+\\.ttl\\.gz", "_Total_Formal_Charge",
                "total_formal_charge");
        loadIntegerField("pc_descr_HBondAcceptor_value_[0-9]+\\.ttl\\.gz", "_Hydrogen_Bond_Acceptor_Count",
                "hydrogen_bond_acceptor_count");
        loadIntegerField("pc_descr_HBondDonor_value_[0-9]+\\.ttl\\.gz", "_Hydrogen_Bond_Donor_Count",
                "hydrogen_bond_donor_count");
        loadIntegerField("pc_descr_HeavyAtomCount_value_[0-9]+\\.ttl\\.gz", "_Non-hydrogen_Atom_Count",
                "non_hydrogen_atom_count");
        loadIntegerField("pc_descr_IsotopeAtomCount_value_[0-9]+\\.ttl\\.gz", "_Isotope_Atom_Count",
                "isotope_atom_count");
        loadIntegerField("pc_descr_RotatableBond_value_[0-9]+\\.ttl\\.gz", "_Rotatable_Bond_Count",
                "rotatable_bond_count");
        loadIntegerField("pc_descr_TautomerCount_value_[0-9]+\\.ttl\\.gz", "_Tautomer_Count", "tautomer_count");
        loadIntegerField("pc_descr_UndefinedAtomStereoCount_value_[0-9]+\\.ttl\\.gz", "_Undefined_Atom_Stereo_Count",
                "undefined_atom_stereo_count");
        loadIntegerField("pc_descr_UndefinedBondStereoCount_value_[0-9]+\\.ttl\\.gz", "_Undefined_Bond_Stereo_Count",
                "undefined_bond_stereo_count");

        loadFloatField("pc_descr_Complexity_value_[0-9]+\\.ttl\\.gz", "_Structure_Complexity", "structure_complexity");
        loadFloatField("pc_descr_ExactMass_value_[0-9]+\\.ttl\\.gz", "_Exact_Mass", "exact_mass");
        loadFloatField("pc_descr_MolecularWeight_value_[0-9]+\\.ttl\\.gz", "_Molecular_Weight", "molecular_weight");
        loadFloatField("pc_descr_MonoIsotopicWeight_value_[0-9]+\\.ttl\\.gz", "_Mono_Isotopic_Weight",
                "mono_isotopic_weight");
        loadFloatField("pc_descr_TPSA_value_[0-9]+\\.ttl\\.gz", "_TPSA", "tpsa");

        loadXLogP3Field("pc_descr_XLogP3_value_[0-9]+\\.ttl\\.gz");

        batch("delete from pubchem.descriptor_compound_bases where compound = ?", oldDescriptors);
        oldDescriptors = null;

        loadStringField("pc_descr_MolecularFormula_value_[0-9]+\\.ttl\\.gz", "_Molecular_Formula",
                "descriptor_compound_molecular_formulas", "molecular_formula");
        loadStringField("pc_descr_isoSMILES_value_[0-9]+\\.ttl\\.gz", "_Isomeric_SMILES",
                "descriptor_compound_isomeric_smileses", "isomeric_smiles");
        loadStringField("pc_descr_canSMILES_value_[0-9]+\\.ttl\\.gz", "_Canonical_SMILES",
                "descriptor_compound_canonical_smileses", "canonical_smiles");
        loadStringField("pc_descr_InChI_value_[0-9]+\\.ttl\\.gz", "_IUPAC_InChI", "descriptor_compound_iupac_inchis",
                "iupac_inchi");
        loadStringField("pc_descr_IUPACName_value_[0-9]+\\.ttl\\.gz", "_Preferred_IUPAC_Name",
                "descriptor_compound_preferred_iupac_names", "preferred_iupac_name");

        loadUnitField("pc_descr_ExactMass_unit_[0-9]+\\.ttl\\.gz", "_Exact_Mass",
                "http://purl.obolibrary.org/obo/UO_0000055");
        loadUnitField("pc_descr_MolecularWeight_unit_[0-9]+\\.ttl\\.gz", "_Molecular_Weight",
                "http://purl.obolibrary.org/obo/UO_0000055");
        loadUnitField("pc_descr_MonoIsotopicWeight_unit_[0-9]+\\.ttl\\.gz", "_Mono_Isotopic_Weight",
                "http://purl.obolibrary.org/obo/UO_0000055");
        loadUnitField("pc_descr_TPSA_unit_[0-9]+\\.ttl\\.gz", "_TPSA", "http://purl.obolibrary.org/obo/UO_0000324");

        System.out.println();
    }
}
