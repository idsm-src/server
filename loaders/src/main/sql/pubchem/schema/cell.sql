create table pubchem.cell_bases
(
    id            integer not null,
    organism      integer,
    label         varchar,
    primary key(id)
);


create table pubchem.cell_alternatives
(
    cell          integer not null,
    alternative   varchar not null,
    primary key(cell, alternative)
);


create table pubchem.cell_occurrences
(
    cell          integer not null,
    occurrence    varchar not null,
    primary key(cell, occurrence)
);


create table pubchem.cell_references
(
    cell          integer not null,
    reference     integer not null,
    primary key(cell, reference)
);


create table pubchem.cell_matches
(
    cell          integer not null,
    match_unit    smallint not null,
    match_id      integer not null,
    primary key(cell, match_unit, match_id)
);


create table pubchem.cell_mesh_matches
(
    cell          integer not null,
    match         varchar not null,
    primary key(cell, match)
);


create table pubchem.cell_wikidata_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_cancerrxgene_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_depmap_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_sanger_passport_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_sanger_line_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_cellosaurus_matches
(
    cell          integer not null,
    match         varchar not null,
    primary key(cell, match)
);


create table pubchem.cell_chembl_card_matches
(
    cell          integer not null,
    match         integer not null,
    primary key(cell, match)
);


create table pubchem.cell_anatomies
(
    cell          integer not null,
    anatomy       integer not null,
    primary key(cell, anatomy)
);
