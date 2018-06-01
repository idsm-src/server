package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class Compound extends Loader
{
    private static void loadBiosystems(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into compound_biosystems(compound, biosystem) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/BFO_0000056"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
            }
        }.load();

        stream.close();
    }


    private static void loadComponents(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 480, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000480"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadDrugproducts(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_active_ingredients(compound, unit, ingredient) values (?, ?, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));

                if(object.getURI().startsWith("http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID(object, "http://purl.bioontology.org/ontology/SNOMEDCT/"));
                }
                else if(object.getURI().startsWith("http://purl.bioontology.org/ontology/NDFRT/N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID(object, "http://purl.bioontology.org/ontology/NDFRT/N"));
                }
                else
                {
                    throw new IOException();
                }
            }
        }.load();

        stream.close();
    }


    private static void loadIsotopologues(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 455, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000455"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadParents(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 1024, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadSameConnectivities(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 462, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000462"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadStereoisomers(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation, compound_to) values (?, 461, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000461"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadRoles(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into compound_roles(compound, roleid) values (?, 0)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/has-role")
                        || !object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into compound_types(compound, unit, type) values (?, ?, ?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));

                if(object.getURI().startsWith("http://purl.obolibrary.org/obo/CHEBI_"))
                {
                    setValue(2, 0);
                    setValue(3, getIntID(object, "http://purl.obolibrary.org/obo/CHEBI_"));
                }
                else if(object.getURI().startsWith("http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID(object, "http://purl.bioontology.org/ontology/SNOMEDCT/"));
                }
                else if(object.getURI().startsWith("http://purl.bioontology.org/ontology/NDFRT/N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID(object, "http://purl.bioontology.org/ontology/NDFRT/N"));
                }
                else if(object.getURI().startsWith("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
                {
                    setValue(2, 3);
                    setValue(3, getIntID(object, "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"));
                }
                else if(object.getURI().equals("http://www.biopax.org/release/biopax-level3.owl#SmallMolecule"))
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

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);


        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_compound2biosystem"))
                    loadBiosystems(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2component"))
                    loadComponents(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2drugproduct"))
                    loadDrugproducts(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2isotopologue"))
                    loadIsotopologues(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2parent"))
                    loadParents(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2sameconnectivity"))
                    loadSameConnectivities(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2stereoisomer"))
                    loadStereoisomers(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound_role"))
                    loadRoles(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound_type"))
                    loadTypes(path + File.separatorChar + name);
                else if(name.startsWith("pc_compound2descriptor"))
                    System.out.println("ignore " + path + File.separator + name);
                else
                    System.out.println("unsupported " + path + File.separator + name);
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
        loadDirectory("RDF/compound/general");
    }
}
