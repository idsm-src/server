package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.typed;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.pubchem.PubChemRDF.bp;
import static cz.iocb.load.pubchem.PubChemRDF.cito;
import static cz.iocb.load.pubchem.PubChemRDF.dcterms;
import static cz.iocb.load.pubchem.PubChemRDF.obo;
import static cz.iocb.load.pubchem.PubChemRDF.rdf;
import static cz.iocb.load.pubchem.PubChemRDF.rdfs;
import static cz.iocb.load.pubchem.PubChemRDF.skos;
import static cz.iocb.load.pubchem.PubChemRDF.up;
import static cz.iocb.load.pubchem.PubChemRDF.vocab;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleDispatcher;
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


    private static ArrayList<Description> descriptions = new ArrayList<>();


    static
    {
        descriptions.add(new Description("PATHBANK", "http://pathbank.org/view/", "SMP[0-9]{5,7}"));
        descriptions.add(new Description("REACTOME", "http://identifiers.org/reactome:", "R-[A-Z]{3}-[1-9][0-9]*"));
        descriptions.add(new Description("BIOCYC", "http://identifiers.org/biocyc:", ".*CYC:.*"));
        descriptions.add(new Description("WIKIPATHWAY", "http://identifiers.org/wikipathways:", "WP[1-9][0-9]*"));
        descriptions.add(
                new Description("PLANTCYC", "https://pmn.plantcyc.org/pathway?", "orgid=[A-Z0-9_]+&id=[-A-Z0-9]+"));
        descriptions.add(new Description("PLANTREACTOME", "https://plantreactome.gramene.org/content/detail/",
                "R-OSA-[0-9]{7}"));
        descriptions.add(new Description("PHARMGKB", "http://identifiers.org/pharmgkb.pathways:", "PA[1-9][0-9]*"));
        descriptions.add(new Description("FAIRDOMHUB", "https://fairdomhub.org/models/", "[0-9]+"));
        descriptions.add(new Description("LIPIDMAPS",
                "https://www.lipidmaps.org/data/IntegratedPathwaysData/SetupIntegratedPathways.pl?"
                        + "imgsize=730&Mode=BMDMATPS11&DataType=",
                ".*"));
        descriptions.add(new Description("PANTHERDB", "http://identifiers.org/panther.pathway:", "P[0-9]{5}"));
        descriptions.add(new Description("PIDPATHWAY", "http://identifiers.org/pid.pathway:", ".*"));
    }


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> pathways = new EntityTable<>("pubchem.pathway_bases", intKey("id"), null,
            integer("source"), uniqueVarchar("title"), typed("reference_type", "pubchem.pathway_reference_type"),
            varchar("reference"), integer("organism"));
    private static final MissingEntities<Integer> missingPathways = new MissingEntities<>("pathway", true);


    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "title", dcterms + "source", up + "organism",
                bp + "pathwayComponent", cito + "isDiscussedBy", rdfs + "seeAlso", skos + "related", obo + "RO_0000057",
                rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Pathway", bp + "Pathway");
        dispatcher.checkPrefixes(all(), obo + "RO_0000057", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/");
        // the values of rdfs:seeAlso are not checked
    }


    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(vocab + "Pathway", (subject, object) -> {
            Integer pathwayID = getIntID(subject, prefix);

            pathways.reference(pathwayID);
            missingPathways.described(pathwayID);
        });
    }


    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            String title = getString(object);

            pathways.set(pathwayID, "title", title);
        });
    }


    private static void loadSources(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "source", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            Integer sourceID = Source.getSourceID(object.getURI());

            pathways.set(pathwayID, "source", sourceID);
        });
    }


    private static void loadSameAsReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            String iri = object.getURI();

            // workaround
            if(iri.startsWith("https://glycosmos.org/pathways/"))
                iri = iri.replaceFirst("^https://glycosmos\\.org/pathways/", "http://identifiers.org/reactome:");

            Description description = null;

            for(Description test : descriptions)
                if(iri.matches(test.pattern))
                    description = test;

            if(description == null)
                throw new IOException(iri);

            pathways.set(pathwayID, "reference_type", description.name);
            pathways.set(pathwayID, "reference", iri.substring(description.prefix.length()));
        });
    }


    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            // workaround
            if(object.getURI().equals(Taxonomy.prefix))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            pathways.set(pathwayID, "organism", organismID);
        });
    }


    private static void loadCompounds(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepCompounds = new IntPairSet();
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = new IntPairSet();

        load("select pathway,compound from pubchem.pathway_compounds", oldCompounds);

        dispatcher.on(obo + "RO_0000057", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            Integer compoundID = Compound.getCompoundID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, compoundID);

            if(oldCompounds.remove(pair))
                keepCompounds.add(pair);
            else if(!keepCompounds.contains(pair))
                newCompounds.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_compounds where pathway=? and compound=?", oldCompounds);
            store("insert into pubchem.pathway_compounds(pathway,compound) values(?,?)", newCompounds);
        });
    }


    private static void loadProteins(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepProteins = new IntPairSet();
        IntPairSet newProteins = new IntPairSet();
        IntPairSet oldProteins = new IntPairSet();

        load("select pathway,protein from pubchem.pathway_proteins", oldProteins);

        dispatcher.on(obo + "RO_0000057", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/"))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            Integer proteinID = Protein.getProteinID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, proteinID);

            if(oldProteins.remove(pair))
                keepProteins.add(pair);
            else if(!keepProteins.contains(pair))
                newProteins.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_proteins where pathway=? and protein=?", oldProteins);
            store("insert into pubchem.pathway_proteins(pathway,protein) values(?,?)", newProteins);
        });
    }


    private static void loadGenes(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepGenes = new IntPairSet();
        IntPairSet newGenes = new IntPairSet();
        IntPairSet oldGenes = new IntPairSet();

        load("select pathway,gene from pubchem.pathway_genes", oldGenes);

        dispatcher.on(obo + "RO_0000057", (subject, object) -> {
            if(!startsWith(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/"))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            Integer geneID = Gene.getGeneID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, geneID);

            if(oldGenes.remove(pair))
                keepGenes.add(pair);
            else if(!keepGenes.contains(pair))
                newGenes.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_genes where pathway=? and gene=?", oldGenes);
            store("insert into pubchem.pathway_genes(pathway,gene) values(?,?)", newGenes);
        });
    }


    private static void loadComponents(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepComponents = new IntPairSet();
        IntPairSet newComponents = new IntPairSet();
        IntPairSet oldComponents = new IntPairSet();

        load("select pathway,component from pubchem.pathway_components", oldComponents);

        dispatcher.on(bp + "pathwayComponent", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            Integer componentID = getPathwayID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, componentID);

            if(oldComponents.remove(pair))
                keepComponents.add(pair);
            else if(!keepComponents.contains(pair))
                newComponents.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_components where pathway=? and component=?", oldComponents);
            store("insert into pubchem.pathway_components(pathway,component) values(?,?)", newComponents);
        });
    }


    private static void loadReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select pathway,reference from pubchem.pathway_references", oldReferences);

        dispatcher.on(cito + "isDiscussedBy", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            Integer referenceID = Reference.getReferenceID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, referenceID);

            if(oldReferences.remove(pair))
                keepReferences.add(pair);
            else if(!keepReferences.contains(pair))
                newReferences.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_references where pathway=? and reference=?", oldReferences);
            store("insert into pubchem.pathway_references(pathway,reference) values(?,?)", newReferences);
        });
    }


    private static void loadRelatedPathways(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntPairSet keepRelations = new IntPairSet();
        IntPairSet newRelations = new IntPairSet();
        IntPairSet oldRelations = new IntPairSet();

        load("select pathway,related from pubchem.pathway_related_pathways", oldRelations);

        dispatcher.on(skos + "related", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            Integer relatedID = getPathwayID(object.getURI());

            Pair<Integer, Integer> pair = Pair.getPair(pathwayID, relatedID);

            if(oldRelations.remove(pair))
                keepRelations.add(pair);
            else if(!keepRelations.contains(pair))
                newRelations.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_related_pathways where pathway=? and related=?", oldRelations);
            store("insert into pubchem.pathway_related_pathways(pathway,related) values(?,?)", newRelations);
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load pathways ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTitles(dispatcher);
        loadSources(dispatcher);
        loadSameAsReferences(dispatcher);
        loadOrganisms(dispatcher);
        loadCompounds(dispatcher);
        loadProteins(dispatcher);
        loadGenes(dispatcher);
        loadComponents(dispatcher);
        loadReferences(dispatcher);
        loadRelatedPathways(dispatcher);

        dispatcher.load("pubchem/RDF/pathway/pc_pathway.ttl.gz");
        missingPathways.settle();
        dispatcher.finish();

        pathways.flush();

        System.out.println();
    }


    static void finish() throws IOException, SQLException
    {
        System.out.println("finish pathways ...");

        pathways.store();

        System.out.println();
    }


    static Integer getPathwayID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer pathwayID = Integer.parseInt(value.substring(prefixLength));

        if(pathways.reference(pathwayID))
            missingPathways.referenced(pathwayID);

        return pathwayID;
    }
}
