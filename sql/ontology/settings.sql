grant select on ontology.classes to sparql;

--------------------------------------------------------------------------------

grant select on ontology.properties to sparql;

--------------------------------------------------------------------------------

grant select on ontology.individuals to sparql;

--------------------------------------------------------------------------------

create index resource_labels__label on ontology.resource_labels(label);
grant select on ontology.resource_labels to sparql;

--------------------------------------------------------------------------------

create index superclasses__class on ontology.superclasses(class_unit, class_id);
create index superclasses__superclass on ontology.superclasses(superclass_unit, superclass_id);
grant select on ontology.superclasses to sparql;

--------------------------------------------------------------------------------

create index superproperties__property on ontology.superproperties(property_unit, property_id);
create index superproperties__superproperty on ontology.superproperties(superproperty_unit, superproperty_id);
grant select on ontology.superproperties to sparql;

--------------------------------------------------------------------------------

create index property_domains__property on ontology.property_domains(property_unit, property_id);
create index property_domains__domain on ontology.property_domains(domain_unit, domain_id);
grant select on ontology.property_domains to sparql;

--------------------------------------------------------------------------------

create index property_ranges__property on ontology.property_ranges(property_unit, property_id);
create index property_ranges__range on ontology.property_ranges(range_unit, range_id);
grant select on ontology.property_ranges to sparql;

--------------------------------------------------------------------------------

create index somevaluesfrom_restrictions__property on ontology.somevaluesfrom_restrictions(property_unit, property_id);
create index somevaluesfrom_restrictions__class on ontology.somevaluesfrom_restrictions(class_unit, class_id);
grant select on ontology.somevaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index allvaluesfrom_restrictions__property on ontology.allvaluesfrom_restrictions(property_unit, property_id);
create index allvaluesfrom_restrictions__class on ontology.allvaluesfrom_restrictions(class_unit, class_id);
grant select on ontology.allvaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index cardinality_restrictions__property on ontology.cardinality_restrictions(property_unit, property_id);
create index cardinality_restrictions__cardinality on ontology.cardinality_restrictions(cardinality);
grant select on ontology.cardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index mincardinality_restrictions__property on ontology.mincardinality_restrictions(property_unit, property_id);
create index mincardinality_restrictions__cardinality on ontology.mincardinality_restrictions(cardinality);
grant select on ontology.mincardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index maxcardinality_restrictions__property on ontology.maxcardinality_restrictions(property_unit, property_id);
create index maxcardinality_restrictions__cardinality on ontology.maxcardinality_restrictions(cardinality);
grant select on ontology.maxcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index resources__reftable__iri on ontology.resources__reftable using hash (iri);
grant select on ontology.resources__reftable to sparql;

--------------------------------------------------------------------------------

grant select on ontology.resource_categories__reftable to sparql;
