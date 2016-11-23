sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:inchikey
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:inchikey .;

--------------------------------------------------------------------------------

drop function iri_inchikey;
drop function iri_inchikey_INVERSE;

--------------------------------------------------------------------------------

drop table inchikey_subjects;
drop table inchikey_compounds;
drop table inchikey_bases;
