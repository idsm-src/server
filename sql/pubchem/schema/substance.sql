create table pubchem.substance_bases
(
    id           integer not null,
    source       smallint,
    available    date,
    modified     date,
    compound     integer,
    primary key(id)
);


create table pubchem.substance_types
(
    substance    integer not null,
    chebi        integer not null,
    primary key(substance, chebi)
);


create table pubchem.substance_chembl_matches
(
    substance    integer not null,
    chembl       integer not null,
    primary key(substance, chembl)
);


create table pubchem.substance_glytoucan_matches
(
    substance    integer not null,
    glytoucan    varchar not null,
    primary key(substance)
);


create table pubchem.substance_references
(
    substance    integer not null,
    reference    integer not null,
    primary key(substance, reference)
);


create table pubchem.substance_pdblinks
(
    substance    integer not null,
    pdblink      char(4) not null,
    primary key(substance, pdblink)
);


create table pubchem.substance_synonyms
(
    substance    integer not null,
    synonym      integer not null,
    primary key(substance, synonym)
);
