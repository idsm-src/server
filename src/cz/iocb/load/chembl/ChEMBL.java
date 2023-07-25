package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import cz.iocb.load.common.Updater;



public class ChEMBL extends Updater
{
    protected static String version = "33.0";


    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init();

            connection.createStatement().execute("alter schema chembl rename to chembl_old");

            Document.load();
            Journal.load();
            MoleculeReference.load();
            Target.load();
            TargetComponent.load();
            TargetComponentReference.load();

            try(Statement statement = connection.createStatement())
            {
                try(ResultSet result = statement.executeQuery("select count(*) from chembl_tmp.molecule_dictionary"))
                {
                    if(result.next())
                        setCount("ChEMBL Substances", result.getInt(1));
                }

                try(ResultSet result = statement.executeQuery("select count(*) from chembl_tmp.assays"))
                {
                    if(result.next())
                        setCount("ChEMBL Assays", result.getInt(1));
                }
            }

            setVersion("ChEMBL", version);

            connection.createStatement().execute("alter schema chembl_tmp rename to chembl");

            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
