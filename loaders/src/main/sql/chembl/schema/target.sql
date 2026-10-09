create table chembl.targets
(
    id             integer not null,
    chembl_id      varchar,
    type           varchar,
    label          varchar,
    organism       varchar,
    taxonomy       integer,
    cell_line      integer,
    species_group  boolean,
    primary key(id)
);


create table chembl.target_components
(
    target     integer not null,
    component  integer not null,
    primary key(target, component)
);


create table chembl.target_exact_matches
(
    target     integer not null,
    component  integer not null,
    primary key(target, component)
);


create table chembl.target_related_matches
(
    target     integer not null,
    component  integer not null,
    primary key(target, component)
);


create type chembl.target_relationship_type as enum
(
    'EQUIVALENT TO',
    'OVERLAPS WITH',
    'SUBSET OF',
    'SUPERSET OF'
);


create table chembl.target_relations
(
    target        integer not null,
    relationship  chembl.target_relationship_type not null,
    related       integer not null,
    primary key(target, relationship, related)
);
