create table measuregroup_bases
(
    bioassay        integer not null,
    measuregroup    integer not null,
    source          smallint,
    title           nvarchar,
    primary key(bioassay, measuregroup)
);


create table measuregroup_proteins
(
    bioassay        integer not null,
    measuregroup    integer not null,
    protein         integer not null,
    primary key(bioassay, measuregroup, protein)
);


create table measuregroup_genes
(
    bioassay        integer not null,
    measuregroup    integer not null,
    gene            integer not null,
    primary key(bioassay, measuregroup, gene)
);
