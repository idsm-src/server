grant select on ontology.classes to sparql;

--------------------------------------------------------------------------------

grant select on ontology.properties to sparql;

--------------------------------------------------------------------------------

grant select on ontology.individuals to sparql;

--------------------------------------------------------------------------------

create index resource_labels__resource_unit_resource_id on ontology.resource_labels(resource_unit, resource_id);
create index resource_labels__label on ontology.resource_labels(label);
grant select on ontology.resource_labels to sparql;

--------------------------------------------------------------------------------

create index resource_comments__resource_unit_resource_id on ontology.resource_comments(resource_unit, resource_id);
create index resource_comments__comment on ontology.resource_comments using hash (comment);
grant select on ontology.resource_comments to sparql;

--------------------------------------------------------------------------------

create index resource_see_alsos__resource_unit_resource_id on ontology.resource_see_alsos(resource_unit, resource_id);
create index resource_see_alsos__see_also_unit_see_also_id on ontology.resource_see_alsos(see_also_unit, see_also_id);
create index resource_see_alsos__see_also_string on ontology.resource_see_alsos using hash (see_also_string);
grant select on ontology.resource_see_alsos to sparql;

--------------------------------------------------------------------------------

create index resource_definers__resource_unit_resource_id on ontology.resource_definers(resource_unit, resource_id);
create index resource_definers__definer_unit_definer_id on ontology.resource_definers(definer_unit, definer_id);
create index resource_definers__definer_string on ontology.resource_definers(definer_string);
grant select on ontology.resource_definers to sparql;

--------------------------------------------------------------------------------

create index resource_deprecated_flags__resource_unit_resource_id on ontology.resource_deprecated_flags(resource_unit, resource_id);
create index resource_deprecated_flags__flag on ontology.resource_deprecated_flags(flag);
grant select on ontology.resource_deprecated_flags to sparql;

--------------------------------------------------------------------------------

create index superclasses__class_unit_class_id on ontology.superclasses(class_unit, class_id);
create index superclasses__superclass_unit_superclass_id on ontology.superclasses(superclass_unit, superclass_id);
grant select on ontology.superclasses to sparql;

--------------------------------------------------------------------------------

create index superproperties__property_unit_property_id on ontology.superproperties(property_unit, property_id);
create index superproperties__superproperty_unit_superproperty_id on ontology.superproperties(superproperty_unit, superproperty_id);
grant select on ontology.superproperties to sparql;

--------------------------------------------------------------------------------

create index property_domains__property_unit_property_id on ontology.property_domains(property_unit, property_id);
create index property_domains__domain_unit_domain_id on ontology.property_domains(domain_unit, domain_id);
grant select on ontology.property_domains to sparql;

--------------------------------------------------------------------------------

create index property_ranges__property_unit_property_id on ontology.property_ranges(property_unit, property_id);
create index property_ranges__range_unit_range_id on ontology.property_ranges(range_unit, range_id);
grant select on ontology.property_ranges to sparql;

--------------------------------------------------------------------------------

create index somevaluesfrom_restrictions__property_unit_property_id on ontology.somevaluesfrom_restrictions(property_unit, property_id);
create index somevaluesfrom_restrictions__class_unit_class_id on ontology.somevaluesfrom_restrictions(class_unit, class_id);
grant select on ontology.somevaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index allvaluesfrom_restrictions__property_unit_property_id on ontology.allvaluesfrom_restrictions(property_unit, property_id);
create index allvaluesfrom_restrictions__class_unit_class_id on ontology.allvaluesfrom_restrictions(class_unit, class_id);
grant select on ontology.allvaluesfrom_restrictions to sparql;

--------------------------------------------------------------------------------

create index cardinality_restrictions__property_unit_property_id on ontology.cardinality_restrictions(property_unit, property_id);
create index cardinality_restrictions__cardinality on ontology.cardinality_restrictions(cardinality);
grant select on ontology.cardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index mincardinality_restrictions__property_unit_property_id on ontology.mincardinality_restrictions(property_unit, property_id);
create index mincardinality_restrictions__cardinality on ontology.mincardinality_restrictions(cardinality);
grant select on ontology.mincardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index maxcardinality_restrictions__property_unit_property_id on ontology.maxcardinality_restrictions(property_unit, property_id);
create index maxcardinality_restrictions__cardinality on ontology.maxcardinality_restrictions(cardinality);
grant select on ontology.maxcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index resource_types__resource_unit_resource_id on ontology.resource_types(resource_unit, resource_id);
create index resource_types__type_unit_type_id on ontology.resource_types(type_unit, type_id);
grant select on ontology.resource_types to sparql;

--------------------------------------------------------------------------------

create index class_equivalents__class_unit_class_id on ontology.class_equivalents(class_unit, class_id);
create index class_equivalents__equivalent_unit_equivalent_id on ontology.class_equivalents(equivalent_unit, equivalent_id);
grant select on ontology.class_equivalents to sparql;

--------------------------------------------------------------------------------

create index class_disjoints__class_unit_class_id on ontology.class_disjoints(class_unit, class_id);
create index class_disjoints__disjoint_unit_disjoint_id on ontology.class_disjoints(disjoint_unit, disjoint_id);
grant select on ontology.class_disjoints to sparql;

--------------------------------------------------------------------------------

create index class_complements__class_unit_class_id on ontology.class_complements(class_unit, class_id);
create index class_complements__complement_unit_complement_id on ontology.class_complements(complement_unit, complement_id);
grant select on ontology.class_complements to sparql;

--------------------------------------------------------------------------------

create index class_intersections__class_unit_class_id on ontology.class_intersections(class_unit, class_id);
create index class_intersections__list_unit_list_id on ontology.class_intersections(list_unit, list_id);
grant select on ontology.class_intersections to sparql;

--------------------------------------------------------------------------------

create index class_unions__class_unit_class_id on ontology.class_unions(class_unit, class_id);
create index class_unions__list_unit_list_id on ontology.class_unions(list_unit, list_id);
grant select on ontology.class_unions to sparql;

--------------------------------------------------------------------------------

create index class_enumerations__class_unit_class_id on ontology.class_enumerations(class_unit, class_id);
create index class_enumerations__list_unit_list_id on ontology.class_enumerations(list_unit, list_id);
grant select on ontology.class_enumerations to sparql;

--------------------------------------------------------------------------------

create index class_disjoint_unions__class_unit_class_id on ontology.class_disjoint_unions(class_unit, class_id);
create index class_disjoint_unions__list_unit_list_id on ontology.class_disjoint_unions(list_unit, list_id);
grant select on ontology.class_disjoint_unions to sparql;

--------------------------------------------------------------------------------

create index class_keys__class_unit_class_id on ontology.class_keys(class_unit, class_id);
create index class_keys__list_unit_list_id on ontology.class_keys(list_unit, list_id);
grant select on ontology.class_keys to sparql;

--------------------------------------------------------------------------------

create index property_inverses__property_unit_property_id on ontology.property_inverses(property_unit, property_id);
create index property_inverses__inverse_unit_inverse_id on ontology.property_inverses(inverse_unit, inverse_id);
grant select on ontology.property_inverses to sparql;

--------------------------------------------------------------------------------

create index property_equivalents__property_unit_property_id on ontology.property_equivalents(property_unit, property_id);
create index property_equivalents__equivalent_unit_equivalent_id on ontology.property_equivalents(equivalent_unit, equivalent_id);
grant select on ontology.property_equivalents to sparql;

--------------------------------------------------------------------------------

create index property_disjoints__property_unit_property_id on ontology.property_disjoints(property_unit, property_id);
create index property_disjoints__disjoint_unit_disjoint_id on ontology.property_disjoints(disjoint_unit, disjoint_id);
grant select on ontology.property_disjoints to sparql;

--------------------------------------------------------------------------------

create index property_chains__property_unit_property_id on ontology.property_chains(property_unit, property_id);
create index property_chains__list_unit_list_id on ontology.property_chains(list_unit, list_id);
grant select on ontology.property_chains to sparql;

--------------------------------------------------------------------------------

create index different_individuals__individual_unit_individual_id on ontology.different_individuals(individual_unit, individual_id);
create index different_individuals__different_unit_different_id on ontology.different_individuals(different_unit, different_id);
grant select on ontology.different_individuals to sparql;

--------------------------------------------------------------------------------

create index resource_members__resource_unit_resource_id on ontology.resource_members(resource_unit, resource_id);
create index resource_members__list_unit_list_id on ontology.resource_members(list_unit, list_id);
grant select on ontology.resource_members to sparql;

--------------------------------------------------------------------------------

create index resource_distinct_members__resource_unit_resource_id on ontology.resource_distinct_members(resource_unit, resource_id);
create index resource_distinct_members__list_unit_list_id on ontology.resource_distinct_members(list_unit, list_id);
grant select on ontology.resource_distinct_members to sparql;

--------------------------------------------------------------------------------

create index datatype_bases__datatype_unit_datatype_id on ontology.datatype_bases(datatype_unit, datatype_id);
create index datatype_bases__base_unit_base_id on ontology.datatype_bases(base_unit, base_id);
grant select on ontology.datatype_bases to sparql;

--------------------------------------------------------------------------------

create index datatype_restrictions__datatype_unit_datatype_id on ontology.datatype_restrictions(datatype_unit, datatype_id);
create index datatype_restrictions__list_unit_list_id on ontology.datatype_restrictions(list_unit, list_id);
grant select on ontology.datatype_restrictions to sparql;

--------------------------------------------------------------------------------

create index same_individuals__individual_unit_individual_id on ontology.same_individuals(individual_unit, individual_id);
create index same_individuals__same_unit_same_id on ontology.same_individuals(same_unit, same_id);
create index same_individuals__same_string on ontology.same_individuals(same_string);
grant select on ontology.same_individuals to sparql;

--------------------------------------------------------------------------------

create index lists__first_unit_first_id on ontology.lists(first_unit, first_id);
create index lists__first_string on ontology.lists(first_string);
create index lists__first_integer on ontology.lists(first_integer);
create index lists__first_float on ontology.lists(first_float);
create index lists__rest_unit_rest_id on ontology.lists(rest_unit, rest_id);
grant select on ontology.lists to sparql;

--------------------------------------------------------------------------------

create index hasvalue_restrictions__property_unit_property_id on ontology.hasvalue_restrictions(property_unit, property_id);
create index hasvalue_restrictions__value_unit_value_id on ontology.hasvalue_restrictions(value_unit, value_id);
create index hasvalue_restrictions__value_string on ontology.hasvalue_restrictions(value_string);
create index hasvalue_restrictions__value_integer on ontology.hasvalue_restrictions(value_integer);
create index hasvalue_restrictions__value_float on ontology.hasvalue_restrictions(value_float);
create index hasvalue_restrictions__value_boolean on ontology.hasvalue_restrictions(value_boolean);
grant select on ontology.hasvalue_restrictions to sparql;

--------------------------------------------------------------------------------

create index hasself_restrictions__property_unit_property_id on ontology.hasself_restrictions(property_unit, property_id);
create index hasself_restrictions__value on ontology.hasself_restrictions(value);
grant select on ontology.hasself_restrictions to sparql;

--------------------------------------------------------------------------------

create index qualifiedcardinality_restrictions__property_unit_property_id on ontology.qualifiedcardinality_restrictions(property_unit, property_id);
create index qualifiedcardinality_restrictions__cardinality on ontology.qualifiedcardinality_restrictions(cardinality);
grant select on ontology.qualifiedcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index minqualifiedcardinality_restrictions__property_unit_property_id on ontology.minqualifiedcardinality_restrictions(property_unit, property_id);
create index minqualifiedcardinality_restrictions__cardinality on ontology.minqualifiedcardinality_restrictions(cardinality);
grant select on ontology.minqualifiedcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index maxqualifiedcardinality_restrictions__property_unit_property_id on ontology.maxqualifiedcardinality_restrictions(property_unit, property_id);
create index maxqualifiedcardinality_restrictions__cardinality on ontology.maxqualifiedcardinality_restrictions(cardinality);
grant select on ontology.maxqualifiedcardinality_restrictions to sparql;

--------------------------------------------------------------------------------

create index incomplete_restrictions__property_unit_property_id on ontology.incomplete_restrictions(property_unit, property_id);
grant select on ontology.incomplete_restrictions to sparql;

--------------------------------------------------------------------------------

create index restriction_classes__class_unit_class_id on ontology.restriction_classes(class_unit, class_id);
grant select on ontology.restriction_classes to sparql;

--------------------------------------------------------------------------------

create index restriction_dataranges__datarange_unit_datarange_id on ontology.restriction_dataranges(datarange_unit, datarange_id);
grant select on ontology.restriction_dataranges to sparql;

--------------------------------------------------------------------------------

create index facet_restrictions__restriction_unit_restriction_id on ontology.facet_restrictions(restriction_unit, restriction_id);
create index facet_restrictions__facet on ontology.facet_restrictions(facet);
create index facet_restrictions__value_string on ontology.facet_restrictions(value_string);
create index facet_restrictions__value_integer on ontology.facet_restrictions(value_integer);
create index facet_restrictions__value_double on ontology.facet_restrictions(value_double);
grant select on ontology.facet_restrictions to sparql;

--------------------------------------------------------------------------------

create index ontology_relations__ontology_unit_ontology_id on ontology.ontology_relations(ontology_unit, ontology_id);
create index ontology_relations__property on ontology.ontology_relations(property);
create index ontology_relations__target_unit_target_id on ontology.ontology_relations(target_unit, target_id);
create index ontology_relations__target_string on ontology.ontology_relations(target_string);
grant select on ontology.ontology_relations to sparql;

--------------------------------------------------------------------------------

create index uncategorized_resources__iri on ontology.uncategorized_resources using hash (iri);
grant select on ontology.uncategorized_resources to sparql;

--------------------------------------------------------------------------------

grant select on ontology.units to sparql;
