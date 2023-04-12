package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



public class Cell extends Updater
{
    private static IntHashSet usedCells;
    private static IntHashSet newCells;
    private static IntHashSet oldCells;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedCells = new IntHashSet();
        newCells = new IntHashSet();
        oldCells = getIntSet("select id from pubchem.cell_bases");

        new QueryResultProcessor(patternQuery("?cell rdf:type sio:SIO_010054"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");

                if(!oldCells.remove(cellID))
                    newCells.add(cellID);

                usedCells.add(cellID);
            }
        }.load(model);

        batch("insert into pubchem.cell_bases(id) values (?)", newCells);
        newCells.clear();
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = getIntStringMap("select id, label from pubchem.cell_bases where label is not null");

        new QueryResultProcessor(patternQuery("?cell skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                String label = getString("label");

                addCellID(cellID);

                if(!label.equals(oldLabels.remove(cellID)))
                    newLabels.put(cellID, label);
            }
        }.load(model);

        batch("update pubchem.cell_bases set label = null where id = ?", oldLabels.keySet());
        batch("insert into pubchem.cell_bases(id, label) values (?,?) "
                + "on conflict (id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap();
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism from pubchem.cell_bases where organism is not null");

        new QueryResultProcessor(patternQuery("?cell up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                if(getIRI("organism").equals("http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"))
                    return;

                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                int organismID = Taxonomy
                        .getTaxonomyID(getIntID("organism", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));

                addCellID(cellID);

                if(organismID != oldOrganisms.removeKeyIfAbsent(cellID, NO_VALUE))
                    newOrganisms.put(cellID, organismID);
            }
        }.load(model);

        batch("update pubchem.cell_bases set organism = null where id = ?", oldOrganisms.keySet());
        batch("insert into pubchem.cell_bases(id, organism) values (?,?) "
                + "on conflict (id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select cell, alternative from pubchem.cell_alternatives");

        new QueryResultProcessor(patternQuery("?cell skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(cellID, alternative);
                addCellID(cellID);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.cell_alternatives where cell = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.cell_alternatives(cell, alternative) values (?,?)", newAlternatives);
    }


    private static void loadOccurrences(Model model) throws IOException, SQLException
    {
        IntStringPairSet newOccurrences = new IntStringPairSet();
        IntStringPairSet oldOccurrences = getIntStringPairSet("select cell, occurrence from pubchem.cell_occurrences");

        new QueryResultProcessor(patternQuery("?cell obo:BFO_0000050 ?occurrence"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                String occurrence = getString("occurrence");

                IntObjectPair<String> pair = PrimitiveTuples.pair(cellID, occurrence);
                addCellID(cellID);

                if(!oldOccurrences.remove(pair))
                    newOccurrences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.cell_occurrences where cell = ? and occurrence = ?", oldOccurrences);
        batch("insert into pubchem.cell_occurrences(cell, occurrence) values (?,?)", newOccurrences);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = getIntPairSet("select cell, reference from pubchem.cell_references");

        new QueryResultProcessor(patternQuery("?cell cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                int referenceID = Reference
                        .getReferenceID(getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));

                IntIntPair pair = PrimitiveTuples.pair(cellID, referenceID);
                addCellID(cellID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.cell_references where cell = ? and reference = ?", oldReferences);
        batch("insert into pubchem.cell_references(cell, reference) values (?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntTripletSet newMatches = new IntTripletSet();
        IntTripletSet oldMatches = getIntTripletSet("select cell, match_unit, match_id from pubchem.cell_matches");

        new QueryResultProcessor(patternQuery("?cell skos:closeMatch ?match. "
                + "filter(!strstarts(str(?match), 'https://web.expasy.org/cellosaurus/CVCL_'))"
                + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                Identifier match = Ontology.getId(getIRI("match"));

                IntTriplet triplet = new IntTriplet(cellID, match.unit, match.id);
                addCellID(cellID);

                if(!oldMatches.remove(triplet))
                    newMatches.add(triplet);

            }
        }.load(model);

        batch("delete from pubchem.cell_matches where cell = ? and match_unit = ? and match_id = ?", oldMatches);
        batch("insert into pubchem.cell_matches(cell, match_unit, match_id) values (?,?,?)", newMatches);
    }


    private static void loadCellosaurusCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select cell, match from pubchem.cell_cellosaurus_matches");

        new QueryResultProcessor(patternQuery("?cell skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://web.expasy.org/cellosaurus/CVCL_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                String match = getStringID("match", "https://web.expasy.org/cellosaurus/CVCL_");

                IntObjectPair<String> pair = PrimitiveTuples.pair(cellID, match);
                addCellID(cellID);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.cell_cellosaurus_matches where cell = ? and match = ?", oldMatches);
        batch("insert into pubchem.cell_cellosaurus_matches(cell, match) values (?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select cell, match from pubchem.cell_mesh_matches");

        new QueryResultProcessor(patternQuery(
                "?cell skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int cellID = getIntID("cell", "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(cellID, match);
                addCellID(cellID);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.cell_mesh_matches where cell = ? and match = ?", oldMatches);
        batch("insert into pubchem.cell_mesh_matches(cell, match) values (?,?)", newMatches);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load cells ...");

        Model model = getModel("pubchem/RDF/cell/pc_cell.ttl.gz");
        check(model, "pubchem/cell/check.sparql");

        loadBases(model);
        loadLabels(model);
        loadOrganisms(model);
        loadAlternatives(model);
        loadOccurrences(model);
        loadReferences(model);
        loadCloseMatches(model);
        loadCellosaurusCloseMatches(model);
        loadMeshCloseMatches(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish cells ...");

        batch("delete from pubchem.cell_bases where id = ?", oldCells);
        batch("insert into pubchem.cell_bases(id) values (?)" + " on conflict do nothing", newCells);

        usedCells = null;
        newCells = null;
        oldCells = null;

        System.out.println();
    }


    static void addCellID(int cellID)
    {
        synchronized(newCells)
        {
            if(usedCells.add(cellID))
            {
                System.out.println("    add missing cell CELLID" + cellID);

                if(!oldCells.remove(cellID))
                    newCells.add(cellID);
            }
        }
    }
}
