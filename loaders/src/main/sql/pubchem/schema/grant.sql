create table pubchem.grant_bases
(
    id              integer not null,
    iri             varchar unique not null,
    number          varchar,
    organization    integer,
    primary key(id)
);
