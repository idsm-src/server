package cz.iocb.load.pdb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.Updater;



public final class PDB extends Updater
{
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:ccd/*/*/*_model.sdf");


    public static void loadCompounds() throws IOException, SQLException
    {
        StringStringMap keepCompounds = new StringStringMap();
        StringStringMap newCompounds = new StringStringMap();
        StringStringMap oldCompounds = new StringStringMap();

        load("select name,molfile from pdb.compounds", oldCompounds);

        InputStream input = openFile("pdb/ccd.tar.gz");

        if(input == null)
            return;

        try(TarArchiveInputStream tar = new TarArchiveInputStream(
                new GzipCompressorInputStream(new BufferedInputStream(input))))
        {
            TarArchiveEntry entry;

            while((entry = tar.getNextEntry()) != null)
            {
                if(!tar.canReadEntryData(entry) || entry.isDirectory())
                    continue;

                String name = entry.getName();

                if(matcher.matches(Paths.get(name)))
                {
                    String molfile = new String(tar.readAllBytes(), StandardCharsets.UTF_8);
                    String id = name.replaceAll("ccd/[^/]*/([^/]*)/[^/]*_model.sdf", "$1");

                    if(molfile.equals(oldCompounds.remove(id)))
                    {
                        keepCompounds.put(id, molfile);
                    }
                    else
                    {
                        String keep = keepCompounds.get(id);

                        if(molfile.equals(keep))
                            continue;

                        if(keep != null)
                        {
                            Problems.error("multiple values of pdb.compounds.molfile", id);
                            continue;
                        }

                        String put = newCompounds.put(id, molfile);

                        if(put != null && !molfile.equals(put))
                            Problems.error("multiple values of pdb.compounds.molfile", id);
                    }
                }
            }

            if(keepCompounds.isEmpty() && newCompounds.isEmpty())
            {
                Problems.error("no structure", "pdb.compounds");
                return;
            }

            store("delete from pdb.compounds where name=? and molfile=?", oldCompounds);
            store("insert into pdb.compounds(name,molfile) values(?,?) "
                    + "on conflict(name) do update set molfile=EXCLUDED.molfile", newCompounds);
        }
    }


    public static void main(String[] args) throws Exception
    {
        try
        {
            init("pdb");

            String version = getReader("pdb/version.txt.gz").readLine();
            System.out.println("=== load pdb version " + version + " ===");
            System.out.println();

            loadCompounds();

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from pdb.compounds"))
                {
                    if(result.next())
                        setCount("PDB Chemical Components", result.getInt(1));
                }
            }

            setVersion("PDB Chemical Components (PDBeChem)", version);

            updateVersion();

            checkFiles("pdb");
            checkProblems();

            syncIndex("pdb", true);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
