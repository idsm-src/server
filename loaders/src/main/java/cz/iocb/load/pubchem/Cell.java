package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.varchar;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Cell extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> cells = new EntityTable<>("pubchem.cell_bases", intKey("id"), null,
            integer("organism"), varchar("label"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?cell rdf:type sio:SIO_010054"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getIntID("cell", prefix);

                cells.reference(cellID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?cell skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String label = getString("label");

                cells.set(cellID, "label", label);
            }
        }.load(model);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?cell up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(getIRI("organism").equals(Taxonomy.prefix))
                    return;

                Integer cellID = getCellID(getIRI("cell"));
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                cells.set(cellID, "organism", organismID);
            }
        }.load(model);
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

        new QueryResultProcessor(patternQuery("""
                ?cell rdfs:seeAlso ?match. filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/mesh:'))\
                filter(!strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))\
                filter(!strstarts(str(?match), 'http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/cellosaurus:CVCL_'))"""))
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


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?cell rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
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


    private static void loadWikidataCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select cell,match from pubchem.cell_wikidata_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?cell rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                Integer match = getIntID("match", "http://www.wikidata.org/entity/Q");

                Pair<Integer, Integer> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_wikidata_matches where cell=? and match=?", oldMatches);
        store("insert into pubchem.cell_wikidata_matches(cell,match) values(?,?)", newMatches);
    }


    private static void loadCellosaurusMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_cellosaurus_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?cell rdfs:seeAlso ?match. "
                + "filter(strstarts(str(?match), 'http://identifiers.org/cellosaurus:CVCL_'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                String match = getStringID("match", "http://identifiers.org/cellosaurus:CVCL_");

                Pair<Integer, String> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_cellosaurus_matches where cell=? and match=?", oldMatches);
        store("insert into pubchem.cell_cellosaurus_matches(cell,match) values(?,?)", newMatches);
    }


    private static void loadChemblCardCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select cell,match from pubchem.cell_chembl_card_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?cell rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                Integer match = getIntID("match", "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL");

                Pair<Integer, Integer> pair = Pair.getPair(cellID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_chembl_card_matches where cell=? and match=?", oldMatches);
        store("insert into pubchem.cell_chembl_card_matches(cell,match) values(?,?)", newMatches);
    }


    private static void loadAnatomies(Model model) throws IOException, SQLException
    {
        IntPairSet newAnatomies = new IntPairSet();
        IntPairSet oldAnatomies = new IntPairSet();

        load("select cell,anatomy from pubchem.cell_anatomies", oldAnatomies);

        new QueryResultProcessor(patternQuery("?cell obo:RO_0001000 ?anatomy"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer cellID = getCellID(getIRI("cell"));
                Integer anatomyID = Anatomy.getAnatomyID(getIRI("anatomy"));

                Pair<Integer, Integer> pair = Pair.getPair(cellID, anatomyID);

                if(!oldAnatomies.remove(pair))
                    newAnatomies.add(pair);
            }
        }.load(model);

        store("delete from pubchem.cell_anatomies where cell=? and anatomy=?", oldAnatomies);
        store("insert into pubchem.cell_anatomies(cell,anatomy) values(?,?)", newAnatomies);
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
        loadMeshCloseMatches(model);
        loadWikidataCloseMatches(model);
        loadCellosaurusMatches(model);
        loadChemblCardCloseMatches(model);
        loadAnatomies(model);

        model.close();

        cells.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish cells ...");

        cells.store();

        System.out.println();
    }


    static Integer getCellID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer cellID = Integer.parseInt(value.substring(prefixLength));

        if(cells.reference(cellID))
            System.out.println("    add missing cell CELLID" + cellID);

        return cellID;
    }
}
