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
import cz.iocb.load.common.DataException;
import cz.iocb.load.common.Problems;
import cz.iocb.load.common.SdfReader;
import cz.iocb.load.common.StructureTable;
import cz.iocb.load.common.Updater;



/**
 * Loader of the structures of the DrugBank compounds from the open structures archive into the table
 * {@code drugbank.compounds}.
 */
public class DrugBank extends Updater
{
    /**
     * Not instantiable.
     */
    private DrugBank()
    {
    }


    /**
     * Loads the structures of the compounds from the SDF files of the open structures archive; another entry of the
     * archive is reported as an unknown file.
     *
     * @throws IOException if the archive cannot be read
     * @throws SQLException if a database error occurs
     */
    private static void loadCompounds() throws IOException, SQLException
    {
        String file = "drugbank/drugbank_all_open_structures.sdf.zip";

        StructureTable compounds = new StructureTable("drugbank.compounds", "id", "molfile");
        compounds.load();

        try(ZipInputStream zip = new ZipInputStream(getZipStream(file)))
        {
            ZipEntry entry;

            while((entry = zip.getNextEntry()) != null)
            {
                if(!entry.getName().endsWith(".sdf"))
                {
                    Problems.error("unknown file", file + "/" + entry.getName());
                    continue;
                }

                System.out.println("    " + entry.getName());
                BufferedReader reader = new BufferedReader(new InputStreamReader(zip, StandardCharsets.UTF_8));

                SdfReader.read(entry.getName(), reader, "DRUGBANK_ID", (id, molfile) -> {
                    if(!id.startsWith("DB"))
                        throw new DataException("unexpected compound", id);

                    compounds.put(Integer.parseInt(id.substring(2)), molfile);
                });
            }
        }

        compounds.store();
    }


    /**
     * Runs the loader.
     *
     * @param args the command-line arguments (ignored)
     * @throws IOException if an I/O error occurs
     * @throws SQLException if a database error occurs
     */
    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init("drugbank");

            String version = getReader("drugbank/version.txt.gz").readLine();
            System.out.println("=== load drugbank version " + version + " ===");
            System.out.println();

            System.out.println("load compounds ...");
            loadCompounds();
            System.out.println();

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from drugbank.compounds"))
                {
                    if(result.next())
                        setCount("DrugBank Compounds", result.getInt(1));
                }
            }

            setVersion("DrugBank Compounds", version);

            updateVersion();

            checkFiles("drugbank");
            checkProblems();

            syncIndex("drugbank", true);
            commit();
        }
        catch(Throwable e)
        {
            fail(e);
        }
    }
}
