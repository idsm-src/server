create table pubchem.endpoint_bases
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    primary key(substance, bioassay, measuregroup)
);


create table pubchem.endpoint_outcomes
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    outcome_id      smallint not null,
    primary key(substance, bioassay, measuregroup, outcome_id)
);


create table pubchem.endpoint_measurements
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    type_id         integer not null,
    label           varchar not null,
    primary key(substance, bioassay, measuregroup)
);


create table pubchem.endpoint_measurement_values
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    value           real not null,
    primary key(substance, bioassay, measuregroup, value)
);


create table pubchem.endpoint_references
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    reference       integer not null,
    primary key(substance, bioassay, measuregroup, reference)
);
