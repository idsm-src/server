create table chembl.activities
(
    id                   integer not null,
    chembl_id            varchar,
    assay                integer,
    molecule             integer,
    document             integer,
    endpoint_id          integer,
    unit_id              integer,
    qudt_id              integer,
    type                 varchar,
    relation             varchar,
    value                float8,
    units                varchar,
    standard_type        varchar,
    standard_relation    varchar,
    standard_value       float8,
    standard_units       varchar,
    pchembl              float8,
    comment              varchar,
    validity_comment     varchar,
    potential_duplicate  boolean,
    primary key(id)
);
