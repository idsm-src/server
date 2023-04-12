package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



public class Disease extends Updater
{
    private static IntHashSet usedDiseases;
    private static IntHashSet newDiseases;
    private static IntHashSet oldDiseases;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedDiseases = new IntHashSet();
        newDiseases = new IntHashSet();
        oldDiseases = getIntSet("select id from pubchem.disease_bases");

        new QueryResultProcessor(patternQuery("?disease rdf:type obo:DOID_4"))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID");

                if(!oldDiseases.remove(diseaseID))
                    newDiseases.add(diseaseID);

                usedDiseases.add(diseaseID);
            }
        }.load(model);

        batch("insert into pubchem.disease_bases(id) values (?)", newDiseases);
        newDiseases.clear();
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = getIntStringMap("select id, label from pubchem.disease_bases where label is not null");

        new QueryResultProcessor(patternQuery("?disease skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getDiseaseID(getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
                String label = getString("label");

                if(!label.equals(oldLabels.remove(diseaseID)))
                    newLabels.put(diseaseID, label);
            }
        }.load(model);

        batch("update pubchem.disease_bases set label = null where id = ?", oldLabels.keySet());
        batch("insert into pubchem.disease_bases(id, label) values (?,?) "
                + "on conflict (id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select disease, alternative from pubchem.disease_alternatives");

        new QueryResultProcessor(patternQuery("?disease skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getDiseaseID(getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(diseaseID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.disease_alternatives where disease = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.disease_alternatives(disease, alternative) values (?,?)", newAlternatives);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntTripletSet newMatches = new IntTripletSet();
        IntTripletSet oldMatches = getIntTripletSet(
                "select disease, match_unit, match_id from pubchem.disease_matches");

        new QueryResultProcessor(patternQuery(
                "?disease skos:closeMatch ?match. " + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getDiseaseID(getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
                Identifier match = Ontology.getId(getIRI("match"));

                IntTriplet triplet = new IntTriplet(diseaseID, match.unit, match.id);

                if(!oldMatches.remove(triplet))
                    newMatches.add(triplet);
            }
        }.load(model);

        batch("delete from pubchem.disease_matches where disease = ? and match_unit = ? and match_id = ?", oldMatches);
        batch("insert into pubchem.disease_matches(disease, match_unit, match_id) values (?,?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select disease, match from pubchem.disease_mesh_matches");

        new QueryResultProcessor(patternQuery(
                "?disease skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getDiseaseID(getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(diseaseID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.disease_mesh_matches where disease = ? and match = ?", oldMatches);
        batch("insert into pubchem.disease_mesh_matches(disease, match) values (?,?)", newMatches);
    }


    private static void loadRelatedMatches(Model model) throws IOException, SQLException
    {
        IntTripletSet newMatches = new IntTripletSet();
        IntTripletSet oldMatches = getIntTripletSet(
                "select disease, match_unit, match_id from pubchem.disease_related_matches");

        new QueryResultProcessor(patternQuery("?disease skos:relatedMatch ?match."))
        {
            @Override
            protected void parse() throws IOException
            {
                int diseaseID = getDiseaseID(getIntID("disease", "http://rdf.ncbi.nlm.nih.gov/pubchem/disease/DZID"));
                Identifier match = Ontology.getId(getIRI("match"));

                IntTriplet triplet = new IntTriplet(diseaseID, match.unit, match.id);

                if(!oldMatches.remove(triplet))
                    newMatches.add(triplet);

            }
        }.load(model);

        batch("delete from pubchem.disease_related_matches where disease = ? and match_unit = ? and match_id = ?",
                oldMatches);
        batch("insert into pubchem.disease_related_matches(disease, match_unit, match_id) values (?,?,?)", newMatches);
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

        batch("delete from pubchem.disease_bases where id = ?", oldDiseases);
        batch("insert into pubchem.disease_bases(id) values (?)" + " on conflict do nothing", newDiseases);

        usedDiseases = null;
        newDiseases = null;
        oldDiseases = null;

        System.out.println();
    }


    static int getDiseaseID(int diseaseID) throws IOException
    {
        synchronized(newDiseases)
        {
            if(!usedDiseases.contains(diseaseID))
            {
                System.out.println("    add missing disease DZID" + diseaseID);

                if(!oldDiseases.remove(diseaseID))
                    newDiseases.add(diseaseID);

                usedDiseases.add(diseaseID);
            }
        }

        return diseaseID;
    }
}
