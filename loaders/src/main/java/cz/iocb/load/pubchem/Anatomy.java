package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Anatomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> anatomies = new EntityTable<>("pubchem.anatomy_bases", intKey("id"), null,
            varchar("label"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?anatomy rdf:type sio:SIO_001262"))
        {
            @Override
            protected void parse() throws IOException
            {
                anatomies.reference(getIntID("anatomy", prefix));
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?anatomy skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                anatomies.set(getAnatomyID(getIRI("anatomy")), "label", getString("label"));
            }
        }.load(model);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select anatomy,alternative from pubchem.anatomy_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?anatomy skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getAnatomyID(getIRI("anatomy"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(anatomyID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.anatomy_alternatives where anatomy=? and alternative=?", oldAlternatives);
        store("insert into pubchem.anatomy_alternatives(anatomy,alternative) values(?,?)", newAlternatives);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select anatomy,match_unit,match_id from pubchem.anatomy_matches", oldMatches);

        new QueryResultProcessor(patternQuery("""
                ?anatomy rdfs:seeAlso ?match. \
                filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/mesh:'))"""))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getAnatomyID(getIRI("anatomy"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(anatomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);

            }
        }.load(model);

        store("delete from pubchem.anatomy_matches where anatomy=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.anatomy_matches(anatomy,match_unit,match_id) values(?,?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select anatomy,match from pubchem.anatomy_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?anatomy rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getAnatomyID(getIRI("anatomy"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(anatomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.anatomy_mesh_matches where anatomy=? and match=?", oldMatches);
        store("insert into pubchem.anatomy_mesh_matches(anatomy,match) values(?,?)", newMatches);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select anatomy,patent from pubchem.anatomy_patents", oldPatents);

        new QueryResultProcessor(patternQuery("?anatomy cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getAnatomyID(getIRI("anatomy"));
                Integer patentID = Patent.getPatentID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(anatomyID, patentID);

                if(!oldPatents.remove(pair))
                    newPatents.add(pair);
            }
        }.load(model);

        store("delete from pubchem.anatomy_patents where anatomy=? and patent=?", oldPatents);
        store("insert into pubchem.anatomy_patents(anatomy,patent) values(?,?)", newPatents);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load anatomys ...");

        Model model = getModel("pubchem/RDF/anatomy/pc_anatomy.ttl.gz");

        check(model, "pubchem/anatomy/check.sparql");

        loadBases(model);
        loadLabels(model);
        loadAlternatives(model);
        loadCloseMatches(model);
        loadMeshCloseMatches(model);
        loadReferences(model);

        model.close();

        anatomies.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish anatomies ...");

        anatomies.store();

        System.out.println();
    }


    static Integer getAnatomyID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer anatomyID = Integer.parseInt(value.substring(prefixLength));

        if(anatomies.reference(anatomyID))
            System.out.println("    add missing anatomy ANATOMYID" + anatomyID);

        return anatomyID;
    }
}
