create table concept_bases
(
    id         smallint identity,
    iri        varchar not null,
    label      nvarchar,
    scheme     smallint,
    broader    smallint,
    primary key(id),
    unique(iri)
);
