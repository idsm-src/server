package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.Ontology.Identifier;
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
                "insert into compound_relations(compound_from, relation_unit, relation_id, compound_to) values (?,?,?,?)")
        {
            final Identifier relation = Ontology.getId("http://semanticscience.org/resource/CHEMINF_000480");

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000480"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, relation.unit);
                setValue(3, relation.id);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadDrugproducts(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_active_ingredients(compound, ingredient_unit, ingredient_id) values (?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#is_active_ingredient_of"))
                    throw new IOException();

                Identifier ingredient = Ontology.getId(object.getURI());

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, ingredient.unit);
                setValue(3, ingredient.id);
            }
        }.load();

        stream.close();
    }


    private static void loadIsotopologues(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation_unit, relation_id, compound_to) values (?,?,?,?)")
        {
            final Identifier relation = Ontology.getId("http://semanticscience.org/resource/CHEMINF_000455");

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000455"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, relation.unit);
                setValue(3, relation.id);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadParents(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation_unit, relation_id, compound_to) values (?,?,?,?)")
        {
            final Identifier relation = Ontology.getId("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent");

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, relation.unit);
                setValue(3, relation.id);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadSameConnectivities(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation_unit, relation_id, compound_to) values (?,?,?,?)")
        {
            final Identifier relation = Ontology.getId("http://semanticscience.org/resource/CHEMINF_000462");

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000462"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, relation.unit);
                setValue(3, relation.id);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadStereoisomers(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into compound_relations(compound_from, relation_unit, relation_id, compound_to) values (?,?,?,?)")
        {
            final Identifier relation = Ontology.getId("http://semanticscience.org/resource/CHEMINF_000461");

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000461"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, relation.unit);
                setValue(3, relation.id);
                setValue(4, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        }.load();

        stream.close();
    }


    private static void loadRoles(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into compound_roles(compound, role_id) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/has-role"))
                    throw new IOException();

                Identifier role = Ontology.getId(object.getURI());

                if(role.unit != Ontology.unitUncategorized)
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, role.id);
            }
        }.load();

        stream.close();
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into compound_types(compound, type_unit, type_id) values (?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                Identifier type = Ontology.getId(object.getURI());

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, type.unit);
                setValue(3, type.id);
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
