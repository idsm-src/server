package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;



public class Concept extends Loader
{
    public static Map<String, Short> loadBases(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new TableLoader(model, loadQuery("concept/bases.sparql"),
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
        };

        return map;
    }


    public static void loadScheme(Model model, Map<String, Short> concepts) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?concept skos:inScheme ?scheme"),
                "update concept_bases set scheme=? where id=?")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getMapID("scheme", concepts));
                setValue(2, getMapID("concept", concepts));
            }
        };
    }


    public static void loadBroader(Model model, Map<String, Short> concepts) throws IOException, SQLException
    {
        // workaround: filter(?concept != ?broader)
        new TableLoader(model, patternQuery("?concept skos:broader ?broader filter(?concept != ?broader)"),
                "update concept_bases set broader=? where id=?")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getMapID("broader", concepts));
                setValue(2, getMapID("concept", concepts));
            }
        };
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);

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
