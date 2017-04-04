create table biosystem_bases
(
    id          integer not null,
    source      smallint not null,
    title       varchar not null,
    organism    integer,
    primary key(id)
);


create table biosystem_components
(
    biosystem    integer not null,
    component    integer not null,
    primary key(biosystem, component)
);


create table biosystem_references
(
    biosystem    integer not null,
    reference    integer not null,
    primary key(biosystem, reference)
);


create table biosystem_matches
(
    biosystem      integer not null,
    wikipathway    integer not null,
    primary key(biosystem)
);
