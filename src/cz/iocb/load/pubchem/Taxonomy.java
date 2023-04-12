package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



public class Taxonomy extends Updater
{
    private static IntHashSet usedTaxonomies;
    private static IntHashSet newTaxonomies;
    private static IntHashSet oldTaxonomies;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedTaxonomies = new IntHashSet();
        newTaxonomies = new IntHashSet();
        oldTaxonomies = getIntSet("select id from pubchem.taxonomy_bases");

        new QueryResultProcessor(patternQuery("?taxonomy rdf:type sio:SIO_010000"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID");

                if(!oldTaxonomies.remove(taxonomyID))
                    newTaxonomies.add(taxonomyID);

                usedTaxonomies.add(taxonomyID);
            }
        }.load(model);

        batch("insert into pubchem.taxonomy_bases(id) values (?)", newTaxonomies);
        newTaxonomies.clear();
    }


    private static void loadLabels(Model model) throws IOException, SQLException
    {
        IntStringMap newLabels = new IntStringMap();
        IntStringMap oldLabels = getIntStringMap(
                "select id, label from pubchem.taxonomy_bases where label is not null");

        new QueryResultProcessor(patternQuery("?taxonomy skos:prefLabel ?label"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                String label = getString("label");

                if(!label.equals(oldLabels.remove(taxonomyID)))
                    newLabels.put(taxonomyID, label);
            }
        }.load(model);

        batch("update pubchem.taxonomy_bases set label = null where id = ?", oldLabels.keySet());
        batch("insert into pubchem.taxonomy_bases(id, label) values (?,?) "
                + "on conflict (id) do update set label=EXCLUDED.label", newLabels);
    }


    private static void loadAlternatives(Model model) throws IOException, SQLException
    {
        IntStringPairSet newAlternatives = new IntStringPairSet();
        IntStringPairSet oldAlternatives = getIntStringPairSet(
                "select taxonomy, alternative from pubchem.taxonomy_alternatives");

        new QueryResultProcessor(patternQuery("?taxonomy skos:altLabel ?alternative"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                String alternative = getString("alternative");

                IntObjectPair<String> pair = PrimitiveTuples.pair(taxonomyID, alternative);

                if(!oldAlternatives.remove(pair))
                    newAlternatives.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_alternatives where taxonomy = ? and alternative = ?", oldAlternatives);
        batch("insert into pubchem.taxonomy_alternatives(taxonomy, alternative) values (?,?)", newAlternatives);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = getIntPairSet("select taxonomy, reference from pubchem.taxonomy_references");

        new QueryResultProcessor(patternQuery("?taxonomy cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                int referenceID = Reference
                        .getReferenceID(getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));

                IntIntPair pair = PrimitiveTuples.pair(taxonomyID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_references where taxonomy = ? and reference = ?", oldReferences);
        batch("insert into pubchem.taxonomy_references(taxonomy, reference) values (?,?)", newReferences);
    }


    private static void loadUniprotCloseMatches(Model model) throws IOException, SQLException
    {
        IntHashSet newMatches = new IntHashSet();
        IntHashSet oldMatches = getIntSet("select taxonomy from pubchem.taxonomy_uniprot_matches");

        new QueryResultProcessor(patternQuery("?taxonomy skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://purl.uniprot.org/taxonomy/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                int match = getIntID("match", "http://purl.uniprot.org/taxonomy/");

                if(taxonomyID != match)
                    throw new IOException();

                if(!oldMatches.remove(taxonomyID))
                    newMatches.add(taxonomyID);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_uniprot_matches where taxonomy = ?", oldMatches);
        batch("insert into pubchem.taxonomy_uniprot_matches(taxonomy) values (?)", newMatches);
    }


    private static void loadMeshCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet("select taxonomy, match from pubchem.taxonomy_mesh_matches");

        new QueryResultProcessor(patternQuery(
                "?taxonomy skos:closeMatch ?match. " + "filter(strstarts(str(?match), 'http://id.nlm.nih.gov/mesh/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                String match = getStringID("match", "http://id.nlm.nih.gov/mesh/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_mesh_matches where taxonomy = ? and match = ?", oldMatches);
        batch("insert into pubchem.taxonomy_mesh_matches(taxonomy, match) values (?,?)", newMatches);
    }


    private static void loadCatalogueoflifeCloseMatches(Model model) throws IOException, SQLException
    {
        IntStringPairSet newMatches = new IntStringPairSet();
        IntStringPairSet oldMatches = getIntStringPairSet(
                "select taxonomy, match from pubchem.taxonomy_catalogueoflife_matches");

        new QueryResultProcessor(patternQuery("?taxonomy skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.catalogueoflife.org/data/taxon/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                String match = getStringID("match", "https://www.catalogueoflife.org/data/taxon/");

                IntObjectPair<String> pair = PrimitiveTuples.pair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_catalogueoflife_matches where taxonomy = ? and match = ?", oldMatches);
        batch("insert into pubchem.taxonomy_catalogueoflife_matches(taxonomy, match) values (?,?)", newMatches);
    }


    private static void loadThesaurusCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select taxonomy, match from pubchem.taxonomy_thesaurus_matches");

        new QueryResultProcessor(patternQuery("?taxonomy skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                Identifier match = Ontology.getId(getIRI("match"));

                if(match.unit != Ontology.unitThesaurus)
                    throw new IOException();

                IntIntPair pair = PrimitiveTuples.pair(taxonomyID, match.id);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_thesaurus_matches where taxonomy = ? and match = ?", oldMatches);
        batch("insert into pubchem.taxonomy_thesaurus_matches(taxonomy, match) values (?,?)", newMatches);
    }


    private static void loadItisCloseMatches(Model model) throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet();
        IntPairSet oldMatches = getIntPairSet("select taxonomy, match from pubchem.taxonomy_itis_matches");

        new QueryResultProcessor(patternQuery("?taxonomy skos:closeMatch ?match. "
                + "filter(strstarts(str(?match), 'https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value='))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int taxonomyID = getTaxonomyID(
                        getIntID("taxonomy", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));
                int match = getIntID("match",
                        "https://www.itis.gov/servlet/SingleRpt/SingleRpt?search_topic=TSN&search_value=");

                IntIntPair pair = PrimitiveTuples.pair(taxonomyID, match);

                if(!oldMatches.remove(pair))
                    newMatches.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.taxonomy_itis_matches where taxonomy = ? and match = ?", oldMatches);
        batch("insert into pubchem.taxonomy_itis_matches(taxonomy, match) values (?,?)", newMatches);
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
        loadUniprotCloseMatches(model);
        loadMeshCloseMatches(model);
        loadCatalogueoflifeCloseMatches(model);
        loadThesaurusCloseMatches(model);
        loadItisCloseMatches(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish taxonomies ...");

        batch("delete from pubchem.taxonomy_bases where id = ?", oldTaxonomies);
        batch("insert into pubchem.taxonomy_bases(id) values (?)" + " on conflict do nothing", newTaxonomies);

        usedTaxonomies = null;
        newTaxonomies = null;
        oldTaxonomies = null;

        System.out.println();
    }


    static int getTaxonomyID(int taxonomyID) throws IOException
    {
        synchronized(newTaxonomies)
        {
            if(!usedTaxonomies.contains(taxonomyID))
            {
                System.out.println("    add missing taxonomy TAXID" + taxonomyID);

                if(!oldTaxonomies.remove(taxonomyID))
                    newTaxonomies.add(taxonomyID);

                usedTaxonomies.add(taxonomyID);
            }
        }

        return taxonomyID;
    }
}
