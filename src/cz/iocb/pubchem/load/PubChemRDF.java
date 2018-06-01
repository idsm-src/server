package cz.iocb.pubchem.load;

import java.io.IOException;
import java.sql.SQLException;



public class PubChemRDF
{
    public static void main(String[] args) throws SQLException, IOException
    {
        Source.load("RDF/source/pc_source.ttl.gz");
        Biosystem.load("RDF/biosystem/pc_biosystem.ttl.gz");
        Concept.load("RDF/concept/pc_concept.ttl.gz");
        ConservedDomain.load("RDF/conserveddomain/pc_conserveddomain.ttl.gz");
        Gene.load("RDF/gene/pc_gene.ttl.gz");
        Protein.load("RDF/protein/pc_protein.ttl.gz");

        Compound.loadDirectory("RDF/compound/general");
        Endpoint.loadDirectory("RDF/endpoint");
        InchiKey.loadDirectory("RDF/inchikey");
        Measuregroup.loadDirectory("RDF/measuregroup");
        Reference.loadDirectory("RDF/reference");

        Synonym.loadDirectory("RDF/synonym");
        Substance.loadDirectory("RDF/substance");

        CompoundDescriptor.loadDirectory("RDF/descriptor/compound");
        SubstanceDescriptor.loadDirectory("RDF/descriptor/substance");

        BioassayXML.loadDirectory("Bioassay/XML");
        //CompoundSDF.loadDirectory("SDF");

        Ontology.loadDirectory("ontology");
    }
}
