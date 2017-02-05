sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:substance
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:substance .;
sparql drop iri class iri:substance_type .;
sparql drop iri class iri:substance_chembl .;
sparql drop iri class iri:substance_ebi_chembl .;

--------------------------------------------------------------------------------

drop function iri_substance_chembl;
drop function iri_substance_chembl_INVERSE;
drop function iri_substance_ebi_chembl;
drop function iri_substance_ebi_chembl_INVERSE;

--------------------------------------------------------------------------------

drop table substance_synonyms;
drop table substance_pdblinks;
drop table substance_references;
drop table substance_matches;
drop table substance_types;
drop table substance_bases;
