sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:biosystem
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:biosystem .;
sparql drop iri class iri:wikipathway .;

--------------------------------------------------------------------------------

drop table biosystem_matches;
drop table biosystem_references;
drop table biosystem_components;
drop table biosystem_bases;
