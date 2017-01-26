create table source_bases
(
    id       smallint identity,
    iri      varchar not null,
    title    nvarchar,
    primary key(id),
    unique(iri)
);


create table source_subjects__reftable
(
    id     smallint identity,
    iri    varchar not null,
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
    __             smallint identity,
    source         smallint not null,
    alternative    nvarchar not null,
    primary key(__)
);
