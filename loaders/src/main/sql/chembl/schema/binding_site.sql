create table chembl.binding_sites
(
    id         integer not null,
    chembl_id  varchar,
    name       varchar,
    target     integer,
    primary key(id)
);
