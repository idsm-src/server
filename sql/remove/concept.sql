sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:concept
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:concept .;

--------------------------------------------------------------------------------

drop function iri_concept;
drop function iri_concept_INVERSE;

--------------------------------------------------------------------------------

drop table concept_bases;
