create table source_bases
(
    id       smallint,
    iri      varchar not null,
    title    varchar,
    primary key(id),
    unique(iri)
);


create table source_subjects
(
    source     smallint not null,
    subject    smallint not null,
    primary key(source, subject)
);


create table source_alternatives
(
    __             smallint,
    source         smallint not null,
    alternative    varchar not null,
    primary key(__)
);
