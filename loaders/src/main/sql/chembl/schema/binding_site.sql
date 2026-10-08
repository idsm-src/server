create table chembl.binding_site_bases
(
    id         integer not null,
    chembl_id  varchar,
    name       varchar,
    target     integer,
    primary key(id)
);
