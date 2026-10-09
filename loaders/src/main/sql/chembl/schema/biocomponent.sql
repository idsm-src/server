create table chembl.biocomponents
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
