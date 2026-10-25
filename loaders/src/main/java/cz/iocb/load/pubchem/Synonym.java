package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



/**
 * Loader of the PubChem synonyms ({@code pubchem/RDF/synonym}), identified by the MD5 hashes of their names, into the
 * tables {@code pubchem.synonyms} and {@code pubchem.synonym_values} and the tables of their types, compounds and
 * subjects.
 */
class Synonym extends Updater
{
    /**
     * MD5 hash of the name of a synonym, held as two longs.
     */
    private static final class MD5
    {
        /**
         * Upper 64 bits of the hash.
         */
        private final long hi;

        /**
         * Lower 64 bits of the hash.
         */
        private final long lo;

        /**
         * Creates the hash from its hexadecimal form.
         *
         * @param value the 32 lower-case hexadecimal digits of the hash
         */
        public MD5(String value)
        {
            this(value, 0);
        }

        /**
         * Creates the hash from its hexadecimal form in a string.
         *
         * @param value the string holding the 32 lower-case hexadecimal digits of the hash
         * @param offset the index of the first digit in the string
         */
        public MD5(String value, int offset)
        {
            hi = parse(offset + 0, value);
            lo = parse(offset + 16, value);
        }

        /**
         * Parses 16 lower-case hexadecimal digits of a string.
         *
         * @param offset the index of the first digit
         * @param value the string
         * @return the parsed value
         */
        private static long parse(int offset, String value)
        {
            long ret = 0;

            for(int i = offset; i < offset + 16; i++)
            {
                char ch = value.charAt(i);
                long digit = ch >= 'a' ? 10 + ch - 'a' : ch - '0';

                if(digit < 0 || digit > 15)
                    throw new RuntimeException("unexpected md5 value: " + ch);

                ret = ret << 4 | digit;
            }

            return ret;
        }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == this)
                return true;

            if(obj == null || obj.getClass() != this.getClass())
                return false;

            MD5 other = (MD5) obj;

            return hi == other.hi && lo == other.lo;
        }

        @Override
        public int hashCode()
        {
            return Long.hashCode(lo);
        }

        @Override
        public String toString()
        {
            char[] ret = new char[32];

            long lo = this.lo;
            long hi = this.hi;

            for(int i = 31; i >= 16; i--)
            {
                long digit = 0x0f & lo;
                lo >>>= 4;
                ret[i] = (char) (digit >= 10 ? 'a' + digit - 10 : '0' + digit);
            }

            for(int i = 15; i >= 0; i--)
            {
                long digit = 0x0f & hi;
                hi >>>= 4;
                ret[i] = (char) (digit >= 10 ? 'a' + digit - 10 : '0' + digit);
            }

            return new String(ret);
        }
    }


    /**
     * Map of the synonym hashes to integers, read from and written to the database as hexadecimal strings.
     */
    @SuppressWarnings("serial")
    private static class MD5IntMap extends SqlMap<MD5, Integer>
    {
        /**
         * Creates an empty map.
         */
        private MD5IntMap()
        {
        }

        @Override
        public MD5 getKey(ResultSet result) throws SQLException
        {
            return new MD5(result.getString(1));
        }

        @Override
        public Integer getValue(ResultSet result) throws SQLException
        {
            return result.getInt(2);
        }

        @Override
        public void set(PreparedStatement statement, MD5 key, Integer value) throws SQLException
        {
            statement.setString(1, key.toString());
            statement.setInt(2, value);
        }
    }


    /**
     * IRI prefix of the synonyms.
     */
    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_";

    /**
     * Length of {@link #prefix}.
     */
    static final int prefixLength = prefix.length();

    /**
     * Ids of the stored synonyms met in the data, by their hashes.
     */
    private static final MD5IntMap keepHashes = new MD5IntMap();

    /**
     * Ids of the synonyms met in the data that are not stored yet, by their hashes.
     */
    private static final MD5IntMap newHashes = new MD5IntMap();

    /**
     * Id of the next new synonym.
     */
    private static int nextMd5ID;

    /**
     * Id of the next new synonym value.
     */
    private static int nextValueID;


    /**
     * Not instantiable.
     */
    private Synonym()
    {
    }


    /**
     * Loads the synonyms and their values from the value files and stores their changes.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadBases() throws IOException, SQLException
    {
        MD5IntMap oldHashes = new MD5IntMap();

        IntStringSet keepValues = new IntStringSet();
        IntStringPairIntMap newValues = new IntStringPairIntMap();
        IntStringPairIntMap oldValues = new IntStringPairIntMap();

        load("select md5,id from pubchem.synonyms", oldHashes);
        load("select synonym,value,id from pubchem.synonym_values", oldValues);

        nextMd5ID = oldHashes.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;
        nextValueID = oldValues.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

        processFiles("pubchem/RDF/synonym", "pc_synonym_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000300"))
                            return;

                        String value = getString(object);
                        MD5 md5 = getSynonymMD5(subject.getURI());
                        Integer md5ID = null;

                        synchronized(newHashes)
                        {
                            md5ID = keepHashes.get(md5);

                            if(md5ID == null)
                                md5ID = newHashes.get(md5);

                            if(md5ID == null)
                            {
                                if((md5ID = oldHashes.remove(md5)) == null)
                                    newHashes.put(md5, md5ID = nextMd5ID++);
                                else
                                    keepHashes.put(md5, md5ID);
                            }
                        }

                        Pair<Integer, String> pair = Pair.getPair(md5ID, value);

                        synchronized(newValues)
                        {
                            if(oldValues.remove(pair) != null)
                                keepValues.add(pair);
                            else if(!keepValues.contains(pair))
                                newValues.put(pair, nextValueID++);
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.synonyms where md5=? and id=?", oldHashes);
        store("insert into pubchem.synonyms(md5,id) values(?,?)", newHashes);

        store("delete from pubchem.synonym_values where synonym=? and value=? and id=?", oldValues);
        store("insert into pubchem.synonym_values(synonym,value,id) values(?,?,?)", newValues);
    }


    /**
     * Loads the CHEMINF types of the synonyms and stores their changes in {@code pubchem.synonym_types}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTypes() throws IOException, SQLException
    {
        IntPairSet keepTypes = new IntPairSet();
        IntPairSet newTypes = new IntPairSet();
        IntPairSet oldTypes = new IntPairSet();

        load("select synonym,type_id from pubchem.synonym_types", oldTypes);

        processFiles("pubchem/RDF/synonym", "pc_synonym_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            return;

                        if(object.getURI().equals("http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#Synonym"))
                            return;

                        Integer md5ID = getSynonymID(subject.getURI());
                        Integer typeID = getIntID(object, "http://semanticscience.org/resource/CHEMINF_");

                        if(md5ID != null)
                        {
                            Pair<Integer, Integer> pair = Pair.getPair(md5ID, typeID);

                            synchronized(newTypes)
                            {
                                if(oldTypes.remove(pair))
                                    keepTypes.add(pair);
                                else if(!keepTypes.contains(pair))
                                    newTypes.add(pair);
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

        store("delete from pubchem.synonym_types where synonym=? and type_id=?", oldTypes);
        store("insert into pubchem.synonym_types(synonym,type_id) values(?,?)", newTypes);
    }


    /**
     * Loads the compounds of the synonyms and stores their changes in {@code pubchem.synonym_compounds}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadCompounds() throws IOException, SQLException
    {
        IntPairSet keepCompounds = new IntPairSet();
        IntPairSet newCompounds = new IntPairSet();
        IntPairSet oldCompounds = new IntPairSet();

        load("select synonym,compound from pubchem.synonym_compounds", oldCompounds);

        processFiles("pubchem/RDF/synonym", "pc_synonym2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getTtlStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!checkPredicate(subject, predicate, object,
                                "http://semanticscience.org/resource/SIO_000011"))
                            return;

                        Integer md5ID = getSynonymID(subject.getURI());

                        if(md5ID != null)
                        {
                            Integer compoundID = Compound.getCompoundID(object.getURI());

                            Pair<Integer, Integer> pair = Pair.getPair(md5ID, compoundID);

                            synchronized(newCompounds)
                            {
                                if(oldCompounds.remove(pair))
                                    keepCompounds.add(pair);
                                else if(!keepCompounds.contains(pair))
                                    newCompounds.add(pair);
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

        store("delete from pubchem.synonym_compounds where synonym=? and compound=?", oldCompounds);
        store("insert into pubchem.synonym_compounds(synonym,compound) values(?,?)", newCompounds);
    }


    /**
     * Loads the subjects of the synonyms, which are PubChem concepts or MeSH descriptors, and stores their changes in
     * {@code pubchem.synonym_concept_subjects} and {@code pubchem.synonym_mesh_subjects}.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    private static void loadTopics() throws IOException, SQLException
    {
        IntPairSet keepConceptSubjects = new IntPairSet();
        IntPairSet newConceptSubjects = new IntPairSet();
        IntPairSet oldConceptSubjects = new IntPairSet();

        IntStringSet keepMeshSubjects = new IntStringSet();
        IntStringSet newMeshSubjects = new IntStringSet();
        IntStringSet oldMeshSubjects = new IntStringSet();

        load("select synonym,concept from pubchem.synonym_concept_subjects", oldConceptSubjects);
        load("select synonym,subject from pubchem.synonym_mesh_subjects", oldMeshSubjects);

        try(InputStream stream = getTtlStream("pubchem/RDF/synonym/pc_synonym_topic.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(!checkPredicate(subject, predicate, object, "http://purl.org/dc/terms/subject"))
                        return;

                    Integer md5ID = getSynonymID(subject.getURI());
                    String value = object.getURI();

                    if(md5ID == null)
                    {
                        Problems.warning("ignored triple of an unknown synonym in " + predicate.getURI(),
                                text(subject) + " " + text(object));
                    }
                    else if(value.startsWith(Concept.prefix))
                    {
                        Integer conceptID = Concept.getConceptID(object.getURI());

                        Pair<Integer, Integer> pair = Pair.getPair(md5ID, conceptID);

                        if(oldConceptSubjects.remove(pair))
                            keepConceptSubjects.add(pair);
                        else if(!keepConceptSubjects.contains(pair))
                            newConceptSubjects.add(pair);
                    }
                    else
                    {
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");

                        Pair<Integer, String> pair = Pair.getPair(md5ID, subjectID);

                        if(oldMeshSubjects.remove(pair))
                            keepMeshSubjects.add(pair);
                        else if(!keepMeshSubjects.contains(pair))
                            newMeshSubjects.add(pair);
                    }
                }
            }.load(stream);
        }

        store("delete from pubchem.synonym_concept_subjects where synonym=? and concept=?", oldConceptSubjects);
        store("insert into pubchem.synonym_concept_subjects(synonym,concept) values(?,?)", newConceptSubjects);

        store("delete from pubchem.synonym_mesh_subjects where synonym=? and subject=?", oldMeshSubjects);
        store("insert into pubchem.synonym_mesh_subjects(synonym,subject) values(?,?)", newMeshSubjects);
    }


    /**
     * Loads the synonyms from the PubChemRDF files.
     *
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    static void load() throws IOException, SQLException
    {
        System.out.println("load synonyms ...");

        loadBases();
        loadTypes();
        loadCompounds();
        loadTopics();

        System.out.println();
    }


    /**
     * Returns the hash of the synonym with the given IRI.
     *
     * @param value the IRI of the synonym
     * @return the hash of the synonym
     * @throws IOException if the IRI is not an IRI of a synonym
     */
    private static MD5 getSynonymMD5(String value) throws IOException
    {
        if(!value.startsWith(prefix) || !value.substring(prefixLength).matches("[0-9a-f]{32}"))
            throw new DataException("unexpected IRI", value);

        return new MD5(value, prefixLength);
    }


    /**
     * Returns the id of the synonym with the given IRI.
     *
     * @param value the IRI of the synonym
     * @return the id of the synonym, or null for a synonym without a value in the data
     * @throws IOException if the IRI is not an IRI of a synonym
     */
    static Integer getSynonymID(String value) throws IOException
    {
        MD5 md5 = getSynonymMD5(value);

        Integer md5ID = keepHashes.get(md5);

        if(md5ID != null)
            return md5ID;

        return newHashes.get(md5);
    }
}
