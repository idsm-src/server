package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class Measuregroup extends Loader
{
    static private abstract class MeasuregroupStreamTableLoader extends StreamTableLoader
    {
        static final String prefix = "http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID";
        static private Map<String, Short> outcomes;

        static private synchronized Map<String, Short> getOutcomes() throws SQLException, IOException
        {
            if(outcomes == null)
                outcomes = getMapping("endpoint_outcomes__reftable");

            return outcomes;
        }

        public MeasuregroupStreamTableLoader(InputStream stream, String sql)
        {
            super(stream, sql);
        }

        void setIDValues(int idx, Node node) throws IOException, SQLException
        {
            String iri = node.getURI();
            int bioassay;
            int measuregroup;

            if(!iri.startsWith(prefix))
                throw new IOException();

            int grp = iri.indexOf("_", prefix.length() + 1);

            if(grp != -1 && iri.indexOf("_PMID") == grp)
            {
                String part = iri.substring(grp + 5);
                bioassay = Integer.parseInt(iri.substring(prefix.length(), grp));

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
            else if(grp != -1)
            {
                bioassay = Integer.parseInt(iri.substring(prefix.length(), grp));
                measuregroup = Integer.parseInt(iri.substring(grp + 1));

                if(measuregroup == 2147483647)
                    throw new IOException();
            }
            else
            {
                bioassay = Integer.parseInt(iri.substring(prefix.length()));
                measuregroup = 2147483647; // magic number
            }

            setValue(idx + 0, bioassay);
            setValue(idx + 1, measuregroup);
        }

        @Override
        public void insertStub(Node subject, Node predicate, Node object) throws SQLException, IOException
        {
            String iri = subject.getURI();

            if(!iri.equals("http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/AID493040"))
            {
                super.insertStub(subject, predicate, object);
            }
            else
            {
                // workaround
                for(int id : getOutcomes().values())
                {
                    Node fakeSubject = NodeFactory.createURI(iri + "_" + id);
                    super.insertStub(fakeSubject, predicate, object);
                }
            }
        }
    }


    private static void loadTypes(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new MeasuregroupStreamTableLoader(stream, "insert into measuregroup_bases(bioassay, measuregroup) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
                    throw new IOException();

                if(!object.getURI().equals("http://www.bioassayontology.org/bao#BAO_0000040"))
                    throw new IOException();

                setIDValues(1, subject);
            }
        }.load();
    }


    private static void loadSources(String file) throws IOException, SQLException
    {
        Map<String, Short> sources = getMapping("source_bases");
        List<String> newSources = new ArrayList<String>();
        int newSourceOffset = sources.size();

        InputStream stream = getStream(file);

        new MeasuregroupStreamTableLoader(stream,
                "update measuregroup_bases set source=? where bioassay=? and measuregroup=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/source"))
                    throw new IOException();

                String iri = object.getURI();

                if(sources.get(iri) == null)
                {
                    sources.put(iri, (short) sources.size());
                    newSources.add(iri);
                }

                setValue(1, getMapID(object, sources));
                setIDValues(2, subject);
            }
        }.load();

        stream.close();


        try(Connection connection = getConnection())
        {
            try(PreparedStatement insertStatement = connection
                    .prepareStatement("insert into source_bases (id, iri, title) values (?,?,?)"))
            {
                for(int i = 0; i < newSources.size(); i++)
                {
                    short sourceID = (short) (newSourceOffset + i);
                    String sourceIri = newSources.get(i);
                    String sourceTitle = generateSourceTitle(sourceIri);

                    System.out.println("  add missing source: " + sourceIri + " (" + sourceTitle + ")");

                    insertStatement.setShort(1, sourceID);
                    insertStatement.setString(2, sourceIri);
                    insertStatement.setString(3, sourceTitle);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    private static void loadTitles(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new MeasuregroupStreamTableLoader(stream,
                "update measuregroup_bases set title=? where bioassay=? and measuregroup=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/title"))
                    throw new IOException();

                setValue(1, getString(object));
                setIDValues(2, subject);
            }
        }.load();
    }


    private static void loadProteins(String file) throws IOException, SQLException
    {
        Map<String, Integer> proteinTable = new HashMap<String, Integer>();

        try(Connection connection = getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement("select id, name from protein_bases"))
            {
                try(java.sql.ResultSet result = statement.executeQuery())
                {
                    while(result.next())
                        proteinTable.put(result.getString(2), result.getInt(1));
                }
            }
        }

        ArrayList<String> newProteins = new ArrayList<String>();
        int newProteinOffset = proteinTable.size();


        InputStream stream = getStream(file);

        new MeasuregroupStreamTableLoader(stream,
                "insert into measuregroup_proteins(bioassay, measuregroup, protein) values (?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/BFO_0000057"))
                    throw new IOException();

                if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"))
                    return;

                String proteinName = getStringID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/protein/");
                Integer proteinID = proteinTable.get(proteinName);

                if(proteinID == null)
                {
                    proteinID = proteinTable.size();
                    proteinTable.put(proteinName, proteinID);
                    newProteins.add(proteinName);
                }

                setIDValues(1, subject);
                setValue(3, proteinID);
            }
        }.load();

        stream.close();


        try(Connection connection = getConnection())
        {
            try(PreparedStatement insertStatement = connection
                    .prepareStatement("insert into protein_bases (id, name) values (?,?)"))
            {
                for(int i = 0; i < newProteins.size(); i++)
                {
                    int proteinID = newProteinOffset + i;
                    String proteinName = newProteins.get(i);

                    System.out.println("  add missing protein: " + proteinName);

                    insertStatement.setInt(1, proteinID);
                    insertStatement.setString(2, proteinName);

                    insertStatement.addBatch();
                }

                insertStatement.executeBatch();
            }
        }
    }


    private static void loadGenes(String file) throws SQLException, IOException
    {
        InputStream stream = getStream(file);

        new MeasuregroupStreamTableLoader(stream,
                "insert into measuregroup_genes(bioassay, measuregroup, gene) values (?,?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.obolibrary.org/obo/BFO_0000057"))
                    throw new IOException();

                if(object.getURI().startsWith("http://rdf.ncbi.nlm.nih.gov/pubchem/protein/GI"))
                    return;

                setIDValues(1, subject);
                setValue(3, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/gene/GID"));
            }
        }.load();

        stream.close();
    }


    private static void loadGenesAndProteins(String file) throws SQLException, IOException
    {
        loadGenes(file);
        loadProteins(file);
    }


    public static void loadDirectory(String path) throws IOException, SQLException
    {
        loadTypes(path + File.separatorChar + "pc_measuregroup_type.ttl.gz");

        File dir = new File(getPubchemDirectory() + path);

        Arrays.asList(dir.listFiles()).stream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.equals("pc_measuregroup_source.ttl.gz"))
                    loadSources(path + File.separatorChar + "pc_measuregroup_source.ttl.gz");
                else if(name.equals("pc_measuregroup_title.ttl.gz"))
                    loadTitles(path + File.separatorChar + "pc_measuregroup_title.ttl.gz");
                else if(name.equals("pc_measuregroup2protein.ttl.gz"))
                    loadGenesAndProteins(path + File.separatorChar + "pc_measuregroup2protein.ttl.gz");
                else if(name.startsWith("pc_measuregroup2endpoint"))
                    System.out.println("ignore " + path + File.separatorChar + name);
                else if(!name.equals("pc_measuregroup_type.ttl.gz"))
                    System.out.println("unsupported " + path + File.separatorChar + name);
            }
            catch(IOException | SQLException e)
            {
                System.err.println("exception for " + name);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        loadDirectory("RDF/measuregroup");
    }
}
