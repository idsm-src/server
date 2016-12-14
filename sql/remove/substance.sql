sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:substance
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:substance .;
sparql drop iri class iri:substance_type .;
sparql drop iri class iri:substance_chembl .;
sparql drop iri class iri:substance_ebi_chembl .;
sparql drop iri class iri:substance_schembl .;
sparql drop iri class iri:substance_ebi_schembl .;

--------------------------------------------------------------------------------

drop table substance_synonyms;
drop table substance_pdblinks;
drop table substance_references;
drop table substance_schembl_matches;
drop table substance_chembl_matches;
drop table substance_types;
drop table substance_bases;
