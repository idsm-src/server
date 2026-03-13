package cz.iocb.load.pdb;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import cz.iocb.load.common.Updater;



public final class PDB extends Updater
{
    private static final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:ccd/*/*/*_model.sdf");


    public static void loadCompounds() throws IOException, SQLException
    {
        StringStringMap keepCompounds = new StringStringMap();
        StringStringMap newCompounds = new StringStringMap();
        StringStringMap oldCompounds = new StringStringMap();

        load("select name,molfile from pdb.compound_bases", oldCompounds);

        try(TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(
                new BufferedInputStream(new FileInputStream(baseDirectory + "pdb/ccd.tar.gz")))))
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
                            return;
                        else if(keep != null)
                            throw new IOException();

                        String put = newCompounds.put(id, molfile);

                        if(put != null && !molfile.equals(put))
                            throw new IOException();
                    }
                }
            }

            store("delete from pdb.compound_bases where name=? and molfile=?", oldCompounds);
            store("insert into pdb.compound_bases(name,molfile) values(?,?) "
                    + "on conflict(name) do update set molfile=EXCLUDED.molfile", newCompounds);
        }
    }


    public static void main(String[] args) throws Exception
    {
        try
        {
            init();

            loadCompounds();

            try(Statement statement = connection.createStatement())
            {
                statement.execute("select sachem.cleanup('pdb')");
                statement.execute("select sachem.sync_data('pdb', false, true)");
            }

            updateVersion();
            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
