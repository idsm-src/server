package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.intPairKey;
import static cz.iocb.load.common.EntityTable.integer;
import static cz.iocb.load.common.EntityTable.uniqueVarchar;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Measuregroup extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Pair<Integer, Integer>> measuregroups = new EntityTable<>("pubchem.measuregroups",
            intPairKey("bioassay", "measuregroup"), null, integer("source"), uniqueVarchar("title"));

    private static final IntPairIntSet keepSubstances = new IntPairIntSet();
    private static final IntPairIntSet newSubstances = new IntPairIntSet();
    private static final IntPairIntSet oldSubstances = new IntPairIntSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select bioassay,measuregroup,substance from pubchem.measuregroup_substances", oldSubstances);
    }


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


    private static void checkEndpoints() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/measuregroup", "pc_measuregroup2endpoint_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getStringID(subject, prefix);

                        if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/OBI_0000299"))
                            return;

                        getStringID(object, Endpoint.prefix);
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load measuregroups ...");

        loadBases();
        loadTypes();
        loadSources();
        loadTitles();
        loadProteinsAndGenes();
        checkEndpoints();

        measuregroups.flush();

        System.out.println();
    }


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


    static void addMeasuregroupID(Integer bioassay, Integer measuregroup)
    {
        measuregroups.reference(Pair.getPair(bioassay, measuregroup));
    }


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


    private static Pair<Integer, Integer> parseMeasuregroup(Node node) throws IOException
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
