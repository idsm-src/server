sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:compound
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:compound .;
sparql drop iri class iri:compound_sdfile .;
sparql drop iri class iri:compound_relation .;
sparql drop iri class iri:compound_role .;
sparql drop iri class iri:compound_type .;

--------------------------------------------------------------------------------

drop function iri_compound_relation;
drop function iri_compound_relation_INVERSE;
drop function iri_compound_role;
drop function iri_compound_role_INVERSE;
drop function iri_compound_type;
drop function iri_compound_type_INV_1;
drop function iri_compound_type_INV_2;

--------------------------------------------------------------------------------

drop view compound_bases;
drop table compound_active_ingredients;
drop table compound_types;
drop table compound_type_units__reftable;
drop table compound_biosystems;
drop table compound_roles;
drop table compound_roles__reftable;
drop table compound_relations;
drop table compound_relations__reftable;
drop table compound_sdfiles_gzip;
