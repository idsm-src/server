sparql alter quad storage virtrdf:PubchemQuadStorage
{
    drop map:ontology
}.;

--------------------------------------------------------------------------------

sparql drop iri class iri:class .;
sparql drop iri class iri:property .;

--------------------------------------------------------------------------------

drop function iri_class;
drop function iri_class_INVERSE;
drop function iri_property;
drop function iri_property_INVERSE;

--------------------------------------------------------------------------------

drop table property_ranges;
drop table property_domains;
drop table property_subproperties;
drop table property_bases;
drop table class_subclasses;
drop table class_bases;
