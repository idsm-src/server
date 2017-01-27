package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;



public class ConservedDomain extends Loader
{
    public static void loadBases(Model model) throws IOException, SQLException
    {
        new TableLoader(model, loadQuery("conserveddomain/bases.sparql"),
                "insert into conserveddomain_bases(id, title, abstract) values (?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                setValue(2, getLiteralValue("title"));
                setValue(3, getLiteralValue("abstract"));
            }
        };
    }


    public static void loadReferences(Model model) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?domain cito:isDiscussedBy ?reference"),
                "insert into conserveddomain_references(domain, reference) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("domain", "http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/PSSMID"));
                setValue(2, getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        };
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);

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
