sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:endpoint
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:endpoint .;
sparql drop iri class iri:endpoint_outcome .;

--------------------------------------------------------------------------------

drop function iri_endpoint;
drop function iri_endpoint_INV_1;
drop function iri_endpoint_INV_2;
drop function iri_endpoint_INV_3;
drop function iri_endpoint_outcome;
drop function iri_endpoint_outcome_INVERSE;

--------------------------------------------------------------------------------

drop table bioassay_measuregroups;
drop table endpoint_references;
drop table endpoint_measurements;
drop table endpoint_bases;
drop table endpoint_outcomes__reftable;
