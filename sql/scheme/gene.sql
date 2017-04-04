create table gene_bases
(
    id           integer not null,
    title        varchar not null,
    description  varchar not null,
    primary key(id)
);


create table gene_biosystems
(
    gene         integer not null,
    biosystem    integer not null,
    primary key(gene, biosystem)
);


create table gene_alternatives
(
    __             integer,
    gene           integer not null,
    alternative    varchar not null,
    primary key(__)
);


create table gene_references
(
    gene         integer not null,
    reference    integer not null,
    primary key(gene, reference)
);


create table gene_matches
(
    __       integer,
    gene     integer not null,
    match    varchar not null,
    primary key(__)
);
