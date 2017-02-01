package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class ConservedDomain extends Loader
{
    private static void loadBases(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, loadQuery("conserveddomain/bases.sparql"),
                "insert into conserveddomain_bases(id, title, abstract) values (?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                setValue(2, getLiteralValue("title"));
                setValue(3, getLiteralValue("abstract"));
            }
        }.load();
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?domain cito:isDiscussedBy ?reference"),
                "insert into conserveddomain_references(domain, reference) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                setValue(2, getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        check(model, "conserveddomain/check.sparql");
        loadBases(model);
        loadReferences(model);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/conserveddomain/pc_conserveddomain.ttl.gz");
    }
}
