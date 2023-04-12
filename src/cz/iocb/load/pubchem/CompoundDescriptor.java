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
        IntIntHashMap newValues = new IntIntHashMap();
        IntIntHashMap oldValues = getIntIntMap("select compound, " + field
                + " from pubchem.descriptor_compound_bases where " + field + " is not null");

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
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
        IntFloatHashMap newValues = new IntFloatHashMap();
        IntFloatHashMap oldValues = getIntFloatMap("select compound, " + field
                + " from pubchem.descriptor_compound_bases where " + field + " is not null");

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
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
        IntFloatHashMap newAAValues = new IntFloatHashMap();
        IntFloatHashMap oldAAValues = getIntFloatMap(
                "select compound, xlogp3_aa from pubchem.descriptor_compound_bases where xlogp3_aa is not null");

        IntFloatHashMap newValues = new IntFloatHashMap();
        IntFloatHashMap oldValues = getIntFloatMap(
                "select compound, xlogp3 from pubchem.descriptor_compound_bases where xlogp3 is not null");

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
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
        IntStringMap newValues = new IntStringMap();
        IntStringMap oldValues = getIntStringMap(
                "select compound, " + field + " from pubchem.descriptor_compound_" + table);

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
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

        batch("delete from pubchem.descriptor_compound_" + table + " where compound = ?", oldValues.keySet());
        batch("insert into pubchem.descriptor_compound_" + table + " (compound, " + field + ") values (?,?) "
                + "on conflict (compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


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

                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000221"))
                            throw new IOException();

                        if(!object.getURI().equals(unit))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    private static void checkType(String name, String suffix, String typeName) throws IOException, SQLException
    {
        final String type = "http://semanticscience.org/resource/" + typeName;

        processFiles("pubchem/RDF/descriptor/compound", "pc_descr_" + name + "_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);

                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(!object.getURI().equals(type))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    private static void checkXLogP3Type(String name, String typeName) throws IOException, SQLException
    {
        final String type = "http://semanticscience.org/resource/" + typeName;

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

                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        if(!object.getURI().equals(type))
                            throw new IOException();
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load compound descriptors ...");

        oldDescriptors = getIntSet("select compound from pubchem.descriptor_compound_bases");

        loadIntegerField("CovalentUnitCount", "_Covalent_Unit_Count", "covalent_unit_count");
        loadIntegerField("DefinedAtomStereoCount", "_Defined_Atom_Stereo_Count", "defined_atom_stereo_count");
        loadIntegerField("DefinedBondStereoCount", "_Defined_Bond_Stereo_Count", "defined_bond_stereo_count");
        loadIntegerField("FormalCharge", "_Total_Formal_Charge", "total_formal_charge");
        loadIntegerField("HBondAcceptor", "_Hydrogen_Bond_Acceptor_Count", "hydrogen_bond_acceptor_count");
        loadIntegerField("HBondDonor", "_Hydrogen_Bond_Donor_Count", "hydrogen_bond_donor_count");
        loadIntegerField("HeavyAtomCount", "_Non-hydrogen_Atom_Count", "non_hydrogen_atom_count");
        loadIntegerField("IsotopeAtomCount", "_Isotope_Atom_Count", "isotope_atom_count");
        loadIntegerField("RotatableBond", "_Rotatable_Bond_Count", "rotatable_bond_count");
        loadIntegerField("TautomerCount", "_Tautomer_Count", "tautomer_count");
        loadIntegerField("UndefinedAtomStereoCount", "_Undefined_Atom_Stereo_Count", "undefined_atom_stereo_count");
        loadIntegerField("UndefinedBondStereoCount", "_Undefined_Bond_Stereo_Count", "undefined_bond_stereo_count");

        loadFloatField("Complexity", "_Structure_Complexity", "structure_complexity");
        loadFloatField("ExactMass", "_Exact_Mass", "exact_mass");
        loadFloatField("MolecularWeight", "_Molecular_Weight", "molecular_weight");
        loadFloatField("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "mono_isotopic_weight");
        loadFloatField("TPSA", "_TPSA", "tpsa");

        loadXLogP3Field("XLogP3");

        batch("delete from pubchem.descriptor_compound_bases where compound = ?", oldDescriptors);
        oldDescriptors = null;

        loadStringField("MolecularFormula", "_Molecular_Formula", "molecular_formulas", "molecular_formula");
        loadStringField("isoSMILES", "_Isomeric_SMILES", "isomeric_smileses", "isomeric_smiles");
        loadStringField("canSMILES", "_Canonical_SMILES", "canonical_smileses", "canonical_smiles");
        loadStringField("InChI", "_IUPAC_InChI", "iupac_inchis", "iupac_inchi");
        loadStringField("IUPACName", "_Preferred_IUPAC_Name", "preferred_iupac_names", "preferred_iupac_name");

        checkUnit("ExactMass", "_Exact_Mass", "UO_0000055");
        checkUnit("MolecularWeight", "_Molecular_Weight", "UO_0000055");
        checkUnit("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "UO_0000055");
        checkUnit("TPSA", "_TPSA", "UO_0000324");

        checkType("Complexity", "_Structure_Complexity", "CHEMINF_000390");
        checkType("CovalentUnitCount", "_Covalent_Unit_Count", "CHEMINF_000369");
        checkType("DefinedAtomStereoCount", "_Defined_Atom_Stereo_Count", "CHEMINF_000370");
        checkType("DefinedBondStereoCount", "_Defined_Bond_Stereo_Count", "CHEMINF_000371");
        checkType("ExactMass", "_Exact_Mass", "CHEMINF_000338");
        checkType("FormalCharge", "_Total_Formal_Charge", "CHEMINF_000336");
        checkType("HBondAcceptor", "_Hydrogen_Bond_Acceptor_Count", "CHEMINF_000388");
        checkType("HBondDonor", "_Hydrogen_Bond_Donor_Count", "CHEMINF_000387");
        checkType("HeavyAtomCount", "_Non-hydrogen_Atom_Count", "CHEMINF_000373");
        checkType("IsotopeAtomCount", "_Isotope_Atom_Count", "CHEMINF_000372");
        checkType("MolecularFormula", "_Molecular_Formula", "CHEMINF_000335");
        checkType("MolecularWeight", "_Molecular_Weight", "CHEMINF_000334");
        checkType("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "CHEMINF_000337");
        checkType("RotatableBond", "_Rotatable_Bond_Count", "CHEMINF_000389");
        checkType("TPSA", "_TPSA", "CHEMINF_000392");
        checkType("TautomerCount", "_Tautomer_Count", "CHEMINF_000391");
        checkType("UndefinedAtomStereoCount", "_Undefined_Atom_Stereo_Count", "CHEMINF_000374");
        checkType("UndefinedBondStereoCount", "_Undefined_Bond_Stereo_Count", "CHEMINF_000375");
        checkXLogP3Type("XLogP3", "CHEMINF_000395");
        checkType("canSMILES", "_Canonical_SMILES", "CHEMINF_000376");
        checkType("isoSMILES", "_Isomeric_SMILES", "CHEMINF_000379");
        checkType("InChI", "_IUPAC_InChI", "CHEMINF_000396");
        checkType("IUPACName", "_Preferred_IUPAC_Name", "CHEMINF_000382");

        System.out.println();
    }
}
