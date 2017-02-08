package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.ModelTableLoader;



public class Concept extends Loader
{
    private static Map<String, Short> loadBases(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new ModelTableLoader(model, loadQuery("concept/bases.sparql"),
                "insert into concept_bases(id, iri, label) values (?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getIRI("iri");
                short id = (short) map.size();
                map.put(iri, id);

                setValue(1, id);
                setValue(2, iri);
                setValue(3, getLiteralValue("label"));
            }
        }.load();

        return map;
    }


    private static void loadScheme(Model model, Map<String, Short> concepts) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?concept skos:inScheme ?scheme"),
                "update concept_bases set scheme=? where id=?")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getMapID("scheme", concepts));
                setValue(2, getMapID("concept", concepts));
            }
        }.load();
    }


    private static void loadBroader(Model model, Map<String, Short> concepts) throws IOException, SQLException
    {
        new ModelTableLoader(model, patternQuery("?concept skos:broader ?broader"),
                "update concept_bases set broader=? where id=?")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getMapID("broader", concepts));
                setValue(2, getMapID("concept", concepts));
            }
        }.load();
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        check(model, "concept/check.sparql");
        Map<String, Short> concepts = loadBases(model);
        loadScheme(model, concepts);
        loadBroader(model, concepts);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/concept/pc_concept.ttl.gz");
    }
}
