create table pubchem.endpoint_bases
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    value           integer not null,
    type_id         integer,
    measurement     real,
    label           varchar,
    outcome_id      smallint,
    primary key(substance, bioassay, measuregroup, value)
);


create table pubchem.endpoint_measurements
(
    substance           integer not null,
    bioassay            integer not null,
    measuregroup        integer not null,
    value               integer not null,
    endpoint_type_id    integer,
    measurement_type_id integer,
    measurement         real,
    label               varchar,
    primary key(substance, bioassay, measuregroup, value)
);


create table pubchem.endpoint_references
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    value           integer not null,
    reference       integer not null,
    primary key(substance, bioassay, measuregroup, value, reference)
);
