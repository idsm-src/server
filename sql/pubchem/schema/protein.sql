create table pubchem.protein_bases
(
    id             integer,
    name           varchar unique not null,
    organism_id    integer,
    title          varchar,
    sequence       varchar,
    primary key(id)
);


create table pubchem.protein_alternatives
(
    protein        integer not null,
    alternative    varchar not null,
    primary key(protein, alternative)
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
    primary key(protein, gene)
);


create table pubchem.protein_enzymes
(
    protein    integer not null,
    enzyme     varchar not null,
    primary key(protein, enzyme)
);


create table pubchem.protein_ncbi_matches
(
    protein    integer not null,
    match      varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_uniprot_matches
(
    protein    integer not null,
    match      varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_mesh_matches
(
    protein  integer not null,
    match    varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_thesaurus_matches
(
    protein  integer not null,
    match    integer not null,
    primary key(protein, match)
);


create table pubchem.protein_expasy_matches
(
    protein  integer not null,
    match    varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_guidetopharmacology_matches
(
    protein  integer not null,
    match    integer not null,
    primary key(protein, match)
);


create table pubchem.protein_drugbank_matches
(
    protein  integer not null,
    match    integer not null,
    primary key(protein, match)
);


create table pubchem.protein_chembl_matches
(
    protein  integer not null,
    match    integer not null,
    primary key(protein, match)
);


create table pubchem.protein_glygen_matches
(
    protein  integer not null,
    match    varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_glycosmos_matches
(
    protein  integer not null,
    match    varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_alphafold_matches
(
    protein  integer not null,
    match    varchar not null,
    primary key(protein, match)
);


create table pubchem.protein_conserveddomains
(
    protein    integer not null,
    domain     integer not null,
    primary key(protein, domain)
);


create table pubchem.protein_continuantparts
(
    protein    integer not null,
    part       integer not null,
    primary key(protein, part)
);


create table pubchem.protein_families
(
    protein    integer not null,
    family     integer not null,
    primary key(protein, family)
);


create table pubchem.protein_types
(
    protein       integer not null,
    type_unit     smallint not null,
    type_id       integer not null,
    primary key(protein, type_unit, type_id)
);


create table pubchem.protein_complexes
(
    protein    integer not null,
    primary key(protein)
);
