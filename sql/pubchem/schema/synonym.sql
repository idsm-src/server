create table pubchem.synonym_bases
(
    id       integer not null,
    md5      char(32) unique not null,
    primary key(id)
);


create table pubchem.synonym_values
(
    __         integer,
    synonym    integer not null,
    value      varchar not null,
    primary key(__)
);


create table pubchem.synonym_types
(
    synonym    integer not null,
    type_id    integer not null,
    primary key(synonym, type_id)
);


create table pubchem.synonym_compounds
(
    synonym     integer not null,
    compound    integer not null,
    primary key(synonym, compound)
);


create table pubchem.synonym_mesh_subjects
(
    synonym     integer not null,
    subject     varchar not null,
    primary key(synonym, subject)
);


create table pubchem.synonym_concept_subjects
(
    synonym    integer not null,
    concept    smallint not null,
    primary key(synonym, concept)
);
