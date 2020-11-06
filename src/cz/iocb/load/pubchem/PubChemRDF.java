package cz.iocb.load.pubchem;

import java.io.IOException;
import java.sql.SQLException;
import cz.iocb.load.common.Updater;
import cz.iocb.load.ontology.Ontology;



public class PubChemRDF extends Updater
{
    public static void main(String[] args) throws SQLException, IOException
    {
        try
        {
            init();

            //Ontology.load();
            Ontology.loadCategories();

            Concept.load();
            Source.load(); // depends on Concept
            ConservedDomain.load();

            Gene.load();
            Protein.load(); // depends on Ontology

            Bioassay.load(); // depends on Ontology
            Compound.load(); // depends on Ontology
            Pathway.load(); // depends on Source, Compound, Protein, Gene, Ontology

            InchiKey.load(); // depends on Compound
            Measuregroup.load(); // depends on Protein
            Protein.finish();
            Reference.load(); // depends on Ontology

            Synonym.load(); // depends on Concept and Compound
            Concept.finish();

            Substance.load(); // depends on Synonym and Compound
            Synonym.finish();

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

            //ConstraintChecker.check();
        }
        catch(Throwable e)
        {
            e.printStackTrace();
            rollback();
        }
    }
}
