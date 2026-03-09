create table molmedb.substance_bases
(
    id               integer not null,
    parent_id        integer,
    charge           integer,
    ph_start         real,
    ph_end           real,
    molecular_weight real,
    logp             real,
    identifier       varchar,
    canonical_smiles varchar,
    inchi            varchar,
    inchikey         varchar,
    primary key(id)
);


create table molmedb.substance_identifiers
(
    substance_id     integer not null,
    type             smallint not null,
    value            varchar not null,
    primary key(substance_id, type, value)
);


create table molmedb.substance_links
(
    substance_id     integer not null,
    type             smallint not null,
    value            integer not null,
    primary key(substance_id, type, value)
);


create table molmedb.obsoleted_substances
(
    identifier          varchar not null,
    substance_id        integer not null,
    primary key(identifier)
);
