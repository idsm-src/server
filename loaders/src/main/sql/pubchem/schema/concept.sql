create table pubchem.concept_bases
(
    id         smallint not null,
    iri        varchar unique not null,
    label      varchar,
    scheme     smallint,
    broader    smallint,
    primary key(id)
);
