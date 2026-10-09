package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.obo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.up;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Cell extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> cells = new EntityTable<>("pubchem.cells", intKey("id"), null,
            integer("organism"), varchar("label"));
    private static final MissingEntities<Integer> missingCells = new MissingEntities<>("cell", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", skos + "prefLabel", skos + "altLabel", rdfs + "seeAlso",
                cito + "isDiscussedBy", up + "organism", obo + "BFO_0000050", obo + "RO_0001000");
        dispatcher.checkTypes(all(), vocab + "Cell", sio + "SIO_010054");
        dispatcher.checkPrefixes(all(), rdfs + "seeAlso", "http://purl.obolibrary.org/obo/CL_",
                "http://purl.obolibrary.org/obo/CLO_", "http://purl.obolibrary.org/obo/BTO_",
                "http://www.ebi.ac.uk/efo/EFO_", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "http://id.nlm.nih.gov/mesh/", "https://www.cancerrxgene.org/translation/CellLine/",
                "https://depmap.org/portal/cell_line/ACH-", "https://cellmodelpassports.sanger.ac.uk/passports/SIDM",
                "https://cancer.sanger.ac.uk/cell_lines/sample/overview?id=", "http://identifiers.org/ncit:C",
                "http://identifiers.org/mesh:", "http://identifiers.org/CL:", "http://identifiers.org/lincs.cell:LCL-",
                "http://identifiers.org/efo:", "http://identifiers.org/BTO:", "http://identifiers.org/CLO:",
                "http://www.wikidata.org/entity/Q", "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL",
                "http://identifiers.org/cellosaurus:CVCL_", "https://lincs.hms.harvard.edu/db/cells/",
                "https://glyconnect.expasy.org/all/cell_lines/");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(sio + "SIO_010054", (subject, object) -> {
            Integer cellID = getIntID(subject, prefix);

            cells.reference(cellID);
            missingCells.described(cellID);
        });
    }


    private static void loadLabels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            Integer cellID = getCellID(subject.getURI());
            String label = getString(object);

            cells.set(cellID, "label", label);
        });
    }


    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            // workaround
            if(object.getURI().equals(Taxonomy.prefix))
                return;

            Integer cellID = getCellID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            cells.set(cellID, "organism", organismID);
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select cell,alternative from pubchem.cell_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer cellID = getCellID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(cellID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_alternatives where cell=? and alternative=?", oldAlternatives);
            store("insert into pubchem.cell_alternatives(cell,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadOccurrences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepOccurrences = new IntStringSet();
        IntStringSet newOccurrences = new IntStringSet();
        IntStringSet oldOccurrences = new IntStringSet();

        load("select cell,occurrence from pubchem.cell_occurrences", oldOccurrences);

        dispatcher.on(obo + "BFO_0000050", (subject, object) -> {
            Integer cellID = getCellID(subject.getURI());
            String occurrence = getString(object);

            Pair<Integer, String> pair = Pair.getPair(cellID, occurrence);

            if(oldOccurrences.remove(pair))
                keepOccurrences.add(pair);
            else if(!keepOccurrences.contains(pair))
                newOccurrences.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_occurrences where cell=? and occurrence=?", oldOccurrences);
            store("insert into pubchem.cell_occurrences(cell,occurrence) values(?,?)", newOccurrences);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select cell,reference from pubchem.cell_references", oldReferences);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer cellID = getCellID(subject.getURI());
            Integer referenceID = Reference.getReferenceID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(cellID, referenceID);

            if(oldReferences.remove(pair))
                keepReferences.add(pair);
            else if(!keepReferences.contains(pair))
                newReferences.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_references where cell=? and reference=?", oldReferences);
            store("insert into pubchem.cell_references(cell,reference) values(?,?)", newReferences);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select cell,match_unit,match_id from pubchem.cell_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:",
                    "http://www.wikidata.org/entity/Q", "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL",
                    "http://identifiers.org/cellosaurus:CVCL_"))
                return;

            Integer cellID = getCellID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getId(object.getURI());

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(cellID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_matches where cell=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.cell_matches(cell,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_mesh_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://id.nlm.nih.gov/mesh/"))
                return;

            Integer cellID = getCellID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(cellID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_mesh_matches where cell=? and match=?", oldMatches);
            store("insert into pubchem.cell_mesh_matches(cell,match) values(?,?)", newMatches);
        });
    }


    private static void loadWikidataCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select cell,match from pubchem.cell_wikidata_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://www.wikidata.org/entity/Q"))
                return;

            Integer cellID = getCellID(subject.getURI());
            Integer match = getIntID(object, "http://www.wikidata.org/entity/Q");

            Pair<Integer, Integer> pair = Pair.getPair(cellID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_wikidata_matches where cell=? and match=?", oldMatches);
            store("insert into pubchem.cell_wikidata_matches(cell,match) values(?,?)", newMatches);
        });
    }


    private static void loadCellosaurusMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select cell,match from pubchem.cell_cellosaurus_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/cellosaurus:CVCL_"))
                return;

            Integer cellID = getCellID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/cellosaurus:CVCL_");

            Pair<Integer, String> pair = Pair.getPair(cellID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_cellosaurus_matches where cell=? and match=?", oldMatches);
            store("insert into pubchem.cell_cellosaurus_matches(cell,match) values(?,?)", newMatches);
        });
    }


    private static void loadChemblCardCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select cell,match from pubchem.cell_chembl_card_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL"))
                return;

            Integer cellID = getCellID(subject.getURI());
            Integer match = getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL");

            Pair<Integer, Integer> pair = Pair.getPair(cellID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_chembl_card_matches where cell=? and match=?", oldMatches);
            store("insert into pubchem.cell_chembl_card_matches(cell,match) values(?,?)", newMatches);
        });
    }


    private static void loadAnatomies(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepAnatomies = new IntPairSet();
        IntPairSet newAnatomies = new IntPairSet();
        IntPairSet oldAnatomies = new IntPairSet();

        load("select cell,anatomy from pubchem.cell_anatomies", oldAnatomies);

        dispatcher.on(obo + "RO_0001000", (subject, object) -> {
            Integer cellID = getCellID(subject.getURI());
            Integer anatomyID = Anatomy.getAnatomyID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(cellID, anatomyID);

            if(oldAnatomies.remove(pair))
                keepAnatomies.add(pair);
            else if(!keepAnatomies.contains(pair))
                newAnatomies.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.cell_anatomies where cell=? and anatomy=?", oldAnatomies);
            store("insert into pubchem.cell_anatomies(cell,anatomy) values(?,?)", newAnatomies);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load cells ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadOrganisms(dispatcher);
        loadAlternatives(dispatcher);
        loadOccurrences(dispatcher);
        loadReferences(dispatcher);
        loadCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadWikidataCloseMatches(dispatcher);
        loadCellosaurusMatches(dispatcher);
        loadChemblCardCloseMatches(dispatcher);
        loadAnatomies(dispatcher);

        dispatcher.load("pubchem/RDF/cell/pc_cell.ttl.gz");
        missingCells.settle();
        dispatcher.finish();

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
            missingCells.referenced(cellID);

        return cellID;
    }
}
