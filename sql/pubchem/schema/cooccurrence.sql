create table pubchem.chemical_chemical_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.chemical_disease_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.chemical_gene_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.chemical_enzyme_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.disease_chemical_cooccurrences
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


create table pubchem.disease_gene_cooccurrences
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


create table pubchem.gene_chemical_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.enzyme_chemical_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);


create table pubchem.gene_disease_cooccurrences
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


create table pubchem.gene_gene_cooccurrences
(
    subject     integer not null,
    object      integer not null,
    value       integer not null,
    primary key(subject, object)
);
