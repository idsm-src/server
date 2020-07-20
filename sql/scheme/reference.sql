create table reference_bases
(
    id          integer not null,
    type_id     smallint not null,
    dcdate      date,
    title       varchar,
    citation    varchar,
    primary key(id)
);


create table reference_discusses
(
    reference    integer not null,
    statement    integer not null,
    primary key(reference, statement)
);


create table reference_subject_descriptors
(
    reference     integer not null,
    descriptor    integer not null,
    qualifier     integer not null,
    primary key(reference, descriptor, qualifier)
);


create table reference_primary_subject_descriptors
(
    reference     integer not null,
    descriptor    integer not null,
    qualifier     integer not null,
    primary key(reference, descriptor, qualifier)
);
