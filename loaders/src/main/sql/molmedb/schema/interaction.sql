create table molmedb.interaction_bases
(
    id                      integer not null,
    substance_id            integer,
    membrane_id             integer,
    method_id               integer,
    publication_id          integer,
    model_publication_id    integer,
    logk                    real,
    logk_accuracy           real,
    logperm                 real,
    logperm_accuracy        real,
    x_min                   real,
    x_min_accuracy          real,
    gpen                    real,
    gpen_accuracy           real,
    gwat                    real,
    gwat_accuracy           real,
    temperature             real,
    ph                      real,
    charge                  varchar,
    comment                 varchar,
    primary key(id)
);


create table molmedb.fluorescent_interaction_bases
(
    id                      integer not null,
    substance_id            integer,
    membrane_id             integer,
    method_id               integer,
    publication_id          integer,
    model_publication_id    integer,
    theta                   real,
    theta_accuracy          real,
    abs_wl                  real,
    abs_wl_accuracy         real,
    fluo_wl                 real,
    fluo_wl_accuracy        real,
    qy                      real,
    qy_accuracy             real,
    lt                      real,
    lt_accuracy             real,
    temperature             real,
    ph                      real,
    charge                  varchar,
    comment                 varchar,
    primary key(id)
);


create table molmedb.membrane_bases
(
    id                  integer not null,
    category            integer,
    parent_category     integer,
    name                varchar,
    abbreviation        varchar,
    description         varchar,
    primary key(id)
);


create table molmedb.membrane_parts
(
    membrane_id         integer not null,
    chebi_id            integer not null,
    primary key(membrane_id, chebi_id)
);


create table molmedb.method_bases
(
    id                  integer not null,
    category            integer,
    parent_category     integer,
    name                varchar,
    abbreviation        varchar,
    description         varchar,
    primary key(id)
);

