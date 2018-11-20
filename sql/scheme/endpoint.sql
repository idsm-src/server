create table endpoint_bases
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    outcome_id      smallint not null,
    primary key(substance, bioassay, measuregroup)
);


create table endpoint_measurements
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    type_id         integer not null,
    value           real not null,
    label           varchar not null,
    primary key(substance, bioassay, measuregroup)
);


create table endpoint_references
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    reference       integer not null,
    primary key(substance, bioassay, measuregroup, reference)
);
