create type pubchem.pathway_reference_type as enum
(
    'PATHBANK',
    'BIOCYC',
    'REACTOME',
    'WIKIPATHWAY',
    'PLANTCYC',
    'PID',
    'INOH',
    'PLANTREACTOME',
    'PHARMGKB',
    'FAIRDOMHUB',
    'LIPIDMAPS'
);


create table pubchem.pathway_bases
(
    id              integer not null,
    source          smallint not null,
    title           varchar not null,
    reference_type  pubchem.pathway_reference_type not null,
    reference       varchar not null,
    organism_id     integer,
    primary key(id)
);


create table pubchem.pathway_compounds
(
    pathway     integer not null,
    compound    integer not null,
    primary key(pathway, compound)
);


create table pubchem.pathway_proteins
(
    pathway     integer not null,
    protein     integer not null,
    primary key(pathway, protein)
);


create table pubchem.pathway_genes
(
    pathway     integer not null,
    gene        integer not null,
    primary key(pathway, gene)
);


create table pubchem.pathway_components
(
    pathway     integer not null,
    component   integer not null,
    primary key(pathway, component)
);


create table pubchem.pathway_related_pathways
(
    pathway     integer not null,
    related     integer not null,
    primary key(pathway, related)
);


create table pubchem.pathway_references
(
    pathway     integer not null,
    reference   integer not null,
    primary key(pathway, reference)
);
