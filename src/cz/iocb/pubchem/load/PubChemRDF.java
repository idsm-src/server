package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.pubchem.load.common.Updater;



public class PubChemRDF extends Updater
{
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();

            Ontology.load();

            Concept.load();
            Source.load(); // depends on Concept
            Biosystem.load(); // depends on Source
            ConservedDomain.load();

            Gene.load();
            Protein.load();

            Compound.load(); // depends on Ontology
            InchiKey.load(); // depends on Compound
            Measuregroup.load(); // depends on Source and Protein
            Protein.finish();
            Reference.load(); // depends on Ontology

            Synonym.load(); // depends on Concept and Compound
            Concept.finish();

            Substance.load(); // depends on Synonym and Compound

            Endpoint.load(); // depends on Ontology, Substance and Measuregroup
            Measuregroup.finish();
            Endpoint.finish();

            CompoundDescriptor.load(); // depends on Compound
            Compound.finish();

            SubstanceDescriptor.load(); // depends on Substance
            Substance.finish();

            BioassayXML.load(); // depends on Source
            Source.finish();

            commit();

            ConstraintChecker.check();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
