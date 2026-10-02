package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class MoleculeReference extends Updater
{
    private static class Description
    {
        final String name;
        final String prefix;
        final String pattern;

        Description(String name, String prefix, String pattern)
        {
            this.name = name;
            this.prefix = prefix;
            this.pattern = pattern;
        }
    }


    private static HashMap<String, Description> descriptions = new HashMap<>();
    private static int id = 0;


    private static Description rheaChebi = new Description("RHEA CHEBI", "https://www.rhea-db.org/rhea?query=CHEBI%3A",
            "https://www\\.rhea-db\\.org/rhea\\?query=(CHEBI|POLYMER)%3A[1-9][0-9]*");

    private static Description rheaPolymer = new Description("RHEA POLYMER",
            "https://www.rhea-db.org/rhea?query=POLYMER%3A",
            "https://www\\.rhea-db\\.org/rhea\\?query=(CHEBI|POLYMER)%3A[1-9][0-9]*");

    static
    {
        descriptions.put("BindingDbRef", new Description("BINDING DB",
                "https://www.bindingdb.org/rwd/bind/chemsearch/marvin/SDFdownload.jsp?download_file=/rwd/bind/downloads/BindingDB_All_202601_tsv.zip",
                "https://www\\.bindingdb\\.org/rwd/bind/chemsearch/marvin/SDFdownload\\.jsp\\?download_file=/rwd/bind/downloads/BindingDB_All_202601_tsv\\.zip[1-9][0-9]*"));
        descriptions.put("BrendaRef",
                new Description("BRENDA", "https://www.brenda-enzymes.org/ligand.php?brenda_ligand_id=",
                        "https://www\\.brenda-enzymes\\.org/ligand\\.php\\?brenda_ligand_id=[1-9][0-9]*"));
        descriptions.put("CcdcRef", new Description("CCDC",
                "https://www.ccdc.cam.ac.uk/structures/search?sid=UNICHEM&pid=csd:",
                "https://www\\.ccdc\\.cam\\.ac\\.uk/structures/search\\?sid=UNICHEM&pid=csd:[A-Z]{6}(-UNICHEM-[1-9][0-9]*)?"));
        descriptions.put("ChebiRef", new Description("CHEBI", "https://www.ebi.ac.uk/chebi/CHEBI%3A",
                "https://www\\.ebi\\.ac\\.uk/chebi/CHEBI%3A[1-9][0-9]*"));
        descriptions.put("ClinicalTrialsRef", new Description("CLINICAL TRIALS", "https://clinicaltrials.gov/study/NCT",
                "https://clinicaltrials\\.gov/study/NCT[0-9]{8}(-UNICHEM-[1-9][0-9]*)?"));
        descriptions.put("CompToxRef",
                new Description("COMPTOX", "https://comptox.epa.gov/dashboard/chemical/details/DTXSID",
                        "https://comptox\\.epa\\.gov/dashboard/chemical/details/DTXSID[0-9]+"));
        descriptions.put("DrugCentralRef", new Description("DRUG CENTRAL", "https://drugcentral.org/drugcard/",
                "https://drugcentral\\.org/drugcard/[A-Za-z0-9+%-]+"));
        descriptions.put("DrugbankRef", new Description("DRUGBANK", "https://go.drugbank.com/drugs/DB",
                "https://go\\.drugbank\\.com/drugs/DB[0-9]{5}"));
        descriptions.put("FdaSrsRef",
                new Description("FDA SRS", "https://d20b1koi85gdl2.cloudfront.net/uniisearch/srs/unii/",
                        "https://d20b1koi85gdl2\\.cloudfront\\.net/uniisearch/srs/unii/[A-Z0-9]{10}"));
        descriptions.put("FooDbRef", new Description("FOO DB", "https://foodb.ca/compounds/FDB",
                "https://foodb\\.ca/compounds/FDB[0-9]{6}"));
        descriptions.put("HmdbRef", new Description("HMDB", "https://www.hmdb.ca/metabolites/HMDB",
                "https://www\\.hmdb\\.ca/metabolites/HMDB[0-9]{7}"));
        descriptions.put("IupharRef",
                new Description("IUPHAR", "https://www.guidetopharmacology.org/GRAC/LigandDisplayForward?ligandId=",
                        "https://www\\.guidetopharmacology\\.org/GRAC/LigandDisplayForward\\?ligandId=[1-9][0-9]*"));
        descriptions.put("LipidMapsRef",
                new Description("LIPID MAPS", "https://www.lipidmaps.org/data/LMSDRecord.php?LMID=LM",
                        "https://www\\.lipidmaps\\.org/data/LMSDRecord\\.php\\?LMID=LM[A-Z0-9]+"));
        descriptions.put("MolportRef", new Description("MOLPORT", "https://www.molport.com/shop/compound/Molport-",
                "https://www\\.molport\\.com/shop/compound/Molport(-[0-9]{3}){3}"));
        descriptions.put("NmrShiftDb2Ref",
                new Description("NMR SHIFT DB2", "https://nmrshiftdb.nmr.uni-koeln.de/molecule/",
                        "https://nmrshiftdb\\.nmr\\.uni-koeln\\.de/molecule/[1-9][0-9]*"));
        descriptions.put("PdbeRef", new Description("PDBE",
                "https://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/",
                "https://www\\.ebi\\.ac\\.uk/pdbe-srv/pdbechem/chemicalCompound/show/[A-Z0-9_]*\\+-\\+Ideal\\+conformer"));
        descriptions.put("ProbesAndDrugsRef", new Description("PROBES AND DRUGS",
                "https://www.probes-drugs.org/compounds/PD", "https://www\\.probes-drugs\\.org/compounds/PD[0-9]{6}"));
        descriptions.put("PubchemRef", new Description("PUBCHEM", "https://pubchem.ncbi.nlm.nih.gov/compound/",
                "https://pubchem\\.ncbi\\.nlm\\.nih\\.gov/compound/[1-9][0-9]*"));
        descriptions.put("RcsbPdbRef", new Description("RCSB PDB", "https://www.rcsb.org/ligand/",
                "https://www\\.rcsb\\.org/ligand/[A-Z0-9]+"));
        descriptions.put("RheaRef", new Description("RHEA", "https://www.rhea-db.org/rhea?query=",
                "https://www\\.rhea-db\\.org/rhea\\?query=(CHEBI|POLYMER)%3A[1-9][0-9]*"));
        descriptions.put("SureChemblRef", new Description("SURE CHEMBL", "https://www.surechembl.org/chemical/",
                "https://www\\.surechembl\\.org/chemical/[1-9][0-9]*"));
        descriptions.put("SwissLipidsRef",
                new Description("SWISS LIPIDS", "https://www.swisslipids.org/#/entity/SLM%3A",
                        "https://www\\.swisslipids\\.org/#/entity/SLM%3A[0-9]{9}"));
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_references(refmol_id, molecule_id, reference_type, reference) "
                        + "values(?,?,?::chembl_tmp.molecule_reference_type,?)"))
        {
            new QueryResultProcessor(patternQuery("?molecule cco:moleculeXref ?reference. ?reference rdf:type ?type."))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Description description = descriptions
                            .get(getStringID("type", "http://rdf.ebi.ac.uk/terms/chembl#"));

                    if(!getIRI("reference").matches(description.pattern))
                        throw new IOException("wrong value: " + getIRI("reference"));

                    if(description.name.equals("RHEA"))
                        description = getIRI("reference").contains("POLYMER") ? rheaPolymer : rheaChebi;

                    String reference = getStringID("reference", description.prefix);

                    if(description.name.equals("PDBE"))
                        reference = reference.substring(0, reference.length() - "+-+Ideal+conformer".length());

                    statement.setInt(1, id++);
                    statement.setInt(2, getIntID("molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                    statement.setString(3, description.name);
                    statement.setString(4, reference);
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_pubchem_references(molecule_id, compound_id) values(?,?)"))
        {
            new QueryResultProcessor(
                    patternQuery("?molecule cco:moleculeXref ?compound. ?compound rdf:type cco:PubchemRef"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1, getIntID("molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                    statement.setInt(2, getIntID("compound", "https://pubchem.ncbi.nlm.nih.gov/compound/"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_chebi_references(molecule_id, chebi_id) values(?,?)"))
        {
            new QueryResultProcessor(patternQuery("?molecule cco:moleculeXref ?chebi. ?chebi rdf:type cco:ChebiRef"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1, getIntID("molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                    statement.setInt(2, getIntID("chebi", "https://www.ebi.ac.uk/chebi/CHEBI%3A"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_" + ChEMBL.version + "_unichem.ttl.gz");
    }
}
