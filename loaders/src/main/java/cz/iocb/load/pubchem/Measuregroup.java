package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intPairKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem measuregroups ({@code pubchem/RDF/measuregroup}) into the table {@code pubchem.measuregroups}
 * and the tables of their participants and substances.
 */
class Measuregroup extends Updater
{
    /**
     * IRI prefix of the measuregroups.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Table of the measuregroups, keyed by their bioassays and measuregroup numbers.
     */
    private static final EntityTable<Pair<Integer, Integer>> measuregroups = new EntityTable<>("pubchem.measuregroups",
            intPairKey("bioassay", "measuregroup"), null, integer("source"), uniqueVarchar("title"));

    /**
     * Stored substances of the measuregroups met in the data.
     */
    private static final IntPairIntSet keepSubstances = new IntPairIntSet();

    /**
     * Substances of the measuregroups met in the data that are not stored yet.
     */
    private static final IntPairIntSet newSubstances = new IntPairIntSet();

    /**
     * Stored substances of the measuregroups not met in the data so far; those left are deleted.
     */
    private static final IntPairIntSet oldSubstances = new IntPairIntSet();


    /**
     * Not instantiable.
     */
    private Measuregroup()
    {
    }


    /**
     * Loads the stored substances of the measuregroups.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases() throws IOException, SQLException
    {
        load("select bioassay,measuregroup,substance from pubchem.measuregroup_substances", oldSubstances);
    }


    /**
     * Checks the types of the measuregroups and adds the measuregroups they describe.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTypes() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        return;

                    checkType(subject, object, "http://www.bioassayontology.org/bao#BAO_0000040",
                            "http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#MeasureGroup");

                    parseMeasuregroup(subject);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the sources of the measuregroups.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadSources() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup_source.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/source"))
                        return;

                    Pair<Integer, Integer> measuregroup = parseMeasuregroup(subject);
                    Integer sourceID = Source.getSourceID(object.getURI());

                    measuregroups.set(measuregroup, "source", sourceID);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the titles of the measuregroups.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTitles() throws IOException, SQLException
    {
        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup_title.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/title"))
                        return;

                    Pair<Integer, Integer> measuregroup = parseMeasuregroup(subject);
                    String title = getString(object);

                    measuregroups.set(measuregroup, "title", title);
                }
            }.load(stream);
        }
    }


    /**
     * Loads the participants of the measuregroups (proteins, genes, taxonomies, cells and anatomies) and stores their
     * changes in the tables of the participants.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadProteinsAndGenes() throws IOException, SQLException
    {
        IntPairIntSet keepProteins = new IntPairIntSet();
        IntPairIntSet newProteins = new IntPairIntSet();
        IntPairIntSet oldProteins = new IntPairIntSet();

        IntPairIntSet keepGenes = new IntPairIntSet();
        IntPairIntSet newGenes = new IntPairIntSet();
        IntPairIntSet oldGenes = new IntPairIntSet();

        IntPairIntSet keepTaxonomies = new IntPairIntSet();
        IntPairIntSet newTaxonomies = new IntPairIntSet();
        IntPairIntSet oldTaxonomies = new IntPairIntSet();

        IntPairIntSet keepCells = new IntPairIntSet();
        IntPairIntSet newCells = new IntPairIntSet();
        IntPairIntSet oldCells = new IntPairIntSet();

        IntPairIntSet keepAnatomies = new IntPairIntSet();
        IntPairIntSet newAnatomies = new IntPairIntSet();
        IntPairIntSet oldAnatomies = new IntPairIntSet();

        load("select bioassay,measuregroup,protein from pubchem.measuregroup_proteins", oldProteins);
        load("select bioassay,measuregroup,gene from pubchem.measuregroup_genes", oldGenes);
        load("select bioassay,measuregroup,taxonomy from pubchem.measuregroup_taxonomies", oldTaxonomies);
        load("select bioassay,measuregroup,cell from pubchem.measuregroup_cells", oldCells);
        load("select bioassay,measuregroup,anatomy from pubchem.measuregroup_anatomies", oldAnatomies);

        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup2participant.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/RO_0000057"))
                        return;

                    if(object.getURI().startsWith(Protein.prefix))
                    {
                        Integer proteinID = Protein.getProteinID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, proteinID);

                        if(oldProteins.remove(pair))
                            keepProteins.add(pair);
                        else if(!keepProteins.contains(pair))
                            newProteins.add(pair);
                    }
                    else if(object.getURI().startsWith(Gene.prefix))
                    {
                        Integer geneID = Gene.getGeneID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, geneID);

                        if(oldGenes.remove(pair))
                            keepGenes.add(pair);
                        else if(!keepGenes.contains(pair))
                            newGenes.add(pair);
                    }
                    else if(object.getURI().startsWith(Taxonomy.prefix))
                    {
                        Integer taxonomyID = Taxonomy.getTaxonomyID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, taxonomyID);

                        if(oldTaxonomies.remove(pair))
                            keepTaxonomies.add(pair);
                        else if(!keepTaxonomies.contains(pair))
                            newTaxonomies.add(pair);
                    }
                    else if(object.getURI().startsWith(Cell.prefix))
                    {
                        Integer cellID = Cell.getCellID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, cellID);

                        if(oldCells.remove(pair))
                            keepCells.add(pair);
                        else if(!keepCells.contains(pair))
                            newCells.add(pair);
                    }
                    else if(object.getURI().startsWith(Anatomy.prefix))
                    {
                        Integer anatomyID = Anatomy.getAnatomyID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, anatomyID);

                        if(oldAnatomies.remove(pair))
                            keepAnatomies.add(pair);
                        else if(!keepAnatomies.contains(pair))
                            newAnatomies.add(pair);
                    }
                    else
                    {
                        unexpectedValue(subject, predicate, object);
                    }
                }
            }.load(stream);

            store("delete from pubchem.measuregroup_proteins where bioassay=? and measuregroup=? and protein=?",
                    oldProteins);
            store("insert into pubchem.measuregroup_proteins(bioassay,measuregroup,protein) values(?,?,?)",
                    newProteins);

            store("delete from pubchem.measuregroup_genes where bioassay=? and measuregroup=? and gene=?", oldGenes);
            store("insert into pubchem.measuregroup_genes(bioassay,measuregroup,gene) values(?,?,?)", newGenes);

            store("delete from pubchem.measuregroup_taxonomies where bioassay=? and measuregroup=? and taxonomy=?",
                    oldTaxonomies);
            store("insert into pubchem.measuregroup_taxonomies(bioassay,measuregroup,taxonomy) values(?,?,?)",
                    newTaxonomies);

            store("delete from pubchem.measuregroup_cells where bioassay=? and measuregroup=? and cell=?", oldCells);
            store("insert into pubchem.measuregroup_cells(bioassay,measuregroup,cell) values(?,?,?)", newCells);

            store("delete from pubchem.measuregroup_anatomies where bioassay=? and measuregroup=? and anatomy=?",
                    oldAnatomies);
            store("insert into pubchem.measuregroup_anatomies(bioassay,measuregroup,anatomy) values(?,?,?)",
                    newAnatomies);
        }
    }


    /**
     * Loads the measuregroups from the PubChemRDF files and flushes their table.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load measuregroups ...");

        loadBases();
        loadTypes();
        loadSources();
        loadTitles();
        loadProteinsAndGenes();

        measuregroups.flush();

        System.out.println();
    }


    /**
     * Loads the measuregroups of the substances, to which the endpoints have already added the measuregroups of their
     * substances. The source states the measuregroup of every substance with an endpoint and states each just once, so
     * the number of its triples differs from the number of the pairs whenever an endpoint has added a pair that the
     * source does not state.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void loadSubstances() throws IOException, SQLException
    {
        System.out.println("load measuregroup substances ...");

        AtomicLong count = new AtomicLong();

        processFiles("pubchem/RDF/substance", "pc_substance2measuregroup_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/RO_0000056"))
                            return;

                        Integer substanceID = Substance.getSubstanceID(subject.getURI());
                        Pair<Integer, Integer> measuregroup = parseMeasuregroup(object);

                        addMeasuregroupSubstance(measuregroup.getOne(), measuregroup.getTwo(), substanceID);
                        count.incrementAndGet();
                    }
                }.load(stream);
            }
        });

        long pairs = (long) keepSubstances.size() + newSubstances.size();

        if(count.get() != pairs)
            Problems.error(
                    "number of the triples of http://purl.obolibrary.org/obo/RO_0000056 different from the "
                            + "number of the measuregroups of substances",
                    count.get() + " triples, " + pairs + " pairs");

        System.out.println();
    }


    /**
     * Stores the measuregroups, including those referenced by the parts loaded later, and the changes of the substances
     * of the measuregroups.
     *
     * @throws SQLException if a database error occurs
     */
    static void finish() throws SQLException
    {
        System.out.println("finish measuregroups ...");

        measuregroups.store();

        store("delete from pubchem.measuregroup_substances where bioassay=? and measuregroup=? and substance=?",
                oldSubstances);
        store("insert into pubchem.measuregroup_substances(bioassay,measuregroup,substance) values(?,?,?)",
                newSubstances);

        System.out.println();
    }


    /**
     * Records a reference to the measuregroup; a measuregroup without a row gets one.
     *
     * @param bioassay the id of the bioassay
     * @param measuregroup the number of the measuregroup
     */
    static void addMeasuregroupID(Integer bioassay, Integer measuregroup)
    {
        measuregroups.reference(Pair.getPair(bioassay, measuregroup));
    }


    /**
     * Records that the substance belongs to the measuregroup.
     *
     * @param bioassay the id of the bioassay
     * @param measuregroup the number of the measuregroup
     * @param substance the id of the substance
     */
    static void addMeasuregroupSubstance(Integer bioassay, Integer measuregroup, Integer substance)
    {
        Pair<Pair<Integer, Integer>, Integer> triplet = Pair.getPair(Pair.getPair(bioassay, measuregroup), substance);

        synchronized(newSubstances)
        {
            if(!keepSubstances.contains(triplet) && !newSubstances.contains(triplet))
            {
                if(oldSubstances.remove(triplet))
                    keepSubstances.add(triplet);
                else
                    newSubstances.add(triplet);
            }
        }
    }


    /**
     * Parses the IRI of a measuregroup into its bioassay and measuregroup number and references both. The IRIs without
     * a number get reserved ones: 2147483647 for {@code AID<n>}, 2147483646 for {@code AID<n>_}, -2147483647 for
     * {@code AID<n>_PMID}, and {@code AID<n>_PMID<m>} gets -m.
     *
     * @param node the IRI of the measuregroup
     * @return the id of the bioassay and the number of the measuregroup
     * @throws IOException if the node is not an IRI of a measuregroup
     */
    static Pair<Integer, Integer> parseMeasuregroup(Node node) throws IOException
    {
        String iri = node.getURI();
        Integer bioassay;
        Integer measuregroup;

        if(!iri.startsWith(prefix))
            throw new DataException("unexpected IRI", iri);

        int grp = iri.indexOf("_", prefixLength + 1);

        if(grp != -1 && iri.indexOf("_PMID") == grp)
        {
            String part = iri.substring(grp + 5);
            bioassay = Integer.parseInt(iri.substring(prefixLength, grp));

            if(part.isEmpty())
            {
                measuregroup = -2147483647; // magic number
            }
            else
            {
                measuregroup = -Integer.parseInt(part);

                if(measuregroup == -2147483647 || measuregroup == 0)
                    throw new DataException("unexpected IRI", iri);
            }
        }
        else if(grp != -1 && grp != iri.length() - 1)
        {
            bioassay = Integer.parseInt(iri.substring(prefixLength, grp));
            measuregroup = Integer.parseInt(iri.substring(grp + 1));

            if(measuregroup > 2147483645)
                throw new DataException("unexpected IRI", iri);
        }
        else if(grp != -1)
        {
            bioassay = Integer.parseInt(iri.substring(prefixLength, grp));
            measuregroup = 2147483646; // magic number
        }
        else
        {
            bioassay = Integer.parseInt(iri.substring(prefixLength));
            measuregroup = 2147483647; // magic number
        }

        addMeasuregroupID(bioassay, measuregroup);
        Bioassay.addBioassayID(bioassay);

        return Pair.getPair(bioassay, measuregroup);
    }
}
