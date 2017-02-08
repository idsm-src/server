package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class Gene extends Loader
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, loadQuery("gene/bases.sparql"),
                "insert into gene_bases(id, title, description) values (?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
                setValue(2, getLiteralValue("title"));
                setValue(3, getLiteralValue("description"));
            }
        }.load();
    }


    private static void loadBiosystems(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?gene obo:BFO_0000056 ?biosystem"),
                "insert into gene_biosystems(gene, biosystem) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
                setValue(2, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
            }
        }.load();
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?gene dcterms:alternative ?alternative"),
                //"insert into gene_alternatives (__, gene, alternative) values (?,?,?)")
                "insert into gene_alternatives (gene, alternative) values (?,?)")
        {
            //short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                //setValue(1, nextID++);
                setValue(1/*2*/, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
                setValue(2/*3*/, getLiteralValue("alternative"));
            }
        }.load();
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?gene cito:isDiscussedBy ?reference"),
                "insert into gene_references(gene, reference) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
                setValue(2, getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?gene skos:closeMatch ?match"),
                "insert into gene_matches(__, gene, match) values (?,?,?)")
        {
            int nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, nextID++);
                setValue(2, getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
                setValue(3, getStringID("match", "http://rdf.ebi.ac.uk/resource/ensembl/"));
            }
        }.load();
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        check(model, "gene/check.sparql");
        loadBases(model);
        loadBiosystems(model);
        loadAlternatives(model);
        loadReferences(model);
        loadCloseMatches(model);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/gene/pc_gene.ttl.gz");
    }
}
