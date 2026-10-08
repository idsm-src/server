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



public class Taxonomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> taxonomies = new EntityTable<>("pubchem.taxonomy_bases", intKey("id"),
            null, varchar("label"));


    private static void loadBases(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?taxonomy rdf:type sio:SIO_010000"))
        {
            @Override
            protected void parse() throws IOException
            {
                taxonomies.reference(getIntID("taxonomy", prefix));
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        new QueryResultProcessor(patternQuery("?taxonomy skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                taxonomies.set(getTaxonomyID(getIRI("taxonomy")), "label", getString("label"));
            }
        }.load(model);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringSet newAlternatives = new IntStringSet();
        IntStringSet oldAlternatives = new IntStringSet();

        load("select taxonomy,alternative from pubchem.taxonomy_alternatives", oldAlternatives);

        new QueryResultProcessor(patternQuery("?taxonomy skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                String alternative = getString("alternative");

                Pair<Integer, String> pair = Pair.getPair(taxonomyID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_alternatives where taxonomy=? and alternative=?", oldAlternatives);
        store("insert into pubchem.taxonomy_alternatives(taxonomy,alternative) values(?,?)", newAlternatives);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select taxonomy,reference from pubchem.taxonomy_references", oldReferences);
        load("select taxonomy,patent from pubchem.taxonomy_patents", oldPatents);

        new QueryResultProcessor(patternQuery("?taxonomy cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));

                if(getIRI("reference").startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                {
                    Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                    Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, referenceID);

                    if(!oldReferences.remove(pair))
                        newReferences.add(pair);
                }
                else
                {
                    Integer patentID = Patent.getPatentID(getIRI("reference"));

                    Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, patentID);

                    if(!oldPatents.remove(pair))
                        newPatents.add(pair);
                }

            }
        }.load(model);

        store("delete from pubchem.taxonomy_references where taxonomy=? and reference=?", oldReferences);
        store("insert into pubchem.taxonomy_references(taxonomy,reference) values(?,?)", newReferences);

        store("delete from pubchem.taxonomy_patents where taxonomy=? and patent=?", oldPatents);
        store("insert into pubchem.taxonomy_patents(taxonomy,patent) values(?,?)", newPatents);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select taxonomy,match_unit,match_id from pubchem.taxonomy_matches", oldMatches);

        new QueryResultProcessor(patternQuery("""
                ?taxonomy rdfs:seeAlso ?match. \
                filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/mesh:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/taxonomy:'))\
                filter(!strstarts(str(?match), 'http://identifiers.org/col:'))\
                filter(!strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))"""))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                Pair<Integer, Integer> match = Ontology.getId(getIRI("match"));

                Pair<Integer, Pair<Integer, Integer>> pair = Pair.getPair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_matches where taxonomy=? and match_unit=? and match_id=?", oldMatches);
        store("insert into pubchem.taxonomy_matches(taxonomy,match_unit,match_id) values(?,?,?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select taxonomy,match from pubchem.taxonomy_mesh_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?taxonomy rdfs:seeAlso ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                Pair<Integer, String> pair = Pair.getPair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_mesh_matches where taxonomy=? and match=?", oldMatches);
        store("insert into pubchem.taxonomy_mesh_matches(taxonomy,match) values(?,?)", newMatches);
    }


    private static void loadCatalogueoflifeCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select taxonomy,match from pubchem.taxonomy_catalogueoflife_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?taxonomy rdfs:seeAlso ?match. " + "filter(strstarts(str(?match), 'http://identifiers.org/col:'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                String match = getStringID("match", "http://identifiers.org/col:");

                Pair<Integer, String> pair = Pair.getPair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_catalogueoflife_matches where taxonomy=? and match=?", oldMatches);
        store("insert into pubchem.taxonomy_catalogueoflife_matches(taxonomy,match) values(?,?)", newMatches);
    }


    private static void loadWikidataCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = new IntPairSet();

        load("select taxonomy,match from pubchem.taxonomy_wikidata_matches", oldMatches);

        new QueryResultProcessor(patternQuery("?taxonomy rdfs:seeAlso ?match. "
                + "filter(strstarts(str(?match), 'http://www.wikidata.org/entity/Q'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                Integer match = getIntID("match", "http://www.wikidata.org/entity/Q");

                Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_wikidata_matches where taxonomy=? and match=?", oldMatches);
        store("insert into pubchem.taxonomy_wikidata_matches(taxonomy,match) values(?,?)", newMatches);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load taxonomies ...");

        Model model = getModel("pubchem/RDF/taxonomy/pc_taxonomy.ttl.gz");

        check(model, "pubchem/taxonomy/check.sparql");

        loadBases(model);
        loadLabels(model);
        loadAlternatives(model);
        loadReferences(model);
        loadCloseMatches(model);
        loadMeshCloseMatches(model);
        loadCatalogueoflifeCloseMatches(model);
        loadWikidataCloseMatches(model);

        model.close();

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
            throw new IOException("unexpected IRI: " + value);

        Integer taxonomyID = Integer.parseInt(value.substring(prefixLength));

        if(taxonomies.reference(taxonomyID))
            System.out.println("    add missing taxonomy TAXID" + taxonomyID);

        return taxonomyID;
    }
}
