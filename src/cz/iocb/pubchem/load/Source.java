package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Model;



public class Source extends Loader
{
    public static Map<String, Short> loadBases(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new TableLoader(model, loadQuery("source/bases.sparql"),
                "insert into source_bases (id, iri, title) values (?,?,?)")
        {
            short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getIRI("iri");
                map.put(iri, nextID);

                setValue(1, nextID++);
                setValue(2, iri);
                setValue(3, getLiteralValue("title"));
            }
        };

        return map;
    }


    public static Map<String, Short> loadSubjectsReftable(Model model) throws IOException, SQLException
    {
        Map<String, Short> map = new HashMap<String, Short>();

        new TableLoader(model, distinctPatternQuery("[] dcterms:subject ?iri"),
                "insert into source_subjects__reftable (id, iri) values (?,?)")
        {
            short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                String iri = getIRI("iri");
                map.put(iri, nextID);

                setValue(1, nextID++);
                setValue(2, iri);
            }
        };

        return map;
    }


    public static void loadSubjects(Model model, Map<String, Short> sources, Map<String, Short> subjects)
            throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?source dcterms:subject ?subject"),
                "insert into source_subjects (source, subject) values (?,?)")
        {
            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, getMapID("source", sources));
                setValue(2, getMapID("subject", subjects));
            }
        };
    }


    public static void loadAlternatives(Model model, Map<String, Short> sources) throws IOException, SQLException
    {
        new TableLoader(model, patternQuery("?source dcterms:alternative ?alternative"),
                "insert into source_alternatives (__, source, alternative) values (?, ?,?)")
        {
            short nextID = 0;

            @Override
            public void insert() throws SQLException, IOException
            {
                setValue(1, nextID++);
                setValue(2, getMapID("source", sources));
                setValue(3, getLiteralValue("alternative"));
            }
        };
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = loadModel(file);

        check(model, "source/check.sparql");
        Map<String, Short> sources = loadBases(model);
        Map<String, Short> subjects = loadSubjectsReftable(model);
        loadSubjects(model, sources, subjects);
        loadAlternatives(model, sources);

        model.close();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        load("RDF/source/pc_source.ttl.gz");
    }
}
