package cz.iocb.pubchem.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import cz.iocb.pubchem.load.common.Loader;



public class CompoundSDF extends Loader
{
    private static void loadSDFile(String name) throws Exception
    {
        try(Connection connection = getConnection())
        {
            try(PreparedStatement insertStatement = connection
                    .prepareStatement("insert into compound_sdfiles (compound, sdf) values (?,?)"))
            {
                try(InputStream stream = getStream(name))
                {
                    Reader decoder = new InputStreamReader(stream, "US-ASCII");
                    BufferedReader reader = new BufferedReader(decoder);
                    String line;

                    while((line = reader.readLine()) != null)
                    {
                        int compoundID = -1;
                        StringBuilder sdfBuilder = new StringBuilder();


                        sdfBuilder.append('\n');
                        sdfBuilder.append(line);

                        line = reader.readLine();
                        sdfBuilder.append(line);
                        sdfBuilder.append('\n');


                        for(int i = 0; i < 2; i++)
                        {
                            line = reader.readLine();
                            sdfBuilder.append(line);
                            sdfBuilder.append('\n');
                        }

                        int atoms = Integer.parseInt(line.substring(0, 3).trim());
                        int bonds = Integer.parseInt(line.substring(3, 6).trim());


                        for(int i = 0; i < atoms; i++)
                        {
                            line = reader.readLine();
                            sdfBuilder.append(line);
                            sdfBuilder.append('\n');
                        }


                        for(int i = 0; i < bonds; i++)
                        {
                            line = reader.readLine();
                            sdfBuilder.append(line);
                            sdfBuilder.append('\n');
                        }

                        while((line = reader.readLine()) != null)
                        {
                            sdfBuilder.append(line);
                            sdfBuilder.append('\n');

                            if(line.compareTo("M  END") == 0)
                                break;
                        }

                        String sdf = sdfBuilder.toString();

                        while((line = reader.readLine()) != null)
                        {
                            if(line.equals("$$$$"))
                                break;

                            if(line.equals("> <PUBCHEM_COMPOUND_CID>"))
                            {
                                line = reader.readLine();
                                compoundID = Integer.parseInt(line);
                            }
                        }

                        insertStatement.setInt(1, compoundID);
                        insertStatement.setString(2, sdf);
                        insertStatement.addBatch();
                    }
                }

                insertStatement.executeBatch();
            }
        }
    }


    public static void loadDirectory(String path) throws SQLException, IOException
    {
        File dir = new File(getPubchemDirectory() + File.separatorChar + path);


        Arrays.asList(dir.listFiles()).parallelStream().map(f -> f.getName()).filter(n -> n.endsWith(".gz"))
                .forEach(name -> {
                    try
                    {
                        loadSDFile(path + File.separatorChar + name);
                    }
                    catch(Exception e)
                    {
                        System.err.println("exception for " + name);
                        e.printStackTrace();
                        System.exit(1);
                    }
                });
    }


    public static void main(String[] args) throws Exception
    {
        loadDirectory("SDF");
    }
}
