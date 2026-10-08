create table chembl.document_bases
(
    id          integer not null,
    chembl_id   varchar,
    journal     integer,
    type        varchar,
    title       varchar,
    year        integer,
    volume      varchar,
    issue       varchar,
    first_page  varchar,
    last_page   varchar,
    doi         varchar,
    pubmed      integer,
    primary key(id)
);
