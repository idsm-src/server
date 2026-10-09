create table mesh.resources
(
    id          varchar not null,
    type_id     integer,
    primary key(id)
);


create table mesh.resource_alt_labels
(
    resource    varchar not null,
    label       varchar not null,
    primary key(resource, label)
);


create table mesh.resource_previous_indexing_values
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource, value)
);


create table mesh.resource_sources
(
    resource    varchar not null,
    source      varchar not null,
    primary key(resource, source)
);


create table mesh.resource_thesauruses
(
    resource    varchar not null,
    thesaurus   varchar not null,
    primary key(resource, thesaurus)
);


create table mesh.resource_labels
(
    resource    varchar not null,
    label       varchar not null,
    primary key(resource, label)
);


create table mesh.resource_abbreviations
(
    resource        varchar not null,
    abbreviation    varchar not null,
    primary key(resource)
);


create table mesh.resource_annotations
(
    resource    varchar not null,
    annotation  varchar not null,
    primary key(resource)
);


create table mesh.resource_casn1_labels
(
    resource    varchar not null,
    label       varchar not null,
    primary key(resource)
);


create table mesh.resource_consider_also_values
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource)
);


create table mesh.resource_entry_versions
(
    resource    varchar not null,
    version     varchar not null,
    primary key(resource)
);


create table mesh.resource_history_notes
(
    resource    varchar not null,
    note        varchar not null,
    primary key(resource)
);


create table mesh.resource_last_active_years
(
    resource    varchar not null,
    year        varchar not null,
    primary key(resource)
);


create table mesh.resource_lexical_tags
(
    resource    varchar not null,
    tag         varchar not null,
    primary key(resource)
);


create table mesh.resource_notes
(
    resource    varchar not null,
    note        varchar not null,
    primary key(resource)
);


create table mesh.resource_online_notes
(
    resource    varchar not null,
    note        varchar not null,
    primary key(resource)
);


create table mesh.resource_pref_labels
(
    resource    varchar not null,
    label       varchar not null,
    primary key(resource)
);


create table mesh.resource_public_mesh_notes
(
    resource    varchar not null,
    note        varchar not null,
    primary key(resource)
);


create table mesh.resource_scope_notes
(
    resource    varchar not null,
    note        varchar not null,
    primary key(resource)
);


create table mesh.resource_sort_versions
(
    resource    varchar not null,
    version     varchar not null,
    primary key(resource)
);


create table mesh.resource_related_registry_numbers
(
    resource    varchar not null,
    number      varchar not null,
    primary key(resource, number)
);


create table mesh.resource_identifiers
(
    resource    varchar not null,
    identifier  varchar not null,
    primary key(resource)
);


create table mesh.resource_nlm_classification_numbers
(
    resource    varchar not null,
    number      varchar not null,
    primary key(resource)
);


create table mesh.resource_registry_numbers
(
    resource    varchar not null,
    number      varchar not null,
    primary key(resource)
);


create table mesh.resource_created_dates
(
    resource    varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(resource)
);


create table mesh.resource_revised_dates
(
    resource    varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(resource)
);


create table mesh.resource_established_dates
(
    resource    varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(resource)
);


create table mesh.resource_active_flags
(
    resource    varchar not null,
    flag        boolean not null,
    primary key(resource)
);


create table mesh.resource_frequencies
(
    resource    varchar not null,
    frequency   integer not null,
    primary key(resource)
);


create table mesh.resource_allowable_qualifiers
(
    resource    varchar not null,
    qualifier   varchar not null,
    primary key(resource, qualifier)
);


create table mesh.resource_broader_concepts
(
    resource    varchar not null,
    concept     varchar not null,
    primary key(resource, concept)
);


create table mesh.resource_broader_descriptors
(
    resource    varchar not null,
    descriptor  varchar not null,
    primary key(resource, descriptor)
);


create table mesh.resource_broader_qualifiers
(
    resource    varchar not null,
    qualifier   varchar not null,
    primary key(resource, qualifier)
);


create table mesh.resource_concepts
(
    resource    varchar not null,
    concept     varchar not null,
    primary key(resource, concept)
);


create table mesh.resource_indexer_consider_also_relations
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource, value)
);


create table mesh.resource_mapped_to_relations
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource, value)
);


create table mesh.resource_narrower_concepts
(
    resource    varchar not null,
    concept     varchar not null,
    primary key(resource, concept)
);


create table mesh.resource_pharmacological_actions
(
    resource    varchar not null,
    action      varchar not null,
    primary key(resource, action)
);


create table mesh.resource_preferred_mapped_to_relations
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource, value)
);


create table mesh.resource_related_concepts
(
    resource    varchar not null,
    concept     varchar not null,
    primary key(resource, concept)
);


create table mesh.resource_see_also_relations
(
    resource    varchar not null,
    reference   varchar not null,
    primary key(resource, reference)
);


create table mesh.resource_terms
(
    resource    varchar not null,
    term        varchar not null,
    primary key(resource, term)
);


create table mesh.resource_tree_numbers
(
    resource    varchar not null,
    number      varchar not null,
    primary key(resource, number)
);


create table mesh.resource_descriptors
(
    resource    varchar not null,
    descriptor  varchar not null,
    primary key(resource)
);


create table mesh.resource_qualifiers
(
    resource    varchar not null,
    qualifier   varchar not null,
    primary key(resource)
);


create table mesh.resource_parent_tree_numbers
(
    resource    varchar not null,
    number      varchar not null,
    primary key(resource)
);


create table mesh.resource_preferred_concepts
(
    resource    varchar not null,
    concept     varchar not null,
    primary key(resource)
);


create table mesh.resource_preferred_terms
(
    resource    varchar not null,
    term        varchar not null,
    primary key(resource)
);


create table mesh.resource_use_instead_relations
(
    resource    varchar not null,
    value       varchar not null,
    primary key(resource)
);
