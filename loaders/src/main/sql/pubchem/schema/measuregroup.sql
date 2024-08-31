create table pubchem.measuregroup_bases
(
    bioassay        integer not null,
    measuregroup    integer not null,
    source          smallint,
    title           varchar,
    primary key(bioassay, measuregroup)
);


create table pubchem.measuregroup_substances
(
    bioassay        integer not null,
    measuregroup    integer not null,
    substance       integer not null,
    primary key(bioassay, measuregroup, substance)
);


create table pubchem.measuregroup_proteins
(
    bioassay        integer not null,
    measuregroup    integer not null,
    protein         integer not null,
    primary key(bioassay, measuregroup, protein)
);


create table pubchem.measuregroup_genes
(
    bioassay        integer not null,
    measuregroup    integer not null,
    gene            integer not null,
    primary key(bioassay, measuregroup, gene)
);


create table pubchem.measuregroup_taxonomies
(
    bioassay        integer not null,
    measuregroup    integer not null,
    taxonomy        integer not null,
    primary key(bioassay, measuregroup, taxonomy)
);


create table pubchem.measuregroup_cells
(
    bioassay        integer not null,
    measuregroup    integer not null,
    cell            integer not null,
    primary key(bioassay, measuregroup, cell)
);


create table pubchem.measuregroup_anatomies
(
    bioassay        integer not null,
    measuregroup    integer not null,
    anatomy         integer not null,
    primary key(bioassay, measuregroup, anatomy)
);
