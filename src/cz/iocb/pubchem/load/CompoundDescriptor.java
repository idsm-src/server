package cz.iocb.pubchem.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.PubchemFileTableLoader;



public class CompoundDescriptor extends Loader
{
    private static abstract class DescriptorSimpleFileTableLoader extends PubchemFileTableLoader
    {
        private static final BitSet ids = new BitSet();
        protected final ArrayList<Integer> idList = new ArrayList<Integer>(Loader.batchSize);


        public DescriptorSimpleFileTableLoader(BufferedReader reader, String field)
        {
            super(reader, "update descriptor_compound_bases set " + field + "=? where compound=?");
        }


        @Override
        public void beforeBatch() throws SQLException, IOException
        {
            synchronized(CompoundDescriptor.class)
            {
                try (Connection connection = Loader.getConnection())
                {
                    try (PreparedStatement insertStatement = connection
                            .prepareStatement("insert into descriptor_compound_bases(compound) values (?)"))
                    {
                        int count = 0;

                        for(Integer id : idList)
                        {
                            if(ids.length() <= id || ids.get(id) == false)
                            {
                                count++;
                                ids.set(id);

                                insertStatement.setInt(1, id);
                                insertStatement.addBatch();
                            }
                        }

                        if(count > 0)
                            insertStatement.executeBatch();
                    }
                }
            }

            idList.clear();
            idList.ensureCapacity(Loader.batchSize);
        }
    }


    protected static void processIntegerFile(String file, String suffix, String field) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new DescriptorSimpleFileTableLoader(reader, field)
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:has-value"))
                    throw new IOException();

                Integer id = getIntID(subject, "descriptor:CID", suffix);
                idList.add(id);

                setValue(1, getInteger(object));
                setValue(2, id);
            }
        }.load();

        reader.close();
    }

    protected static void processFloatFile(String file, String suffix, String field) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new DescriptorSimpleFileTableLoader(reader, field)
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:has-value"))
                    throw new IOException();

                Integer id = getIntID(subject, "descriptor:CID", suffix);
                idList.add(id);

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        reader.close();
    }


    protected static void processXLogP3File(String file, String field) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new DescriptorSimpleFileTableLoader(reader, field)
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:has-value"))
                    throw new IOException();

                String suffix = subject.endsWith("-AA") ? "_XLogP3-AA" : "_XLogP3";
                Integer id = getIntID(subject, "descriptor:CID", suffix);
                idList.add(id);

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        reader.close();
    }


    protected static void processStringFile(String file, String suffix, String table, String field, int limit)
            throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);
        LinkedHashMap<Integer, String> bigValues = new LinkedHashMap<Integer, String>();

        new PubchemFileTableLoader(reader, "insert into " + table + "(compound, " + field + ") values (?,?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:has-value"))
                    throw new IOException();

                Integer compound = getIntID(subject, "descriptor:CID", suffix);
                String value = getString(object);

                if(limit > 0 && value.length() >= limit)
                {
                    bigValues.put(compound, value);
                }
                else
                {
                    setValue(1, compound);
                    setValue(2, value);
                }
            }
        }.load();

        if(bigValues.size() > 0)
        {
            try (Connection connection = Loader.getConnection())
            {
                try (PreparedStatement insertStatement = connection
                        .prepareStatement("insert into " + table + "_long(compound, " + field + ") values (?,?)"))
                {
                    for(Entry<Integer, String> entry : bigValues.entrySet())
                    {
                        insertStatement.setInt(1, entry.getKey());
                        insertStatement.setString(2, entry.getValue());
                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }
            }
        }

        reader.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException, InterruptedException
    {
        File dir = new File(getPubchemDirectory() + path);
        File[] files = dir.listFiles();

        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        AtomicInteger counter = new AtomicInteger(-1);

        for(int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        for(int i = counter.incrementAndGet(); i < files.length; i = counter.incrementAndGet())
                        {
                            String name = files[i].getName();
                            String loc = path + File.separatorChar + files[i].getName();

                            if(name.startsWith("pc_descr_canSMILES_value"))
                                processStringFile(loc, "_Canonical_SMILES", "descriptor_compound_canonical_smileses",
                                        "canonical_smiles", 0);
                            else if(name.startsWith("pc_descr_Complexity_value"))
                                processFloatFile(loc, "_Structure_Complexity", "structure_complexity");
                            else if(name.startsWith("pc_descr_CovalentUnitCount_value"))
                                processIntegerFile(loc, "_Covalent_Unit_Count", "covalent_unit_count");
                            else if(name.startsWith("pc_descr_DefinedAtomStereoCount_value"))
                                processIntegerFile(loc, "_Defined_Atom_Stereo_Count", "defined_atom_stereo_count");
                            else if(name.startsWith("pc_descr_DefinedBondStereoCount_value"))
                                processIntegerFile(loc, "_Defined_Bond_Stereo_Count", "defined_bond_stereo_count");
                            else if(name.startsWith("pc_descr_ExactMass_value"))
                                processFloatFile(loc, "_Exact_Mass", "exact_mass");
                            else if(name.startsWith("pc_descr_FormalCharge_value"))
                                processIntegerFile(loc, "_Total_Formal_Charge", "total_formal_charge");
                            else if(name.startsWith("pc_descr_HBondAcceptor_value"))
                                processIntegerFile(loc, "_Hydrogen_Bond_Acceptor_Count",
                                        "hydrogen_bond_acceptor_count");
                            else if(name.startsWith("pc_descr_HBondDonor_value"))
                                processIntegerFile(loc, "_Hydrogen_Bond_Donor_Count", "hydrogen_bond_donor_count");
                            else if(name.startsWith("pc_descr_HeavyAtomCount_value"))
                                processIntegerFile(loc, "_Non-hydrogen_Atom_Count", "non_hydrogen_atom_count");
                            else if(name.startsWith("pc_descr_InChI_value"))
                                processStringFile(loc, "_IUPAC_InChI", "descriptor_compound_iupac_inchis",
                                        "iupac_inchi", 2048);
                            else if(name.startsWith("pc_descr_isoSMILES_value"))
                                processStringFile(loc, "_Isomeric_SMILES", "descriptor_compound_isomeric_smileses",
                                        "isomeric_smiles", 0);
                            else if(name.startsWith("pc_descr_IsotopeAtomCount_value"))
                                processIntegerFile(loc, "_Isotope_Atom_Count", "isotope_atom_count");
                            else if(name.startsWith("pc_descr_IUPACName_value"))
                                processStringFile(loc, "_Preferred_IUPAC_Name",
                                        "descriptor_compound_preferred_iupac_names", "preferred_iupac_name", 2048);
                            else if(name.startsWith("pc_descr_MolecularFormula_value"))
                                processStringFile(loc, "_Molecular_Formula", "descriptor_compound_molecular_formulas",
                                        "molecular_formula", 0);
                            else if(name.startsWith("pc_descr_MolecularWeight_value"))
                                processFloatFile(loc, "_Molecular_Weight", "molecular_weight");
                            else if(name.startsWith("pc_descr_MonoIsotopicWeight_value"))
                                processFloatFile(loc, "_Mono_Isotopic_Weight", "mono_isotopic_weight");
                            else if(name.startsWith("pc_descr_RotatableBond_value"))
                                processIntegerFile(loc, "_Rotatable_Bond_Count", "rotatable_bond_count");
                            else if(name.startsWith("pc_descr_TautomerCount_value"))
                                processIntegerFile(loc, "_Tautomer_Count", "tautomer_count");
                            else if(name.startsWith("pc_descr_TPSA_value"))
                                processFloatFile(loc, "_TPSA", "tpsa");
                            else if(name.startsWith("pc_descr_UndefinedAtomStereoCount_value"))
                                processIntegerFile(loc, "_Undefined_Atom_Stereo_Count", "undefined_atom_stereo_count");
                            else if(name.startsWith("pc_descr_UndefinedBondStereoCount_value"))
                                processIntegerFile(loc, "_Undefined_Bond_Stereo_Count", "undefined_bond_stereo_count");
                            else if(name.startsWith("pc_descr_XLogP3_value"))
                                processXLogP3File(loc, "xlogp3_aa");
                            else if(name.matches("pc_descr_.*_type_[0-9]+.ttl.gz"))
                                System.out.println("ignore " + loc);
                            else
                                System.out.println("unsupported " + loc);
                        }
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            };

            threads[i].start();
        }

        for(int i = 0; i < threads.length; i++)
            threads[i].join();
    }


    public static void main(String[] args) throws IOException, SQLException, InterruptedException
    {
        loadDirectory("RDF/descriptor/compound");
    }
}
