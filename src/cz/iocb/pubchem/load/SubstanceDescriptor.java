package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import org.apache.jena.graph.Node;
import cz.iocb.pubchem.load.common.Loader;
import cz.iocb.pubchem.load.common.StreamTableLoader;



public class SubstanceDescriptor extends Loader
{
    protected static void processVersionFile(String file) throws IOException, SQLException
    {
        InputStream stream = getStream(file);

        new StreamTableLoader(stream, "insert into descriptor_substance_bases(substance, version) values (?,?)")
        {
            @Override
            public void insert(Node subject, Node predicate, Node object) throws SQLException, IOException
            {
                if(!predicate.getURI().equals("http://semanticscience.org/resource/has-value"))
                    throw new IOException();

                int id = getIntID(subject, "http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/SID", "_Substance_Version");

                setValue(1, id);
                setValue(2, getInteger(object));
            }
        }.load();

        stream.close();
    }


    public static void loadDirectory(String path) throws IOException, SQLException, InterruptedException
    {
        File dir = new File(getPubchemDirectory() + path);

        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).forEach(name -> {
            try
            {
                if(name.startsWith("pc_descr_SubstanceVersion_value"))
                    processVersionFile(path + File.separatorChar + name);
                else if(name.matches("pc_descr_.*_type_[0-9]+.ttl.gz"))
                    System.out.println("ignore " + path + File.separatorChar + name);
                else
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


    public static void main(String[] args) throws IOException, SQLException, InterruptedException
    {
        loadDirectory("RDF/descriptor/substance");
    }
}
