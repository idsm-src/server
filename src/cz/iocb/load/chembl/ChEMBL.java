package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.Updater;



public class ChEMBL extends Updater
{
    public static void main(String[] args) throws IOException, SQLException
    {
        try
        {
            init();

            Document.load();
            Journal.load();
            MoleculeReference.load();
            Target.load();
            TargetComponent.load();
            TargetComponentReference.load();

            commit();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
