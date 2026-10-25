package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.typed;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import static cz.iocb.load.common.EntityTable.varchar;
import static cz.iocb.load.common.TripleDispatcher.all;
import static cz.iocb.load.common.TripleDispatcher.is;
import static cz.iocb.load.common.TripleDispatcher.startsWith;
import static cz.iocb.load.common.TripleDispatcher.text;
import static cz.iocb.load.common.TripleStreamProcessor.getIntID;
import static cz.iocb.load.common.TripleStreamProcessor.getString;
import static cz.iocb.load.common.TripleStreamProcessor.getStringID;
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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleDispatcher;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem pathways ({@code pubchem/RDF/pathway}) into the table {@code pubchem.pathways} and the tables
 * of their participants, components, references, related pathways and GlyCosmos matches.
 */
class Pathway extends Updater
{
    /**
     * External database a pathway is taken from, as its reference IRI tells.
     */
    private static class Description
    {
        /**
         * Name of the database, a value of the reference type.
         */
        final String name;

        /**
         * IRI prefix of the pathways of the database.
         */
        final String prefix;

        /**
         * Regular expression of the IRIs of the pathways of the database.
         */
        final String pattern;

        /**
         * Creates the description of a database.
         *
         * @param name the name of the database
         * @param prefix the IRI prefix of the pathways of the database
         * @param suffix the regular expression of the rest of their IRIs
         */
        Description(String name, String prefix, String suffix)
        {
            this.name = name;
            this.prefix = prefix;
            this.pattern = prefix.replaceAll("([?.])", "\\\\$1") + suffix;
        }
    }


    /**
     * Descriptions of the external databases of the pathways.
     */
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


    /**
     * IRI prefix of the pathways.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/pathway/PWID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * IRI prefix of the GlyCosmos pathways.
     */
    static final String glycosmosPrefix = "https://glycosmos.org/pathways/";

    /**
     * Table of the pathways.
     */
    private static final EntityTable<Integer> pathways = new EntityTable<>("pubchem.pathways", intKey("id"), null,
            integer("source"), uniqueVarchar("title"), typed("reference_type", "pubchem.pathway_reference_type"),
            varchar("reference"), integer("organism"));

    /**
     * Pathways referenced but not described by the data.
     */
    private static final MissingEntities<Integer> missingPathways = new MissingEntities<>("pathway", true);


    /**
     * Not instantiable.
     */
    private Pathway()
    {
    }


    /**
     * Registers the checks of the predicates, types and values of the pathway file.
     *
     * @param dispatcher the dispatcher of the triples
     */
    private static void check(TripleDispatcher dispatcher)
    {
        dispatcher.checkPredicates(all(), dcterms + "title", dcterms + "source", up + "organism",
                bp + "pathwayComponent", cito + "isDiscussedBy", rdfs + "seeAlso", skos + "related", obo + "RO_0000057",
                rdf + "type");
        dispatcher.checkTypes(all(), vocab + "Pathway", bp + "Pathway");
        dispatcher.checkPrefixes(all(), obo + "RO_0000057", "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID",
                "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/", "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/");
        // the values of rdfs:seeAlso are checked by loadSameAsReferences() and loadGlycosmosMatches()
    }


    /**
     * Registers the handler that adds the pathways described by the data.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.onType(vocab + "Pathway", (subject, object) -> {
            Integer pathwayID = getIntID(subject, prefix);

            pathways.reference(pathwayID);
            missingPathways.described(pathwayID);
        });
    }


    /**
     * Registers the handler that sets the titles of the pathways.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "title", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            String title = getString(object);

            pathways.set(pathwayID, "title", title);
        });
    }


    /**
     * Registers the handler that sets the sources of the pathways.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSources(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(dcterms + "source", (subject, object) -> {
            Integer pathwayID = getPathwayID(subject.getURI());
            Integer sourceID = Source.getSourceID(object.getURI());

            pathways.set(pathwayID, "source", sourceID);
        });
    }


    /**
     * Registers the handler that sets the external databases and ids of the pathways from their references other than
     * GlyCosmos.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSameAsReferences(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(startsWith(object, glycosmosPrefix))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            String iri = object.getURI();
            Description description = null;

            for(Description test : descriptions)
                if(iri.matches(test.pattern))
                    description = test;

            if(description == null)
                throw new DataException("unexpected reference", iri);

            pathways.set(pathwayID, "reference_type", description.name);
            pathways.set(pathwayID, "reference", iri.substring(description.prefix.length()));
        });
    }


    /**
     * Registers the handler of the GlyCosmos pathways matching the pathways and the action that stores their changes in
     * {@code pubchem.pathway_glycosmos_matches}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadGlycosmosMatches(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        IntStringSet keepMatches = new IntStringSet();
        IntStringSet newMatches = new IntStringSet();
        IntStringSet oldMatches = new IntStringSet();

        load("select pathway,match from pubchem.pathway_glycosmos_matches", oldMatches);

        dispatcher.on(rdfs + "seeAlso", (subject, object) -> {
            if(!startsWith(object, glycosmosPrefix))
                return;

            Integer pathwayID = getPathwayID(subject.getURI());
            String match = getStringID(object, glycosmosPrefix);

            if(!match.matches("R-[A-Z]{3}-[1-9][0-9]*"))
                throw new DataException("unexpected reference", object.getURI());

            Pair<Integer, String> pair = Pair.getPair(pathwayID, match);

            if(oldMatches.remove(pair))
                keepMatches.add(pair);
            else if(!keepMatches.contains(pair))
                newMatches.add(pair);
        });

        dispatcher.after(() -> {
            store("delete from pubchem.pathway_glycosmos_matches where pathway=? and match=?", oldMatches);
            store("insert into pubchem.pathway_glycosmos_matches(pathway,match) values(?,?)", newMatches);
        });
    }


    /**
     * Registers the handler that sets the organisms of the pathways.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadOrganisms(TripleDispatcher dispatcher) throws IOException, SQLException
    {
        dispatcher.on(up + "organism", (subject, object) -> {
            // workaround
            if(is(object, Taxonomy.prefix))
            {
                Problems.warning("value of " + up + "organism without a taxonomy id", text(subject));
                return;
            }

            Integer pathwayID = getPathwayID(subject.getURI());
            Integer organismID = Taxonomy.getTaxonomyID(object.getURI());

            pathways.set(pathwayID, "organism", organismID);
        });
    }


    /**
     * Registers the handler of the compounds participating in the pathways and the action that stores their changes in
     * {@code pubchem.pathway_compounds}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the proteins participating in the pathways and the action that stores their changes in
     * {@code pubchem.pathway_proteins}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the genes participating in the pathways and the action that stores their changes in
     * {@code pubchem.pathway_genes}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the component pathways of the pathways and the action that stores their changes in
     * {@code pubchem.pathway_components}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the references discussing the pathways and the action that stores their changes in
     * {@code pubchem.pathway_references}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Registers the handler of the related pathways of the pathways and the action that stores their changes in
     * {@code pubchem.pathway_related_pathways}.
     *
     * @param dispatcher the dispatcher of the triples
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
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


    /**
     * Loads the pathways from the PubChemRDF file and flushes their table.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load pathways ...");

        TripleDispatcher dispatcher = new TripleDispatcher();

        check(dispatcher);
        loadBases(dispatcher);
        loadTitles(dispatcher);
        loadSources(dispatcher);
        loadSameAsReferences(dispatcher);
        loadGlycosmosMatches(dispatcher);
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


    /**
     * Stores the pathways, including those referenced by the parts loaded later, and deletes the stored pathways that
     * are neither described nor referenced.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void finish() throws IOException, SQLException
    {
        System.out.println("finish pathways ...");

        pathways.store();

        System.out.println();
    }


    /**
     * Returns the id of the pathway with the given IRI; a pathway not described by the data gets a row and is counted
     * as missing.
     *
     * @param value the IRI of the pathway
     * @return the id of the pathway
     * @throws IOException if the IRI is not an IRI of a pathway
     */
    static Integer getPathwayID(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer pathwayID = Integer.parseInt(value.substring(prefixLength));

        if(pathways.reference(pathwayID))
            missingPathways.referenced(pathwayID);

        return pathwayID;
    }
}
