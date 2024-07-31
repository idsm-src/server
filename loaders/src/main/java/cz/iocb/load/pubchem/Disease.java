package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Disease extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepDiseases = new IntSet();
    private static final IntSet newDiseases = new IntSet();
    private static final IntSet oldDiseases = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.disease_bases", oldDiseases);

        new QueryResultProcessor(patternQuery("?disease rdf:type obo:DOID_4"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getIntID("disease", prefix);

                if(oldDiseases.remove(diseaseID))
                    keepDiseases.add(diseaseID);
                else
                    newDiseases.add(diseaseID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select id,label from pubchem.disease_bases where label is not null", oldLabels);

        new QueryResultProcessor(patternQuery("?disease skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getDiseaseID(getIRI("disease"), true);
                String label = getString("label");

                if(label.equals(oldLabels.remove(diseaseID)))
                {
                    keepLabels.put(diseaseID, label);
                }
                else
                {
                    String keep = keepLabels.get(diseaseID);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLabels.put(diseaseID, label);

                    if(put != null && !label.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.disease_bases set label=null where id=? and label=?", oldLabels);
        store("insert into pubchem.disease_bases(id,label) values(?,?) "
                + "on conflict(id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select disease,alternative from pubchem.disease_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?disease skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getDiseaseID(getIRI("disease"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(diseaseID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.disease_alternatives where disease=? and alternative=?", oldAlternatives);
        store("insert into pubchem.disease_alternatives(disease,alternative) values(?,?)", newAlternatives);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?disease skos:closeMatch ?match. filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getDiseaseID(getIRI("disease"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.disease_matches where disease=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.disease_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select disease,match from pubchem.disease_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?disease skos:closeMatch ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getDiseaseID(getIRI("disease"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(diseaseID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.disease_mesh_matches where disease=? and match=?", oldMatches);
        store("insert into pubchem.disease_mesh_matches(disease,match) values(?,?)", newMatches);
    }


    private static void loadRelatedMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select disease,match_unit,match_id from pubchem.disease_related_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?disease skos:relatedMatch ?match."))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer diseaseID = getDiseaseID(getIRI("disease"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(diseaseID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);

            }
        }.load(model);

        store("delete from pubchem.disease_related_matches where disease=? and match_unit=? and match_id=?",
                oldMatches);
        store("insert into pubchem.disease_related_matches(disease,match_unit,match_id) values(?,?,?)", newMatches);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load diseases ...");

        Model model = getModel("pubchem/RDF/disease/pc_disease.ttl.gz");
        check(model, "pubchem/disease/check.sparql");

        loadBases(model);
        loadLabels(model);
        loadAlternatives(model);
        loadCloseMatches(model);
        loadMeshCloseMatches(model);
        loadRelatedMatches(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish diseases ...");

        store("delete from pubchem.disease_bases where id=?", oldDiseases);
        store("insert into pubchem.disease_bases(id) values(?)", newDiseases);

        System.out.println();
    }


    static Integer getDiseaseID(String value) throws IOException
    {
        return getDiseaseID(value, false);
    }


    static Integer getDiseaseID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer diseaseID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newDiseases)
        {
            if(newDiseases.contains(diseaseID))
            {
                if(forceKeep)
                {
                    newDiseases.remove(diseaseID);
                    keepDiseases.add(diseaseID);
                }
            }
            else if(!keepDiseases.contains(diseaseID))
            {
                System.out.println("    add missing disease DZID" + diseaseID);

                if(!oldDiseases.remove(diseaseID) && !forceKeep)
                    newDiseases.add(diseaseID);
                else
                    keepDiseases.add(diseaseID);
            }
        }

        return diseaseID;
    }
}
