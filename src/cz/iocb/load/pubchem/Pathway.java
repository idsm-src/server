package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.PreparedStatement;
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
        descriptions.add(new Description("BIOCYC", "https://biocyc.org/", "[^/]*/NEW-IMAGE\\?object=.*"));
        descriptions.add(new Description("REACTOME", "http://identifiers.org/reactome/", "R-[A-Z]{3}-[1-9][0-9]*"));
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
    }


    private static void loadBases(Model model) throws IOException, SQLException
    {
        IntHashSet newPathways = new IntHashSet(200000);
        IntHashSet oldPathways = getIntSet("select id from pubchem.pathway_bases", 200000);

        new QueryResultProcessor(patternQuery("?pathway rdf:type bp:Pathway"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                if(!oldPathways.remove(pathwayID))
                    newPathways.add(pathwayID);
            }
        }.load(model);

        batch("delete from pubchem.pathway_bases where id = ?", oldPathways);


        IntStringMap newTitles = new IntStringMap(200000);
        IntStringMap oldTitles = getIntStringMap("select id, title from pubchem.pathway_bases", 200000);

        new QueryResultProcessor(patternQuery("?pathway dcterms:title ?title"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                String title = getString("title");

                if(!title.equals(oldTitles.remove(pathwayID)))
                    newTitles.put(pathwayID, title);
            }
        }.load(model);

        oldPathways.forEach(key -> oldTitles.remove(key));

        if(!oldTitles.isEmpty())
            throw new IOException();


        IntIntHashMap newSources = new IntIntHashMap(200000);
        IntIntHashMap oldSources = getIntIntMap("select id, source from pubchem.pathway_bases", 200000);

        new QueryResultProcessor(patternQuery("?pathway dcterms:source ?source"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int sourceID = Source.getSourceID(getIRI("source"));

                if(oldSources.removeKeyIfAbsent(pathwayID, NO_VALUE) != sourceID)
                    newSources.put(pathwayID, sourceID);
            }
        }.load(model);

        oldPathways.forEach(key -> oldSources.remove(key));

        if(!oldSources.isEmpty())
            throw new IOException();


        IntStringPairMap newReferences = new IntStringPairMap(200000);
        IntStringPairMap oldReferences = getIntStringPairMap(
                "select id, reference_type::varchar, reference from pubchem.pathway_bases", 200000);

        new QueryResultProcessor(patternQuery("?pathway skos:exactMatch ?match"))
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

                if(!pair.equals(oldReferences.remove(pathwayID)))
                    newReferences.put(pathwayID, pair);
            }
        }.load(model);

        oldPathways.forEach(key -> oldReferences.remove(key));

        if(!oldReferences.isEmpty())
            throw new IOException();


        batch("insert into pubchem.pathway_bases(id, source, title, reference_type, reference) values (?,?,?,?::pubchem.pathway_reference_type,?)",
                newPathways, (PreparedStatement statement, int pathway) -> {
                    Pair<String, String> reference = newReferences.remove(pathway);

                    statement.setInt(1, pathway);
                    statement.setInt(2, newSources.getOrThrow(pathway));
                    statement.setString(3, newTitles.remove(pathway));
                    statement.setString(4, reference.getOne());
                    statement.setString(5, reference.getTwo());
                    newSources.remove(pathway);
                });

        batch("update pubchem.pathway_bases set source = ? where id = ?", newSources, Direction.REVERSE);
        batch("update pubchem.pathway_bases set title = ? where id = ?", newTitles, Direction.REVERSE);
        batch("update pubchem.pathway_bases set reference_type = ?::pubchem.pathway_reference_type, reference = ? where id = ?",
                newReferences, Direction.REVERSE);
    }


    private static void loadOrganisms(Model model) throws IOException, SQLException
    {
        IntIntHashMap newOrganisms = new IntIntHashMap(200000);
        IntIntHashMap oldOrganisms = getIntIntMap(
                "select id, organism_id from pubchem.pathway_bases where organism_id is not null", 1000000);

        new QueryResultProcessor(patternQuery("?pathway bp:organism ?organism"))
        {
            @Override
            protected void parse() throws IOException
            {
                if(getIRI("organism").equals("http://identifiers.org/taxonomy/"))
                    return;

                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int organismID = getIntID("organism", "http://identifiers.org/taxonomy/");

                if(organismID != oldOrganisms.removeKeyIfAbsent(pathwayID, NO_VALUE))
                    newOrganisms.put(pathwayID, organismID);
            }
        }.load(model);

        batch("update pubchem.pathway_bases set organism_id = null where id = ?", oldOrganisms.keySet());
        batch("update pubchem.pathway_bases set organism_id = ? where id = ?", newOrganisms, Direction.REVERSE);
    }


    private static void loadCompounds(Model model) throws IOException, SQLException
    {
        IntPairSet newCompounds = new IntPairSet(2000000);
        IntPairSet oldCompounds = getIntPairSet("select pathway, compound from pubchem.pathway_compounds", 2000000);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?compound "
                + "filter(strstarts(str(?compound), 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int compoundID = getIntID("compound", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                Compound.addCompoundID(compoundID);

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, compoundID);

                if(!oldCompounds.remove(pair))
                    newCompounds.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_compounds where pathway = ? and compound = ?", oldCompounds);
        batch("insert into pubchem.pathway_compounds(pathway, compound) values (?,?)", newCompounds);
    }


    private static void loadProteins(Model model) throws IOException, SQLException
    {
        IntPairSet newProteins = new IntPairSet(1000000);
        IntPairSet oldProteins = getIntPairSet("select pathway, protein from pubchem.pathway_proteins", 1000000);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?protein "
                + "filter(strstarts(str(?protein), 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                String proteinName = getIRI("protein")
                        .replaceFirst("^http://rdf\\.ncbi\\.nlm\\.nih\\.gov/pubchem/protein/", "");
                int proteinID = Protein.getProteinID(proteinName);

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, proteinID);

                if(!oldProteins.remove(pair))
                    newProteins.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_proteins where pathway = ? and protein = ?", oldProteins);
        batch("insert into pubchem.pathway_proteins(pathway, protein) values (?,?)", newProteins);
    }


    private static void loadGenes(Model model) throws IOException, SQLException
    {
        IntPairSet newGenes = new IntPairSet(1000000);
        IntPairSet oldGenes = getIntPairSet("select pathway, gene from pubchem.pathway_genes", 1000000);

        new QueryResultProcessor(patternQuery("?pathway obo:RO_0000057 ?gene "
                + "filter(strstarts(str(?gene), 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/'))"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int geneID = getIntID("gene", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, geneID);

                if(!oldGenes.remove(pair))
                    newGenes.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_genes where pathway = ? and gene = ?", oldGenes);
        batch("insert into pubchem.pathway_genes(pathway, gene) values (?,?)", newGenes);
    }


    private static void loadComponents(Model model) throws IOException, SQLException
    {
        IntPairSet newComponents = new IntPairSet(1000000);
        IntPairSet oldComponents = getIntPairSet("select pathway, component from pubchem.pathway_components", 1000000);

        new QueryResultProcessor(patternQuery("?pathway bp:pathwayComponent ?component"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int componentID = getIntID("component", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, componentID);

                if(!oldComponents.remove(pair))
                    newComponents.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_components where pathway = ? and component = ?", oldComponents);
        batch("insert into pubchem.pathway_components(pathway, component) values (?,?)", newComponents);
    }


    private static void loadReferences(Model model) throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(1000000);
        IntPairSet oldReferences = getIntPairSet("select pathway, reference from pubchem.pathway_references", 1000000);

        new QueryResultProcessor(patternQuery("?pathway cito:isDiscussedBy ?reference"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int referenceID = getIntID("reference", "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, referenceID);

                if(!oldReferences.remove(pair))
                    newReferences.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.pathway_references where pathway = ? and reference = ?", oldReferences);
        batch("insert into pubchem.pathway_references(pathway, reference) values (?,?)", newReferences);
    }


    private static void loadRelatedPathways(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet(2000);
        IntPairSet oldRelations = getIntPairSet("select pathway, related from pubchem.pathway_related_pathways", 2000);

        new QueryResultProcessor(patternQuery("?pathway skos:related ?related"))
        {
            @Override
            protected void parse() throws IOException
            {
                int pathwayID = getIntID("pathway", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");
                int relatedID = getIntID("related", "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID");

                IntIntPair pair = PrimitiveTuples.pair(pathwayID, relatedID);

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
}
