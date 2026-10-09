package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Anatomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/anatomy/ANATOMYID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> anatomies = new EntityTable<>("pubchem.anatomies", intKey("id"), null,
            varchar("label"));
    private static final MissingEntities<Integer> missingAnatomies = new MissingEntities<>("anatomy", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", skos + "prefLabel", skos + "altLabel", rdfs + "seeAlso",
                cito + "isDiscussedBy");
        dispatcher.checkTypes(all(), vocab + "Anatomy", sio + "SIO_001262");
        dispatcher.checkPrefixes(all(), rdfs + "seeAlso", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "http://purl.obolibrary.org/obo/BTO_", "http://www.ebi.ac.uk/efo/EFO_",
                "https://www.ebi.ac.uk/chembl/tissue_report_card/CHEMBL", "https://www.nextprot.org/term/TS-",
                "http://identifiers.org/efo:", "http://identifiers.org/BTO:", "http://identifiers.org/ncit:C",
                "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:", "http://purl.obolibrary.org/obo/UBERON_",
                "http://identifiers.org/UBERON:");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(sio + "SIO_001262", (subject, object) -> {
            Integer anatomyID = getIntID(subject, prefix);

            anatomies.reference(anatomyID);
            missingAnatomies.described(anatomyID);
        });
    }


    private static void loadLabels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            anatomies.set(getAnatomyID(subject.getURI()), "label", getString(object));
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select anatomy,alternative from pubchem.anatomy_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer anatomyID = getAnatomyID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(anatomyID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.anatomy_alternatives where anatomy=? and alternative=?", oldAlternatives);
            store("insert into pubchem.anatomy_alternatives(anatomy,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select anatomy,match_unit,match_id from pubchem.anatomy_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:"))
                return;

            Integer anatomyID = getAnatomyID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getId(object.getURI());

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(anatomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.anatomy_matches where anatomy=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.anatomy_matches(anatomy,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select anatomy,match from pubchem.anatomy_mesh_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://id.nlm.nih.gov/mesh/"))
                return;

            Integer anatomyID = getAnatomyID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(anatomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.anatomy_mesh_matches where anatomy=? and match=?", oldMatches);
            store("insert into pubchem.anatomy_mesh_matches(anatomy,match) values(?,?)", newMatches);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepPatents = new IntPairSet();
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select anatomy,patent from pubchem.anatomy_patents", oldPatents);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer anatomyID = getAnatomyID(subject.getURI());
            Integer patentID = Patent.getPatentID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(anatomyID, patentID);

            if(oldPatents.remove(pair))
                keepPatents.add(pair);
            else if(!keepPatents.contains(pair))
                newPatents.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.anatomy_patents where anatomy=? and patent=?", oldPatents);
            store("insert into pubchem.anatomy_patents(anatomy,patent) values(?,?)", newPatents);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load anatomys ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadAlternatives(dispatcher);
        loadCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadReferences(dispatcher);

        dispatcher.load("pubchem/RDF/anatomy/pc_anatomy.ttl.gz");
        missingAnatomies.settle();
        dispatcher.finish();

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
            missingAnatomies.referenced(anatomyID);

        return anatomyID;
    }
}
