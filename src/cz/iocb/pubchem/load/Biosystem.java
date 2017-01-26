package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import org.apache.jena.rdf.model.Model;



public class Biosystem extends Loader
{
    public static Map<String, Short> loadSources(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = getMapping("source_bases");

        new TableLoader(model, distinctPatternQuery("[] dcterms:source ?source"), "source_bases (id, iri) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getIRI("source");

                if(map.get(iri) == null)
                {
                    System.out.println("  add missing source: " + iri);

                    short id = (short) map.size();
                    map.put(iri, id);

                    setValue(1, id);
                    setValue(2, iri);
                }
            }
        };

        return map;
    }


    public static void loadBases(Model model, Map<String, Short> sources) throws IOException, SQLException
    {
        new TableLoader(model, loadQuery("biosystem/bases.sparql"),
                "biosystem_bases(id, source, title, organism) values (?,?,?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
                setValue(2, getMapID("source", sources));
                setValue(3, getLiteralValue("title"));
                setValue(4, getIntID("organism", "http://identifiers.org/taxonomy/"));
            }
        };
    }


    public static void loadComponents(Model model) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?biosystem bp:pathwayComponent ?component"),
                "biosystem_components(biosystem, component) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
                setValue(2, getIntID("component", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
            }
        };
    }


    public static void loadReferences(Model model) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?biosystem cito:isDiscussedBy ?reference"),
                "biosystem_references(biosystem, reference) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
                setValue(2, getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        };
    }


    public static void loadMatches(Model model) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?biosystem skos:exactMatch ?wikipathway"),
                "biosystem_matches(biosystem, wikipathway) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getIntID("biosystem", "http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/BSID"));
                setValue(2, getIntID("wikipathway", "http://identifiers.org/wikipathways/WP"));
            }
        };
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);

        check(model, "biosystem/check.sparql");
        Map<String, Short> sources = loadSources(model);
        loadBases(model, sources);
        loadComponents(model);
        loadReferences(model);
        loadMatches(model);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/biosystem/pc_biosystem.ttl.gz");
    }
}
