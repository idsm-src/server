package cz.iocb.pubchem.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;
import cz.iocb.pubchem.load.common.VoidStreamRDF;



public class CompoundDescriptor extends Loader
{
    private static abstract class DescriptorSimpleFileTableLoader extends StreamTableLoader
    {
        private static final BitSet ids = new BitSet();
        protected final ArrayList<Integer> idList = new ArrayList<Integer>(Loader.batchSize);


        public DescriptorSimpleFileTableLoader(InputStream stream, String field)
        {
            super(stream, "update descriptor_compound_bases set " + field + "=? where compound=?");
        }


        @Override
        public void beforeBatch() throws SQLException, IOException
        {
            ArrayList<Integer> idAddList = new ArrayList<Integer>(Loader.batchSize);

            for(Integer id : idList)
                if(ids.length() <= id || ids.get(id) == false)
                    idAddList.add(id);

            idList.clear();
            idList.ensureCapacity(Loader.batchSize);

            if(idAddList.size() == 0)
                return;


            try (Connection connection = Loader.getConnection())
            {
                try (PreparedStatement insertStatement = connection
                        .prepareStatement("insert soft descriptor_compound_bases(compound) values (?)"))
                {
                    for(Integer id : idAddList)
                    {
                        insertStatement.setInt(1, id);
                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }
            }

            synchronized(CompoundDescriptor.class)
            {
                for(Integer id : idAddList)
                    ids.set(id);
            }
        }
    }


    protected static void StreamTableLoader(String file, String suffix, String field) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new DescriptorSimpleFileTableLoader(stream, field)
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                idList.add(id);

                setValue(1, getInteger(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    protected static void processIntegerFile(String file, String suffix, String field) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new DescriptorSimpleFileTableLoader(stream, field)
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                idList.add(id);

                setValue(1, getInteger(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    protected static void processFloatFile(String file, String suffix, String field) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new DescriptorSimpleFileTableLoader(stream, field)
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                idList.add(id);

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    protected static void processXLogP3File(String file, String field) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new DescriptorSimpleFileTableLoader(stream, field)
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                String suffix = subject.getURI().endsWith("-AA") ? "_XLogP3-AA" : "_XLogP3";
                int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
                idList.add(id);

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    protected static void processStringFile(String file, String suffix, String table, String field, int limit)
            throws IOException, SQLException
    {
        InputStream stream = getStream(file);
        LinkedHashMap<Integer, String> bigValues = new LinkedHashMap<Integer, String>();

        new StreamTableLoader(stream, "insert into " + table + "(compound, " + field + ") values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                int compound = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix);
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

        stream.close();
    }


    protected static void processUnitFile(String file, String suffix, String unit)
            throws FileNotFoundException, IOException
    {
        InputStream stream = getStream(file);

        try
        {
            RDFDataMgr.parse(new VoidStreamRDF()
            {
                @Override
                public void triple(Triple triple)
                {
                    String object = triple.getObject().getURI();

                    if(!object.equals(unit))
                        throw new RuntimeException(new IOException());
                }
            }, stream, Lang.TURTLE);
        }
        catch (RuntimeException e)
        {
            if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else
                throw e;
        }

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException, InterruptedException
    {
        File dir = new File(getPubchemDirectory() + path);

        Arrays.asList(dir.listFiles()).parallelStream().forEach(file -> {
            String name = file.getName();
            String loc = path + File.separatorChar + name;

            try
            {
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
                    processIntegerFile(loc, "_Hydrogen_Bond_Acceptor_Count", "hydrogen_bond_acceptor_count");
                else if(name.startsWith("pc_descr_HBondDonor_value"))
                    processIntegerFile(loc, "_Hydrogen_Bond_Donor_Count", "hydrogen_bond_donor_count");
                else if(name.startsWith("pc_descr_HeavyAtomCount_value"))
                    processIntegerFile(loc, "_Non-hydrogen_Atom_Count", "non_hydrogen_atom_count");
                else if(name.startsWith("pc_descr_InChI_value"))
                    processStringFile(loc, "_IUPAC_InChI", "descriptor_compound_iupac_inchis", "iupac_inchi", 2048);
                else if(name.startsWith("pc_descr_isoSMILES_value"))
                    processStringFile(loc, "_Isomeric_SMILES", "descriptor_compound_isomeric_smileses",
                            "isomeric_smiles", 0);
                else if(name.startsWith("pc_descr_IsotopeAtomCount_value"))
                    processIntegerFile(loc, "_Isotope_Atom_Count", "isotope_atom_count");
                else if(name.startsWith("pc_descr_IUPACName_value"))
                    processStringFile(loc, "_Preferred_IUPAC_Name", "descriptor_compound_preferred_iupac_names",
                            "preferred_iupac_name", 2048);
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
                else if(name.startsWith("pc_descr_ExactMass_unit"))
                    processUnitFile(loc, "_Exact_Mass", "http://purl.obolibrary.org/obo/UO_0000055");
                else if(name.startsWith("pc_descr_MolecularWeight_unit"))
                    processUnitFile(loc, "_Molecular_Weight", "http://purl.obolibrary.org/obo/UO_0000055");
                else if(name.startsWith("pc_descr_MonoIsotopicWeight_unit"))
                    processUnitFile(loc, "_Mono_Isotopic_Weight", "http://purl.obolibrary.org/obo/UO_0000055");
                else if(name.startsWith("pc_descr_TPSA_unit"))
                    processUnitFile(loc, "_TPSA", "http://purl.obolibrary.org/obo/UO_0000324");
                else if(name.matches("pc_descr_.*_type_[0-9]+.ttl.gz"))
                    System.out.println("ignore " + loc);
                else
                    System.out.println("unsupported " + loc);
            }
            catch (IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }


    public static void main(String[] args) throws IOException, SQLException, InterruptedException
    {
        loadDirectory("RDF/descriptor/compound");
    }
}
