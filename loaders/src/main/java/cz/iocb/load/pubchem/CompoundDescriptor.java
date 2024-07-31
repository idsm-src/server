package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class CompoundDescriptor extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID";
    static final int prefixLength = prefix.length();

    private static final IntSet oldDescriptors = new IntSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select compound from pubchem.descriptor_compound_bases", oldDescriptors);
    }


    private static void loadIntegerField(String name, String suffix, String field) throws IOException, SQLException
    {
        IntIntMap keepValues = new IntIntMap();
        IntIntMap newValues = new IntIntMap();
        IntIntMap oldValues = new IntIntMap();

        load("select compound," + field + " from pubchem.descriptor_compound_bases where " + field + " is not null",
                oldValues);

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

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        Integer value = getInteger(object);

                        synchronized(newValues)
                        {
                            oldDescriptors.remove(id);

                            if(value.equals(oldValues.remove(id)))
                            {
                                keepValues.put(id, value);
                            }
                            else
                            {
                                Integer keep = keepValues.get(id);

                                if(value.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.descriptor_compound_bases set " + field + "=null where compound=? and " + field + "=?",
                oldValues);
        store("insert into pubchem.descriptor_compound_bases (compound," + field + ") values(?,?) "
                + "on conflict(compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


    private static void loadFloatField(String name, String suffix, String field) throws IOException, SQLException
    {
        IntFloatMap keepValues = new IntFloatMap();
        IntFloatMap newValues = new IntFloatMap();
        IntFloatMap oldValues = new IntFloatMap();

        load("select compound," + field + " from pubchem.descriptor_compound_bases where " + field + " is not null",
                oldValues);

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

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        Float value = getFloat(object);

                        synchronized(newValues)
                        {
                            oldDescriptors.remove(id);

                            if(value.equals(oldValues.remove(id)))
                            {
                                keepValues.put(id, value);
                            }
                            else
                            {
                                Float keep = keepValues.get(id);

                                if(value.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Float put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.descriptor_compound_bases set " + field + "=null where compound=? and " + field + "=?",
                oldValues);
        store("insert into pubchem.descriptor_compound_bases (compound," + field + ") values(?,?) "
                + "on conflict(compound) do update set " + field + "=EXCLUDED." + field, newValues);
    }


    private static void loadXLogP3Field(String name) throws IOException, SQLException
    {
        IntFloatMap keepAAValues = new IntFloatMap();
        IntFloatMap newAAValues = new IntFloatMap();
        IntFloatMap oldAAValues = new IntFloatMap();

        IntFloatMap keepValues = new IntFloatMap();
        IntFloatMap newValues = new IntFloatMap();
        IntFloatMap oldValues = new IntFloatMap();

        load("select compound,xlogp3_aa from pubchem.descriptor_compound_bases where xlogp3_aa is not null",
                oldAAValues);
        load("select compound,xlogp3 from pubchem.descriptor_compound_bases where xlogp3 is not null", oldValues);

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

                        Float value = getFloat(object);

                        if(subject.getURI().endsWith("-AA"))
                        {
                            Integer id = getDescriptorID(subject.getURI(), "_XLogP3-AA");

                            synchronized(newAAValues)
                            {
                                oldDescriptors.remove(id);

                                if(value.equals(oldAAValues.remove(id)))
                                {
                                    keepAAValues.put(id, value);
                                }
                                else
                                {
                                    Float keep = keepAAValues.get(id);

                                    if(value.equals(keep))
                                        return;
                                    else if(keep != null)
                                        throw new IOException();

                                    Float put = newAAValues.put(id, value);

                                    if(put != null && !value.equals(put))
                                        throw new IOException();
                                }
                            }
                        }
                        else
                        {
                            Integer id = getDescriptorID(subject.getURI(), "_XLogP3");

                            synchronized(newValues)
                            {
                                oldDescriptors.remove(id);

                                if(value.equals(oldValues.remove(id)))
                                {
                                    keepValues.put(id, value);
                                }
                                else
                                {
                                    Float keep = keepValues.get(id);

                                    if(value.equals(keep))
                                        return;
                                    else if(keep != null)
                                        throw new IOException();

                                    Float put = newValues.put(id, value);

                                    if(put != null && !value.equals(put))
                                        throw new IOException();
                                }
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.descriptor_compound_bases set xlogp3_aa=null where compound=? and xlogp3_aa=?",
                oldAAValues);
        store("insert into pubchem.descriptor_compound_bases (compound,xlogp3_aa) values(?,?) "
                + "on conflict(compound) do update set xlogp3_aa=EXCLUDED.xlogp3_aa", newAAValues);

        store("update pubchem.descriptor_compound_bases set xlogp3=null where compound=? and xlogp3=?", oldValues);
        store("insert into pubchem.descriptor_compound_bases (compound,xlogp3) values(?,?) "
                + "on conflict(compound) do update set xlogp3=EXCLUDED.xlogp3", newValues);
    }


    private static void loadStringField(String name, String suffix, String table, String field)
            throws IOException, SQLException
    {
        IntStringMap keepValues = new IntStringMap();
        IntStringMap newValues = new IntStringMap();
        IntStringMap oldValues = new IntStringMap();

        load("select compound," + field + " from pubchem.descriptor_compound_" + table, oldValues);

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

                        Integer id = getDescriptorID(subject.getURI(), suffix);
                        String value = getString(object);

                        synchronized(newValues)
                        {
                            if(value.equals(oldValues.remove(id)))
                            {
                                keepValues.put(id, value);
                            }
                            else
                            {
                                String keep = keepValues.get(id);

                                if(value.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newValues.put(id, value);

                                if(put != null && !value.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.descriptor_compound_" + table + " where compound=? and " + field + " =? ",
                oldValues);
        store("insert into pubchem.descriptor_compound_" + table + " (compound," + field + ") values(?,?) "
                + "on conflict(compound) do update set " + field + "=EXCLUDED." + field, newValues);
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

                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                            throw new IOException();

                        if(!value.equals(Integer.toString(id)))
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

        loadBases();

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

        loadStringField("MolecularFormula", "_Molecular_Formula", "molecular_formulas", "molecular_formula");
        loadStringField("isoSMILES", "_Isomeric_SMILES", "isomeric_smileses", "isomeric_smiles");
        loadStringField("canSMILES", "_Canonical_SMILES", "canonical_smileses", "canonical_smiles");
        loadStringField("InChI", "_IUPAC_InChI", "iupac_inchis", "iupac_inchi");
        loadStringField("IUPACName", "_Preferred_IUPAC_Name", "preferred_iupac_names", "preferred_iupac_name");

        checkUnit("ExactMass", "_Exact_Mass", "UO_0000055");
        checkUnit("MolecularWeight", "_Molecular_Weight", "UO_0000055");
        checkUnit("MonoIsotopicWeight", "_Mono_Isotopic_Weight", "UO_0000055");
        checkUnit("TPSA", "_TPSA", "UO_0000324");

        checkIdentifier("Compound_Identifier", "_Compound_Identifier");

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
        checkType("Compound_Identifier", "_Compound_Identifier", "CHEMINF_000140");

        System.out.println();
    }


    static void finish() throws SQLException
    {
        store("delete from pubchem.descriptor_compound_bases where compound=?", oldDescriptors);
    }


    private static Integer getDescriptorID(String value, String suffix) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        if(!value.endsWith(suffix))
            throw new IOException("unexpected IRI: " + value);

        Integer id = Integer.parseInt(value.substring(prefixLength, value.length() - suffix.length()));

        Compound.addCompoundID(id);

        return id;
    }
}
