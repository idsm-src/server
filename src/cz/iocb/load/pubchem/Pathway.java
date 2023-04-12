package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.Tuples;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



class Pathway extends Updater
{
    private static class Description
    {
        final String name;
        final String prefix;
        final String pattern;

        Description(String name, String prefix, String suffix)
        {
            this.name = name;
            this.prefix = prefix;
            this.pattern = prefix.replaceAll("([?.])", "\\\\$1") + suffix;
        }
    }


    private static ArrayList<Description> descriptions = new ArrayList<Description>();


    static
    {
        descriptions.add(new Description("PATHBANK", "http://pathbank.org/view/", "SMP[0-9]{5,7}"));
        descriptions.add(new Description("BIOCYC_IMAGE", "https://biocyc.org/", "[^/]*/NEW-IMAGE\\?object=.*"));
        descriptions.add(new Description("REACTOME", "http://identifiers.org/reactome/", "R-[A-Z]{3}-[1-9][0-9]*"));
        descriptions.add(new Description("BIOCYC", "http://identifiers.org/biocyc/", "[^:]*:[^:]*"));
        descriptions.add(new Description("WIKIPATHWAY", "http://identifiers.org/wikipathways/WP", "[1-9][0-9]*"));
        descriptions.add(new Description("PLANTCYC", "https://pmn.plantcyc.org/", "[^/]*/new-image\\?object=.*"));
        descriptions
                .add(new Description("PID", "http://pid.nci.nih.gov/search/pathway_landing.shtml?pathway_id=", ".*"));
        descriptions.add(new Description("INOH", "http://www.inoh.org/inohviewer/inohclient.jnlp?id=", ".*"));
        descriptions.add(
                new Description("PLANTREACTOME", "http://plantreactome.gramene.org/content/detail/", "R-OSA-[0-9]{7}"));
        descriptions.add(new Description("PHARMGKB", "https://www.pharmgkb.org/pathway/", "PA[1-9][0-9]*"));
        descriptions.add(new Description("FAIRDOMHUB", "https://fairdomhub.org/models/", "[0-9]+"));
        descriptions.add(new Description("LIPIDMAPS",
                "http://www.lipidmaps.org/data/IntegratedPathwaysData/SetupIntegratedPathways.pl?imgsize=730&Mode=BMDMATPS11&DataType=",
                ".*"));
        descriptions.add(new Description("PANTHERDB", "http://www.pantherdb.org/pathway/pathDetail.do?clsAccession=",
                "P[0-9]{5}"));
    }


    private static IntHashSet usedPathways;
    private static IntHashSet newPathways;
    private static IntHashSet oldPathways;


    private static void loadBases(Model model) throws IOException, SQLException
    {
        usedPathways = new IntHashSet();
        newPathways = new IntHashSet();
        oldPathways = getIntSet("select id from pubchem.pathway_bases");

        new QueryResultProcessor(patternQuery("?pathway rdf:type bp:Pathway"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                if(!oldPathways.remove(pathwayID))
                    newPathways.add(pathwayID);

                usedPathways.add(pathwayID);
            }
        }.load(model);

        batch("insert into pubchem.pathway_bases(id) values (?)", newPathways);
        newPathways.clear();
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.pathway_bases where title is not null");

        new QueryResultProcessor(patternQuery("?pathway dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                String title = getString("title");

                addPathwayID(pathwayID);

                if(!title.equals(oldTitles.remove(pathwayID)))
                    newTitles.put(pathwayID, title);
            }
        }.load(model);

        batch("update pubchem.pathway_bases set title = null where id = ?", oldTitles.keySet());
        batch("insert into pubchem.pathway_bases(id, title) values (?,?) "
                + "on conflict (id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadSources(Model model) throws IOException, SQLException
    {
        IntIntHashMap newSources = new IntIntHashMap();
        IntIntHashMap oldSources = getIntIntMap(
                "select id, source from pubchem.pathway_bases where source is not null");

        new QueryResultProcessor(patternQuery("?pathway dcterms:source ?source"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int sourceID = Source.getSourceID(getStringID("source", "http://rdf.ncbi.nlm.nih.gov/pubchem/source/"));

                addPathwayID(pathwayID);

                if(oldSources.removeKeyIfAbsent(pathwayID, NO_VALUE) != sourceID)
                    newSources.put(pathwayID, sourceID);
            }
        }.load(model);

        batch("update pubchem.pathway_bases set source = null where id = ?", oldSources.keySet());
        batch("insert into pubchem.pathway_bases(id, source) values (?,?) "
                + "on conflict (id) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadSameAsReferences(Model model) throws IOException, SQLException
    {
        IntStringPairMap newReferences = new IntStringPairMap();
        IntStringPairMap oldReferences = getIntStringPairMap(
                "select id, reference_type::varchar, reference from pubchem.pathway_bases where reference is not null");

        new QueryResultProcessor(patternQuery("?pathway owl:sameAs ?match"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                String iri = getIRI("match");

                Description description = null;

                for(Description test : descriptions)
                {
                    if(iri.matches(test.pattern))
                        description = test;
                }

                if(description == null)
                    throw new IOException(iri);

                Pair<String, String> pair = Tuples.pair(description.name, getStringID("match", description.prefix));
                addPathwayID(pathwayID);

                if(!pair.equals(oldReferences.remove(pathwayID)))
                    newReferences.put(pathwayID, pair);
            }
        }.load(model);

        batch("update pubchem.pathway_bases set reference_type = null, reference = null where id = ?",
                oldReferences.keySet());
        batch("insert into pubchem.pathway_bases(id, reference_type, reference) values (?,?::pubchem.pathway_reference_type,?) "
                + "on conflict (id) do update set reference_type=EXCLUDED.reference_type, reference=EXCLUDED.reference",
                newReferences);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap();
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism from pubchem.pathway_bases where organism is not null");

        new QueryResultProcessor(patternQuery("?pathway up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                if(getIRI("organism").equals("http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"))
                    return;

                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int organismID = Taxonomy
                        .getTaxonomyID(getIntID("organism", "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"));

                addPathwayID(pathwayID);

                if(organismID != oldOrganisms.removeKeyIfAbsent(pathwayID, NO_VALUE))
                    newOrganisms.put(pathwayID, organismID);
            }
        }.load(model);

        batch("update pubchem.pathway_bases set organism = null where id = ?", oldOrganisms.keySet());
        batch("insert into pubchem.pathway_bases(id, organism) values (?,?) "
                + "on conflict (id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadCompounds(Model model) throws IOException, SQLException
    {
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = getIntPairSet("select pathway, compound from pubchem.pathway_compounds");

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?compound "
                + "filter(strstarts(str(?compound), 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int compoundID = getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, compoundID);
                Compound.addCompoundID(compoundID);
                addPathwayID(pathwayID);

                if(!oldCompounds.remove(pair))
                    newCompounds.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_compounds where pathway = ? and compound = ?", oldCompounds);
        batch("insert into pubchem.pathway_compounds(pathway, compound) values (?,?)", newCompounds);
    }


    private static void loadProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newProteins = new IntPairSet();
        IntPairSet oldProteins = getIntPairSet("select pathway, protein from pubchem.pathway_proteins");

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?protein "
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                String proteinName = getStringID("protein", "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                int proteinID = Protein.getProteinID(proteinName);

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, proteinID);
                addPathwayID(pathwayID);

                if(!oldProteins.remove(pair))
                    newProteins.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_proteins where pathway = ? and protein = ?", oldProteins);
        batch("insert into pubchem.pathway_proteins(pathway, protein) values (?,?)", newProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = getIntPairSet("select pathway, gene from pubchem.pathway_genes");

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?gene "
                + "filter(strstarts(str(?gene), 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, geneID);
                addPathwayID(pathwayID);
                Gene.addGeneID(geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_genes where pathway = ? and gene = ?", oldGenes);
        batch("insert into pubchem.pathway_genes(pathway, gene) values (?,?)", newGenes);
    }


    private static void loadComponents(Model model) throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet();
        IntPairSet oldComponents = getIntPairSet("select pathway, component from pubchem.pathway_components");

        new QueryResultProcessor(patternQuery("?pathway bp:pathwayComponent ?component"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int componentID = getIntID("component", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, componentID);
                addPathwayID(pathwayID);
                addPathwayID(componentID);

                if(!oldComponents.remove(pair))
                    newComponents.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_components where pathway = ? and component = ?", oldComponents);
        batch("insert into pubchem.pathway_components(pathway, component) values (?,?)", newComponents);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = getIntPairSet("select pathway, reference from pubchem.pathway_references");

        new QueryResultProcessor(patternQuery("?pathway cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int referenceID = Reference
                        .getReferenceID(getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"));

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, referenceID);
                addPathwayID(pathwayID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_references where pathway = ? and reference = ?", oldReferences);
        batch("insert into pubchem.pathway_references(pathway, reference) values (?,?)", newReferences);
    }


    private static void loadRelatedPathways(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = getIntPairSet("select pathway, related from pubchem.pathway_related_pathways");

        new QueryResultProcessor(patternQuery("?pathway skos:related ?related"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int relatedID = getIntID("related", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, relatedID);
                addPathwayID(pathwayID);
                addPathwayID(relatedID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_related_pathways where pathway = ? and related = ?", oldRelations);
        batch("insert into pubchem.pathway_related_pathways(pathway, related) values (?,?)", newRelations);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load pathways ...");

        Model model = getModel("pubchem/RDF/pathway/pc_pathway.ttl.gz");
        check(model, "pubchem/pathway/check.sparql");

        loadBases(model);
        loadTitles(model);
        loadSources(model);
        loadSameAsReferences(model);
        loadOrganisms(model);
        loadCompounds(model);
        loadProteins(model);
        loadGenes(model);
        loadComponents(model);
        loadReferences(model);
        loadRelatedPathways(model);

        model.close();
        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish pathways ...");

        batch("delete from pubchem.pathway_bases where id = ?", oldPathways);
        batch("insert into pubchem.pathway_bases(id) values (?)" + " on conflict do nothing", newPathways);

        usedPathways = null;
        newPathways = null;
        oldPathways = null;

        System.out.println();
    }


    static void addPathwayID(int patwwayID)
    {
        synchronized(newPathways)
        {
            if(usedPathways.add(patwwayID))
            {
                System.out.println("    add missing patwway PWID" + patwwayID);

                if(!oldPathways.remove(patwwayID))
                    newPathways.add(patwwayID);
            }
        }
    }
}
