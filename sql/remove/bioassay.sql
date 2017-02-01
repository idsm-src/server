sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:bioassay
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:bioassay .;
sparql drop iri class iri:bioassay_data .;

--------------------------------------------------------------------------------

drop function iri_bioassay_data;
drop function iri_bioassay_data_INV_1;
drop function iri_bioassay_data_INV_2;

--------------------------------------------------------------------------------

drop table bioassay_measuregroups;
drop table bioassay_data;
drop table bioassay_bases;
