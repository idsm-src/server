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
--  __             smallint identity,
    gene           integer not null,
    alternative    nvarchar not null,
--  primary key(__)
    primary key(gene, alternative) -- ugly workaround
);


create table gene_references
(
    gene         integer not null,
    reference    integer not null,
    primary key(gene, reference)
);
