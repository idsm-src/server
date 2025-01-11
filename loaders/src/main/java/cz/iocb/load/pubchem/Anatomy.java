package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Anatomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepAnatomies = new IntSet();
    private static final IntSet newAnatomies = new IntSet();
    private static final IntSet oldAnatomies = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.anatomy_bases", oldAnatomies);

        new QueryResultProcessor(patternQuery("?anatomy rdf:type sio:SIO_001262"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getIntID("anatomy", prefix);

                if(oldAnatomies.remove(anatomyID))
                    keepAnatomies.add(anatomyID);
                else
                    newAnatomies.add(anatomyID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select id,label from pubchem.anatomy_bases where label is not null", oldLabels);

        new QueryResultProcessor(patternQuery("?anatomy skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer anatomyID = getAnatomyID(getIRI("anatomy"), true);
                String label = getString("label");

                if(label.equals(oldLabels.remove(anatomyID)))
                {
                    keepLabels.put(anatomyID, label);
                }
                else
                {
                    String keep = keepLabels.get(anatomyID);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLabels.put(anatomyID, label);

                    if(put != null && !label.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.anatomy_bases set label=null where id=? and label=?", oldLabels);
        store("insert into pubchem.anatomy_bases(id,label) values(?,?) "
                + "on conflict(id) do update set label=EXCLUDED.label", newLabels);
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

        new QueryResultProcessor(patternQuery(
                "?anatomy rdfs:seeAlso ?match. " + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"
                        + "filter(!strstarts(str(?match), 'http://identifiers.org/mesh:'))"))
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

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish anatomies ...");

        store("delete from pubchem.anatomy_bases where id=?", oldAnatomies);
        store("insert into pubchem.anatomy_bases(id) values(?)", newAnatomies);

        System.out.println();
    }


    static Integer getAnatomyID(String value) throws IOException
    {
        return getAnatomyID(value, false);
    }


    static Integer getAnatomyID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer anatomyID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newAnatomies)
        {
            if(newAnatomies.contains(anatomyID))
            {
                if(forceKeep)
                {
                    newAnatomies.remove(anatomyID);
                    keepAnatomies.add(anatomyID);
                }
            }
            else if(!keepAnatomies.contains(anatomyID))
            {
                System.out.println("    add missing anatomy ANATOMYID" + anatomyID);

                if(!oldAnatomies.remove(anatomyID) && !forceKeep)
                    newAnatomies.add(anatomyID);
                else
                    keepAnatomies.add(anatomyID);
            }
        }

        return anatomyID;
    }
}
