create table molmedb.substances
(
    id               integer not null,
    parent           integer,
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
    substance        integer not null,
    type             smallint not null,
    value            varchar not null,
    primary key(substance, type, value)
);


create table molmedb.substance_links
(
    substance        integer not null,
    type             smallint not null,
    value            integer not null,
    primary key(substance, type, value)
);


create table molmedb.substance_obsolete_identifiers
(
    identifier          varchar not null,
    substance           integer not null,
    primary key(identifier)
);
