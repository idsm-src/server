package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class Reference extends Loader
{
    private static void loadTypes(String file, Map<String, Short> types) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into reference_bases(id, type) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
                setValue(2, getMapID(object, types));
            }
        }.load();
    }


    private static void loadDates(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update reference_bases set dcdate=cast(? as date) where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/date"))
                    throw new IOException();

                setValue(1, getString(object));
                setValue(2, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();
    }


    private static void loadCitations(String file, int limit) throws IOException, SQLException
    {
        LinkedHashMap<Integer, String> bigValues = new LinkedHashMap<Integer, String>();
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update reference_bases set citation=? where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/bibliographicCitation"))
                    throw new IOException();

                int reference = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID");
                String value = getString(object);

                if(limit > 0 && value.length() >= limit)
                {
                    bigValues.put(reference, value);
                }
                else
                {
                    setValue(1, value);
                    setValue(2, reference);
                }
            }
        }.load();


        if(bigValues.size() > 0)
        {
            try (Connection connection = Loader.getConnection())
            {
                try (PreparedStatement insertStatement = connection
                        .prepareStatement("insert into reference_citations_long(reference, citation) values (?,?)"))
                {
                    for(Entry<Integer, String> entry : bigValues.entrySet())
                    {
                        insertStatement.setInt(1, entry.getKey());
                        insertStatement.setString(2, entry.getValue());
                        insertStatement.addBatch();
                    }

                    insertStatement.executeBatch();
                }
            }
        }
    }


    private static void loadTitles(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "update reference_bases set title=? where id=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                    throw new IOException();

                setValue(1, getString(object));
                setValue(2, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));
            }
        }.load();
    }


    private static void loadChemicalDiseases(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into reference_discusses(reference, statement) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/spar/cito/discusses"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));

                if(object.getURI().charAt(27) == 'M')
                    setValue(2, getIntID(object, "http://id.nlm.nih.gov/mesh/M"));
                else
                    setValue(2, -getIntID(object, "http://id.nlm.nih.gov/mesh/C"));
            }
        }.load();
    }


    private static void loadMeshheadings(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into reference_subject_descriptors(reference, descriptor, qualifier) values (?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/spar/fabio/hasSubjectTerm"))
                    throw new IOException();

                setValue(1, getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/reference/PMID"));

                String value = getStringID(object, "http://id.nlm.nih.gov/mesh/D");
                int idx = value.indexOf('Q');

                if(idx == -1)
                {
                    setValue(2, Integer.parseInt(value));
                    setValue(3, -1);
                }
                else
                {
                    setValue(2, Integer.parseInt(value.substring(0, idx)));
                    setValue(3, Integer.parseInt(value.substring(idx + 1)));
                }
            }
        }.load();
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        Map<String, Short> types = getMapping("reference_types__reftable");
        loadTypes(path + File.separatorChar + "pc_reference_type.ttl.gz", types);

        File dir = new File(getPubchemDirectory() + path);

        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_reference_citation"))
                    loadCitations(path + File.separatorChar + name, 2048);
                else if(name.startsWith("pc_reference_date"))
                    loadDates(path + File.separatorChar + name);
                else if(name.startsWith("pc_reference_title"))
                    loadTitles(path + File.separatorChar + name);
                else if(name.startsWith("pc_reference2chemical_disease"))
                    loadChemicalDiseases(path + File.separatorChar + name);
                else if(name.startsWith("pc_reference2meshheading"))
                    loadMeshheadings(path + File.separatorChar + name);
                else if(!name.startsWith("pc_reference_type"))
                    System.out.println("unsupported " + path + File.separatorChar + name);
            }
            catch (IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }


    public static void main(String[] args) throws IOException, SQLException, ParseException
    {
        loadDirectory("RDF/reference");
    }
}
