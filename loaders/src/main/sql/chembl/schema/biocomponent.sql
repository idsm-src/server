create table chembl.biocomponent_bases
(
    id           integer not null,
    chembl_id    varchar,
    type         varchar,
    description  varchar,
    organism     varchar,
    taxonomy     integer,
    sequence     varchar,
    primary key(id)
);
