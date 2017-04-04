create table concept_bases
(
    id         smallint,
    iri        varchar not null,
    label      varchar,
    scheme     smallint,
    broader    smallint,
    primary key(id),
    unique(iri)
);
