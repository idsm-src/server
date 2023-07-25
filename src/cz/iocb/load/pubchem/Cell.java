package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Cell extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepCells = new IntSet();
    private static final IntSet newCells = new IntSet();
    private static final IntSet oldCells = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.cell_bases", oldCells);

        new QueryResultProcessor(patternQuery("?cell rdf:type sio:SIO_010054"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getIntID("cell", prefix);

                if(oldCells.remove(cellID))
                    keepCells.add(cellID);
                else
                    newCells.add(cellID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select id,label from pubchem.cell_bases where label is not null", oldLabels);

        new QueryResultProcessor(patternQuery("?cell skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"), true);
                String label = getString("label");

                if(label.equals(oldLabels.remove(cellID)))
                {
                    keepLabels.put(cellID, label);
                }
                else
                {
                    String keep = keepLabels.get(cellID);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLabels.put(cellID, label);

                    if(put != null && !label.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.cell_bases set label=null where id=? and label=?", oldLabels);
        store("insert into pubchem.cell_bases(id,label) values(?,?) on conflict(id) do update set label=EXCLUDED.label",
                newLabels);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntMap keepOrganisms = new IntIntMap();
        IntIntMap newOrganisms = new IntIntMap();
        IntIntMap oldOrganisms = new IntIntMap();

        load("select id,organism from pubchem.cell_bases where organism is not null", oldOrganisms);

        new QueryResultProcessor(patternQuery("?cell up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(getIRI("organism").equals(Taxonomy.prefix))
                    return;

                Integer cellID = getCellID(getIRI("cell"), true);
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                if(organismID.equals(oldOrganisms.remove(cellID)))
                {
                    keepOrganisms.put(cellID, organismID);
                }
                else
                {
                    Integer keep = keepOrganisms.get(cellID);

                    if(organismID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newOrganisms.put(cellID, organismID);

                    if(put != null && !organismID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.cell_bases set organism=null where id=? and organism=?", oldOrganisms);
        store("insert into pubchem.cell_bases(id,organism) values(?,?) "
                + "on conflict(id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select cell,alternative from pubchem.cell_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?cell skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(cellID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_alternatives where cell=? and alternative=?", oldAlternatives);
        store("insert into pubchem.cell_alternatives(cell,alternative) values(?,?)", newAlternatives);
    }


    private static void loadOccurrences(Model model) throws IOException, SQLException
    {
        IntStringSet newOccurrences = new IntStringSet();
        IntStringSet oldOccurrences = new IntStringSet();

        load("select cell,occurrence from pubchem.cell_occurrences", oldOccurrences);

        new QueryResultProcessor(patternQuery("?cell obo:BFO_0000050 ?occurrence"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String occurrence = getString("occurrence");

                Pair<Integer, String> pair = Pair.getPair(cellID, occurrence);

                if(!oldOccurrences.remove(pair))
                    newOccurrences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_occurrences where cell=? and occurrence=?", oldOccurrences);
        store("insert into pubchem.cell_occurrences(cell,occurrence) values(?,?)", newOccurrences);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select cell,reference from pubchem.cell_references", oldReferences);

        new QueryResultProcessor(patternQuery("?cell cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(cellID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_references where cell=? and reference=?", oldReferences);
        store("insert into pubchem.cell_references(cell,reference) values(?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select cell,match_unit,match_id from pubchem.cell_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?cell skos:closeMatch ?match. "
                + "filter(!strstarts(str(?match), 'https://web.expasy.org/cellosaurus/CVCL_'))"
                + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);

            }
        }.load(model);

        store("delete from pubchem.cell_matches where cell=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.cell_matches(cell,match_unit,match_id) values(?,?,?)", newMatches);
    }


    private static void loadCellosaurusCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_cellosaurus_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?cell skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://web.expasy.org/cellosaurus/CVCL_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String match = getStringID("match", "https://web.expasy.org/cellosaurus/CVCL_");

                Pair<Integer, String> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_cellosaurus_matches where cell=? and match=?", oldMatches);
        store("insert into pubchem.cell_cellosaurus_matches(cell,match) values(?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?cell skos:closeMatch ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_mesh_matches where cell=? and match=?", oldMatches);
        store("insert into pubchem.cell_mesh_matches(cell,match) values(?,?)", newMatches);
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

        store("delete from pubchem.cell_bases where id=?", oldCells);
        store("insert into pubchem.cell_bases(id) values(?)", newCells);

        System.out.println();
    }


    static Integer getCellID(String value) throws IOException
    {
        return getCellID(value, false);
    }


    static Integer getCellID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer cellID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newCells)
        {
            if(newCells.contains(cellID))
            {
                if(forceKeep)
                {
                    newCells.remove(cellID);
                    keepCells.add(cellID);
                }
            }
            else if(!keepCells.contains(cellID))
            {
                System.out.println("    add missing cell CELLID" + cellID);

                if(!oldCells.remove(cellID) && !forceKeep)
                    newCells.add(cellID);
                else
                    keepCells.add(cellID);
            }
        }

        return cellID;
    }
}
