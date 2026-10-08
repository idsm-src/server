create table chembl.source_bases
(
    id           integer not null,
    chembl_id    varchar,
    label        varchar,
    description  varchar,
    primary key(id)
);
