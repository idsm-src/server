package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class Journal extends Updater
{
    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl.journal_dictionary(id, label, title, short_title, issn, eissn) values (?,?,?,?,?,?)"))
        {
            // @formatter:off
            new QueryResultProcessor(patternQuery("?journal rdf:type cco:Journal. "
                    + "optional { ?journal rdfs:label ?label }"
                    + "optional { ?journal dcterms:title ?title }"
                    + "optional { ?journal bibo:shortTitle ?shortTitle }"
                    + "optional { ?journal bibo:issn ?issn }"
                    + "optional { ?journal bibo:eissn ?eissn }"))
            // @formatter:on
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1,
                            getIntID("journal", "http://rdf.ebi.ac.uk/resource/chembl/journal/CHEMBL_JRN_"));
                    statement.setString(2, getString("label"));
                    statement.setString(3, getString("title"));
                    statement.setString(4, getString("shortTitle"));
                    statement.setString(5, getString("issn"));
                    statement.setString(6, getString("eissn"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }

        model.close();
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_29.0_journal.ttl.gz");
    }
}
