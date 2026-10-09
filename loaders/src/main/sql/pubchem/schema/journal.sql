create table pubchem.journals
(
    id              integer not null,
    catalog_id      varchar,
    title           varchar,
    abbreviation    varchar,
    issn            varchar,
    eissn           varchar,
    primary key(id)
);
