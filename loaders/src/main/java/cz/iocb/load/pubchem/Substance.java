package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Substance extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID";
    static final int prefixLength = prefix.length();

    private static final IntSet keepSubstances = new IntSet();
    private static final IntSet newSubstances = new IntSet();
    private static final IntSet oldSubstances = new IntSet();


    private static void loadBases() throws IOException, SQLException
    {
        load("select id from pubchem.substance_bases", oldSubstances);
    }


    private static void loadCompoundsAndTypes() throws IOException, SQLException
    {
        IntIntMap keepCompounds = new IntIntMap();
        IntIntMap newCompounds = new IntIntMap();
        IntIntMap oldCompounds = new IntIntMap();

        load("select id,compound from pubchem.substance_bases where compound is not null", oldCompounds);

        processFiles("pubchem/RDF/substance", "pc_substance2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000477"))
                            throw new IOException();

                        Integer substanceID = getSubstanceID(subject.getURI(), false, true);
                        Integer compoundID = Compound.getCompoundID(object.getURI());

                        synchronized(newCompounds)
                        {
                            if(compoundID.equals(oldCompounds.remove(substanceID)))
                            {
                                keepCompounds.put(substanceID, compoundID);
                            }
                            else
                            {
                                Integer keep = keepCompounds.get(substanceID);

                                if(compoundID.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newCompounds.put(substanceID, compoundID);

                                if(put != null && !compoundID.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.substance_bases set compound=null where id=? and compound=?", oldCompounds);
        store("insert into pubchem.substance_bases(id,compound) values(?,?) "
                + "on conflict(id) do update set compound=EXCLUDED.compound", newCompounds);


        Map<Integer, List<Integer>> classes = new HashMap<Integer, List<Integer>>();

        BiConsumer<Integer, Integer> consumer = (substance, compound) -> {
            List<Integer> list = classes.get(compound);

            if(list == null)
            {
                list = new ArrayList<Integer>();
                classes.put(compound, list);
            }

            list.add(substance);
        };

        keepCompounds.forEach(consumer);
        newCompounds.forEach(consumer);


        IntPairSet keepTypes = new IntPairSet();
        IntPairSet newTypes = new IntPairSet();
        IntPairSet oldTypes = new IntPairSet();

        load("select substance,chebi from pubchem.substance_types", oldTypes);

        try(InputStream stream = getTtlStream("pubchem/RDF/substance/pc_substance_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                    Integer chebiID = getIntID(object, "http://purl.obolibrary.org/obo/CHEBI_");

                    Pair<Integer, Integer> pair = Pair.getPair(substanceID, chebiID);

                    if(oldTypes.remove(pair))
                        keepTypes.add(pair);
                    else if(!keepTypes.contains(pair))
                        newTypes.add(pair);


                    // extension

                    Integer compoundID = keepCompounds.get(substanceID);

                    if(compoundID == null)
                        compoundID = newCompounds.get(substanceID);

                    if(compoundID != null)
                    {
                        List<Integer> substances = classes.get(compoundID);

                        if(substances != null)
                        {
                            for(Integer s : substances)
                            {
                                Pair<Integer, Integer> p = Pair.getPair(s, chebiID);

                                if(oldTypes.remove(p))
                                    keepTypes.add(p);
                                else if(!keepTypes.contains(p))
                                    newTypes.add(p);
                            }
                        }
                    }
                }
            }.load(stream);
        }

        store("delete from pubchem.substance_types where substance=? and chebi=?", oldTypes);
        store("insert into pubchem.substance_types(substance,chebi) values(?,?)", newTypes);
    }


    private static void loadAvailabilities() throws IOException, SQLException
    {
        IntStringMap keepAvailabilities = new IntStringMap();
        IntStringMap newAvailabilities = new IntStringMap();
        IntStringMap oldAvailabilities = new IntStringMap();

        load("select id,available::varchar from pubchem.substance_bases where available is not null",
                oldAvailabilities);

        processFiles("pubchem/RDF/substance", "pc_substance_available_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/available"))
                            throw new IOException();

                        Integer substanceID = getSubstanceID(subject.getURI(), false, true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newAvailabilities)
                        {
                            if(date.equals(oldAvailabilities.remove(substanceID)))
                            {
                                keepAvailabilities.put(substanceID, date);
                            }
                            else
                            {
                                String keep = keepAvailabilities.get(substanceID);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newAvailabilities.put(substanceID, date);

                                if(put != null && !date.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.substance_bases set available=null where id=? and available=?::date", oldAvailabilities);
        store("insert into pubchem.substance_bases(id,available) values(?,?::date) "
                + "on conflict(id) do update set available=EXCLUDED.available", newAvailabilities);
    }


    private static void loadModifiedDates() throws IOException, SQLException
    {
        IntStringMap keepModifiedDates = new IntStringMap();
        IntStringMap newModifiedDates = new IntStringMap();
        IntStringMap oldModifiedDates = new IntStringMap();

        load("select id,modified::varchar from pubchem.substance_bases where modified is not null", oldModifiedDates);

        processFiles("pubchem/RDF/substance", "pc_substance_modified_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/modified"))
                            throw new IOException();

                        Integer substanceID = getSubstanceID(subject.getURI(), false, true);
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        synchronized(newModifiedDates)
                        {
                            if(date.equals(oldModifiedDates.remove(substanceID)))
                            {
                                keepModifiedDates.put(substanceID, date);
                            }
                            else
                            {
                                String keep = keepModifiedDates.get(substanceID);

                                if(date.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                String put = newModifiedDates.put(substanceID, date);

                                if(put != null && !date.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.substance_bases set modified=null where id=? and modified=?::date", oldModifiedDates);
        store("insert into pubchem.substance_bases(id,modified) values(?,?::date) "
                + "on conflict(id) do update set modified=EXCLUDED.modified", newModifiedDates);
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntIntMap keepSources = new IntIntMap();
        IntIntMap newSources = new IntIntMap();
        IntIntMap oldSources = new IntIntMap();

        load("select id,source from pubchem.substance_bases where source is not null", oldSources);

        processFiles("pubchem/RDF/substance", "pc_substance_source_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                            throw new IOException();

                        Integer substanceID = getSubstanceID(subject.getURI(), false, true);
                        Integer sourceID = Source.getSourceID(object.getURI());

                        synchronized(newSources)
                        {
                            if(sourceID.equals(oldSources.remove(substanceID)))
                            {
                                keepSources.put(substanceID, sourceID);
                            }
                            else
                            {
                                Integer keep = keepSources.get(substanceID);

                                if(sourceID.equals(keep))
                                    return;
                                else if(keep != null)
                                    throw new IOException();

                                Integer put = newSources.put(substanceID, sourceID);

                                if(put != null && !sourceID.equals(put))
                                    throw new IOException();
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("update pubchem.substance_bases set source=null where id=? and source=?", oldSources);
        store("insert into pubchem.substance_bases(id,source) values(?,?) "
                + "on conflict(id) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadMatches() throws IOException, SQLException
    {
        IntPairSet keepChemblMatches = new IntPairSet();
        IntPairSet newChemblMatches = new IntPairSet();
        IntPairSet oldChemblMatches = new IntPairSet();

        IntStringMap keepGlytoucanMatches = new IntStringMap();
        IntStringMap newGlytoucanMatches = new IntStringMap();
        IntStringMap oldGlytoucanMatches = new IntStringMap();

        load("select substance,chembl from pubchem.substance_chembl_matches", oldChemblMatches);
        load("select substance,glytoucan from pubchem.substance_glytoucan_matches", oldGlytoucanMatches);

        processFiles("pubchem/RDF/substance", "pc_substance_match\\.ttl[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/2004/02/skos/core#exactMatch"))
                            throw new IOException();

                        String value = object.getURI();

                        if(value.startsWith("http://rdf.ebi.ac.uk/resource/chembl/molecule/"))
                        {
                            // workaround
                            if(value.matches("http://rdf\\.ebi\\.ac\\.uk/resource/chembl/molecule/[Cc]hembl[0-9]+"))
                            {
                                System.out.println("    fix wrong ChEMBL iri: " + value);
                                value = value.replaceFirst("molecule/[Cc]hembl", "molecule/CHEMBL");
                            }

                            Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                            Integer chemblID = getIntID(value, "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL");

                            Pair<Integer, Integer> pair = Pair.getPair(substanceID, chemblID);

                            synchronized(newChemblMatches)
                            {
                                if(oldChemblMatches.remove(pair))
                                    keepChemblMatches.add(pair);
                                else if(!keepChemblMatches.contains(pair))
                                    newChemblMatches.add(pair);
                            }
                        }
                        else if(value.startsWith("https://identifiers.org/glytoucan:"))
                        {
                            Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                            String match = getStringID(object, "https://identifiers.org/glytoucan:");

                            synchronized(newGlytoucanMatches)
                            {
                                if(match.equals(oldGlytoucanMatches.remove(substanceID)))
                                {
                                    keepGlytoucanMatches.put(substanceID, match);
                                }
                                else
                                {
                                    String keep = keepGlytoucanMatches.get(substanceID);

                                    if(match.equals(keep))
                                        return;
                                    else if(keep != null)
                                        throw new IOException();

                                    String put = newGlytoucanMatches.put(substanceID, match);

                                    if(put != null && !match.equals(put))
                                        throw new IOException();
                                }
                            }
                        }
                        else if(!value.startsWith("http://linkedchemistry.info/chembl/chemblid/"))
                        {
                            throw new IOException();
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_chembl_matches where substance=? and chembl=?", oldChemblMatches);
        store("insert into pubchem.substance_chembl_matches(substance,chembl) values(?,?)", newChemblMatches);

        store("delete from pubchem.substance_glytoucan_matches where substance=? and glytoucan=?", oldGlytoucanMatches);
        store("insert into pubchem.substance_glytoucan_matches(substance,glytoucan) values(?,?) "
                + "on conflict(substance) do update set glytoucan=EXCLUDED.glytoucan", newGlytoucanMatches);
    }


    private static void loadPdbLinks() throws IOException, SQLException
    {
        IntStringSet keepLinks = new IntStringSet();
        IntStringSet newLinks = new IntStringSet();
        IntStringSet oldLinks = new IntStringSet();

        load("select substance,pdblink from pubchem.substance_pdblinks", oldLinks);

        try(InputStream stream = getTtlStream("pubchem/RDF/substance/pc_substance2pdb.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://rdf.wwpdb.org/schema/pdbx-v40.owl#link_to_pdb"))
                        throw new IOException();

                    Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                    String link = getStringID(object, "http://rdf.wwpdb.org/pdb/");

                    if(!link.isEmpty())
                    {
                        Pair<Integer, String> pair = Pair.getPair(substanceID, link);

                        if(oldLinks.remove(pair))
                            keepLinks.add(pair);
                        else if(!keepLinks.contains(pair))
                            newLinks.add(pair);
                    }
                }
            }.load(stream);
        }

        store("delete from pubchem.substance_pdblinks where substance=? and pdblink=?", oldLinks);
        store("insert into pubchem.substance_pdblinks(substance,pdblink) values(?,?)", newLinks);
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntPairSet keepReferences = new IntPairSet();
        IntPairSet newReferences = new IntPairSet();
        IntPairSet oldReferences = new IntPairSet();

        load("select substance,reference from pubchem.substance_references", oldReferences);

        try(InputStream stream = getTtlStream("pubchem/RDF/substance/pc_substance2reference.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/isDiscussedBy"))
                        throw new IOException();

                    Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                    Integer referenceID = Reference.getReferenceID(object.getURI());

                    Pair<Integer, Integer> pair = Pair.getPair(substanceID, referenceID);

                    if(oldReferences.remove(pair))
                        keepReferences.add(pair);
                    else if(!keepReferences.contains(pair))
                        newReferences.add(pair);
                }
            }.load(stream);
        }

        store("delete from pubchem.substance_references where substance=? and reference=?", oldReferences);
        store("insert into pubchem.substance_references(substance,reference) values(?,?)", newReferences);
    }


    private static void loadSynonyms() throws IOException, SQLException
    {
        IntPairSet keepSynonyms = new IntPairSet();
        IntPairSet newSynonyms = new IntPairSet();
        IntPairSet oldSynonyms = new IntPairSet();

        load("select substance,synonym from pubchem.substance_synonyms", oldSynonyms);

        processFiles("pubchem/RDF/substance", "pc_substance2descriptor_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000008"))
                            throw new IOException();

                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false, false);
                        Integer md5ID = Synonym.getSynonymID(object.getURI());

                        if(md5ID != null)
                        {
                            Pair<Integer, Integer> pair = Pair.getPair(substanceID, md5ID);

                            synchronized(newSynonyms)
                            {
                                if(oldSynonyms.remove(pair))
                                    keepSynonyms.add(pair);
                                else if(!keepSynonyms.contains(pair))
                                    newSynonyms.add(pair);
                            }
                        }
                        else
                        {
                            System.out.println("    ignore synonym " + object.getURI() + " for sio:SIO_000008");
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_synonyms where substance=? and synonym=?", oldSynonyms);
        store("insert into pubchem.substance_synonyms(substance,synonym) values(?,?)", newSynonyms);
    }


    private static void checkMeasuregroups() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance2measuregroup_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        getIntID(subject, prefix);

                        if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/RO_0000056"))
                            throw new IOException();

                        getStringID(object, Measuregroup.prefix);
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load substances ...");

        loadBases();
        loadCompoundsAndTypes();
        loadAvailabilities();
        loadModifiedDates();
        loadSources();
        loadMatches();
        loadPdbLinks();
        loadReferences();
        loadSynonyms();
        checkMeasuregroups();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish substances ...");

        store("delete from pubchem.substance_bases where id=?", oldSubstances);
        store("insert into pubchem.substance_bases(id) values(?)", newSubstances);

        System.out.println();
    }


    static void addSubstanceID(Integer substanceID) throws IOException
    {
        synchronized(newSubstances)
        {
            if(!keepSubstances.contains(substanceID) && !newSubstances.contains(substanceID))
            {
                System.out.println("    add missing substance SID" + substanceID);

                if(oldSubstances.remove(substanceID))
                    keepSubstances.add(substanceID);
                else
                    newSubstances.add(substanceID);
            }
        }
    }


    static Integer getSubstanceID(String value) throws IOException
    {
        return getSubstanceID(value, true, false);
    }


    private static Integer getSubstanceID(String value, boolean verbose, boolean keepForce) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        Integer substanceID = Integer.parseInt(value.substring(prefixLength));

        synchronized(newSubstances)
        {
            if(!keepSubstances.contains(substanceID) && !newSubstances.contains(substanceID))
            {
                if(verbose)
                    System.out.println("    add missing substance SID" + substanceID);

                if(!oldSubstances.remove(substanceID) && !keepForce)
                    newSubstances.add(substanceID);
                else
                    keepSubstances.add(substanceID);
            }
        }

        return substanceID;
    }


    public static int size()
    {
        return newSubstances.size() + keepSubstances.size();
    }
}
