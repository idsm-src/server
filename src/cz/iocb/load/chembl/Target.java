package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Target extends Updater
{
    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection
                .prepareStatement("update chembl_32.target_dictionary set cell_line_id = ? where id = ?"))
        {
            new QueryResultProcessor(patternQuery("?target cco:isTargetForCellLine ?line"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1, getIntID("line", "http://rdf.ebi.ac.uk/resource/chembl/cell_line/CHEMBL"));
                    statement.setInt(2, getIntID("target", "http://rdf.ebi.ac.uk/resource/chembl/target/CHEMBL"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_32.0_target.ttl.gz");
    }
}
