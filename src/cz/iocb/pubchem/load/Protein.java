package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import cz.iocb.pubchem.load.Ontology.Identifier;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class Protein extends Loader
{
    private static Map<String, Integer> loadBases(Model model) throws IOException, SQLException
    {
        Map<String, Integer> map = new HashMap<String, Integer>();

        new ModelTableLoader(model, loadQuery("protein/bases.sparql"),
                "insert into protein_bases(id, name, organism_id, title) values (?,?,?,?)")
        {
            int nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getIRI("protein");
                map.put(iri, nextID);

                Identifier organism = Ontology.getId(getIRI("organism"));

                if(organism.unit != Ontology.unitTaxonomy)
                    throw new IOException();

                setValue(1, nextID++);
                setValue(2, getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"));
                setValue(3, organism.id);
                setValue(4, getLiteralValue("title"));
            }
        }.load();

        return map;
    }


    private static void loadReferences(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein cito:isDiscussedBy ?reference"),
                "insert into protein_references(protein, reference) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();
    }


    private static void loadPdbLinks(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein pdbo:link_to_pdb ?pdblink"),
                "insert into protein_pdblinks(protein, pdblink) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, getStringID("pdblink", "http://rdf.wwpdb.org/pdb/"));
            }
        }.load();
    }


    private static void loadSimilarProteins(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein vocab:hasSimilarProtein ?similar"),
                "insert into protein_similarproteins(protein, simprotein) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, proteins.get(getIRI("similar")));
            }
        }.load();
    }


    private static void loadGenes(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein vocab:encodedBy ?gene"),
                "insert into protein_genes(protein, gene) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
            }
        }.load();
    }


    private static void loadCloseMatches(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein skos:closeMatch ?match"),
                "insert into protein_closematches(__, protein, match) values (?,?,?)")
        {
            int nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, nextID++);
                setValue(2, proteins.get(getIRI("protein")));
                setValue(3, getStringID("match", "http://purl.uniprot.org/uniprot/"));
            }
        }.load();
    }


    private static void loadConservedDomains(Model model, Map<String, Integer> proteins)
            throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein obo:BFO_0000110 ?domain"),
                "insert into protein_conserveddomains(protein, domain) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
            }
        }.load();
    }


    private static void loadContinuantParts(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein obo:BFO_0000178 ?part"),
                "insert into protein_continuantparts(protein, part) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, proteins.get(getIRI("part")));
            }
        }.load();
    }


    private static void loadProcesses(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model,
                patternQuery("?protein obo:BFO_0000056 ?process "
                        + "filter(strstarts(str(?process), \"http://purl.obolibrary.org/obo/GO_\"))"),
                "insert into protein_processes(protein, process_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier process = Ontology.getId(getIRI("process"));

                if(process.unit != Ontology.unitGO)
                    throw new IOException();

                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, process.id);
            }
        }.load();
    }


    private static void loadBiosystems(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model,
                patternQuery("?protein obo:BFO_0000056 ?biosystem "
                        + "filter(strstarts(str(?biosystem), \"http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID\"))"),
                "insert into protein_biosystems(protein, biosystem) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
            }
        }.load();
    }


    private static void loadFunctions(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein obo:BFO_0000160 ?function"),
                "insert into protein_functions(protein, function_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier function = Ontology.getId(getIRI("function"));

                if(function.unit != Ontology.unitGO)
                    throw new IOException();

                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, function.id);
            }
        }.load();
    }


    private static void loadLocations(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?protein obo:BFO_0000171 ?location"),
                "insert into protein_locations(protein, location_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier location = Ontology.getId(getIRI("location"));

                if(location.unit != Ontology.unitGO)
                    throw new IOException();

                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, location.id);
            }
        }.load();
    }


    private static void loadTypes(Model model, Map<String, Integer> proteins) throws IOException, SQLException
    {
        new ModelTableLoader(model,
                patternQuery("?protein rdf:type ?type "
                        + "filter(strstarts(str(?type), \"http://purl.obolibrary.org/obo/PR_\"))"),
                "insert into protein_types(protein, type_id) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                Identifier type = Ontology.getId(getIRI("type"));

                if(type.unit != Ontology.unitPR)
                    throw new IOException();

                setValue(1, proteins.get(getIRI("protein")));
                setValue(2, type.id);
            }
        }.load();
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        check(model, "protein/check.sparql");
        Map<String, Integer> proteins = loadBases(model);
        loadReferences(model, proteins);
        loadPdbLinks(model, proteins);
        loadSimilarProteins(model, proteins);
        loadGenes(model, proteins);
        loadCloseMatches(model, proteins);
        loadConservedDomains(model, proteins);
        loadContinuantParts(model, proteins);
        loadProcesses(model, proteins);
        loadBiosystems(model, proteins);
        loadFunctions(model, proteins);
        loadLocations(model, proteins);
        loadTypes(model, proteins);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/protein/pc_protein.ttl.gz");
    }
}
