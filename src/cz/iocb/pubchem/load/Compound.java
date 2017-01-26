package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;



public class Compound extends Loader
{
    private static void loadBiosystems(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-biosystems.sparql");

        new TableLoader(model, patternQuery("?compound obo:BFO_0000056 ?biosystem"),
                "compound_biosystems(compound, biosystem) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
            }
        };

        model.close();
    }


    private static void loadComponents(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-components.sparql");

        new TableLoader(model, patternQuery("?compound_from sio:CHEMINF_000480 ?compound_to"),
                "compound_relations(compound_from, relation, compound_to) values (?, 480, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound_from", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("compound_to", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadDrugproducts(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-drugproducts.sparql");

        new TableLoader(model, patternQuery("?compound vocab:is_active_ingredient_of ?ingredient"),
                "compound_active_ingredients(compound, unit, ingredient) values (?, ?, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                String ingredient = getIRI("ingredient");
                setValue(1, getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));

                if(ingredient.startsWith("http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID("ingredient", "http://purl.bioontology.org/ontology/SNOMEDCT/"));
                }
                else if(ingredient.startsWith("http://purl.bioontology.org/ontology/NDFRT/N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID("ingredient", "http://purl.bioontology.org/ontology/NDFRT/N"));
                }
                else
                {
                    throw new IOException();
                }
            }
        };

        model.close();
    }


    private static void loadIsotopologues(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-isotopologues.sparql");

        new TableLoader(model, patternQuery("?compound_from sio:CHEMINF_000455 ?compound_to"),
                "compound_relations(compound_from, relation, compound_to) values (?, 455, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound_from", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("compound_to", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadParents(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-parents.sparql");

        new TableLoader(model, patternQuery("?compound_from vocab:has_parent ?compound_to"),
                "compound_relations(compound_from, relation, compound_to) values (?, 1024, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound_from", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("compound_to", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadSameConnectivities(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-same-connectivities.sparql");

        new TableLoader(model, patternQuery("?compound_from sio:CHEMINF_000462 ?compound_to"),
                "compound_relations(compound_from, relation, compound_to) values (?, 462, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound_from", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("compound_to", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadStereoisomers(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-stereoisomers.sparql");

        new TableLoader(model, patternQuery("?compound_from sio:CHEMINF_000461 ?compound_to"),
                "compound_relations(compound_from, relation, compound_to) values (?, 461, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound_from", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, getIntID("compound_to", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadRoles(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-roles.sparql");

        new TableLoader(model, patternQuery("?compound obo:has-role vocab:FDAApprovedDrugs"),
                "compound_roles(compound, roleid) values (?, 0)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
            }
        };

        model.close();
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);
        check(model, "compound/check-types.sparql");

        new TableLoader(model, patternQuery("?compound rdf:type ?type"),
                "compound_types(compound, unit, type) values (?, ?, ?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                String type = getIRI("type");
                setValue(1, getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));

                if(type.startsWith("http://purl.obolibrary.org/obo/CHEBI_"))
                {
                    setValue(2, 0);
                    setValue(3, getIntID("type", "http://purl.obolibrary.org/obo/CHEBI_"));
                }
                else if(type.startsWith("http://purl.bioontology.org/ontology/SNOMEDCT/"))
                {
                    setValue(2, 1);
                    setValue(3, getIntID("type", "http://purl.bioontology.org/ontology/SNOMEDCT/"));
                }
                else if(type.startsWith("http://purl.bioontology.org/ontology/NDFRT/N"))
                {
                    setValue(2, 2);
                    setValue(3, getIntID("type", "http://purl.bioontology.org/ontology/NDFRT/N"));
                }
                else if(type.startsWith("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"))
                {
                    setValue(2, 3);
                    setValue(3, getIntID("type", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C"));
                }
                else if(type.equals("http://www.biopax.org/release/biopax-level3.owl#SmallMolecule"))
                {
                    setValue(2, 4);
                    setValue(3, -1);
                }
                else
                {
                    throw new IOException();
                }
            }
        };

        model.close();
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
