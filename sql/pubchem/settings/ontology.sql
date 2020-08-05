grant select on ontology_resource_classes to sparql;

--------------------------------------------------------------------------------

grant select on ontology_resource_properties to sparql;

--------------------------------------------------------------------------------

grant select on ontology_resource_individuals to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_labels__label on ontology_resource_labels(label);
grant select on ontology_resource_labels to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_superclasses__class on ontology_resource_superclasses(class_unit, class_id);
create index ontology_resource_superclasses__superclass on ontology_resource_superclasses(superclass_unit, superclass_id);
grant select on ontology_resource_superclasses to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_superproperties__property on ontology_resource_superproperties(property_unit, property_id);
create index ontology_resource_superproperties__superproperty on ontology_resource_superproperties(superproperty_unit, superproperty_id);
grant select on ontology_resource_superproperties to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_domains__property on ontology_resource_domains(property_unit, property_id);
create index ontology_resource_domains__domain on ontology_resource_domains(domain_unit, domain_id);
grant select on ontology_resource_domains to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_ranges__property on ontology_resource_ranges(property_unit, property_id);
create index ontology_resource_ranges__range on ontology_resource_ranges(range_unit, range_id);
grant select on ontology_resource_ranges to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_somevaluesfrom_restrictions__property on ontology_resource_somevaluesfrom_restrictions(property_unit, property_id);
create index ontology_resource_somevaluesfrom_restrictions__class on ontology_resource_somevaluesfrom_restrictions(class_unit, class_id);
grant select on ontology_resource_somevaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_allvaluesfrom_restrictions__property on ontology_resource_allvaluesfrom_restrictions(property_unit, property_id);
create index ontology_resource_allvaluesfrom_restrictions__class on ontology_resource_allvaluesfrom_restrictions(class_unit, class_id);
grant select on ontology_resource_allvaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_cardinality_restrictions__property on ontology_resource_cardinality_restrictions(property_unit, property_id);
create index ontology_resource_cardinality_restrictions__cardinality on ontology_resource_cardinality_restrictions(cardinality);
grant select on ontology_resource_cardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_mincardinality_restrictions__property on ontology_resource_mincardinality_restrictions(property_unit, property_id);
create index ontology_resource_mincardinality_restrictions__cardinality on ontology_resource_mincardinality_restrictions(cardinality);
grant select on ontology_resource_mincardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index ontology_resource_maxcardinality_restrictions__property on ontology_resource_maxcardinality_restrictions(property_unit, property_id);
create index ontology_resource_maxcardinality_restrictions__cardinality on ontology_resource_maxcardinality_restrictions(cardinality);
grant select on ontology_resource_maxcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

grant select on ontology_resources__reftable to sparql;

--------------------------------------------------------------------------------

grant select on ontology_resource_categories__reftable to sparql;
