sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:source
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:source .;
sparql drop iri class iri:source_subject .;

--------------------------------------------------------------------------------

drop function iri_source;
drop function iri_source_INVERSE;
drop function iri_source_subject;
drop function iri_source_subject_INVERSE;

--------------------------------------------------------------------------------

drop table source_alternatives;
drop table source_subjects;
drop table source_subjects__reftable;
drop table source_bases;
