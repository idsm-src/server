package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.Pair;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class TargetComponent extends Updater
{
    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        IntStringPairIntMap newSynonyms = new IntStringPairIntMap();
        IntStringPairIntMap oldSynonyms = new IntStringPairIntMap();

        load("select component_id,component_synonym,compsyn_id from chembl_tmp.component_synonyms", oldSynonyms);

        new QueryResultProcessor(patternQuery("?component skos:altLabel ?synonym"))
        {
            int nextSynonymID = oldSynonyms.values().stream().max(Integer::compare).orElse(-1).intValue() + 1;

            @Override
            public void parse() throws SQLException, IOException
            {
                Integer compID = getIntID("component",
                        "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/CHEMBL_TC_");
                String synonym = getString("synonym");

                Pair<Integer, String> pair = Pair.getPair(compID, synonym);

                if(oldSynonyms.remove(pair) == null)
                    newSynonyms.put(pair, nextSynonymID++);
            }
        }.load(model);

        store("delete from chembl_tmp.component_synonyms where component_id=? and component_synonym=? and compsyn_id=?",
                oldSynonyms);
        store("insert into chembl_tmp.component_synonyms(component_id,component_synonym,compsyn_id) values(?,?,?)",
                newSynonyms);
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_" + ChEMBL.version + "_targetcmpt.ttl.gz");
    }
}
