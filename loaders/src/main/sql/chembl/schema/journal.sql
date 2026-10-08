create table chembl.journal_bases
(
    id           integer not null,
    chembl_id    varchar,
    label        varchar,
    title        varchar,
    short_title  varchar,
    issn         varchar,
    eissn        varchar,
    primary key(id)
);
