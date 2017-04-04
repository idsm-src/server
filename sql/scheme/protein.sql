create table protein_bases
(
    id          integer,
    name        varchar unique not null,
    organism    integer,
    title       varchar,
    primary key(id)
);


create table protein_references
(
    protein      integer not null,
    reference    integer not null,
    primary key(protein, reference)
);


create table protein_pdblinks
(
    protein    integer not null,
    pdblink    char(4) not null,
    primary key(protein, pdblink)
);


create table protein_similarproteins
(
    protein       integer not null,
    simprotein    integer not null,
    primary key(protein, simprotein)
);


create table protein_genes
(
    protein    integer not null,
    gene       integer not null,
    primary key(protein)
);


create table protein_closematches
(
    __         integer,
    protein    integer not null,
    match      varchar not null,
    primary key(__)
);


create table protein_conserveddomains
(
    protein    integer not null,
    domain     integer not null,
    primary key(protein)
);


create table protein_continuantparts
(
    protein    integer not null,
    part       integer not null,
    primary key(protein, part)
);


create table protein_participates_goes
(
    protein          integer not null,
    participation    integer not null,
    primary key(protein, participation)
);


create table protein_participates_biosystems
(
    protein      integer not null,
    biosystem    integer not null,
    primary key(protein, biosystem)
);


create table protein_functions
(
    protein       integer not null,
    gofunction    integer not null,
    primary key(protein, gofunction)
);


create table protein_locations
(
    protein     integer not null,
    location    integer not null,
    primary key(protein, location)
);


create table protein_types
(
    protein    integer not null,
    type       integer not null,
    primary key(protein, type)
);


create table protein_complexes
(
    protein    integer not null,
    primary key(protein)
);
