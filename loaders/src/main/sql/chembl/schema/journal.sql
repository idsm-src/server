create table chembl_tmp.journal_dictionary
(
    id           integer not null,
    label        varchar,
    title        varchar,
    short_title  varchar,
    issn         varchar,
    eissn        varchar,
    chembl_id    varchar not null generated always as ('CHEMBL_JRN_' || id::varchar) stored,
    primary key(id)
);
