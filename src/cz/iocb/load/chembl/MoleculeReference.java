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


    private static HashMap<String, Description> descriptions = new HashMap<String, Description>();
    private static int id = 0;


    static
    {
        descriptions.put("ZincRef", new Description("ZINC", "http://zinc15.docking.org/substances/",
                "http://zinc15\\.docking\\.org/substances/ZINC[0-9]{12}"));
        descriptions.put("SureChemblRef", new Description("SURE CHEMBL", "https://www.surechembl.org/chemical/",
                "https://www\\.surechembl\\.org/chemical/SCHEMBL[0-9]+"));
        descriptions.put("MolportRef", new Description("MOLPORT", "https://www.molport.com/shop/molecule-link/",
                "https://www\\.molport\\.com/shop/molecule-link/MolPort(-[0-9]{3}){3}"));
        descriptions.put("EmoleculesRef", new Description("EMOLECULES", "https://www.emolecules.com/cgi-bin/more?vid=",
                "https://www\\.emolecules\\.com/cgi-bin/more\\?vid=[1-9][0-9]*"));
        descriptions.put("MculeRef",
                new Description("MCULE", "https://mcule.com/", "https://mcule\\.com/MCULE-[1-9][0-9]*"));
        descriptions.put("NikkajiRef", new Description("NIKKAJI", "http://jglobal.jst.go.jp/en/redirect?Nikkaji_No=",
                "http://jglobal\\.jst\\.go\\.jp/en/redirect\\?Nikkaji_No=[A-Z0-9.]+"));
        descriptions.put("ActorRef", new Description("ACTOR", "http://actor.epa.gov/actor/chemical.xhtml?casrn=",
                "http://actor\\.epa\\.gov/actor/chemical\\.xhtml\\?casrn=[1-9][0-9]*-[0-9]{2}-[0-9]"));
        descriptions.put("PdbeRef",
                new Description("PDBE", "http://www.ebi.ac.uk/pdbe-srv/pdbechem/chemicalCompound/show/",
                        "http://www\\.ebi\\.ac\\.uk/pdbe-srv/pdbechem/chemicalCompound/show/[A-Z0-9]{1,3}"));
        descriptions.put("NmrShiftDb2Ref", new Description("NMR SHIFT DB2", "http://nmrshiftdb.org/molecule/",
                "http://nmrshiftdb\\.org/molecule/[1-9][0-9]*"));
        descriptions.put("KeggLigandRef", new Description("KEGG LIGAND", "http://www.genome.jp/dbget-bin/www_bget?",
                "http://www\\.genome\\.jp/dbget-bin/www_bget\\?C[0-9]{5}"));
        descriptions.put("DrugbankRef", new Description("DRUGBANK", "http://www.drugbank.ca/drugs/",
                "http://www\\.drugbank\\.ca/drugs/DB[0-9]{5}"));
        descriptions.put("HmdbRef", new Description("HMDB", "http://www.hmdb.ca/metabolites/",
                "http://www\\.hmdb\\.ca/metabolites/HMDB[0-9]{7}"));
        descriptions.put("IupharRef",
                new Description("IUPHAR", "http://www.guidetopharmacology.org/GRAC/LigandDisplayForward?ligandId=",
                        "http://www\\.guidetopharmacology\\.org/GRAC/LigandDisplayForward\\?ligandId=[1-9][0-9]*"));
        descriptions.put("SelleckRef", new Description("SELLECK", "http://www.selleckchem.com/products/",
                "http://www\\.selleckchem\\.com/products/[^/]*\\.html"));
        descriptions.put("PharmGkbRef", new Description("PHARM GKB", "https://www.pharmgkb.org/drug/",
                "https://www\\.pharmgkb\\.org/drug/PA[1-9][0-9]*"));
        descriptions.put("AtlasRef", new Description("ATLAS", "http://www.ebi.ac.uk/gxa/query?conditionQuery=",
                "http://www\\.ebi\\.ac\\.uk/gxa/query\\?conditionQuery=.+"));
        descriptions.put("ReconRef", new Description("RECON", "https://vmh.uni.lu/#metabolite/",
                "https://vmh\\.uni\\.lu/#metabolite/[^/]+"));
        descriptions.put("WikipediaMolRef", new Description("WIKIPEDIA MOL", "http://en.wikipedia.org/wiki/",
                "http://en\\.wikipedia\\.org/wiki/.+"));
        descriptions.put("LincsRef", new Description("LINCS", "http://identifiers.org/lincs.smallmolecule/",
                "http://identifiers\\.org/lincs\\.smallmolecule/LSM-[1-9][0-9]*"));
        descriptions.put("FdaSrsRef", new Description("FDA SRS", "https://precision.fda.gov/uniisearch/srs/unii/",
                "https://precision\\.fda\\.gov/uniisearch/srs/unii/[A-Z0-9]{10}"));
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_references(refmol_id, molecule_id, reference_type, reference) "
                        + "values(?,?,?::chembl_tmp.molecule_reference_type,?)"))
        {
            new QueryResultProcessor(patternQuery("?molecule cco:moleculeXref ?reference. ?reference rdf:type ?type "
                    + "filter(?type != cco:PubchemRef && ?type != cco:PubchemThomPharmRef "
                    + "&& ?type != cco:PubchemDotfRef && ?type != cco:ChebiRef)"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Description description = descriptions
                            .get(getStringID("type", "http://rdf.ebi.ac.uk/terms/chembl#"));

                    if(!getIRI("reference").matches(description.pattern))
                        throw new IOException("wrong value: " + getIRI("reference"));

                    String reference = getStringID("reference", description.prefix);

                    if(description.name.equals("SELLECK"))
                        reference = reference.substring(0, reference.length() - ".html".length());

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
                    statement.setInt(2, getIntID("compound", "http://pubchem.ncbi.nlm.nih.gov/compound/"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_pubchem_thom_pharm_references(molecule_id, substance_id) values(?,?)"))
        {
            new QueryResultProcessor(
                    patternQuery("?molecule cco:moleculeXref ?substance. ?substance rdf:type cco:PubchemThomPharmRef"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1, getIntID("molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                    statement.setInt(2, getIntID("substance", "http://pubchem.ncbi.nlm.nih.gov/substance/"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }


        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_tmp.molecule_pubchem_dotf_references(molecule_id, substance_id) values(?,?)"))
        {
            new QueryResultProcessor(
                    patternQuery("?molecule cco:moleculeXref ?substance. ?substance rdf:type cco:PubchemDotfRef"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    statement.setInt(1, getIntID("molecule", "http://rdf.ebi.ac.uk/resource/chembl/molecule/CHEMBL"));
                    statement.setInt(2, getIntID("substance", "http://pubchem.ncbi.nlm.nih.gov/substance/"));
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
                    statement.setInt(2, getIntID("chebi", "http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI%3A"));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_" + ChEMBL.version + "_molecule.ttl.gz");
        load("chembl/rdf/chembl_" + ChEMBL.version + "_unichem.ttl.gz");
    }
}
