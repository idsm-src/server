package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntObjectPair;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class TargetComponent extends Updater
{
    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        IntStringPairIntMap newSynonyms = new IntStringPairIntMap(100000);
        IntStringPairIntMap oldSynonyms = getIntStringPairIntMap(
                "select component_id, component_synonym, compsyn_id from chembl.component_synonyms", 100000);

        new QueryResultProcessor(patternQuery("?component skos:altLabel ?synonym"))
        {
            int nextSynonymID = getIntValue("select coalesce(max(compsyn_id)+1,0) from chembl.component_synonyms");

            @Override
            public void parse() throws SQLException, IOException
            {
                int compID = getIntID("component", "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/CHEMBL_TC_");
                String synonym = getString("synonym");

                IntObjectPair<String> pair = PrimitiveTuples.pair(compID, synonym);

                if(oldSynonyms.removeKeyIfAbsent(pair, NO_VALUE) == NO_VALUE)
                    newSynonyms.put(pair, nextSynonymID++);
            }
        }.load(model);

        batch("delete from chembl.component_synonyms where compsyn_id = ?", oldSynonyms.values());
        batch("insert into chembl.component_synonyms(component_id, component_synonym, compsyn_id) values (?,?,?)",
                newSynonyms);
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_28.0_targetcmpt.ttl.gz");
    }
}
