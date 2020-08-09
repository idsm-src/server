create table pubchem.biosystem_bases
(
    id             integer not null,
    source         smallint not null,
    title          varchar not null,
    organism_id    integer,
    primary key(id)
);


create table pubchem.biosystem_components
(
    biosystem    integer not null,
    component    integer not null,
    primary key(biosystem, component)
);


create table pubchem.biosystem_references
(
    biosystem    integer not null,
    reference    integer not null,
    primary key(biosystem, reference)
);


create table pubchem.biosystem_matches
(
    biosystem      integer not null,
    wikipathway    integer not null,
    primary key(biosystem)
);
