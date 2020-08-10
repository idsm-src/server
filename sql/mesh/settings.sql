grant usage on schema mesh to sparql;

--------------------------------------------------------------------------------

create index mesh_bases__type_id on mesh.mesh_bases(type_id);
grant select on mesh.mesh_bases to sparql;

--------------------------------------------------------------------------------

create index alt_labels__mesh on mesh.alt_labels(mesh);
create index alt_labels__label on mesh.alt_labels(label);
grant select on mesh.alt_labels to sparql;

--------------------------------------------------------------------------------

create index previous_indexing_values__mesh on mesh.previous_indexing_values(mesh);
create index previous_indexing_values__value on mesh.previous_indexing_values(value);
grant select on mesh.previous_indexing_values to sparql;

--------------------------------------------------------------------------------

create index sources__mesh on mesh.sources(mesh);
create index sources__source on mesh.sources(source);
grant select on mesh.sources to sparql;

--------------------------------------------------------------------------------

create index thesauruses__mesh on mesh.thesauruses(mesh);
create index thesauruses__thesaurus on mesh.thesauruses(thesaurus);
grant select on mesh.thesauruses to sparql;

--------------------------------------------------------------------------------

create index labels__mesh on mesh.labels(mesh);
create index labels__label on mesh.labels(label);
grant select on mesh.labels to sparql;

--------------------------------------------------------------------------------

create index abbreviations__abbreviation on mesh.abbreviations(abbreviation);
grant select on mesh.abbreviations to sparql;

--------------------------------------------------------------------------------

create index annotations__annotation on mesh.annotations(annotation);
grant select on mesh.annotations to sparql;

--------------------------------------------------------------------------------

create index casn1_labels__label on mesh.casn1_labels(label);
grant select on mesh.casn1_labels to sparql;

--------------------------------------------------------------------------------

create index consider_also_values__value on mesh.consider_also_values(value);
grant select on mesh.consider_also_values to sparql;

--------------------------------------------------------------------------------

create index entry_versions__version on mesh.entry_versions(version);
grant select on mesh.entry_versions to sparql;

--------------------------------------------------------------------------------

create index history_notes__note on mesh.history_notes(note);
grant select on mesh.history_notes to sparql;

--------------------------------------------------------------------------------

create index last_active_years__year on mesh.last_active_years(year);
grant select on mesh.last_active_years to sparql;

--------------------------------------------------------------------------------

create index lexical_tags__tag on mesh.lexical_tags(tag);
grant select on mesh.lexical_tags to sparql;

--------------------------------------------------------------------------------

create index notese_notes__note on mesh.notese_notes(note);
grant select on mesh.notese_notes to sparql;

--------------------------------------------------------------------------------

create index online_notes__note on mesh.online_notes(note);
grant select on mesh.online_notes to sparql;

--------------------------------------------------------------------------------

create index pref_labels__label on mesh.pref_labels(label);
grant select on mesh.pref_labels to sparql;

--------------------------------------------------------------------------------

create index public_mesh_notes__note on mesh.public_mesh_notes(note);
grant select on mesh.public_mesh_notes to sparql;

--------------------------------------------------------------------------------

create index scope_notes__note on mesh.scope_notes(note);
grant select on mesh.scope_notes to sparql;

--------------------------------------------------------------------------------

create index sort_versions__version on mesh.sort_versions(version);
grant select on mesh.sort_versions to sparql;

--------------------------------------------------------------------------------

create index related_registry_numbers__mesh on mesh.related_registry_numbers(mesh);
create index related_registry_numbers__number on mesh.related_registry_numbers(number);
grant select on mesh.related_registry_numbers to sparql;

--------------------------------------------------------------------------------

create index identifiers__identifier on mesh.identifiers(identifier);
grant select on mesh.identifiers to sparql;

--------------------------------------------------------------------------------

create index nlm_cassification_numbers__number on mesh.nlm_cassification_numbers(number);
grant select on mesh.nlm_cassification_numbers to sparql;

--------------------------------------------------------------------------------

create index registry_numbers__number on mesh.registry_numbers(number);
grant select on mesh.registry_numbers to sparql;

--------------------------------------------------------------------------------

create index created_dates__date_timezone on mesh.created_dates(date, timezone);
grant select on mesh.created_dates to sparql;

--------------------------------------------------------------------------------

create index revised_dates__date_timezone on mesh.revised_dates(date, timezone);
grant select on mesh.revised_dates to sparql;

--------------------------------------------------------------------------------

create index established_dates__date_timezone on mesh.established_dates(date, timezone);
grant select on mesh.established_dates to sparql;

--------------------------------------------------------------------------------

create index active_property__value on mesh.active_property(value);
grant select on mesh.active_property to sparql;

--------------------------------------------------------------------------------

create index frequencies__frequency on mesh.frequencies(frequency);
grant select on mesh.frequencies to sparql;

--------------------------------------------------------------------------------

create index allowable_qualifiers__mesh on mesh.allowable_qualifiers(mesh);
create index allowable_qualifiers__qualifier on mesh.allowable_qualifiers(qualifier);
grant select on mesh.allowable_qualifiers to sparql;

--------------------------------------------------------------------------------

create index broader_concepts__mesh on mesh.broader_concepts(mesh);
create index broader_concepts__concept on mesh.broader_concepts(concept);
grant select on mesh.broader_concepts to sparql;

--------------------------------------------------------------------------------

create index broader_descriptors__mesh on mesh.broader_descriptors(mesh);
create index broader_descriptors__descriptor on mesh.broader_descriptors(descriptor);
grant select on mesh.broader_descriptors to sparql;

--------------------------------------------------------------------------------

create index broader_qualifiers__mesh on mesh.broader_qualifiers(mesh);
create index broader_qualifiers__qualifier on mesh.broader_qualifiers(qualifier);
grant select on mesh.broader_qualifiers to sparql;

--------------------------------------------------------------------------------

create index concepts__mesh on mesh.concepts(mesh);
create index concepts__concept on mesh.concepts(concept);
grant select on mesh.concepts to sparql;

--------------------------------------------------------------------------------

create index indexer_consider_also_relations__mesh on mesh.indexer_consider_also_relations(mesh);
create index indexer_consider_also_relations__value on mesh.indexer_consider_also_relations(value);
grant select on mesh.indexer_consider_also_relations to sparql;

--------------------------------------------------------------------------------

create index mapped_to_relations__mesh on mesh.mapped_to_relations(mesh);
create index mapped_to_relations__value on mesh.mapped_to_relations(value);
grant select on mesh.mapped_to_relations to sparql;

--------------------------------------------------------------------------------

create index narrower_concepts__mesh on mesh.narrower_concepts(mesh);
create index narrower_concepts__concept on mesh.narrower_concepts(concept);
grant select on mesh.narrower_concepts to sparql;

--------------------------------------------------------------------------------

create index pharmacological_actions__mesh on mesh.pharmacological_actions(mesh);
create index pharmacological_actions__action on mesh.pharmacological_actions(action);
grant select on mesh.pharmacological_actions to sparql;

--------------------------------------------------------------------------------

create index preferred_mapped_to_relations__mesh on mesh.preferred_mapped_to_relations(mesh);
create index preferred_mapped_to_relations__value on mesh.preferred_mapped_to_relations(value);
grant select on mesh.preferred_mapped_to_relations to sparql;

--------------------------------------------------------------------------------

create index related_concepts__mesh on mesh.related_concepts(mesh);
create index related_concepts__concept on mesh.related_concepts(concept);
grant select on mesh.related_concepts to sparql;

--------------------------------------------------------------------------------

create index see_also_relations__mesh on mesh.see_also_relations(mesh);
create index see_also_relations__reference on mesh.see_also_relations(reference);
grant select on mesh.see_also_relations to sparql;

--------------------------------------------------------------------------------

create index terms__mesh on mesh.terms(mesh);
create index terms__term on mesh.terms(term);
grant select on mesh.terms to sparql;

--------------------------------------------------------------------------------

create index tree_numbers__mesh on mesh.tree_numbers(mesh);
create index tree_numbers__number on mesh.tree_numbers(number);
grant select on mesh.tree_numbers to sparql;

--------------------------------------------------------------------------------

create index descriptors__descriptor on mesh.descriptors(descriptor);
grant select on mesh.descriptors to sparql;

--------------------------------------------------------------------------------

create index qualifiers__qualifier on mesh.qualifiers(qualifier);
grant select on mesh.qualifiers to sparql;

--------------------------------------------------------------------------------

create index parent_tree_numbers__number on mesh.parent_tree_numbers(number);
grant select on mesh.parent_tree_numbers to sparql;

--------------------------------------------------------------------------------

create index preferred_concept__concept on mesh.preferred_concept(concept);
grant select on mesh.preferred_concept to sparql;

--------------------------------------------------------------------------------

create index preferred_term__term on mesh.preferred_term(term);
grant select on mesh.preferred_term to sparql;

--------------------------------------------------------------------------------

create index use_instead_relations__value on mesh.use_instead_relations(value);
grant select on mesh.use_instead_relations to sparql;
