create table synonym_bases
(
    id       integer identity,
    md5      varchar(32) unique not null,
    primary key(id)
);


create table synonym_values
(
    __         integer identity,
    synonym    integer not null,
    value      nvarchar not null,
    primary key(__)
);


create table synonym_types
(
    synonym    integer not null,
    type       smallint not null,
    primary key(synonym, type)
);


create table synonym_compounds
(
    synonym     integer not null,
    compound    integer not null,
    primary key(synonym, compound)
);


create table synonym_mesh_subjects
(
    synonym    integer not null,
    subject    integer not null,
    primary key(synonym, subject)
);


create table synonym_concept_subjects
(
    synonym    integer not null,
    concept    smallint not null,
    primary key(synonym, concept)
);
