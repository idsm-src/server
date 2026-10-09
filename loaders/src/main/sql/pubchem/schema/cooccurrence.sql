create table pubchem.compound_compound_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.compound_disease_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.compound_genesymbol_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.compound_enzyme_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.disease_compound_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.disease_disease_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.disease_genesymbol_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.disease_enzyme_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.genesymbol_compound_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.enzyme_compound_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.genesymbol_disease_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.enzyme_disease_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.genesymbol_genesymbol_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);
