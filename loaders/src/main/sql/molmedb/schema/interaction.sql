create table molmedb.interactions
(
    id                      integer not null,
    substance               integer,
    membrane                integer,
    method                  integer,
    reference               integer,
    model_reference         integer,
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


create table molmedb.fluorescent_interactions
(
    id                      integer not null,
    substance               integer,
    membrane                integer,
    method                  integer,
    reference               integer,
    model_reference         integer,
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


create table molmedb.membranes
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
    membrane            integer not null,
    chebi               integer not null,
    primary key(membrane, chebi)
);


create table molmedb.methods
(
    id                  integer not null,
    category            integer,
    parent_category     integer,
    name                varchar,
    abbreviation        varchar,
    description         varchar,
    primary key(id)
);

