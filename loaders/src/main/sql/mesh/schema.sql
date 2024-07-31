create table mesh.mesh_bases
(
    id          varchar not null,
    type_id     integer,
    primary key(id)
);


create table mesh.alt_labels
(
    mesh    varchar not null,
    label   varchar not null,
    primary key(mesh, label)
);


create table mesh.previous_indexing_values
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh, value)
);


create table mesh.sources
(
    mesh    varchar not null,
    source  varchar not null,
    primary key(mesh, source)
);


create table mesh.thesauruses
(
    mesh        varchar not null,
    thesaurus   varchar not null,
    primary key(mesh, thesaurus)
);


create table mesh.labels
(
    mesh    varchar not null,
    label   varchar not null,
    primary key(mesh, label)
);


create table mesh.abbreviations
(
    mesh            varchar not null,
    abbreviation    varchar not null,
    primary key(mesh)
);


create table mesh.annotations
(
    mesh        varchar not null,
    annotation  varchar not null,
    primary key(mesh)
);


create table mesh.casn1_labels
(
    mesh    varchar not null,
    label   varchar not null,
    primary key(mesh)
);


create table mesh.consider_also_values
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh)
);


create table mesh.entry_versions
(
    mesh    varchar not null,
    version varchar not null,
    primary key(mesh)
);


create table mesh.history_notes
(
    mesh    varchar not null,
    note    varchar not null,
    primary key(mesh)
);


create table mesh.last_active_years
(
    mesh    varchar not null,
    year    varchar not null,
    primary key(mesh)
);


create table mesh.lexical_tags
(
    mesh    varchar not null,
    tag     varchar not null,
    primary key(mesh)
);


create table mesh.notese_notes
(
    mesh    varchar not null,
    note    varchar not null,
    primary key(mesh)
);


create table mesh.online_notes
(
    mesh    varchar not null,
    note    varchar not null,
    primary key(mesh)
);


create table mesh.pref_labels
(
    mesh    varchar not null,
    label   varchar not null,
    primary key(mesh)
);


create table mesh.public_mesh_notes
(
    mesh    varchar not null,
    note    varchar not null,
    primary key(mesh)
);


create table mesh.scope_notes
(
    mesh    varchar not null,
    note    varchar not null,
    primary key(mesh)
);


create table mesh.sort_versions
(
    mesh    varchar not null,
    version varchar not null,
    primary key(mesh)
);


create table mesh.related_registry_numbers
(
    mesh    varchar not null,
    number  varchar not null,
    primary key(mesh, number)
);


create table mesh.identifiers
(
    mesh        varchar not null,
    identifier  varchar not null,
    primary key(mesh)
);


create table mesh.nlm_cassification_numbers
(
    mesh    varchar not null,
    number  varchar not null,
    primary key(mesh)
);


create table mesh.registry_numbers
(
    mesh    varchar not null,
    number  varchar not null,
    primary key(mesh)
);


create table mesh.created_dates
(
    mesh        varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(mesh)
);


create table mesh.revised_dates
(
    mesh        varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(mesh)
);


create table mesh.established_dates
(
    mesh        varchar not null,
    date        date not null,
    timezone    integer not null,
    primary key(mesh)
);


create table mesh.active_property
(
    mesh    varchar not null,
    value   boolean not null,
    primary key(mesh)
);


create table mesh.frequencies
(
    mesh        varchar not null,
    frequency   integer not null,
    primary key(mesh)
);


create table mesh.allowable_qualifiers
(
    mesh        varchar not null,
    qualifier   varchar not null,
    primary key(mesh, qualifier)
);


create table mesh.broader_concepts
(
    mesh    varchar not null,
    concept varchar not null,
    primary key(mesh, concept)
);


create table mesh.broader_descriptors
(
    mesh        varchar not null,
    descriptor  varchar not null,
    primary key(mesh, descriptor)
);


create table mesh.broader_qualifiers
(
    mesh        varchar not null,
    qualifier   varchar not null,
    primary key(mesh, qualifier)
);


create table mesh.concepts
(
    mesh    varchar not null,
    concept varchar not null,
    primary key(mesh, concept)
);


create table mesh.indexer_consider_also_relations
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh, value)
);


create table mesh.mapped_to_relations
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh, value)
);


create table mesh.narrower_concepts
(
    mesh    varchar not null,
    concept varchar not null,
    primary key(mesh, concept)
);


create table mesh.pharmacological_actions
(
    mesh    varchar not null,
    action  varchar not null,
    primary key(mesh, action)
);


create table mesh.preferred_mapped_to_relations
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh, value)
);


create table mesh.related_concepts
(
    mesh    varchar not null,
    concept varchar not null,
    primary key(mesh, concept)
);


create table mesh.see_also_relations
(
    mesh        varchar not null,
    reference   varchar not null,
    primary key(mesh, reference)
);


create table mesh.terms
(
    mesh    varchar not null,
    term    varchar not null,
    primary key(mesh, term)
);


create table mesh.tree_numbers
(
    mesh    varchar not null,
    number  varchar not null,
    primary key(mesh, number)
);


create table mesh.descriptors
(
    mesh        varchar not null,
    descriptor  varchar not null,
    primary key(mesh)
);


create table mesh.qualifiers
(
    mesh        varchar not null,
    qualifier   varchar not null,
    primary key(mesh)
);


create table mesh.parent_tree_numbers
(
    mesh    varchar not null,
    number  varchar not null,
    primary key(mesh)
);


create table mesh.preferred_concept
(
    mesh    varchar not null,
    concept varchar not null,
    primary key(mesh)
);


create table mesh.preferred_term
(
    mesh    varchar not null,
    term    varchar not null,
    primary key(mesh)
);


create table mesh.use_instead_relations
(
    mesh    varchar not null,
    value   varchar not null,
    primary key(mesh)
);
