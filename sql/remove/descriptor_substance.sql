sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:descriptor_substance
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:descriptor_substance_version .;

--------------------------------------------------------------------------------

drop table descriptor_substance_bases;
