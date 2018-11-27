drop function ontology_resource(unit in smallint, id in integer);
drop function ontology_resource_inv1(iri in varchar);
drop function ontology_resource_inv2(iri in varchar);

--------------------------------------------------------------------------------

drop table ontology_resource_categories__reftable;
drop table ontology_resources__reftable;
drop table ontology_resource_maxcardinality_restrictions;
drop table ontology_resource_mincardinality_restrictions;
drop table ontology_resource_cardinality_restrictions;
drop table ontology_resource_allvaluesfrom_restrictions;
drop table ontology_resource_somevaluesfrom_restrictions;
drop table ontology_resource_ranges;
drop table ontology_resource_domains;
drop table ontology_resource_superproperties;
drop table ontology_resource_superclasses;
drop table ontology_resource_labels;
drop table ontology_resource_individuals;
drop table ontology_resource_properties;
drop table ontology_resource_classes;
