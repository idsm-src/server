create table substance_bases
(
    id           integer not null,
    source       smallint,
    available    datetime,
    modified     datetime,
    compound     integer,
    primary key(id)
);


create table substance_types
(
    substance    integer not null,
    chebi        integer not null,
    primary key(substance, chebi)
);


create table substance_matches
(
    substance    integer not null,
    match        integer not null,
    primary key(substance, match)
);


create table substance_references
(
    substance    integer not null,
    reference    integer not null,
    primary key(substance, reference)
);


create table substance_pdblinks
(
    substance    integer not null,
    pdblink      char(4) not null,
    primary key(substance, pdblink)
);


create table substance_synonyms
(
    substance    integer not null,
    synonym      integer not null,
    primary key(substance, synonym)
);
