package cz.iocb.pubchem.load;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;



public class SDFiles
{
    private static class DataItem
    {
        int compoundID;
        byte[] data;

        public DataItem(int compoundID, byte[] data)
        {
            this.compoundID = compoundID;
            this.data = data;
        }
    }


    private static VirtuosoConnectionPoolDataSource connectionPool;
    private static HashMap<Integer, List<DataItem>> waiting = new HashMap<Integer, List<DataItem>>();

    private static long compressedSize = 0;
    private static long uncompressedSize = 0;
    private static int nextValue = 0;
    private static int done = 0;


    static synchronized int next()
    {
        return nextValue++;
    }


    static synchronized Connection getConnection() throws SQLException
    {
        return connectionPool.getConnection();
    }


    static synchronized void addStat(long compressed, long uncompressed)
    {
        compressedSize += compressed;
        uncompressedSize += uncompressed;
    }


    private static synchronized void resultAdd(int i, List<DataItem> result) throws SQLException
    {
        if(i > done)
        {
            waiting.put(i, result);
            return;
        }


        store(done, result);

        while(true)
        {
            List<DataItem> res = waiting.get(++done);

            if(res == null)
                return;

            waiting.remove(done);
            store(done, res);
        }
    }


    private static void store(int done, List<DataItem> items) throws SQLException
    {
        try (Connection conn = getConnection())
        {
            try (PreparedStatement insertFp = conn
                    .prepareStatement("insert into compound_sdfiles_gz (compound, sdf_gz) values (?,?)"))
            {
                for(DataItem item : items)
                {
                    insertFp.setInt(1, item.compoundID);
                    insertFp.setBytes(2, item.data);
                    insertFp.addBatch();
                }

                insertFp.executeBatch();
            }
        }

        System.out.println("store: " + done + ": " + 10 * compressedSize / (1024 * 1024) / 10.0 + "MB / "
                + 10 * uncompressedSize / (1024 * 1024) / 10.0 + "MB = "
                + 1000 * compressedSize / uncompressedSize / 10.0 + "%");
    }


    private static List<DataItem> parse(InputStream inputStream, String ID) throws Exception
    {
        ArrayList<DataItem> items = new ArrayList<DataItem>();
        long localCompressedSize = 0;
        long localUncompressedSize = 0;


        Reader decoder = new InputStreamReader(inputStream, "US-ASCII");
        BufferedReader reader = new BufferedReader(decoder);

        String line;


        while((line = reader.readLine()) != null)
        {
            int compoundID = -1;
            StringBuilder sdfBuilder = new StringBuilder();


            sdfBuilder.append('\n');
            sdfBuilder.append(line);


            if(ID == null)
                compoundID = Integer.parseInt(line);

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


            Deflater deflater = new Deflater();
            deflater.setLevel(9);
            deflater.setInput(sdf.getBytes());
            deflater.finish();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];

            while(!deflater.finished())
            {
                int count = deflater.deflate(buffer); // returns the generated code... index
                outputStream.write(buffer, 0, count);
            }

            deflater.end();
            outputStream.close();


            byte[] data = outputStream.toByteArray();

            localCompressedSize += data.length;
            localUncompressedSize += sdf.length();


            while((line = reader.readLine()) != null)
            {
                if(line.compareTo("$$$$") == 0)
                    break;

                if(ID != null && line.compareTo(ID) == 0)
                {
                    line = reader.readLine();
                    compoundID = Integer.parseInt(line);
                }
            }


            items.add(new DataItem(compoundID, data));
        }

        addStat(localCompressedSize, localUncompressedSize);
        return items;
    }


    public static void main(String[] args) throws Exception
    {
        int threadCount = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
        int port = Integer.parseInt(args[0]);
        String user = args[1];
        String password = args[2];
        String directoryName = args[3];

        connectionPool = new VirtuosoConnectionPoolDataSource();
        connectionPool.setCharset("UTF-8");
        connectionPool.setInitialPoolSize(1);
        connectionPool.setMaxPoolSize(1);
        connectionPool.setPortNumber(port);
        connectionPool.setUser(user);
        connectionPool.setPassword(password);


        File directory = new File(directoryName);
        final File[] files = directory.listFiles();

        Arrays.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File arg0, File arg1)
            {
                return arg0.getName().compareTo(arg1.getName());
            }
        });;


        Thread[] threads = new Thread[threadCount];

        for(int i = 0; i < threadCount; i++)
        {
            threads[i] = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        for(int i = next(); i < files.length; i = next())
                        {
                            File file = files[i];

                            if(!file.getName().endsWith(".gz"))
                                continue;

                            System.out.println(i + ": " + file.getName());

                            InputStream fileStream = new FileInputStream(directoryName + file.getName());
                            InputStream gzipStream = new GZIPInputStream(fileStream);

                            List<DataItem> items = parse(gzipStream, "> <PUBCHEM_COMPOUND_CID>");

                            /*
                            items.sort(new Comparator<DataItem>(){
                                @Override
                                public int compare(DataItem o1, DataItem o2)
                                {
                                    return Integer.compare(o1.compoundID, o2.compoundID);
                                }});
                            */

                            gzipStream.close();

                            resultAdd(i, items);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            threads[i].start();
        }

        for(int i = 0; i < threadCount; i++)
            threads[i].join();


        System.out.println(uncompressedSize);
        System.out.println(compressedSize);
        System.out.println(100.0 * compressedSize / uncompressedSize);
    }
}
