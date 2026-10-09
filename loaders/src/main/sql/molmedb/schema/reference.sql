create table molmedb.references
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
    reference               integer not null,
    substance               integer not null,
    primary key(reference, substance)
);


create table molmedb.reference_membranes
(
    reference               integer not null,
    membrane                integer not null,
    primary key(reference, membrane)
);


create table molmedb.reference_methods
(
    reference               integer not null,
    method                  integer not null,
    primary key(reference, method)
);


create table molmedb.reference_proteins
(
    reference               integer not null,
    protein                 integer not null,
    primary key(reference, protein)
);
