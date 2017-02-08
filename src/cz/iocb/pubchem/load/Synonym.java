package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class Synonym extends Loader
{
    static private void loadValues(String file, Map<String, Integer> md5hashes, AtomicInteger nextID)
            throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into synonym_values(__, synonym, value) values (?,?,?)")
        {
            LinkedHashMap<Integer, String> newMd5Hashes = new LinkedHashMap<Integer, String>(2 * Loader.batchSize);

            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                String md5 = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_");
                Integer md5ID = md5hashes.get(md5);

                if(md5ID == null)
                {
                    synchronized(md5hashes)
                    {
                        md5ID = md5hashes.get(md5);

                        if(md5ID == null)
                        {
                            md5ID = md5hashes.size();
                            md5hashes.put(md5, md5ID);
                            newMd5Hashes.put(md5ID, md5);
                        }
                    }
                }

                setValue(1, nextID.getAndIncrement());
                setValue(2, md5ID);
                setValue(3, getString(object));
            }

            @Override
            public void beforeBatch() throws SQLException, IOException
            {
                try (Connection connection = getConnection())
                {
                    try (PreparedStatement insertStatement = connection
                            .prepareStatement("insert into synonym_bases (id, md5) values (?,?)"))
                    {
                        for(Entry<Integer, String> entry : newMd5Hashes.entrySet())
                        {
                            insertStatement.setInt(1, entry.getKey());
                            insertStatement.setString(2, entry.getValue());
                            insertStatement.addBatch();
                        }

                        insertStatement.executeBatch();
                        newMd5Hashes.clear();
                    }
                }
            }
        }.load();

        stream.close();
    }


    static private void loadTypes(String file, Map<String, Integer> md5hashes) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into synonym_types(synonym, type) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                String md5 = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_");
                Integer md5ID = md5hashes.get(md5);

                if(md5ID != null)
                {
                    setValue(1, md5ID);
                    setValue(2, getIntID(object, "http://semanticscience.org/resource/CHEMINF_"));
                }
                else
                {
                    System.out.println("  ignore md5 synonym " + md5 + " for rdf:type");
                }
            }
        }.load();

        stream.close();
    }


    static private void loadCompounds(String file, Map<String, Integer> md5hashes) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into synonym_compounds(synonym, compound) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/is-attribute-of"))
                    throw new IOException();

                String md5 = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_");
                Integer md5ID = md5hashes.get(md5);

                if(md5ID != null)
                {
                    setValue(1, md5ID);
                    setValue(2, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                }
                else
                {
                    System.out.println("  ignore md5 synonym " + md5 + " for sio:is-attribute-of");
                }
            }
        }.load();

        stream.close();
    }


    static private void loadTopics(String file, Map<String, Integer> md5hashes) throws IOException, SQLException
    {
        loadMeshSubjects(file, md5hashes);
        loadConceptSubjects(file, md5hashes);
    }


    static private void loadMeshSubjects(String file, Map<String, Integer> md5hashes) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into synonym_mesh_subjects(synonym, subject) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                    throw new IOException();

                if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/concept/"))
                    return;

                String md5 = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_");
                Integer md5ID = md5hashes.get(md5);

                if(md5ID != null)
                {
                    setValue(1, md5ID);
                    setValue(2, getIntID(object, "http://id.nlm.nih.gov/mesh/M"));
                }
                else
                {
                    System.out.println("  ignore md5 synonym " + md5 + " for mesh dcterms:subject");
                }
            }
        }.load();

        stream.close();
    }


    static synchronized private void loadConceptSubjects(String file, Map<String, Integer> md5hashes)
            throws IOException, SQLException
    {
        Map<String, Short> concepts = getMapping("concept_bases");
        ArrayList<String> newConcepts = new ArrayList<String>();
        int newConceptOffset = concepts.size();


        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into synonym_concept_subjects(synonym, concept) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                    throw new IOException();

                String value = object.getURI();

                if(value.startsWith("http://id.nlm.nih.gov/mesh/M"))
                    return;

                String md5 = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/MD5_");
                Integer md5ID = md5hashes.get(md5);

                if(md5ID != null)
                {

                    Short conceptID = concepts.get(value);

                    if(conceptID == null)
                    {
                        conceptID = (short) concepts.size();
                        concepts.put(value, conceptID);
                        newConcepts.add(value);
                    }

                    setValue(1, md5ID);
                    setValue(2, conceptID);
                }
                else
                {
                    System.out.println("  ignore md5 synonym " + md5 + " for concept dcterms:subject");
                }
            }
        }.load();

        stream.close();


        try (Connection connection = getConnection())
        {
            try (PreparedStatement insertStatement = connection
                    .prepareStatement("insert into concept_bases (id, iri) values (?,?)"))
            {
                for(int i = 0; i < newConcepts.size(); i++)
                {
                    short conceptID = (short) (newConceptOffset + i);
                    String conceptIri = newConcepts.get(i);

                    System.out.println("  add missing concept: " + conceptIri);

                    insertStatement.setShort(1, conceptID);
                    insertStatement.setString(2, conceptIri);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);
        List<File> files = Arrays.asList(dir.listFiles());


        HashMap<String, Integer> md5hashes = new HashMap<String, Integer>(250000000);
        Map<String, Integer> md5SynHashes = Collections.synchronizedMap(md5hashes);
        AtomicInteger valueID = new AtomicInteger();

        // workaround: parallel stream cannot be used due to a virtuoso/jdbc issue
        files.parallelStream().map(f -> f.getName()).filter(n -> n.startsWith("pc_synonym_value")).forEach(name -> {
            try
            {
                loadValues(path + File.separatorChar + name, md5SynHashes, valueID);
            }
            catch (IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });


        files.parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_synonym_type"))
                    loadTypes(path + File.separatorChar + name, md5hashes);
                else if(name.startsWith("pc_synonym2compound"))
                    loadCompounds(path + File.separatorChar + name, md5hashes);
                else if(name.startsWith("pc_synonym_topic"))
                    loadTopics(path + File.separatorChar + name, md5hashes);
                else if(!name.startsWith("pc_synonym_value"))
                    System.out.println("unsupported " + path + File.separator + name);
            }
            catch (IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/synonym");
    }
}
