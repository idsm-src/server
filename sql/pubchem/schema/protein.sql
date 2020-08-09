create table pubchem.protein_bases
(
    id             integer,
    name           varchar unique not null,
    organism_id    integer,
    title          varchar,
    primary key(id)
);


create table pubchem.protein_references
(
    protein      integer not null,
    reference    integer not null,
    primary key(protein, reference)
);


create table pubchem.protein_pdblinks
(
    protein    integer not null,
    pdblink    char(4) not null,
    primary key(protein, pdblink)
);


create table pubchem.protein_similarproteins
(
    protein       integer not null,
    simprotein    integer not null,
    primary key(protein, simprotein)
);


create table pubchem.protein_genes
(
    protein    integer not null,
    gene       integer not null,
    primary key(protein)
);


create table pubchem.protein_closematches
(
    __         integer,
    protein    integer not null,
    match      varchar not null,
    primary key(__)
);


create table pubchem.protein_conserveddomains
(
    protein    integer not null,
    domain     integer not null,
    primary key(protein)
);


create table pubchem.protein_continuantparts
(
    protein    integer not null,
    part       integer not null,
    primary key(protein, part)
);


create table pubchem.protein_processes
(
    protein       integer not null,
    process_id    integer not null,
    primary key(protein, process_id)
);


create table pubchem.protein_biosystems
(
    protein      integer not null,
    biosystem    integer not null,
    primary key(protein, biosystem)
);


create table pubchem.protein_functions
(
    protein        integer not null,
    function_id    integer not null,
    primary key(protein, function_id)
);


create table pubchem.protein_locations
(
    protein        integer not null,
    location_id    integer not null,
    primary key(protein, location_id)
);


create table pubchem.protein_types
(
    protein       integer not null,
    type_id       integer not null,
    primary key(protein, type_id)
);


create table pubchem.protein_complexes
(
    protein    integer not null,
    primary key(protein)
);
