create table pubchem.gene_bases
(
    id           integer not null,
    title        varchar not null,
    symbol       varchar not null,
    organism_id  integer not null,
    primary key(id)
);


create table pubchem.gene_alternatives
(
    __             integer,
    gene           integer not null,
    alternative    varchar not null,
    primary key(__)
);


create table pubchem.gene_references
(
    gene         integer not null,
    reference    integer not null,
    primary key(gene, reference)
);


create table pubchem.gene_matches
(
    __       integer,
    gene     integer not null,
    match    varchar not null,
    primary key(__)
);


create table pubchem.gene_ncit_matches
(
    gene     integer not null,
    match    integer not null,
    primary key(gene, match)
);


create table pubchem.gene_processes
(
    gene            integer not null,
    process_id      integer not null,
    primary key(gene, process_id)
);


create table pubchem.gene_functions
(
    gene            integer not null,
    function_id     integer not null,
    primary key(gene, function_id)
);


create table pubchem.gene_locations
(
    gene            integer not null,
    location_id     integer not null,
    primary key(gene, location_id)
);
