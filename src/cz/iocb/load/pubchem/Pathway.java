package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
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
                "http://www.lipidmaps.org/data/IntegratedPathwaysData/SetupIntegratedPathways.pl?"
                        + "imgsize=730&Mode=BMDMATPS11&DataType=",
                ".*"));
        descriptions.add(new Description("PANTHERDB", "http://www.pantherdb.org/pathway/pathDetail.do?clsAccession=",
                "P[0-9]{5}"));
    }


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepPathways = new IntSet();
    private static final IntSet newPathways = new IntSet();
    private static final IntSet oldPathways = new IntSet();


    private static void loadBases(Model model) throws IOException, SQLException
    {
        load("select id from pubchem.pathway_bases", oldPathways);

        new QueryResultProcessor(patternQuery("?pathway rdf:type bp:Pathway"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getIntID("pathway", prefix);

                if(oldPathways.remove(pathwayID))
                    keepPathways.add(pathwayID);
                else
                    newPathways.add(pathwayID);
            }
        }.load(model);
    }


    private static void loadTitles(Model model) throws IOException, SQLException
    {
        IntStringMap keepTitles = new IntStringMap();
        IntStringMap newTitles = new IntStringMap();
        IntStringMap oldTitles = new IntStringMap();

        load("select id,title from pubchem.pathway_bases where title is not null", oldTitles);

        new QueryResultProcessor(patternQuery("?pathway dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"), true);
                String title = getString("title");

                if(title.equals(oldTitles.remove(pathwayID)))
                {
                    keepTitles.put(pathwayID, title);
                }
                else
                {
                    String keep = keepTitles.get(pathwayID);

                    if(title.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    String put = newTitles.put(pathwayID, title);

                    if(put != null && !title.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.pathway_bases set title=null where id=? and title=?", oldTitles);
        store("insert into pubchem.pathway_bases(id,title) values(?,?) "
                + "on conflict(id) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadSources(Model model) throws IOException, SQLException
    {
        IntIntMap keepSources = new IntIntMap();
        IntIntMap newSources = new IntIntMap();
        IntIntMap oldSources = new IntIntMap();

        load("select id,source from pubchem.pathway_bases where source is not null", oldSources);

        new QueryResultProcessor(patternQuery("?pathway dcterms:source ?source"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"), true);
                Integer sourceID = Source.getSourceID(getIRI("source"));

                if(sourceID.equals(oldSources.remove(pathwayID)))
                {
                    keepSources.put(pathwayID, sourceID);
                }
                else
                {
                    Integer keep = keepSources.get(pathwayID);

                    if(sourceID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newSources.put(pathwayID, sourceID);

                    if(put != null && !sourceID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.pathway_bases set source=null where id=? and source=?", oldSources);
        store("insert into pubchem.pathway_bases(id,source) values(?,?) "
                + "on conflict(id) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadSameAsReferences(Model model) throws IOException, SQLException
    {
        IntStringPairMap keepReferences = new IntStringPairMap();
        IntStringPairMap newReferences = new IntStringPairMap();
        IntStringPairMap oldReferences = new IntStringPairMap();

        load("select id,reference_type::varchar,reference from pubchem.pathway_bases where reference is not null",
                oldReferences);

        new QueryResultProcessor(patternQuery("?pathway owl:sameAs ?match"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"), true);
                String iri = getIRI("match");

                Description description = null;

                for(Description test : descriptions)
                    if(iri.matches(test.pattern))
                        description = test;

                if(description == null)
                    throw new IOException(iri);

                Pair<String, String> pair = Pair.getPair(description.name, getStringID("match", description.prefix));

                if(pair.equals(oldReferences.remove(pathwayID)))
                {
                    keepReferences.put(pathwayID, pair);
                }
                else
                {
                    Pair<String, String> keep = keepReferences.get(pathwayID);

                    if(pair.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Pair<String, String> put = newReferences.put(pathwayID, pair);

                    if(put != null && !pair.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.pathway_bases set reference_type=null,reference=null "
                + "where id=? and reference_type=? and reference=?", oldReferences);
        store("insert into pubchem.pathway_bases(id,reference_type,reference) "
                + "values(?,?::pubchem.pathway_reference_type,?) "
                + "on conflict(id) do update set reference_type=EXCLUDED.reference_type, reference=EXCLUDED.reference",
                newReferences);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntMap keepOrganisms = new IntIntMap();
        IntIntMap newOrganisms = new IntIntMap();
        IntIntMap oldOrganisms = new IntIntMap();

        load("select id,organism from pubchem.pathway_bases where organism is not null", oldOrganisms);

        new QueryResultProcessor(patternQuery("?pathway up:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                // workaround
                if(getIRI("organism").equals(Taxonomy.prefix))
                    return;

                Integer pathwayID = getPathwayID(getIRI("pathway"), true);
                Integer organismID = Taxonomy.getTaxonomyID(getIRI("organism"));

                if(organismID.equals(oldOrganisms.remove(pathwayID)))
                {
                    keepOrganisms.put(pathwayID, organismID);
                }
                else
                {
                    Integer keep = keepOrganisms.get(pathwayID);

                    if(organismID.equals(keep))
                        return;
                    else if(keep != null)
                        throw new IOException();

                    Integer put = newOrganisms.put(pathwayID, organismID);

                    if(put != null && !organismID.equals(put))
                        throw new IOException();
                }
            }
        }.load(model);

        store("update pubchem.pathway_bases set organism=null where id=? and organism=?", oldOrganisms);
        store("insert into pubchem.pathway_bases(id,organism) values(?,?) "
                + "on conflict(id) do update set organism=EXCLUDED.organism", newOrganisms);
    }


    private static void loadCompounds(Model model) throws IOException, SQLException
    {
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = new IntPairSet();

        load("select pathway,compound from pubchem.pathway_compounds", oldCompounds);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?compound "
                + "filter(strstarts(str(?compound), 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer compoundID = Compound.getCompoundID(getIRI("compound"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, compoundID);

                if(!oldCompounds.remove(pair))
                    newCompounds.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_compounds where pathway=? and compound=?", oldCompounds);
        store("insert into pubchem.pathway_compounds(pathway,compound) values(?,?)", newCompounds);
    }


    private static void loadProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newProteins = new IntPairSet();
        IntPairSet oldProteins = new IntPairSet();

        load("select pathway,protein from pubchem.pathway_proteins", oldProteins);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?protein "
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer proteinID = Protein.getProteinID(getIRI("protein"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, proteinID);

                if(!oldProteins.remove(pair))
                    newProteins.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_proteins where pathway=? and protein=?", oldProteins);
        store("insert into pubchem.pathway_proteins(pathway,protein) values(?,?)", newProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = new IntPairSet();

        load("select pathway,gene from pubchem.pathway_genes", oldGenes);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?gene "
                + "filter(strstarts(str(?gene), 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer geneID = Gene.getGeneID(getIRI("gene"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_genes where pathway=? and gene=?", oldGenes);
        store("insert into pubchem.pathway_genes(pathway,gene) values(?,?)", newGenes);
    }


    private static void loadComponents(Model model) throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet();
        IntPairSet oldComponents = new IntPairSet();

        load("select pathway,component from pubchem.pathway_components", oldComponents);

        new QueryResultProcessor(patternQuery("?pathway bp:pathwayComponent ?component"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer componentID = getPathwayID(getIRI("component"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, componentID);

                if(!oldComponents.remove(pair))
                    newComponents.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_components where pathway=? and component=?", oldComponents);
        store("insert into pubchem.pathway_components(pathway,component) values(?,?)", newComponents);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select pathway,reference from pubchem.pathway_references", oldReferences);

        new QueryResultProcessor(patternQuery("?pathway cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer referenceID = Reference.getReferenceID(getIRI("reference"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_references where pathway=? and reference=?", oldReferences);
        store("insert into pubchem.pathway_references(pathway,reference) values(?,?)", newReferences);
    }


    private static void loadRelatedPathways(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select pathway,related from pubchem.pathway_related_pathways", oldRelations);

        new QueryResultProcessor(patternQuery("?pathway skos:related ?related"))
        {
            @Override
            protected void parse() throws IOException
            {
                Integer pathwayID = getPathwayID(getIRI("pathway"));
                Integer relatedID = getPathwayID(getIRI("related"));

                Pair<Integer, Integer> pair = Pair.getPair(pathwayID, relatedID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        store("delete from pubchem.pathway_related_pathways where pathway=? and related=?", oldRelations);
        store("insert into pubchem.pathway_related_pathways(pathway,related) values(?,?)", newRelations);
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

        store("delete from pubchem.pathway_bases where id=?", oldPathways);
        store("insert into pubchem.pathway_bases(id) values(?)", newPathways);

        System.out.println();
    }


    static Integer getPathwayID(String value) throws IOException
    {
        return getPathwayID(value, false);
    }


    static Integer getPathwayID(String value, boolean forceKeep) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer pathwayID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newPathways)
        {
            if(newPathways.contains(pathwayID))
            {
                if(forceKeep)
                {
                    newPathways.remove(pathwayID);
                    keepPathways.add(pathwayID);
                }
            }
            else if(!keepPathways.contains(pathwayID))
            {
                System.out.println("    add missing patwway PWID" + pathwayID);

                if(!oldPathways.remove(pathwayID) && !forceKeep)
                    newPathways.add(pathwayID);
                else
                    keepPathways.add(pathwayID);
            }
        }

        return pathwayID;
    }
}
