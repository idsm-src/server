package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class InchiKey extends Loader
{
    private static void loadBases(String file, Set<String> idsSet, AtomicInteger nextID)
            throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into inchikey_bases(id, inchikey) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");

                if(!inchikey.equals(getString(object)))
                    throw new IOException();

                idsSet.add(inchikey);
                setValue(1, nextID.getAndIncrement());
                setValue(2, inchikey);
            }
        }.load();

        stream.close();
    }


    private static void loadCompounds(String file, Set<String> ids) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into inchikey_compounds(inchikey, compound) select id, ? from inchikey_bases where inchikey=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/is-attribute-of"))
                    throw new IOException();

                String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");

                if(!ids.contains(inchikey))
                    System.out.println("  missing inchikey " + inchikey + " for sio:is-attribute-of");

                setValue(1, getIntID(object, "http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID"));
                setValue(2, inchikey);
            }
        }.load();

        stream.close();
    }


    private static void loadSubjects(String file, Set<String> ids) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream,
                "insert into inchikey_subjects(inchikey, subject) select id, ? from inchikey_bases where inchikey=?")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://purl.org/dc/terms/subject"))
                    throw new IOException();

                String inchikey = getStringID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/");

                if(!ids.contains(inchikey))
                    System.out.println("  missing inchikey " + inchikey + " for dcterms:subject");

                setValue(1, getIntID(object, "http://id.nlm.nih.gov/mesh/M"));
                setValue(2, inchikey);
            }
        }.load();

        stream.close();
    }



    public static void loadDirectory(String path) throws IOException, SQLException
    {
        File dir = new File(getPubchemDirectory() + path);
        Set<String> idsSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(125000000));
        AtomicInteger nextID = new AtomicInteger();


        Arrays.asList(dir.listFiles()).stream().map(f -> f.getName())
                .filter(name -> name.startsWith("pc_inchikey_value")).parallel().forEach(name -> {
                    try
                    {
                        loadBases(path + File.separatorChar + name, idsSet, nextID);
                    }
                    catch (IOException | SQLException e)
                    {
                        System.err.println("exception for " + name);
                        e.printStackTrace();
                        System.exit(1);
                    }
                });


        Arrays.asList(dir.listFiles()).stream().map(f -> f.getName()).parallel().forEach(name -> {
            try
            {
                if(name.startsWith("pc_inchikey_topic"))
                    loadSubjects(path + File.separatorChar + name, idsSet);
                else if(name.startsWith("pc_inchikey2compound"))
                    loadCompounds(path + File.separatorChar + name, idsSet);
                else if(name.startsWith("pc_inchikey_type"))
                    System.out.println("ignore " + path + File.separator + name);
                else if(!name.startsWith("pc_inchikey_value"))
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
        loadDirectory("RDF/inchikey");
    }
}
