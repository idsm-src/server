package cz.iocb.load.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import org.apache.jena.graph.Node;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.MD5;
import cz.iocb.load.common.TripleStreamProcessor;
import cz.iocb.load.common.Updater;



class Synonym extends Updater
{
    private static MD5IntMap usedHashes;
    private static int nextMd5ID;
    private static int nextValueID;


    static void loadBases() throws IOException, SQLException
    {
        usedHashes = new MD5IntMap(200000000);

        MD5IntMap newHashes = new MD5IntMap(200000000);
        MD5IntMap oldHashes = getMD5IntMap("select md5, id from synonym_bases", 200000000);
        nextMd5ID = getIntValue("select coalesce(max(id)+1,0) from synonym_bases");

        IntStringPairIntMap newValues = new IntStringPairIntMap(200000000);
        IntStringPairIntMap oldValues = getIntStringPairIntMap("select synonym, value, __ from synonym_values",
                200000000);
        nextValueID = getIntValue("select coalesce(max(__)+1,0) from synonym_values");

        processFiles("RDF/synonym", "pc_synonym_value_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                            throw new IOException();

                        String value = getString(object);
                        MD5 md5 = getSynonymMD5(subject);
                        int md5ID = Integer.MIN_VALUE;

                        synchronized(newHashes)
                        {
                            if((md5ID = oldHashes.removeKeyIfAbsent(md5, Integer.MIN_VALUE)) != Integer.MIN_VALUE)
                                usedHashes.put(md5, md5ID);
                            else if((md5ID = usedHashes.getIfAbsentPut(md5, nextMd5ID)) == nextMd5ID)
                                newHashes.put(md5, nextMd5ID++);
                        }

                        IntObjectPair<String> pair = PrimitiveTuples.pair(md5ID, value);

                        synchronized(newValues)
                        {
                            if(oldValues.removeKeyIfAbsent(pair, Integer.MIN_VALUE) == Integer.MIN_VALUE)
                                newValues.put(pair, nextValueID++);
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from synonym_bases where id = ?", oldHashes.values());
        batch("insert into synonym_bases(md5, id) values (?,?)", newHashes);

        batch("delete from synonym_values where __ = ?", oldValues.values());
        batch("insert into synonym_values(synonym, value, __) values (?,?,?)", newValues);
    }


    private static void loadTypes() throws IOException, SQLException
    {
        IntPairSet newTypes = new IntPairSet(200000000);
        IntPairSet oldTypes = getIntPairSet("select synonym, type_id from synonym_types", 200000000);

        processFiles("RDF/synonym", "pc_synonym_type_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                            throw new IOException();

                        MD5 md5 = getSynonymMD5(subject);
                        int md5ID = usedHashes.getIfAbsent(md5, Integer.MIN_VALUE);
                        int typeID = getIntID(object, "http://semanticscience.org/resource/CHEMINF_");

                        if(md5ID != Integer.MIN_VALUE)
                        {
                            IntIntPair pair = PrimitiveTuples.pair(md5ID, typeID);

                            synchronized(newTypes)
                            {
                                if(!oldTypes.remove(pair))
                                    newTypes.add(pair);
                            }
                        }
                        else
                        {
                            System.out.println("    ignore md5 synonym " + md5 + " for rdf:type");
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from synonym_types where synonym = ? and type_id = ?", oldTypes);
        batch("insert into synonym_types(synonym, type_id) values (?,?)", newTypes);
    }


    private static void loadCompounds() throws IOException, SQLException
    {
        IntPairSet newCompounds = new IntPairSet(200000000);
        IntPairSet oldCompounds = getIntPairSet("select synonym, compound from synonym_compounds", 200000000);

        processFiles("RDF/synonym", "pc_synonym2compound_[0-9]+\\.ttl\\.gz", file -> {
            try(InputStream stream = getStream(file))
            {
                new TripleStreamProcessor()
                {
                    @Override
                    protected void parse(Node subject, Node predicate, Node object) throws IOException
                    {
                        if(!predicate.getURI().equals("http://semanticscience.org/resource/is-attribute-of"))
                            throw new IOException();

                        MD5 md5 = getSynonymMD5(subject);
                        int md5ID = usedHashes.getIfAbsent(md5, Integer.MIN_VALUE);

                        if(md5ID != Integer.MIN_VALUE)
                        {
                            int compoundID = getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID");

                            IntIntPair pair = PrimitiveTuples.pair(md5ID, compoundID);
                            Compound.addCompoundID(compoundID);

                            synchronized(newCompounds)
                            {
                                if(!oldCompounds.remove(pair))
                                    newCompounds.add(pair);
                            }
                        }
                        else
                        {
                            System.out.println("    ignore md5 synonym " + md5 + " for sio:is-attribute-of");
                        }
                    }
                }.load(stream);
            }
        });

        batch("delete from synonym_compounds where synonym = ? and compound = ?", oldCompounds);
        batch("insert into synonym_compounds(synonym, compound) values (?,?)", newCompounds);
    }


    private static void loadTopics() throws IOException, SQLException
    {
        IntPairSet newConceptSubjects = new IntPairSet(50000);
        IntPairSet oldConceptSubjects = getIntPairSet("select synonym, concept from synonym_concept_subjects", 50000);

        IntStringPairSet newMeshSubjects = new IntStringPairSet(1000000);
        IntStringPairSet oldMeshSubjects = getIntStringPairSet("select synonym, subject from synonym_mesh_subjects",
                1000000);

        try(InputStream stream = getStream("RDF/synonym/pc_synonym_topic.ttl.gz"))
        {
            new TripleStreamProcessor()
            {
                @Override
                protected void parse(Node subject, Node predicate, Node object) throws IOException
                {
                    if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                        throw new IOException();

                    String value = object.getURI();

                    MD5 md5 = getSynonymMD5(subject);
                    int md5ID = usedHashes.getIfAbsent(md5, Integer.MIN_VALUE);

                    if(md5ID == Integer.MIN_VALUE)
                    {
                        System.out.println("    ignore md5 synonym " + md5 + " for dcterms:subject");
                    }
                    else if(value.startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"))
                    {
                        int conceptID = Concept.getConceptID(value);

                        IntIntPair pair = PrimitiveTuples.pair(md5ID, conceptID);

                        if(!oldConceptSubjects.remove(pair))
                            newConceptSubjects.add(pair);
                    }
                    else
                    {
                        String subjectID = getStringID(object, "http://id.nlm.nih.gov/mesh/");
                        IntObjectPair<String> pair = PrimitiveTuples.pair(md5ID, subjectID);

                        if(!oldMeshSubjects.remove(pair))
                            newMeshSubjects.add(pair);
                    }
                }
            }.load(stream);
        }

        batch("delete from synonym_concept_subjects where synonym = ? and concept = ?", oldConceptSubjects);
        batch("insert into synonym_concept_subjects(synonym, concept) values (?,?)", newConceptSubjects);

        batch("delete from synonym_mesh_subjects where synonym = ? and subject = ?", oldMeshSubjects);
        batch("insert into synonym_mesh_subjects(synonym, subject) values (?,?)", newMeshSubjects);
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


    public static int getSynonymID(MD5 md5)
    {
        return usedHashes.getIfAbsent(md5, Integer.MIN_VALUE);
    }
}
