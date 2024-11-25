package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class Taxonomy extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepTaxonomies = new IntSet();
    private static final IntSet newTaxonomies = new IntSet();
    private static final IntSet oldTaxonomies = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.taxonomy_bases", oldTaxonomies);

        new QueryResultProcessor(patternQuery("?taxonomy rdf:type sio:SIO_010000"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getIntID("taxonomy", prefix);

                if(oldTaxonomies.remove(taxonomyID))
                    keepTaxonomies.add(taxonomyID);
                else
                    newTaxonomies.add(taxonomyID);
            }
        }.load(model);
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap keepLabels = new IntStringMap();
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = new IntStringMap();

        load("select id,label from pubchem.taxonomy_bases where label is not null", oldLabels);

        new QueryResultProcessor(patternQuery("?taxonomy skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"), true);
                String label = getString("label");

                if(label.equals(oldLabels.remove(taxonomyID)))
                {
                    keepLabels.put(taxonomyID, label);
                }
                else
                {
                    String keep = keepLabels.get(taxonomyID);

                    if(label.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newLabels.put(taxonomyID, label);

                    if(put != null && !label.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.taxonomy_bases set label=null where id=? and label=?", oldLabels);
        store("insert into pubchem.taxonomy_bases(id,label) values(?,?) "
                + "on conflict(id) do update set label=EXCLUDED.label", newLabels);
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

        load("select taxonomy,reference from pubchem.taxonomy_references", oldReferences);

        new QueryResultProcessor(patternQuery("?taxonomy cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(taxonomyID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_references where taxonomy=? and reference=?", oldReferences);
        store("insert into pubchem.taxonomy_references(taxonomy,reference) values(?,?)", newReferences);
    }


    private static void loadCloseMatches(Model model) throws IOException, SQLException
    {
        IntIntPairSet newMatches = new IntIntPairSet();
        IntIntPairSet oldMatches = new IntIntPairSet();

        load("select taxonomy,match_unit,match_id from pubchem.taxonomy_matches", oldMatches);

        new QueryResultProcessor(patternQuery(
                "?taxonomy skos:closeMatch ?match. " + "filter(!strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"
                        + "filter(!strstarts(str(?match), 'https://identifiers.org/mesh:'))"
                        + "filter(!strstarts(str(?match), 'https://www.catalogueoflife.org/data/taxon/'))"
                        + "filter(!strstarts(str(?match), 'https://identifiers.org/col:'))"))
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
                "?taxonomy skos:closeMatch ?match. filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
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

        new QueryResultProcessor(patternQuery("?taxonomy skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.catalogueoflife.org/data/taxon/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer taxonomyID = getTaxonomyID(getIRI("taxonomy"));
                String match = getStringID("match", "https://www.catalogueoflife.org/data/taxon/");

                Pair<Integer, String> pair = Pair.getPair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        store("delete from pubchem.taxonomy_catalogueoflife_matches where taxonomy=? and match=?", oldMatches);
        store("insert into pubchem.taxonomy_catalogueoflife_matches(taxonomy,match) values(?,?)", newMatches);
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

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish taxonomies ...");

        store("delete from pubchem.taxonomy_bases where id=?", oldTaxonomies);
        store("insert into pubchem.taxonomy_bases(id) values(?)", newTaxonomies);

        System.out.println();
    }


    static Integer getTaxonomyID(String value) throws IOException
    {
        return getTaxonomyID(value, false);
    }


    static Integer getTaxonomyID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer taxonomyID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newTaxonomies)
        {
            if(newTaxonomies.contains(taxonomyID))
            {
                if(forceKeep)
                {
                    newTaxonomies.remove(taxonomyID);
                    keepTaxonomies.add(taxonomyID);
                }
            }
            else if(!keepTaxonomies.contains(taxonomyID))
            {
                System.out.println("    add missing taxonomy TAXID" + taxonomyID);

                if(!oldTaxonomies.remove(taxonomyID) && !forceKeep)
                    newTaxonomies.add(taxonomyID);
                else
                    keepTaxonomies.add(taxonomyID);
            }
        }

        return taxonomyID;
    }
}
