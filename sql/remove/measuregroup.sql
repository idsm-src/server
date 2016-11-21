sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:measuregroup
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:measuregroup .;

--------------------------------------------------------------------------------

drop function iri_measuregroup;
drop function iri_measuregroup_INV_1;
drop function iri_measuregroup_INV_2;

--------------------------------------------------------------------------------

drop table measuregroup_genes;
drop table measuregroup_proteins;
drop table measuregroup_bases;
