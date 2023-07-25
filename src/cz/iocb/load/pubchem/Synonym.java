package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Synonym extends Updater
{
    private static final class MD5
    {
        private final long hi;
        private final long lo;

        public MD5(String value)
        {
            this(value, 0);
        }

        public MD5(String value, int offset)
        {
            hi = parse(offset + 0, value);
            lo = parse(offset + 16, value);
        }

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


    @SuppressWarnings("serial")
    private static class MD5IntMap extends SqlMap<MD5, Integer>
    {
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


    static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_";
    static final int prefixLength = prefix.length();

    private static final MD5IntMap keepHashes = new MD5IntMap();
    private static final MD5IntMap newHashes = new MD5IntMap();

    private static int nextMd5ID;
    private static int nextValueID;


    private static void loadBases() throws IOException, SQLException
    {
        MD5IntMap oldHashes = new MD5IntMap();

        IntStringSet keepValues = new IntStringSet();
        IntStringPairIntMap newValues = new IntStringPairIntMap();
        IntStringPairIntMap oldValues = new IntStringPairIntMap();

        load("select md5,id from pubchem.synonym_bases", oldHashes);
        load("select synonym,value,__ from pubchem.synonym_values", oldValues);

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
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000300"))
                            throw new IOException();

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

        store("delete from pubchem.synonym_bases where md5=? and id=?", oldHashes);
        store("insert into pubchem.synonym_bases(md5,id) values(?,?)", newHashes);

        store("delete from pubchem.synonym_values where synonym=? and value=? and __=?", oldValues);
        store("insert into pubchem.synonym_values(synonym,value,__) values(?,?,?)", newValues);
    }


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
                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

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
                            System.out.println(
                                    "    ignore md5 synonym " + getSynonymMD5(subject.getURI()) + " for rdf:type");
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.synonym_types where synonym=? and type_id=?", oldTypes);
        store("insert into pubchem.synonym_types(synonym,type_id) values(?,?)", newTypes);
    }


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
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/SIO_000011"))
                            throw new IOException();

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
                            System.out.println("    ignore md5 synonym " + getSynonymMD5(subject.getURI())
                                    + " for sio:SIO_000011");
                        }
                    }
                }.load(stream);
            }
        });

        store("delete from pubchem.synonym_compounds where synonym=? and compound=?", oldCompounds);
        store("insert into pubchem.synonym_compounds(synonym,compound) values(?,?)", newCompounds);
    }


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
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                        throw new IOException();

                    Integer md5ID = getSynonymID(subject.getURI());
                    String value = object.getURI();

                    if(md5ID == null)
                    {
                        System.out.println(
                                "    ignore md5 synonym " + getSynonymMD5(subject.getURI()) + " for dcterms:subject");
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


    static void load() throws IOException, SQLException
    {
        System.out.println("load synonyms ...");

        loadBases();
        loadTypes();
        loadCompounds();
        loadTopics();

        System.out.println();
    }


    private static MD5 getSynonymMD5(String value) throws IOException
    {
        if(!value.startsWith(prefix))
            throw new IOException("unexpected IRI: " + value);

        return new MD5(value, prefixLength);
    }


    static Integer getSynonymID(String value) throws IOException
    {
        MD5 md5 = getSynonymMD5(value);

        Integer md5ID = keepHashes.get(md5);

        if(md5ID != null)
            return md5ID;

        return newHashes.get(md5);
    }
}
