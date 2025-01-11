package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Measuregroup extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID";
    static final int prefixLength = prefix.length();

    private static final IntPairSet keepMeasuregroups = new IntPairSet();
    private static final IntPairSet newMeasuregroups = new IntPairSet();
    private static final IntPairSet oldMeasuregroups = new IntPairSet();

    private static final IntPairIntSet keepSubstances = new IntPairIntSet();
    private static final IntPairIntSet newSubstances = new IntPairIntSet();
    private static final IntPairIntSet oldSubstances = new IntPairIntSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select bioassay,measuregroup from pubchem.measuregroup_bases", oldMeasuregroups);
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
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    if(!object.getURI().equals("http://www.bioassayontology.org/bao#BAO_0000040"))
                        throw new IOException();

                    parseMeasuregroup(subject, false);
                }
            }.load(stream);
        }
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntPairIntMap keepSources = new IntPairIntMap();
        IntPairIntMap newSources = new IntPairIntMap();
        IntPairIntMap oldSources = new IntPairIntMap();

        load("select bioassay,measuregroup,source from pubchem.measuregroup_bases where source is not null",
                oldSources);

        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup_source.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                        throw new IOException();

                    Pair<Integer, Integer> measuregroup = parseMeasuregroup(subject, true);
                    Integer sourceID = Source.getSourceID(object.getURI());

                    if(sourceID.equals(oldSources.remove(measuregroup)))
                    {
                        keepSources.put(measuregroup, sourceID);
                    }
                    else
                    {
                        Integer keep = keepSources.get(measuregroup);

                        if(sourceID.equals(keep))
                            return;
                        else if(keep != null)
                            throw new IOException();

                        Integer put = newSources.put(measuregroup, sourceID);

                        if(put != null && !sourceID.equals(put))
                            throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("update pubchem.measuregroup_bases set source=null where bioassay=? and measuregroup=? and source=?",
                oldSources);
        store("insert into pubchem.measuregroup_bases(bioassay,measuregroup,source) values(?,?,?) "
                + "on conflict(bioassay,measuregroup) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntPairStringMap keepTitles = new IntPairStringMap();
        IntPairStringMap newTitles = new IntPairStringMap();
        IntPairStringMap oldTitles = new IntPairStringMap();

        load("select bioassay,measuregroup,title from pubchem.measuregroup_bases where title is not null", oldTitles);

        try(InputStream stream = getTtlStream("pubchem/RDF/measuregroup/pc_measuregroup_title.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                        throw new IOException();

                    Pair<Integer, Integer> measuregroup = parseMeasuregroup(subject, true);
                    String title = getString(object);

                    if(title.equals(oldTitles.remove(measuregroup)))
                    {
                        keepTitles.put(measuregroup, title);
                    }
                    else
                    {
                        String keep = keepTitles.get(measuregroup);

                        if(title.equals(keep))
                            return;
                        else if(keep != null)
                            throw new IOException();

                        String put = newTitles.put(measuregroup, title);

                        if(put != null && !title.equals(put))
                            throw new IOException();
                    }
                }
            }.load(stream);
        }

        store("update pubchem.measuregroup_bases set title=null where bioassay=? and measuregroup=? and title=?",
                oldTitles);
        store("insert into pubchem.measuregroup_bases(bioassay,measuregroup,title) values(?,?,?) "
                + "on conflict(bioassay, measuregroup) do update set title=EXCLUDED.title", newTitles);
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
                    if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/RO_0000057"))
                        throw new IOException();

                    if(object.getURI().startsWith(Protein.prefix))
                    {
                        Integer proteinID = Protein.getProteinID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject, false);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, proteinID);

                        if(oldProteins.remove(pair))
                            keepProteins.add(pair);
                        else if(!keepProteins.contains(pair))
                            newProteins.add(pair);
                    }
                    else if(object.getURI().startsWith(Gene.prefix))
                    {
                        Integer geneID = Gene.getGeneID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject, false);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, geneID);

                        if(oldGenes.remove(pair))
                            keepGenes.add(pair);
                        else if(!keepGenes.contains(pair))
                            newGenes.add(pair);
                    }
                    else if(object.getURI().startsWith(Taxonomy.prefix))
                    {
                        Integer taxonomyID = Taxonomy.getTaxonomyID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject, false);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, taxonomyID);

                        if(oldTaxonomies.remove(pair))
                            keepTaxonomies.add(pair);
                        else if(!keepTaxonomies.contains(pair))
                            newTaxonomies.add(pair);
                    }
                    else if(object.getURI().startsWith(Cell.prefix))
                    {
                        Integer cellID = Cell.getCellID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject, false);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, cellID);

                        if(oldCells.remove(pair))
                            keepCells.add(pair);
                        else if(!keepCells.contains(pair))
                            newCells.add(pair);
                    }
                    else if(object.getURI().startsWith(Anatomy.prefix))
                    {
                        Integer anatomyID = Anatomy.getAnatomyID(object.getURI());
                        Pair<Integer, Integer> measuregourp = parseMeasuregroup(subject, false);

                        Pair<Pair<Integer, Integer>, Integer> pair = Pair.getPair(measuregourp, anatomyID);

                        if(oldAnatomies.remove(pair))
                            keepAnatomies.add(pair);
                        else if(!keepAnatomies.contains(pair))
                            newAnatomies.add(pair);
                    }
                    else
                    {
                        throw new IOException();
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

                        if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/OBI_0000299"))
                            throw new IOException();

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

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish measuregroups ...");

        store("delete from pubchem.measuregroup_bases where bioassay=? and measuregroup=?", oldMeasuregroups);
        store("insert into pubchem.measuregroup_bases(bioassay,measuregroup) values(?,?)", newMeasuregroups);

        store("delete from pubchem.measuregroup_substances where bioassay=? and measuregroup=? and substance=?",
                oldSubstances);
        store("insert into pubchem.measuregroup_substances(bioassay,measuregroup,substance) values(?,?,?)",
                newSubstances);

        System.out.println();
    }


    static void addMeasuregroupID(Integer bioassay, Integer measuregroup)
    {
        addMeasuregroupID(bioassay, measuregroup, false);
    }


    static void addMeasuregroupID(Integer bioassay, Integer measuregroup, boolean forceKeep)
    {
        Pair<Integer, Integer> pair = Pair.getPair(bioassay, measuregroup);

        synchronized(newMeasuregroups)
        {
            if(newMeasuregroups.contains(pair))
            {
                if(forceKeep)
                {
                    newMeasuregroups.remove(pair);
                    keepMeasuregroups.add(pair);
                }
            }
            else if(!keepMeasuregroups.contains(pair))
            {
                if(!oldMeasuregroups.remove(pair) && !forceKeep)
                    newMeasuregroups.add(pair);
                else
                    keepMeasuregroups.add(pair);
            }
        }
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


    private static Pair<Integer, Integer> parseMeasuregroup(Node node, boolean forceKeep) throws IOException
    {
        String iri = node.getURI();
        Integer bioassay;
        Integer measuregroup;

        if(!iri.startsWith(prefix))
            throw new IOException();

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
                    throw new IOException();
            }
        }
        else if(grp != -1 && grp != iri.length() - 1)
        {
            bioassay = Integer.parseInt(iri.substring(prefixLength, grp));
            measuregroup = Integer.parseInt(iri.substring(grp + 1));

            if(measuregroup > 2147483645)
                throw new IOException();
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

        addMeasuregroupID(bioassay, measuregroup, forceKeep);
        Bioassay.addBioassayID(bioassay);

        return Pair.getPair(bioassay, measuregroup);
    }
}
