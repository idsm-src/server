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
    gene           integer not null,
    alternative    varchar not null,
    primary key(gene, alternative)
);


create table pubchem.gene_references
(
    gene         integer not null,
    reference    integer not null,
    primary key(gene, reference)
);


create table pubchem.gene_ensembl_matches
(
    gene     integer not null,
    match    varchar not null,
    primary key(gene, match)
);


create table pubchem.gene_mesh_matches
(
    gene     integer not null,
    match    varchar not null,
    primary key(gene, match)
);


create table pubchem.gene_thesaurus_matches
(
    gene     integer not null,
    match    integer not null,
    primary key(gene, match)
);


create table pubchem.gene_ctdbase_matches
(
    gene     integer not null,
    match    integer not null,
    primary key(gene, match)
);


create table pubchem.gene_expasy_matches
(
    gene     integer not null,
    match    varchar not null,
    primary key(gene, match)
);


create table pubchem.gene_medlineplus_matches
(
    gene     integer not null,
    match    varchar not null,
    primary key(gene, match)
);


create table pubchem.gene_omim_matches
(
    gene     integer not null,
    match    integer not null,
    primary key(gene, match)
);


create table pubchem.gene_alliancegenome_matches
(
    gene     integer not null,
    match    varchar not null,
    primary key(gene, match)
);


create table pubchem.gene_genenames_matches
(
    gene     integer not null,
    match    integer not null,
    primary key(gene, match)
);


create table pubchem.gene_kegg_matches
(
    gene     integer not null,
    match    varchar not null,
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


create table pubchem.gene_orthologs
(
    gene            integer not null,
    ortholog        integer not null,
    primary key(gene, ortholog)
);
