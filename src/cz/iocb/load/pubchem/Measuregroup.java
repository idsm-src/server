package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.IntTriplet;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Measuregroup extends Updater
{
    private interface SetFunction<T>
    {
        T set(int bioassay, int measuregroup);
    }


    private static final String measuregroupPrefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID";
    private static final int measuregroupPrefixLength = measuregroupPrefix.length();

    private static IntPairSet usedMeasuregroups;
    private static IntPairSet newMeasuregroups;
    private static IntPairSet oldMeasuregroups;


    private static void loadBases() throws IOException, SQLException
    {
        usedMeasuregroups = new IntPairSet(2000000);
        newMeasuregroups = new IntPairSet(2000000);
        oldMeasuregroups = getIntPairSet("select bioassay, measuregroup from pubchem.measuregroup_bases", 2000000);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        try(InputStream stream = getStream("pubchem/RDF/measuregroup/pc_measuregroup_type.ttl.gz"))
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

                    parseMeasuregroup(subject);
                }
            }.load(stream);
        }
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntPairIntMap newSources = new IntPairIntMap(2000000);
        IntPairIntMap oldSources = getIntPairIntMap(
                "select bioassay, measuregroup, source from pubchem.measuregroup_bases where source is not null",
                2000000);

        try(InputStream stream = getStream("pubchem/RDF/measuregroup/pc_measuregroup_source.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                        throw new IOException();

                    IntIntPair measuregroup = parseMeasuregroup(subject);
                    int sourceID = Source.getSourceID(object.getURI());

                    if(sourceID != oldSources.removeKeyIfAbsent(measuregroup, NO_VALUE))
                        newSources.put(measuregroup, sourceID);
                }
            }.load(stream);
        }

        batch("update pubchem.measuregroup_bases set source = null where bioassay = ? and measuregroup = ?",
                oldSources.keySet(), (PreparedStatement statement, IntIntPair measuregroup) -> {
                    statement.setInt(1, measuregroup.getOne());
                    statement.setInt(2, measuregroup.getTwo());
                });

        batch("insert into pubchem.measuregroup_bases(bioassay, measuregroup, source) values (?,?,?) "
                + "on conflict (bioassay, measuregroup) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadTitles() throws IOException, SQLException
    {
        IntPairStringMap newTitles = new IntPairStringMap(2000000);
        IntPairStringMap oldTitles = getIntPairStringMap(
                "select bioassay, measuregroup, title from pubchem.measuregroup_bases where title is not null",
                2000000);

        try(InputStream stream = getStream("pubchem/RDF/measuregroup/pc_measuregroup_title.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                        throw new IOException();

                    IntIntPair measuregroup = parseMeasuregroup(subject);
                    String title = getString(object);

                    if(!title.equals(oldTitles.remove(measuregroup)))
                        newTitles.put(measuregroup, title);
                }
            }.load(stream);
        }

        batch("update pubchem.measuregroup_bases set title = null where bioassay = ? and measuregroup = ?",
                oldTitles.keySet(), (PreparedStatement statement, IntIntPair measuregroup) -> {
                    statement.setInt(1, measuregroup.getOne());
                    statement.setInt(2, measuregroup.getTwo());
                });

        batch("insert into pubchem.measuregroup_bases(bioassay, measuregroup, title) values (?,?,?) "
                + "on conflict (bioassay, measuregroup) do update set title=EXCLUDED.title", newTitles);
    }


    private static void loadProteinsAndGenes() throws IOException, SQLException
    {
        IntTripletSet newProteins = new IntTripletSet(2000000);
        IntTripletSet oldProteins = getIntTripletSet(
                "select bioassay, measuregroup, protein from pubchem.measuregroup_proteins", 2000000);

        IntTripletSet newGenes = new IntTripletSet(1000000);
        IntTripletSet oldGenes = getIntTripletSet("select bioassay, measuregroup, gene from pubchem.measuregroup_genes",
                1000000);

        IntTripletSet newTaxonomies = new IntTripletSet(1000000);
        IntTripletSet oldTaxonomies = getIntTripletSet(
                "select bioassay, measuregroup, taxonomy from pubchem.measuregroup_taxonomies", 1000000);

        IntTripletSet newCells = new IntTripletSet(1000000);
        IntTripletSet oldCells = getIntTripletSet("select bioassay, measuregroup, cell from pubchem.measuregroup_cells",
                1000000);

        try(InputStream stream = getStream("pubchem/RDF/measuregroup/pc_measuregroup2protein.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/RO_0000057"))
                        throw new IOException();

                    if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC"))
                    {
                        String proteinName = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/ACC");
                        int proteinID = Protein.getProteinID(proteinName);

                        IntTriplet triple = parseMeasuregroup(subject, proteinID);

                        if(!oldProteins.remove(triple))
                            newProteins.add(triple);
                    }
                    else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"))
                    {
                        int geneID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID");

                        IntTriplet triple = parseMeasuregroup(subject, geneID);

                        if(!oldGenes.remove(triple))
                            newGenes.add(triple);
                    }
                    else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID"))
                    {
                        int taxonomyID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/taxonomy/TAXID");

                        IntTriplet triple = parseMeasuregroup(subject, taxonomyID);

                        if(!oldTaxonomies.remove(triple))
                            newTaxonomies.add(triple);
                    }
                    else if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID"))
                    {
                        int cellID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/cell/CELLID");

                        IntTriplet triple = parseMeasuregroup(subject, cellID);

                        if(!oldCells.remove(triple))
                            newCells.add(triple);
                    }
                    else
                    {
                        throw new IOException();
                    }
                }
            }.load(stream);

            batch("delete from pubchem.measuregroup_proteins where bioassay = ? and measuregroup = ? and protein = ?",
                    oldProteins);
            batch("insert into pubchem.measuregroup_proteins(bioassay, measuregroup, protein) values (?,?,?)",
                    newProteins);

            batch("delete from pubchem.measuregroup_genes where bioassay = ? and measuregroup = ? and gene = ?",
                    oldGenes);
            batch("insert into pubchem.measuregroup_genes(bioassay, measuregroup, gene) values (?,?,?)", newGenes);

            batch("delete from pubchem.measuregroup_taxonomies where bioassay = ? and measuregroup = ? and taxonomy = ?",
                    oldTaxonomies);
            batch("insert into pubchem.measuregroup_taxonomies(bioassay, measuregroup, taxonomy) values (?,?,?)",
                    newTaxonomies);

            batch("delete from pubchem.measuregroup_cells where bioassay = ? and measuregroup = ? and cell = ?",
                    oldCells);
            batch("insert into pubchem.measuregroup_cells(bioassay, measuregroup, cell) values (?,?,?)", newCells);
        }
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load measuregroups ...");

        loadBases();
        loadTypes();
        loadSources();
        loadTitles();
        loadProteinsAndGenes();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        batch("delete from pubchem.measuregroup_bases where bioassay = ? and measuregroup = ?", oldMeasuregroups);
        batch("insert into pubchem.measuregroup_bases(bioassay, measuregroup) values (?,?) on conflict do nothing",
                newMeasuregroups);

        usedMeasuregroups = null;
        newMeasuregroups = null;
        oldMeasuregroups = null;
    }


    static void addMeasuregroupID(int bioassay, int measuregroup)
    {
        IntIntPair pair = PrimitiveTuples.pair(bioassay, measuregroup);

        synchronized(newMeasuregroups)
        {
            if(usedMeasuregroups.add(pair) && !oldMeasuregroups.remove(pair))
                newMeasuregroups.add(pair);
        }
    }


    private static <T> T parseMeasuregroup(Node node, SetFunction<T> function) throws IOException
    {
        String iri = node.getURI();
        int bioassay;
        int measuregroup;

        if(!iri.startsWith(measuregroupPrefix))
            throw new IOException();

        int grp = iri.indexOf("_", measuregroupPrefixLength + 1);

        if(grp != -1 && iri.indexOf("_PMID") == grp)
        {
            String part = iri.substring(grp + 5);
            bioassay = Integer.parseInt(iri.substring(measuregroupPrefixLength, grp));

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
            bioassay = Integer.parseInt(iri.substring(measuregroupPrefixLength, grp));
            measuregroup = Integer.parseInt(iri.substring(grp + 1));

            if(measuregroup > 2147483645)
                throw new IOException();
        }
        else if(grp != -1)
        {
            bioassay = Integer.parseInt(iri.substring(measuregroupPrefixLength, grp));
            measuregroup = 2147483646; // magic number
        }
        else
        {
            bioassay = Integer.parseInt(iri.substring(measuregroupPrefixLength));
            measuregroup = 2147483647; // magic number
        }

        addMeasuregroupID(bioassay, measuregroup);
        return function.set(bioassay, measuregroup);
    }


    private static IntIntPair parseMeasuregroup(Node node) throws IOException
    {
        return parseMeasuregroup(node, (bioassay, measuregroup) -> PrimitiveTuples.pair(bioassay, measuregroup));
    }


    private static IntTriplet parseMeasuregroup(Node node, int last) throws IOException
    {
        return parseMeasuregroup(node, (bioassay, measuregroup) -> new IntTriplet(bioassay, measuregroup, last));
    }
}
