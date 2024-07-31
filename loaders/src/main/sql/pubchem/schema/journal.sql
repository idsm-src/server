create table pubchem.journal_bases
(
    id              integer not null,
    catalogid       varchar,
    title           varchar,
    abbreviation    varchar,
    issn            varchar,
    eissn           varchar,
    primary key(id)
);
