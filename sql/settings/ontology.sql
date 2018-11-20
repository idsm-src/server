grant select on ontology_resource_classes to "SPARQL";

--------------------------------------------------------------------------------

grant select on ontology_resource_properties to "SPARQL";

--------------------------------------------------------------------------------

grant select on ontology_resource_individuals to "SPARQL";

--------------------------------------------------------------------------------

create index ontology_resource_labels__label on ontology_resource_labels(label);
grant select on ontology_resource_labels to "SPARQL";

--------------------------------------------------------------------------------

create index ontology_resource_superclasses__class on ontology_resource_superclasses(class_unit, class_id);
create index ontology_resource_superclasses__superclass on ontology_resource_superclasses(superclass_unit, superclass_id);
grant select on ontology_resource_superclasses to "SPARQL";

--------------------------------------------------------------------------------

create index ontology_resource_superproperties__property on ontology_resource_superproperties(property_unit, property_id);
create index ontology_resource_superproperties__superproperty on ontology_resource_superproperties(superproperty_unit, superproperty_id);
grant select on ontology_resource_superproperties to "SPARQL";

--------------------------------------------------------------------------------

create index ontology_resource_domains__property on ontology_resource_domains(property_unit, property_id);
create index ontology_resource_domains__domain on ontology_resource_domains(domain_unit, domain_id);
grant select on ontology_resource_domains to "SPARQL";

--------------------------------------------------------------------------------

create index ontology_resource_ranges__property on ontology_resource_ranges(property_unit, property_id);
create index ontology_resource_ranges__range on ontology_resource_ranges(range_unit, range_id);
grant select on ontology_resource_ranges to "SPARQL";

--------------------------------------------------------------------------------

grant select on ontology_resources__reftable to "SPARQL";

--------------------------------------------------------------------------------

grant select on ontology_resource_categories__reftable to "SPARQL";
