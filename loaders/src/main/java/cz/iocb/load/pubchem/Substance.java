package cz.iocb.load.pubchem;

import static cz.iocb.load.common.EntityTable.date;
import static cz.iocb.load.common.EntityTable.intKey;
import static cz.iocb.load.common.EntityTable.integer;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.EntityTable;
import cz.iocb.load.common.MissingEntities;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Substance extends Updater
{
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/substance/SID";
    static final int prefixLength = prefix.length();

    private static final EntityTable<Integer> substances = new EntityTable<>("pubchem.substances", intKey("id"), null,
            integer("source"), date("available"), date("modified"), integer("compound"));
    private static final MissingEntities<Integer> missingSubstances = new MissingEntities<>("substance", false);


    private static void loadCompoundsAndTypes() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/CHEMINF_000477"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
                        Integer compoundID = Compound.getCompoundID(object.getURI());

                        substances.set(substanceID, "compound", compoundID);
                    }
                }.load(stream);
            }
        });


        Map<Integer, List<Integer>> classes = new HashMap<>();

        BiConsumer<Integer, Integer> consumer = (substance, compound) -> {
            List<Integer> list = classes.get(compound);

            if(list == null)
            {
                list = new ArrayList<>();
                classes.put(compound, list);
            }

            list.add(substance);
        };

        int compoundIndex = substances.columnIndex("compound");

        for(Entry<Integer, Object[]> row : substances.rows())
            if(row.getValue()[compoundIndex] != null)
                consumer.accept(row.getKey(), (Integer) row.getValue()[compoundIndex]);


        IntPairSet keepTypes = new IntPairSet();
        IntPairSet newTypes = new IntPairSet();
        IntPairSet oldTypes = new IntPairSet();

        load("select substance,chebi from pubchem.substance_types", oldTypes);

        processFiles("pubchem/RDF/substance", "pc_substance_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        if(object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Substance"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
                        Integer chebiID = getIntID(object, "http://purl.obolibrary.org/obo/CHEBI_");

                        Pair<Integer, Integer> pair = Pair.getPair(substanceID, chebiID);

                        synchronized(newTypes)
                        {
                            if(oldTypes.remove(pair))
                                keepTypes.add(pair);
                            else if(!keepTypes.contains(pair))
                                newTypes.add(pair);
                        }


                        // extension

                        Integer compoundID = (Integer) substances.get(substanceID, "compound");

                        if(compoundID != null)
                        {
                            List<Integer> substances = classes.get(compoundID);

                            if(substances != null)
                            {
                                for(Integer s : substances)
                                {
                                    Pair<Integer, Integer> p = Pair.getPair(s, chebiID);

                                    synchronized(newTypes)
                                    {
                                        if(oldTypes.remove(p))
                                            keepTypes.add(p);
                                        else if(!keepTypes.contains(p))
                                            newTypes.add(p);
                                    }
                                }
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_types where substance=? and chebi=?", oldTypes);
        store("insert into pubchem.substance_types(substance,chebi) values(?,?)", newTypes);
    }


    private static void loadAvailabilities() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance_available_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/available"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
                        String date = getDate(object, "-04:00", "-05:00");

                        substances.set(substanceID, "available", date);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadModifiedDates() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance_modified_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/modified"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
                        String date = getDate(object, "-04:00", "-05:00");

                        substances.set(substanceID, "modified", date);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadSources() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance_source_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/source"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
                        Integer sourceID = Source.getSourceID(object.getURI());

                        substances.set(substanceID, "source", sourceID);
                    }
                }.load(stream);
            }
        });
    }


    private static void loadMatches() throws IOException, SQLException
    {
        IntPairSet keepChemblMatches = new IntPairSet();
        IntPairSet newChemblMatches = new IntPairSet();
        IntPairSet oldChemblMatches = new IntPairSet();

        IntStringMap keepGlytoucanMatches = new IntStringMap();
        IntStringMap newGlytoucanMatches = new IntStringMap();
        IntStringMap oldGlytoucanMatches = new IntStringMap();

        load("select substance,match from pubchem.substance_chembl_matches", oldChemblMatches);
        load("select substance,match from pubchem.substance_glytoucan_matches", oldGlytoucanMatches);

        processFiles("pubchem/RDF/substance", "pc_substance_seealso_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://www.w3.org/2000/01/rdf-schema#seeAlso"))
                            return;

                        String value = object.getURI();

                        if(value.contains("CHEMBL"))
                        {
                            Integer substanceID = getSubstanceID(subject.getURI(), false);
                            Integer chemblID = value.startsWith("http://identifiers.org") ?
                                    getIntID(value, "http://identifiers.org/chembl.compound:CHEMBL") :
                                    getIntID(value, "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL");

                            Pair<Integer, Integer> pair = Pair.getPair(substanceID, chemblID);

                            synchronized(newChemblMatches)
                            {
                                if(oldChemblMatches.remove(pair))
                                    keepChemblMatches.add(pair);
                                else if(!keepChemblMatches.contains(pair))
                                    newChemblMatches.add(pair);
                            }
                        }
                        else
                        {
                            Integer substanceID = getSubstanceID(subject.getURI(), false);
                            String match = value.startsWith("http://identifiers.org") ?
                                    getStringID(object, "http://identifiers.org/glytoucan:") :
                                    getStringID(object, "http://rdf.glycoinfo.org/glycan/");

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
                                        throw new DataException(
                                                "multiple values of pubchem.substance_glytoucan_matches.match",
                                                substanceID + ": " + keep + ", " + match);

                                    String put = newGlytoucanMatches.put(substanceID, match);

                                    if(put != null && !match.equals(put))
                                        throw new DataException(
                                                "multiple values of pubchem.substance_glytoucan_matches.match",
                                                substanceID + ": " + put + ", " + match);
                                }
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_chembl_matches where substance=? and match=?", oldChemblMatches);
        store("insert into pubchem.substance_chembl_matches(substance,match) values(?,?)", newChemblMatches);

        store("delete from pubchem.substance_glytoucan_matches where substance=? and match=?", oldGlytoucanMatches);
        store("insert into pubchem.substance_glytoucan_matches(substance,match) values(?,?) "
                + "on conflict(substance) do update set match=EXCLUDED.match", newGlytoucanMatches);
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
                    if(!checkPredicate(subject, predicate, object,
                            "http://rdf.wwpdb.org/schema/pdbx-v50.owl#link_to_pdb"))
                        return;

                    Integer substanceID = getSubstanceID(subject.getURI(), false);
                    String link = getStringID(object, "http://rdf.wwpdb.org/pdb/");

                    if(!link.isEmpty())
                    {
                        Pair<Integer, String> pair = Pair.getPair(substanceID, link);

                        if(oldLinks.remove(pair))
                            keepLinks.add(pair);
                        else if(!keepLinks.contains(pair))
                            newLinks.add(pair);
                    }
                    else
                    {
                        Problems.warning("ignored value of " + predicate.getURI() + " without an identifier",
                                text(subject) + " " + text(object));
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

        IntPairSet keepPatents = new IntPairSet();
        IntPairSet newPatents = new IntPairSet();
        IntPairSet oldPatents = new IntPairSet();

        load("select substance,reference from pubchem.substance_references", oldReferences);
        load("select substance,patent from pubchem.substance_patents", oldPatents);

        processFiles("pubchem/RDF/substance", "pc_substance2reference_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        if(!checkPredicate(subject, predicate, object, "http://purl.org/spar/cito/isDiscussedBy"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);

                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/reference/"))
                        {
                            synchronized(newReferences)
                            {
                                Integer referenceID = Reference.getReferenceID(object.getURI());

                                Pair<Integer, Integer> pair = Pair.getPair(substanceID, referenceID);

                                if(oldReferences.remove(pair))
                                    keepReferences.add(pair);
                                else if(!keepReferences.contains(pair))
                                    newReferences.add(pair);
                            }
                        }
                        else
                        {
                            synchronized(newPatents)
                            {
                                Integer patentID = Patent.getPatentID(object.getURI());

                                Pair<Integer, Integer> pair = Pair.getPair(substanceID, patentID);

                                if(oldPatents.remove(pair))
                                    keepPatents.add(pair);
                                else if(!keepPatents.contains(pair))
                                    newPatents.add(pair);
                            }
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.substance_references where substance=? and reference=?", oldReferences);
        store("insert into pubchem.substance_references(substance,reference) values(?,?)", newReferences);

        store("delete from pubchem.substance_patents where substance=? and patent=?", oldPatents);
        store("insert into pubchem.substance_patents(substance,patent) values(?,?)", newPatents);
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
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000008"))
                            return;

                        if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID"))
                            return;

                        Integer substanceID = getSubstanceID(subject.getURI(), false);
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
                            Problems.warning("ignored triple of an unknown synonym in " + predicate.getURI(),
                                    text(subject) + " " + text(object));
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

                        if(!checkPredicate(subject, predicate, object, "http://purl.obolibrary.org/obo/RO_0000056"))
                            return;

                        getStringID(object, Measuregroup.prefix);
                    }
                }.load(stream);
            }
        });
    }


    private static void checkIdentifiers() throws IOException, SQLException
    {
        processFiles("pubchem/RDF/substance", "pc_substance_identifier_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws SQLException, IOException
                    {
                        Integer substanceID = getIntID(subject, prefix);

                        if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/identifier"))
                            return;

                        if(substanceID != Integer.parseInt(getLexicalForm(object)))
                            Problems.error("value of " + predicate.getURI() + " not matching the IRI",
                                    text(subject) + " " + text(object));
                    }
                }.load(stream);
            }
        });
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load substances ...");

        loadCompoundsAndTypes();
        loadAvailabilities();
        loadModifiedDates();
        loadSources();
        loadMatches();
        loadPdbLinks();
        loadReferences();
        loadSynonyms();
        checkMeasuregroups();
        checkIdentifiers();

        substances.flush();

        System.out.println();
    }


    static void finish() throws SQLException
    {
        System.out.println("finish substances ...");

        substances.store();

        System.out.println();
    }


    static void addSubstanceID(Integer substanceID) throws IOException
    {
        if(substances.reference(substanceID))
            missingSubstances.referenced(substanceID);
    }


    static Integer getSubstanceID(String value) throws IOException
    {
        return getSubstanceID(value, true);
    }


    private static Integer getSubstanceID(String value, boolean verbose) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new DataException("unexpected IRI", value);

        Integer substanceID = Integer.parseInt(value.substring(prefixLength));

        if(substances.reference(substanceID) && verbose)
            missingSubstances.referenced(substanceID);

        return substanceID;
    }


    public static int size()
    {
        return substances.size();
    }
}
