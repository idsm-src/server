package cz.iocb.load.chembl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.jena.rdf.model.Model;
import cz.iocb.load.common.QueryResultProcessor;
import cz.iocb.load.common.Updater;



public class TargetComponentReference extends Updater
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
        descriptions.put("GoProcessRef", new Description("GO PROCESS", "http://identifiers.org/obo.go/",
                "http://identifiers\\.org/obo\\.go/GO:[0-9]{7}"));
        descriptions.put("GoFunctionRef", new Description("GO FUNCTION", "http://identifiers.org/obo.go/",
                "http://identifiers\\.org/obo\\.go/GO:[0-9]{7}"));
        descriptions.put("GoComponentRef", new Description("GO COMPONENT", "http://identifiers.org/obo.go/",
                "http://identifiers\\.org/obo\\.go/GO:[0-9]{7}"));
        descriptions.put("ProteinDataBankRef",
                new Description("PDB", "http://identifiers.org/pdb/", "http://identifiers\\.org/pdb/[0-9][A-Z0-9]{3}"));
        descriptions.put("InterproRef", new Description("INTERPRO", "http://identifiers.org/interpro/",
                "http://identifiers\\.org/interpro/IPR[0-9]{6}"));
        descriptions.put("ReactomeRef", new Description("REACTOME", "http://identifiers.org/reactome/",
                "http://identifiers\\.org/reactome/R-[A-Z]{3}-[1-9][0-9]*"));
        descriptions.put("PfamRef",
                new Description("PFAM", "http://identifiers.org/pfam/", "http://identifiers\\.org/pfam/PF[0-9]{5}"));
        descriptions.put("EnzymeClassRef", new Description("ENZYME CLASS", "http://identifiers.org/ec-code/",
                "http://identifiers\\.org/ec-code/((-|[1-9][0-9]*)\\.){3}(-|n?[1-9][0-9]*)"));
        descriptions.put("IntactRef", new Description("INTACT", "http://identifiers.org/intact/",
                "http://identifiers\\.org/intact/[A-Z0-9]*"));
        descriptions.put("UniprotRef", new Description("UNIPROT", "http://purl.uniprot.org/uniprot/",
                "http://purl\\.uniprot\\.org/uniprot/[A-Z0-9]+"));
        descriptions.put("PharmgkbRef", new Description("PHARMGKB", "http://www.pharmgkb.org/gene/",
                "http://www\\.pharmgkb\\.org/gene/PA[1-9][0-9]*"));
        descriptions.put("TimbalRef", new Description("TIMBAL", "http://mordred.bioc.cam.ac.uk/timbal/",
                "http://mordred\\.bioc\\.cam\\.ac\\.uk/timbal/[A-Za-z0-9%()-]+"));
        descriptions.put("CGDRef", new Description("CGD", "http://research.nhgri.nih.gov/CGD/view/?g=",
                "http://research\\.nhgri\\.nih\\.gov/CGD/view/\\?g=[A-Z0-9-]+"));
    }


    public static void load(String file) throws IOException, SQLException
    {
        Model model = getModel(file);

        try(PreparedStatement statement = connection.prepareStatement(
                "insert into chembl_32.component_references(refcomp_id, component_id, reference_type, reference) "
                        + "values (?,?,?::chembl_32.component_reference_type,?)"))
        {
            new QueryResultProcessor(
                    patternQuery("?component cco:targetCmptXref ?reference. ?reference rdf:type ?type"))
            {
                @Override
                public void parse() throws SQLException, IOException
                {
                    Description description = descriptions
                            .get(getStringID("type", "http://rdf.ebi.ac.uk/terms/chembl#"));

                    if(!getIRI("reference").matches(description.pattern))
                        throw new IOException("wrong value: " + getIRI("reference"));

                    statement.setInt(1, id++);
                    statement.setInt(2,
                            getIntID("component", "http://rdf.ebi.ac.uk/resource/chembl/targetcomponent/CHEMBL_TC_"));
                    statement.setString(3, description.name);
                    statement.setString(4, getStringID("reference", description.prefix));
                    statement.addBatch();
                }
            }.load(model);

            statement.executeBatch();
        }
    }


    public static void load() throws IOException, SQLException
    {
        load("chembl/rdf/chembl_32.0_targetcmpt.ttl.gz");
    }
}
