create table molmedb.transporters
(
    id                      integer not null,
    substance               integer,
    protein                 integer,
    membrane                integer,
    method                  integer,
    reference               integer,
    model_reference         integer,
    category                integer,
    km                      real,
    km_accuracy             real,
    ec50                    real,
    ec50_accuracy           real,
    ki                      real,
    ki_accuracy             real,
    ic50                    real,
    ic50_accuracy           real,
    temperature             real,
    ph                      real,
    charge                  varchar,
    comment                 varchar,
    primary key(id)
);


create table molmedb.proteins
(
    id                      integer not null,
    uniprot_id              varchar,
    name                    varchar,
    primary key(id)
);
