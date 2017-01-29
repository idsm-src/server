package cz.iocb.pubchem.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.PubchemFileTableLoader;



public class Compound extends Loader
{
    private static void loadBiosystems(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader, "insert into compound_biosystems(compound, biosystem) values (?,?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("obo:BFO_0000056"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "biosystem:BSID"));
            }
        }.load();

        reader.close();
    }


    private static void loadComponents(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 480, ?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:CHEMINF_000480"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadDrugproducts(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_active_ingredients(compound, unit, ingredient) values (?, ?, ?)")
        {
            {
                prefixes.put("ns2:", "<http://purl.bioontology.org/ontology/NDFRT/>");
            }

            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("vocab:is_active_ingredient_of"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));

                if(object.startsWith("<http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID(object, "<http://purl.bioontology.org/ontology/SNOMEDCT/", ">"));
                }
                else if(object.startsWith("ns2:N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID(object, "ns2:N"));
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load();

        reader.close();
    }


    private static void loadIsotopologues(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 455, ?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:CHEMINF_000455"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadParents(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 1024, ?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("vocab:has_parent"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadSameConnectivities(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 462, ?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:CHEMINF_000462"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadStereoisomers(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 461, ?)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("sio:CHEMINF_000461"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
                setValue(2, getIntID(object, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadRoles(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader, "insert into compound_roles(compound, roleid) values (?, 0)")
        {
            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("obo:has-role") || !object.equals("vocab:FDAApprovedDrugs"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));
            }
        }.load();

        reader.close();
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        BufferedReader reader = getReader(file);

        new PubchemFileTableLoader(reader, "insert into compound_types(compound, unit, type) values (?, ?, ?)")
        {
            {
                prefixes.put("ns4:", "<http://purl.bioontology.org/ontology/NDFRT/>");
                prefixes.put("nci:", "<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>");
            }

            @Override
            public void insert(String subject, String predicate, String object) throws SQLException, IOException
            {
                if(!predicate.equals("rdf:type"))
                    throw new IOException();

                setValue(1, getIntID(subject, "compound:CID"));

                if(object.startsWith("obo:CHEBI_"))
                {
                    setValue(2, 0);
                    setValue(3, getIntID(object, "obo:CHEBI_"));
                }
                else if(object.startsWith("<http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID(object, "<http://purl.bioontology.org/ontology/SNOMEDCT/", ">"));
                }
                else if(object.startsWith("ns4:N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID(object, "ns4:N"));
                }
                else if(object.startsWith("nci:C"))
                {
                    setValue(2, 3);
                    setValue(3, getIntID(object, "nci:C"));
                }
                else if(object.equals("bp:SmallMolecule"))
                {
                    setValue(2, 4);
                    setValue(3, -1);
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load();

        reader.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);

        for(File file : dir.listFiles())
        {
            String name = file.getName();

            if(name.startsWith("pc_compound2biosystem"))
                loadBiosystems(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2component"))
                loadComponents(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2drugproduct"))
                loadDrugproducts(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2isotopologue"))
                loadIsotopologues(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2parent"))
                loadParents(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2sameconnectivity"))
                loadSameConnectivities(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2stereoisomer"))
                loadStereoisomers(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound_role"))
                loadRoles(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound_type"))
                loadTypes(path + File.separatorChar + file.getName());
            else if(name.startsWith("pc_compound2descriptor"))
                System.out.println("ignore " + path + File.separator + file.getName());
            else
                System.out.println("unsupported " + path + File.separator + file.getName());
        }
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/compound/general");
    }
}
