create table molmedb.transporter_bases
(
    id                      integer not null,
    substance_id            integer,
    protein_id              integer,
    membrane_id             integer,
    method_id               integer,
    publication_id          integer,
    model_publication_id    integer,
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


create table molmedb.protein_bases
(
    id                      integer not null,
    uniprot_id              varchar,
    name                    varchar,
    primary key(id)
);
