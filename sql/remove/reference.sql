sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:reference
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:reference .;
sparql drop iri class iri:reference_type .;

--------------------------------------------------------------------------------

drop function iri_reference_type;
drop function iri_reference_type_INVERSE;

--------------------------------------------------------------------------------

drop table reference_subject_descriptors;
drop table reference_discusses;
drop table reference_citations_long;
drop table reference_bases;
drop table reference_types__reftable;
