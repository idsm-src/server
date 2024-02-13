create table pubchem.anatomy_bases
(
    id            integer not null,
    label         varchar,
    primary key(id)
);


create table pubchem.anatomy_alternatives
(
    anatomy       integer not null,
    alternative   varchar not null,
    primary key(anatomy, alternative)
);


create table pubchem.anatomy_matches
(
    anatomy       integer not null,
    match_unit    smallint not null,
    match_id      integer not null,
    primary key(anatomy, match_unit, match_id)
);


create table pubchem.anatomy_chembl_matches
(
    anatomy       integer not null,
    match         integer not null,
    primary key(anatomy, match)
);


create table pubchem.anatomy_nextprot_matches
(
    anatomy       integer not null,
    match         integer not null,
    primary key(anatomy, match)
);


create table pubchem.anatomy_mesh_matches
(
    anatomy       integer not null,
    match         varchar not null,
    primary key(anatomy, match)
);

