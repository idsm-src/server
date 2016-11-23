sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:conserveddomain
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:conserveddomain .;

--------------------------------------------------------------------------------

drop table conserveddomain_references;
drop table conserveddomain_bases;
