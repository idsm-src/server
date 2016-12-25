xml_remove_ns_by_prefix('obo', 2);
xml_set_ns_decl('compound', 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound/', 2);
xml_set_ns_decl('substance', 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance/', 2);
xml_set_ns_decl('descriptor', 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/', 2);
xml_set_ns_decl('synonym', 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym/', 2);
xml_set_ns_decl('inchikey', 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey/', 2);
xml_set_ns_decl('bioassay', 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay/', 2);
xml_set_ns_decl('measuregroup', 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup/', 2);
xml_set_ns_decl('endpoint', 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint/', 2);
xml_set_ns_decl('reference', 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference/', 2);
xml_set_ns_decl('protein', 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein/', 2);
xml_set_ns_decl('conserveddomain', 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain/', 2);
xml_set_ns_decl('gene', 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene/', 2);
xml_set_ns_decl('biosystem', 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem/', 2);
xml_set_ns_decl('source', 'http://rdf.ncbi.nlm.nih.gov/pubchem/source/', 2);
xml_set_ns_decl('concept', 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept/', 2);
xml_set_ns_decl('vocab', 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#', 2);
xml_set_ns_decl('obo', 'http://purl.obolibrary.org/obo/', 2);
xml_set_ns_decl('sio', 'http://semanticscience.org/resource/', 2);
xml_set_ns_decl('skos', 'http://www.w3.org/2004/02/skos/core#', 2);
xml_set_ns_decl('bao', 'http://www.bioassayontology.org/bao#', 2);
xml_set_ns_decl('bp', 'http://www.biopax.org/release/biopax-level3.owl#', 2);
xml_set_ns_decl('ndfrt', 'http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT.owl#', 2);
xml_set_ns_decl('ncit', 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#', 2);
xml_set_ns_decl('wikidata', 'http://www.wikidata.org/entity/', 2);
xml_set_ns_decl('ops', 'http://www.openphacts.org/units/', 2);
xml_set_ns_decl('cito', 'http://purl.org/spar/cito/', 2);
xml_set_ns_decl('fabio', 'http://purl.org/spar/fabio/', 2);
xml_set_ns_decl('uniprot', 'http://purl.uniprot.org/uniprot/', 2);
xml_set_ns_decl('pdbo', 'http://rdf.wwpdb.org/schema/pdbx-v40.owl#', 2);
xml_set_ns_decl('pdbr', 'http://rdf.wwpdb.org/pdb/', 2);
xml_set_ns_decl('taxonomy', 'http://identifiers.org/taxonomy/', 2);
xml_set_ns_decl('reactome', 'http://identifiers.org/reactome/', 2);
xml_set_ns_decl('chembl', 'http://rdf.ebi.ac.uk/resource/chembl/molecule/', 2);
xml_set_ns_decl('chemblchembl', 'http://linkedchemistry.info/chembl/chemblid/', 2);
xml_set_ns_decl('foaf', 'http://xmlns.com/foaf/0.1/', 2);
xml_set_ns_decl('void', 'http://rdfs.org/ns/void#', 2);
xml_set_ns_decl('dcterms', 'http://purl.org/dc/terms/', 2);

--------------------------------------------------------------------------------

xml_set_ns_decl('iri', 'http://bioinfo.iocb.cz/rdf/quad-storage/linked-data-view/iri-class/pubchem#', 2);
xml_set_ns_decl('map', 'http://bioinfo.iocb.cz/rdf/quad-storage/linked-data-view/quad-map/pubchem#', 2);
xml_set_ns_decl('pubchem', 'http://rdf.ncbi.nlm.nih.gov/pubchem/', 2);
xml_set_ns_decl('descriptor', 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/', 2);

xml_set_ns_decl('map-orchem', 'http://bioinfo.iocb.cz/rdf/quad-storage/linked-data-view/quad-map/orchem#', 2);
xml_set_ns_decl('orchem', 'http://bioinfo.iocb.cz/rdf/0.9/orchem#', 2);
xml_set_ns_decl('iri-proc', 'http://bioinfo.iocb.cz/rdf/quad-storage/linked-data-view/iri-class/procedure-calls#', 2);
xml_set_ns_decl('proc', 'http://bioinfo.iocb.cz/rdf/0.9/procedure-calls#', 2);

xml_set_ns_decl('orchem', 'http://bioinfo.iocb.cz/0.9/orchem#', 2);
xml_set_ns_decl('template', 'http://bioinfo.iocb.cz/0.9/template#', 2);

xml_set_ns_decl('map-template', 'http://bioinfo.iocb.cz/rdf/quad-storage/linked-data-view/quad-map/template#', 2);

--------------------------------------------------------------------------------

sparql
create quad storage virtrdf:PubchemQuadStorage
{
};
