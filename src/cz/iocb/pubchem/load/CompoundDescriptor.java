package cz.iocb.pubchem.load;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
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
        public DescriptorSimpleFileTableLoader(InputStream stream, String field)
        {
            super(stream, "insert into descriptor_compound_bases (" + field + ", compound) values (?,?) "
                    + "on conflict (compound) do update set " + field + "=EXCLUDED." + field);
        }
    }


    private static void processIntegerFile(String file, String suffix, String field) throws IOException, SQLException
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

                setValue(1, getInteger(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    private static void processFloatFile(String file, String suffix, String field) throws IOException, SQLException
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

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    private static void processXLogP3File(String file, String field) throws IOException, SQLException
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

                setValue(1, getFloat(object));
                setValue(2, id);
            }
        }.load();

        stream.close();
    }


    private static void processStringFile(String file, String suffix, String table, String field, int limit)
            throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into " + table + "(compound, " + field + ") values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/CID", suffix));
                setValue(2, getString(object));
            }
        }.load();

        stream.close();
    }


    private static void processUnitFile(String file, String suffix, String unit)
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
        catch(RuntimeException e)
        {
            if(e.getCause() instanceof IOException)
                throw(IOException) e.getCause();
            else
                throw e;
        }

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);

        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).forEach(name -> {
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
            catch(IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/descriptor/compound");
    }
}
