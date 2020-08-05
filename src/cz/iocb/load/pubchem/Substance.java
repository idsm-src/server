package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.MD5;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Substance extends Updater
{
    private static IntHashSet usedSubstances;
    private static IntHashSet newSubstances;
    private static IntHashSet oldSubstances;


    private static void loadBases() throws IOException, SQLException
    {
        usedSubstances = new IntHashSet(256000000);
        newSubstances = new IntHashSet(256000000);
        oldSubstances = getIntSet("select id from substance_bases", 256000000);
    }


    private static void loadCompounds() throws IOException, SQLException
    {
        IntIntHashMap newCompounds = new IntIntHashMap(256000000);
        IntIntHashMap oldCompounds = getIntIntMap("select id, compound from substance_bases where compound is not null",
                256000000);

        processFiles("RDF/substance", "pc_substance2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/CHEMINF_000477"))
                            throw new IOException();

                        int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        int compoundID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                        addSubstanceID(substanceID);
                        Compound.addCompoundID(compoundID);

                        synchronized(newCompounds)
                        {
                            if(compoundID != oldCompounds.removeKeyIfAbsent(substanceID, NO_VALUE))
                                newCompounds.put(substanceID, compoundID);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update substance_bases set compound = null where id = ?", oldCompounds.keySet());
        batch("insert into substance_bases(id, compound) values (?,?) "
                + "on conflict (id) do update set compound=EXCLUDED.compound", newCompounds);
    }


    private static void loadAvailabilities() throws IOException, SQLException
    {
        IntStringMap newAvailabilities = new IntStringMap(256000000);
        IntStringMap oldAvailabilities = getIntStringMap(
                "select id, available::varchar from substance_bases where available is not null", 256000000);

        processFiles("RDF/substance", "pc_substance_available_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/available"))
                            throw new IOException();

                        int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        addSubstanceID(substanceID);

                        synchronized(newAvailabilities)
                        {
                            if(!date.equals(oldAvailabilities.remove(substanceID)))
                                newAvailabilities.put(substanceID, date);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update substance_bases set available = null where id = ?", oldAvailabilities.keySet());
        batch("insert into substance_bases(id, available) values (?,cast(? as date)) "
                + "on conflict (id) do update set available=EXCLUDED.available", newAvailabilities);
    }


    private static void loadModifiedDates() throws IOException, SQLException
    {
        IntStringMap newModifiedDates = new IntStringMap(256000000);
        IntStringMap oldModifiedDates = getIntStringMap(
                "select id, modified::varchar from substance_bases where modified is not null", 256000000);

        processFiles("RDF/substance", "pc_substance_modified_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/modified"))
                            throw new IOException();

                        int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        String date = getString(object).replaceFirst("-0[45]:00$", "");

                        addSubstanceID(substanceID);

                        synchronized(newModifiedDates)
                        {
                            if(!date.equals(oldModifiedDates.remove(substanceID)))
                                newModifiedDates.put(substanceID, date);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update substance_bases set modified = null where id = ?", oldModifiedDates.keySet());
        batch("insert into substance_bases(id, modified) values (?,cast(? as date)) "
                + "on conflict (id) do update set modified=EXCLUDED.modified", newModifiedDates);
    }


    private static void loadSources() throws IOException, SQLException
    {
        IntIntHashMap newSources = new IntIntHashMap(256000000);
        IntIntHashMap oldSources = getIntIntMap("select id, source from substance_bases where source is not null",
                256000000);

        processFiles("RDF/substance", "pc_substance_source_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                            throw new IOException();

                        int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        int sourceID = Source.getSourceID(object.getURI());

                        addSubstanceID(substanceID);

                        synchronized(newSources)
                        {
                            if(sourceID != oldSources.removeKeyIfAbsent(substanceID, NO_VALUE))
                                newSources.put(substanceID, sourceID);
                        }
                    }
                }.load(stream);
            }
        });

        batch("update substance_bases set source = null where id = ?", oldSources.keySet());
        batch("insert into substance_bases(id, source) values (?,?) "
                + "on conflict (id) do update set source=EXCLUDED.source", newSources);
    }


    private static void loadMatches() throws IOException, SQLException
    {
        IntPairSet newMatches = new IntPairSet(10000000);
        IntPairSet oldMatches = getIntPairSet("select substance, match from substance_matches", 10000000);

        processFiles("RDF/substance", "pc_substance_match\\.ttl[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/2004/02/skos/core#exactMatch"))
                            throw new IOException();

                        String value = object.getURI();

                        if(!value.startsWith("http://linkedchemistry.info/chembl/chemblid/"))
                        {
                            int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                            int matchID;

                            addSubstanceID(substanceID);

                            if(value.length() > 46 && value.charAt(46) == 'C')
                                matchID = getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL");
                            else
                                matchID = -getIntID(object, "http://rdf.ebi.ac.uk/resource/chembl/molecule/SCHEMBL");

                            IntIntPair pair = PrimitiveTuples.pair(substanceID, matchID);

                            synchronized(newMatches)
                            {
                                if(!oldMatches.remove(pair))
                                    newMatches.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from substance_matches where substance = ? and match = ?", oldMatches);
        batch("insert into substance_matches(substance, match) values (?,?)", newMatches);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        IntPairSet newTypes = new IntPairSet(10000000);
        IntPairSet oldTypes = getIntPairSet("select substance, chebi from substance_types", 10000000);

        try(InputStream stream = getStream("RDF/substance/pc_substance_type.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                        throw new IOException();

                    int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                    int chebiID = getIntID(object, "http://purl.obolibrary.org/obo/CHEBI_");

                    IntIntPair pair = PrimitiveTuples.pair(substanceID, chebiID);
                    addSubstanceID(substanceID);

                    if(!oldTypes.remove(pair))
                        newTypes.add(pair);
                }
            }.load(stream);
        }

        batch("delete from substance_types where substance = ? and chebi = ?", oldTypes);
        batch("insert into substance_types(substance, chebi) values (?,?)", newTypes);
    }


    private static void loadPdbLinks() throws IOException, SQLException
    {
        IntStringPairSet newLinks = new IntStringPairSet(200000);
        IntStringPairSet oldLinks = getIntStringPairSet("select substance, pdblink from substance_pdblinks", 200000);

        try(InputStream stream = getStream("RDF/substance/pc_substance2pdb.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://rdf.wwpdb.org/schema/pdbx-v40.owl#link_to_pdb"))
                        throw new IOException();

                    int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                    String link = getStringID(object, "http://rdf.wwpdb.org/pdb/");

                    if(!link.isEmpty())
                    {
                        IntObjectPair<String> pair = PrimitiveTuples.pair(substanceID, link);
                        addSubstanceID(substanceID);

                        if(!oldLinks.remove(pair))
                            newLinks.add(pair);
                    }
                }
            }.load(stream);
        }

        batch("delete from substance_pdblinks where substance = ? and pdblink = ?", oldLinks);
        batch("insert into substance_pdblinks(substance, pdblink) values (?,?)", newLinks);
    }


    private static void loadReferences() throws IOException, SQLException
    {
        IntPairSet newReferences = new IntPairSet(10000000);
        IntPairSet oldReferences = getIntPairSet("select substance, reference from substance_references", 10000000);

        try(InputStream stream = getStream("RDF/substance/pc_substance2reference.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/spar/cito/isDiscussedBy"))
                        throw new IOException();

                    int substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                    int referenceID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");

                    IntIntPair pair = PrimitiveTuples.pair(substanceID, referenceID);
                    addSubstanceID(substanceID);

                    if(!oldReferences.remove(pair))
                        newReferences.add(pair);
                }
            }.load(stream);
        }

        batch("delete from substance_references where substance = ? and reference = ?", oldReferences);
        batch("insert into substance_references(substance, reference) values (?,?)", newReferences);
    }


    private static void loadSynonyms() throws IOException, SQLException
    {
        IntPairSet newSynonyms = new IntPairSet(256000000);
        IntPairSet oldSynonyms = getIntPairSet("select substance, synonym from substance_synonyms", 256000000);

        processFiles("RDF/substance", "pc_substance2descriptor_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-attribute"))
                            throw new IOException();

                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID"))
                            return;

                        int substanceID;

                        if(subject.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID"))
                            substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID");
                        else
                            substanceID = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/");

                        MD5 md5 = getSynonymMD5(object);
                        int md5ID = Synonym.getSynonymID(md5);

                        if(md5ID != NO_VALUE)
                        {
                            IntIntPair pair = PrimitiveTuples.pair(substanceID, md5ID);
                            addSubstanceID(substanceID);

                            synchronized(newSynonyms)
                            {
                                if(!oldSynonyms.remove(pair))
                                    newSynonyms.add(pair);
                            }
                        }
                        else
                        {
                            System.out.println("    ignore md5 synonym " + md5 + " for sio:has-attribute");
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from substance_synonyms where substance = ? and synonym = ?", oldSynonyms);
        batch("insert into substance_synonyms(substance, synonym) values (?,?)", newSynonyms);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load substances ...");

        loadBases();
        loadCompounds();
        loadAvailabilities();
        loadModifiedDates();
        loadSources();

        loadMatches();
        loadTypes();
        loadPdbLinks();
        loadReferences();
        loadSynonyms();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        batch("delete from substance_bases where id = ?", oldSubstances);
        batch("insert into substance_bases(id) values(?) on conflict do nothing", newSubstances);

        usedSubstances = null;
        newSubstances = null;
        oldSubstances = null;
    }


    static void addSubstanceID(int substanceID)
    {
        synchronized(newSubstances)
        {
            if(usedSubstances.add(substanceID) && !oldSubstances.remove(substanceID))
                newSubstances.add(substanceID);
        }
    }
}
