package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.startingWith;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.sio;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Taxonomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> taxonomies = new EntityTable<>("pubchem.taxonomies", intKey("id"), null,
            varchar("label"));
    private static final MissingEntities<Integer> missingTaxonomies = new MissingEntities<>("taxonomy", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), rdf + "type", dcterms + "identifier", skos + "prefLabel", skos + "altLabel",
                rdfs + "seeAlso", cito + "isDiscussedBy");
        dispatcher.checkTypes(all(), vocab + "Taxonomy", sio + "SIO_010000");
        dispatcher.checkIdentifier(dcterms + "identifier", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID");
        dispatcher.checkPrefixes(all(), rdfs + "seeAlso", "http://purl.uniprot.org/taxonomy/",
                "http://id.nlm.nih.gov/mesh/", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C",
                "https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=",
                "http://identifiers.org/mesh:", "http://identifiers.org/ncit:C", "http://identifiers.org/col:",
                "http://identifiers.org/taxonomy:", "http://rdf.glycoinfo.org/source/",
                "https://glycosmos.org/organisms/show/", "http://www.wikidata.org/entity/Q", "https://eol.org/pages/",
                "https://glyconnect.expasy.org/all/taxonomies/", "https://powo.science.kew.org/taxon/",
                "https://www.ipni.org/n/", "https://www.worldfloraonline.org/taxon/");
        dispatcher.checkPaired(rdfs + "seeAlso", "http://identifiers.org/mesh:", "http://id.nlm.nih.gov/mesh/");
        dispatcher.checkLink(all(), rdfs + "seeAlso", startingWith("http://identifiers.org/taxonomy:"),
                "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID", "http://identifiers.org/taxonomy:");
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(sio + "SIO_010000", (subject, object) -> {
            Integer taxonomyID = getIntID(subject, prefix);

            taxonomies.reference(taxonomyID);
            missingTaxonomies.described(taxonomyID);
        });
    }


    private static void loadLabels(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(skos + "prefLabel", (subject, object) -> {
            taxonomies.set(getTaxonomyID(subject.getURI()), "label", getString(object));
        });
    }


    private static void loadAlternatives(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepAlternatives = new IntStringSet();
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select taxonomy,alternative from pubchem.taxonomy_alternatives", oldAlternatives);

        dispatcher.on(skos + "altLabel", (subject, object) -> {
            Integer taxonomyID = getTaxonomyID(subject.getURI());
            String alternative = getString(object);

            Pair<Integer, String> pair = Pair.getPair(taxonomyID, alternative);

            if(oldAlternatives.remove(pair))
                keepAlternatives.add(pair);
            else if(!keepAlternatives.contains(pair))
                newAlternatives.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_alternatives where taxonomy=? and alternative=?", oldAlternatives);
            store("insert into pubchem.taxonomy_alternatives(taxonomy,alternative) values(?,?)", newAlternatives);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        IntPairSet keepPatents = new IntPairSet();
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select taxonomy,reference from pubchem.taxonomy_references", oldReferences);
        load("select taxonomy,patent from pubchem.taxonomy_patents", oldPatents);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer taxonomyID = getTaxonomyID(subject.getURI());

            if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
            {
                Integer referenceID = Reference.getReferenceID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, referenceID);

                if(oldReferences.remove(pair))
                    keepReferences.add(pair);
                else if(!keepReferences.contains(pair))
                    newReferences.add(pair);
            }
            else
            {
                Integer patentID = Patent.getPatentID(object.getURI());

                Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, patentID);

                if(oldPatents.remove(pair))
                    keepPatents.add(pair);
                else if(!keepPatents.contains(pair))
                    newPatents.add(pair);
            }
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_references where taxonomy=? and reference=?", oldReferences);
            store("insert into pubchem.taxonomy_references(taxonomy,reference) values(?,?)", newReferences);

            store("delete from pubchem.taxonomy_patents where taxonomy=? and patent=?", oldPatents);
            store("insert into pubchem.taxonomy_patents(taxonomy,patent) values(?,?)", newPatents);
        });
    }


    private static void loadCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntIntPairSet keepMatches = new IntIntPairSet();
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select taxonomy,match_unit,match_id from pubchem.taxonomy_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, "http://id.nlm.nih.gov/mesh/", "http://identifiers.org/mesh:",
                    "http://identifiers.org/taxonomy:", "http://identifiers.org/col:",
                    "http://www.wikidata.org/entity/Q"))
                return;

            Integer taxonomyID = getTaxonomyID(subject.getURI());
            Pair<Integer, Integer> match = Ontology.getResourceId(object, null);

            Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(taxonomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_matches where taxonomy=? and match_unit=? and match_id=?", oldMatches);
            store("insert into pubchem.taxonomy_matches(taxonomy,match_unit,match_id) values(?,?,?)", newMatches);
        });
    }


    private static void loadMeshCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select taxonomy,match from pubchem.taxonomy_mesh_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://id.nlm.nih.gov/mesh/"))
                return;

            Integer taxonomyID = getTaxonomyID(subject.getURI());
            String match = getStringID(object, "http://id.nlm.nih.gov/mesh/");

            Pair<Integer, String> pair = Pair.getPair(taxonomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_mesh_matches where taxonomy=? and match=?", oldMatches);
            store("insert into pubchem.taxonomy_mesh_matches(taxonomy,match) values(?,?)", newMatches);
        });
    }


    private static void loadCatalogueoflifeCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select taxonomy,match from pubchem.taxonomy_catalogueoflife_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://identifiers.org/col:"))
                return;

            Integer taxonomyID = getTaxonomyID(subject.getURI());
            String match = getStringID(object, "http://identifiers.org/col:");

            Pair<Integer, String> pair = Pair.getPair(taxonomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_catalogueoflife_matches where taxonomy=? and match=?", oldMatches);
            store("insert into pubchem.taxonomy_catalogueoflife_matches(taxonomy,match) values(?,?)", newMatches);
        });
    }


    private static void loadWikidataCloseMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepMatches = new IntPairSet();
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select taxonomy,match from pubchem.taxonomy_wikidata_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, "http://www.wikidata.org/entity/Q"))
                return;

            Integer taxonomyID = getTaxonomyID(subject.getURI());
            Integer match = getIntID(object, "http://www.wikidata.org/entity/Q");

            Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.taxonomy_wikidata_matches where taxonomy=? and match=?", oldMatches);
            store("insert into pubchem.taxonomy_wikidata_matches(taxonomy,match) values(?,?)", newMatches);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load taxonomies ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadLabels(dispatcher);
        loadAlternatives(dispatcher);
        loadReferences(dispatcher);
        loadCloseMatches(dispatcher);
        loadMeshCloseMatches(dispatcher);
        loadCatalogueoflifeCloseMatches(dispatcher);
        loadWikidataCloseMatches(dispatcher);

        dispatcher.load("pubchem/RDF/taxonomy/pc_taxonomy.ttl.gz");
        missingTaxonomies.settle();
        dispatcher.finish();

        taxonomies.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish taxonomies ...");

        taxonomies.store();

        System.out.println();
    }


    static Integer getTaxonomyID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer taxonomyID = Integer.parseInt(value.substring(prefixLength));

        if(taxonomies.reference(taxonomyID))
            missingTaxonomies.referenced(taxonomyID);

        return taxonomyID;
    }
}
