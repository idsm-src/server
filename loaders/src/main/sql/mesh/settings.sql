create index resources__type_id on mesh.resources(type_id);
grant select on mesh.resources to sparql;

--------------------------------------------------------------------------------

create index resource_alt_labels__resource on mesh.resource_alt_labels(resource);
create index resource_alt_labels__label on mesh.resource_alt_labels(label);
grant select on mesh.resource_alt_labels to sparql;

--------------------------------------------------------------------------------

create index resource_previous_indexing_values__resource on mesh.resource_previous_indexing_values(resource);
create index resource_previous_indexing_values__value on mesh.resource_previous_indexing_values(value);
grant select on mesh.resource_previous_indexing_values to sparql;

--------------------------------------------------------------------------------

create index resource_sources__resource on mesh.resource_sources(resource);
create index resource_sources__source on mesh.resource_sources(source);
grant select on mesh.resource_sources to sparql;

--------------------------------------------------------------------------------

create index resource_thesauruses__resource on mesh.resource_thesauruses(resource);
create index resource_thesauruses__thesaurus on mesh.resource_thesauruses(thesaurus);
grant select on mesh.resource_thesauruses to sparql;

--------------------------------------------------------------------------------

create index resource_labels__resource on mesh.resource_labels(resource);
create index resource_labels__label on mesh.resource_labels(label);
grant select on mesh.resource_labels to sparql;

--------------------------------------------------------------------------------

create index resource_abbreviations__abbreviation on mesh.resource_abbreviations(abbreviation);
grant select on mesh.resource_abbreviations to sparql;

--------------------------------------------------------------------------------

create index resource_annotations__annotation on mesh.resource_annotations(annotation);
grant select on mesh.resource_annotations to sparql;

--------------------------------------------------------------------------------

create index resource_casn1_labels__label on mesh.resource_casn1_labels(label);
grant select on mesh.resource_casn1_labels to sparql;

--------------------------------------------------------------------------------

create index resource_consider_also_values__value on mesh.resource_consider_also_values(value);
grant select on mesh.resource_consider_also_values to sparql;

--------------------------------------------------------------------------------

create index resource_entry_versions__version on mesh.resource_entry_versions(version);
grant select on mesh.resource_entry_versions to sparql;

--------------------------------------------------------------------------------

create index resource_history_notes__note on mesh.resource_history_notes(note);
grant select on mesh.resource_history_notes to sparql;

--------------------------------------------------------------------------------

create index resource_last_active_years__year on mesh.resource_last_active_years(year);
grant select on mesh.resource_last_active_years to sparql;

--------------------------------------------------------------------------------

create index resource_lexical_tags__tag on mesh.resource_lexical_tags(tag);
grant select on mesh.resource_lexical_tags to sparql;

--------------------------------------------------------------------------------

create index resource_notes__note on mesh.resource_notes(note);
grant select on mesh.resource_notes to sparql;

--------------------------------------------------------------------------------

create index resource_online_notes__note on mesh.resource_online_notes(note);
grant select on mesh.resource_online_notes to sparql;

--------------------------------------------------------------------------------

create index resource_pref_labels__label on mesh.resource_pref_labels(label);
grant select on mesh.resource_pref_labels to sparql;

--------------------------------------------------------------------------------

create index resource_public_mesh_notes__note on mesh.resource_public_mesh_notes(note);
grant select on mesh.resource_public_mesh_notes to sparql;

--------------------------------------------------------------------------------

create index resource_scope_notes__note on mesh.resource_scope_notes(note);
grant select on mesh.resource_scope_notes to sparql;

--------------------------------------------------------------------------------

create index resource_sort_versions__version on mesh.resource_sort_versions(version);
grant select on mesh.resource_sort_versions to sparql;

--------------------------------------------------------------------------------

create index resource_related_registry_numbers__resource on mesh.resource_related_registry_numbers(resource);
create index resource_related_registry_numbers__number on mesh.resource_related_registry_numbers(number);
grant select on mesh.resource_related_registry_numbers to sparql;

--------------------------------------------------------------------------------

create index resource_identifiers__identifier on mesh.resource_identifiers(identifier);
grant select on mesh.resource_identifiers to sparql;

--------------------------------------------------------------------------------

create index resource_nlm_classification_numbers__number on mesh.resource_nlm_classification_numbers(number);
grant select on mesh.resource_nlm_classification_numbers to sparql;

--------------------------------------------------------------------------------

create index resource_registry_numbers__number on mesh.resource_registry_numbers(number);
grant select on mesh.resource_registry_numbers to sparql;

--------------------------------------------------------------------------------

create index resource_created_dates__date_timezone on mesh.resource_created_dates(date, timezone);
grant select on mesh.resource_created_dates to sparql;

--------------------------------------------------------------------------------

create index resource_revised_dates__date_timezone on mesh.resource_revised_dates(date, timezone);
grant select on mesh.resource_revised_dates to sparql;

--------------------------------------------------------------------------------

create index resource_established_dates__date_timezone on mesh.resource_established_dates(date, timezone);
grant select on mesh.resource_established_dates to sparql;

--------------------------------------------------------------------------------

create index resource_active_flags__flag on mesh.resource_active_flags(flag);
grant select on mesh.resource_active_flags to sparql;

--------------------------------------------------------------------------------

create index resource_frequencies__frequency on mesh.resource_frequencies(frequency);
grant select on mesh.resource_frequencies to sparql;

--------------------------------------------------------------------------------

create index resource_allowable_qualifiers__resource on mesh.resource_allowable_qualifiers(resource);
create index resource_allowable_qualifiers__qualifier on mesh.resource_allowable_qualifiers(qualifier);
grant select on mesh.resource_allowable_qualifiers to sparql;

--------------------------------------------------------------------------------

create index resource_broader_concepts__resource on mesh.resource_broader_concepts(resource);
create index resource_broader_concepts__concept on mesh.resource_broader_concepts(concept);
grant select on mesh.resource_broader_concepts to sparql;

--------------------------------------------------------------------------------

create index resource_broader_descriptors__resource on mesh.resource_broader_descriptors(resource);
create index resource_broader_descriptors__descriptor on mesh.resource_broader_descriptors(descriptor);
grant select on mesh.resource_broader_descriptors to sparql;

--------------------------------------------------------------------------------

create index resource_broader_qualifiers__resource on mesh.resource_broader_qualifiers(resource);
create index resource_broader_qualifiers__qualifier on mesh.resource_broader_qualifiers(qualifier);
grant select on mesh.resource_broader_qualifiers to sparql;

--------------------------------------------------------------------------------

create index resource_concepts__resource on mesh.resource_concepts(resource);
create index resource_concepts__concept on mesh.resource_concepts(concept);
grant select on mesh.resource_concepts to sparql;

--------------------------------------------------------------------------------

create index resource_indexer_consider_also_relations__resource on mesh.resource_indexer_consider_also_relations(resource);
create index resource_indexer_consider_also_relations__value on mesh.resource_indexer_consider_also_relations(value);
grant select on mesh.resource_indexer_consider_also_relations to sparql;

--------------------------------------------------------------------------------

create index resource_mapped_to_relations__resource on mesh.resource_mapped_to_relations(resource);
create index resource_mapped_to_relations__value on mesh.resource_mapped_to_relations(value);
grant select on mesh.resource_mapped_to_relations to sparql;

--------------------------------------------------------------------------------

create index resource_narrower_concepts__resource on mesh.resource_narrower_concepts(resource);
create index resource_narrower_concepts__concept on mesh.resource_narrower_concepts(concept);
grant select on mesh.resource_narrower_concepts to sparql;

--------------------------------------------------------------------------------

create index resource_pharmacological_actions__resource on mesh.resource_pharmacological_actions(resource);
create index resource_pharmacological_actions__action on mesh.resource_pharmacological_actions(action);
grant select on mesh.resource_pharmacological_actions to sparql;

--------------------------------------------------------------------------------

create index resource_preferred_mapped_to_relations__resource on mesh.resource_preferred_mapped_to_relations(resource);
create index resource_preferred_mapped_to_relations__value on mesh.resource_preferred_mapped_to_relations(value);
grant select on mesh.resource_preferred_mapped_to_relations to sparql;

--------------------------------------------------------------------------------

create index resource_related_concepts__resource on mesh.resource_related_concepts(resource);
create index resource_related_concepts__concept on mesh.resource_related_concepts(concept);
grant select on mesh.resource_related_concepts to sparql;

--------------------------------------------------------------------------------

create index resource_see_also_relations__resource on mesh.resource_see_also_relations(resource);
create index resource_see_also_relations__reference on mesh.resource_see_also_relations(reference);
grant select on mesh.resource_see_also_relations to sparql;

--------------------------------------------------------------------------------

create index resource_terms__resource on mesh.resource_terms(resource);
create index resource_terms__term on mesh.resource_terms(term);
grant select on mesh.resource_terms to sparql;

--------------------------------------------------------------------------------

create index resource_tree_numbers__resource on mesh.resource_tree_numbers(resource);
create index resource_tree_numbers__number on mesh.resource_tree_numbers(number);
grant select on mesh.resource_tree_numbers to sparql;

--------------------------------------------------------------------------------

create index resource_descriptors__descriptor on mesh.resource_descriptors(descriptor);
grant select on mesh.resource_descriptors to sparql;

--------------------------------------------------------------------------------

create index resource_qualifiers__qualifier on mesh.resource_qualifiers(qualifier);
grant select on mesh.resource_qualifiers to sparql;

--------------------------------------------------------------------------------

create index resource_parent_tree_numbers__number on mesh.resource_parent_tree_numbers(number);
grant select on mesh.resource_parent_tree_numbers to sparql;

--------------------------------------------------------------------------------

create index resource_preferred_concepts__concept on mesh.resource_preferred_concepts(concept);
grant select on mesh.resource_preferred_concepts to sparql;

--------------------------------------------------------------------------------

create index resource_preferred_terms__term on mesh.resource_preferred_terms(term);
grant select on mesh.resource_preferred_terms to sparql;

--------------------------------------------------------------------------------

create index resource_use_instead_relations__value on mesh.resource_use_instead_relations(value);
grant select on mesh.resource_use_instead_relations to sparql;
