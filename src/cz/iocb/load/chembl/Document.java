package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Document extends Updater
{
    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection
                .prepareStatement("update chembl_32.docs set journal_id = ? where chembl_id = ?"))
        {
            new QueryResultProcessor(patternQuery("?document cco:hasJournal ?journal"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    if(!getIRI("journal").equals("http://rdf.ebi.ac.uk/resource/chembl/journal/CHEMBL_JRN_null"))
                    {
                        statement.setInt(1,
                                getIntID("journal", "http://rdf.ebi.ac.uk/resource/chembl/journal/CHEMBL_JRN_"));
                        statement.setString(2,
                                getStringID("document", "http://rdf.ebi.ac.uk/resource/chembl/document/"));
                        statement.addBatch();
                    }
                }
            }.load(model);

            statement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_32.0_document.ttl.gz");
    }
}
