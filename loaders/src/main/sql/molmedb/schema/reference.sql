create table molmedb.reference_bases
(
    id                      integer not null,
    doi                     varchar,
    pmid                    varchar,
    citation                varchar,
    label                   varchar,
    homepage                varchar,
    primary key(id)
);


create table molmedb.reference_substances
(
    reference_id            integer not null,
    substance_id            integer not null,
    primary key(reference_id, substance_id)
);


create table molmedb.reference_membranes
(
    reference_id            integer not null,
    membrane_id             integer not null,
    primary key(reference_id, membrane_id)
);


create table molmedb.reference_methods
(
    reference_id            integer not null,
    method_id               integer not null,
    primary key(reference_id, method_id)
);


create table molmedb.reference_proteins
(
    reference_id            integer not null,
    protein_id              integer not null,
    primary key(reference_id, protein_id)
);
