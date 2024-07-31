create table pubchem.source_bases
(
    id          smallint not null,
    iri         varchar unique not null,
    title       varchar,
    homepage    varchar,
    license     varchar,
    rights      varchar,
    primary key(id)
);


create table pubchem.source_subjects
(
    source     smallint not null,
    subject    smallint not null,
    primary key(source, subject)
);


create table pubchem.source_alternatives
(
    source         smallint not null,
    alternative    varchar not null,
    primary key(source, alternative)
);
