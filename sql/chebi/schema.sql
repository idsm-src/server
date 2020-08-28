create schema chebi;


create table chebi.classes
(
    id          integer not null,
    primary key(id)
);


create table chebi.parents
(
    chebi       integer not null,
    parent      integer not null,
    primary key(chebi, parent)
);


create table chebi.stars
(
    chebi       integer not null,
    star        integer not null,
    primary key(chebi)
);


create table chebi.replacements
(
    chebi       integer not null,
    replacement integer not null,
    primary key(chebi)
);


create table chebi.obsolescence_reasons
(
    chebi       integer not null,
    reason      integer not null,
    primary key(chebi)
);


create table chebi.restrictions
(
    id                  integer not null,
    chebi               integer not null,
    value_restriction   integer not null,
    property_unit       smallint not null,
    property_id         integer not null,
    primary key(id)
);


create table chebi.axioms
(
    id              integer not null,
    chebi           integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    target          varchar not null,
    type_id         integer,
    reference       varchar,
    source          varchar,
    primary key(id)
);


create table chebi.references
(
    chebi       integer not null,
    reference   varchar not null,
    primary key(chebi, reference)
);


create table chebi.related_synonyms
(
    chebi       integer not null,
    synonym     varchar not null,
    primary key(chebi, synonym)
);


create table chebi.exact_synonyms
(
    chebi       integer not null,
    synonym     varchar not null,
    primary key(chebi, synonym)
);


create table chebi.formulas
(
    chebi       integer not null,
    formula     varchar not null,
    primary key(chebi, formula)
);


create table chebi.masses
(
    chebi       integer not null,
    mass        varchar not null,
    primary key(chebi, mass)
);


create table chebi.monoisotopic_masses
(
    chebi       integer not null,
    mass        varchar not null,
    primary key(chebi, mass)
);


create table chebi.alternative_identifiers
(
    chebi       integer not null,
    identifier  varchar not null,
    primary key(chebi, identifier)
);


create table chebi.labels
(
    chebi       integer not null,
    label       varchar not null,
    primary key(chebi)
);


create table chebi.identifiers
(
    chebi       integer not null,
    identifier  varchar not null,
    primary key(chebi)
);


create table chebi.namespaces
(
    chebi       integer not null,
    namespace   varchar not null,
    primary key(chebi)
);


create table chebi.charges
(
    chebi       integer not null,
    charge      varchar not null,
    primary key(chebi)
);


create table chebi.smiles_codes
(
    chebi       integer not null,
    smiles      varchar not null,
    primary key(chebi)
);


create table chebi.inchikeys
(
    chebi       integer not null,
    inchikey    varchar not null,
    primary key(chebi)
);


create table chebi.inchies
(
    chebi       integer not null,
    inchi       varchar not null,
    primary key(chebi)
);


create table chebi.definitions
(
    chebi       integer not null,
    definition  varchar not null,
    primary key(chebi)
);


create table chebi.deprecated_flags
(
    chebi       integer not null,
    flag        boolean not null,
    primary key(chebi)
);
