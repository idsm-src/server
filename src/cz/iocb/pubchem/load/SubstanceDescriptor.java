package cz.iocb.pubchem.load;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
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
        File[] files = dir.listFiles();

        Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
        AtomicInteger counter = new AtomicInteger(-1);

        for(int i = 0; i < threads.length; i++)
        {
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        for(int i = counter.incrementAndGet(); i < files.length; i = counter.incrementAndGet())
                        {
                            String name = files[i].getName();
                            String loc = path + File.separatorChar + files[i].getName();

                            if(name.startsWith("pc_descr_SubstanceVersion_value"))
                                processVersionFile(loc);
                            else if(name.matches("pc_descr_.*_type_[0-9]+.ttl.gz"))
                                System.out.println("ignore " + loc);
                            else
                                System.out.println("unsupported " + loc);
                        }
                    }
                    catch (Throwable e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            };

            threads[i].start();
        }

        for(int i = 0; i < threads.length; i++)
            threads[i].join();
    }


    public static void main(String[] args) throws IOException, SQLException, InterruptedException
    {
        loadDirectory("RDF/descriptor/substance");
    }
}
