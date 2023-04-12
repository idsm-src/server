create table pubchem.taxonomy_bases
(
    id            integer not null,
    label         varchar,
    primary key(id)
);


create table pubchem.taxonomy_alternatives
(
    taxonomy      integer not null,
    alternative   varchar not null,
    primary key(taxonomy, alternative)
);


create table pubchem.taxonomy_references
(
    taxonomy      integer not null,
    reference     integer not null,
    primary key(taxonomy, reference)
);


create table pubchem.taxonomy_uniprot_matches
(
    taxonomy      integer not null,
    primary key(taxonomy)
);


create table pubchem.taxonomy_mesh_matches
(
    taxonomy      integer not null,
    match         varchar not null,
    primary key(taxonomy, match)
);


create table pubchem.taxonomy_catalogueoflife_matches
(
    taxonomy      integer not null,
    match         varchar not null,
    primary key(taxonomy, match)
);


create table pubchem.taxonomy_thesaurus_matches
(
    taxonomy      integer not null,
    match         integer not null,
    primary key(taxonomy, match)
);


create table pubchem.taxonomy_itis_matches
(
    taxonomy      integer not null,
    match         integer not null,
    primary key(taxonomy, match)
);
