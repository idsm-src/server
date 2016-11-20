sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:concept
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:concept .;
sparql drop iri class iri:concept_type .;

--------------------------------------------------------------------------------

drop function iri_concept;
drop function iri_concept_INVERSE;
drop function iri_concept_type;
drop function iri_concept_type_INVERSE;

--------------------------------------------------------------------------------

drop view concept_imports;
drop view concept_types;
drop table concept_bases;
