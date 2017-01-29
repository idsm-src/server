package cz.iocb.pubchem.load.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;



public abstract class PubchemFileTableLoader extends SimpleFileTableLoader
{
    protected final HashMap<String, String> prefixes = new HashMap<String, String>();

    public PubchemFileTableLoader(BufferedReader reader, String sql)
    {
        super(reader, sql);

        prefixes.put("rdf:", "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
        prefixes.put("rdfs:", "<http://www.w3.org/2000/01/rdf-schema#>");
        prefixes.put("xsd:", "<http://www.w3.org/2001/XMLSchema#>");
        prefixes.put("owl:", "<http://www.w3.org/2002/07/owl#>");

        prefixes.put("compound:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/compound/>");
        prefixes.put("substance:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/substance/>");
        prefixes.put("descriptor:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/>");
        prefixes.put("synonym:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/>");
        prefixes.put("inchikey:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/>");
        prefixes.put("bioassay:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/>");
        prefixes.put("measuregroup:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/>");
        prefixes.put("endpoint:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/>");
        prefixes.put("reference:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/reference/>");
        prefixes.put("protein:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/protein/>");
        prefixes.put("conserveddomain:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/>");
        prefixes.put("gene:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/gene/>");
        prefixes.put("biosystem:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/>");
        prefixes.put("source:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/source/>");
        prefixes.put("concept:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/concept/>");
        prefixes.put("vocab:", "<http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#>");
        prefixes.put("obo:", "<http://purl.obolibrary.org/obo/>");
        prefixes.put("sio:", "<http://semanticscience.org/resource/>");
        prefixes.put("skos:", "<http://www.w3.org/2004/02/skos/core#>");
        prefixes.put("bao:", "<http://www.bioassayontology.org/bao#>");
        prefixes.put("bp:", "<http://www.biopax.org/release/biopax-level3.owl#>");
        prefixes.put("ndfrt:", "<http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#>");
        prefixes.put("ncit:", "<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#>");
        prefixes.put("wikidata:", "<http://www.wikidata.org/entity/>");
        prefixes.put("ops:", "<http://www.openphacts.org/units/>");
        prefixes.put("cito:", "<http://purl.org/spar/cito/>");
        prefixes.put("fabio:", "<http://purl.org/spar/fabio/>");
        prefixes.put("uniprot:", "<http://purl.uniprot.org/uniprot/>");
        prefixes.put("pdbo:", "<http://rdf.wwpdb.org/schema/pdbx-v40.owl#>");
        prefixes.put("pdbr:", "<http://rdf.wwpdb.org/pdb/>");
        prefixes.put("taxonomy:", "<http://identifiers.org/taxonomy/>");
        prefixes.put("reactome:", "<http://identifiers.org/reactome/>");
        prefixes.put("chembl:", "<http://rdf.ebi.ac.uk/resource/chembl/molecule/>");
        prefixes.put("chemblchembl:", "<http://linkedchemistry.info/chembl/chemblid/>");
        prefixes.put("foaf:", "<http://xmlns.com/foaf/0.1/>");
        prefixes.put("void:", "<http://rdfs.org/ns/void#>");
        prefixes.put("dcterms:", "<http://purl.org/dc/terms/>");
    }

    @Override
    public void prefix(String name, String iri) throws SQLException, IOException
    {
        String prefix = prefixes.get(name);

        if(prefix == null)
            System.out.println("  unknown prefix " + name);

        if(!prefix.equals(iri))
            throw new IOException("unexpected prefix definition: " + name + " as " + iri);
    }
}
