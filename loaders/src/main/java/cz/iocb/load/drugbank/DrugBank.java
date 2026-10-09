package cz.iocb.load.drugbank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
import cz.iocb.load.common.Updater;



public class DrugBank extends Updater
{
    /*
     * Loads the structures of the compounds from the SDF files of the open structures archive.
     */
    private static void loadCompounds() throws IOException, SQLException
    {
        StructureTable compounds = new StructureTable("drugbank.compound_bases", "id", "molfile");
        compounds.load();

        try(ZipInputStream zip = new ZipInputStream(getZipStream("drugbank/drugbank_all_open_structures.sdf.zip")))
        {
            ZipEntry entry;

            while((entry = zip.getNextEntry()) != null)
            {
                if(!entry.getName().endsWith(".sdf"))
                    continue;

                System.out.println("    " + entry.getName());
                BufferedReader reader = new BufferedReader(new InputStreamReader(zip, StandardCharsets.UTF_8));

                SdfReader.read(entry.getName(), reader, "DRUGBANK_ID", (id, molfile) -> {
                    if(!id.startsWith("DB"))
                        throw new IOException("unexpected compound " + id);

                    compounds.put(Integer.parseInt(id.substring(2)), molfile);
                });
            }
        }

        compounds.store();
    }


    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init();

            String version = getReader("drugbank/version.txt.gz").readLine();
            System.out.println("=== load drugbank version " + version + " ===");
            System.out.println();

            System.out.println("load compounds ...");
            loadCompounds();
            System.out.println();

            syncIndex("drugbank", true);

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from drugbank.compound_bases"))
                {
                    if(result.next())
                        setCount("DrugBank Compounds", result.getInt(1));
                }
            }

            setVersion("DrugBank Compounds", version);

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
