create table chembl.cell_lines
(
    id           integer not null,
    chembl_id    varchar,
    label        varchar,
    description  varchar,
    organism     varchar,
    taxonomy     integer,
    cellosaurus  varchar,
    clo_id       integer,
    efo_unit     smallint,
    efo_id       integer,
    primary key(id)
);
