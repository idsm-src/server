package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.jena.rdf.model.Model;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;
import cz.iocb.load.ontology.Ontology.Identifier;



class Bioassay extends Updater
{
    private static void loadStages(Model model) throws IOException, SQLException
    {
        IntIntHashMap newStages = new IntIntHashMap(100000);
        IntIntHashMap oldStages = getIntIntMap("select bioassay, stage from pubchem.bioassay_stages", 100000);

        new QueryResultProcessor(patternQuery("?bioassay bao:BAO_0000210 ?stage"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                Identifier stage = Ontology.getId(getIRI("stage"));

                if(stage.unit != Ontology.unitBAO)
                    throw new IOException();

                if(stage.id != oldStages.removeKeyIfAbsent(bioassayID, NO_VALUE))
                    newStages.put(bioassayID, stage.id);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_stages where bioassay = ?", oldStages.keySet());
        batch("insert into pubchem.bioassay_stages(bioassay, stage) values (?,?) "
                + "on conflict (bioassay) do update set stage=EXCLUDED.stage", newStages);
    }



    private static void loadConfirmatoryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet(100000);
        IntPairSet oldRelations = getIntPairSet(
                "select bioassay, confirmatory_assay from pubchem.bioassay_confirmatory_assays", 100000);

        new QueryResultProcessor(patternQuery("?confirmatory bao:BAO_0000540 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int confirmatoryID = getIntID("confirmatory", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, confirmatoryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_confirmatory_assays where bioassay = ? and confirmatory_assay = ?",
                oldRelations);
        batch("insert into pubchem.bioassay_confirmatory_assays(bioassay, confirmatory_assay) values (?,?)",
                newRelations);
    }


    private static void loadPrimaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet(20000);
        IntPairSet oldRelations = getIntPairSet("select bioassay, primary_assay from pubchem.bioassay_primary_assays",
                20000);

        new QueryResultProcessor(patternQuery("?primary bao:BAO_0001067 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int primaryID = getIntID("primary", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, primaryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_primary_assays where bioassay = ? and primary_assay = ?", oldRelations);
        batch("insert into pubchem.bioassay_primary_assays(bioassay, primary_assay) values (?,?)", newRelations);
    }


    private static void loadSummaryAssays(Model model) throws IOException, SQLException
    {
        IntPairSet newRelations = new IntPairSet(20000);
        IntPairSet oldRelations = getIntPairSet("select bioassay, summary_assay from pubchem.bioassay_summary_assays",
                20000);

        new QueryResultProcessor(patternQuery("?summary bao:BAO_0001094 ?bioassay"))
        {
            @Override
            protected void parse() throws IOException
            {
                int bioassayID = getIntID("bioassay", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");
                int summaryID = getIntID("summary", "http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/AID");

                IntIntPair pair = PrimitiveTuples.pair(bioassayID, summaryID);

                if(!oldRelations.remove(pair))
                    newRelations.add(pair);
            }
        }.load(model);

        batch("delete from pubchem.bioassay_summary_assays where bioassay = ? and summary_assay = ?", oldRelations);
        batch("insert into pubchem.bioassay_summary_assays(bioassay, summary_assay) values (?,?)", newRelations);
    }


    static void load() throws IOException, SQLException
    {
        System.out.println("load bioassay ...");

        Model model = getModel("pubchem/RDF/bioassay/pc_bioassay.ttl.gz");
        check(model, "pubchem/bioassay/check.sparql");

        loadStages(model);
        loadConfirmatoryAssays(model);
        loadPrimaryAssays(model);
        loadSummaryAssays(model);

        model.close();
        System.out.println();
    }
}
