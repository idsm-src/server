create table chebi.classes
(
    id          integer not null,
    primary key(id)
);


create table chebi.class_parents
(
    class       integer not null,
    parent      integer not null,
    primary key(class, parent)
);


create table chebi.class_stars
(
    class       integer not null,
    star_id     integer not null,
    primary key(class)
);


create table chebi.class_replacements
(
    class       integer not null,
    replacement integer not null,
    primary key(class)
);


create table chebi.class_obsolescence_reasons
(
    class       integer not null,
    reason_id   integer not null,
    primary key(class)
);


create table chebi.restrictions
(
    id                  integer not null,
    class               integer not null,
    value_restriction   integer not null,
    property_unit       smallint not null,
    property_id         integer not null,
    primary key(id)
);


create table chebi.axioms
(
    id              integer not null,
    class           integer not null,
    property_unit   smallint not null,
    property_id     integer not null,
    target          varchar not null,
    type_id         integer,
    reference       varchar,
    source          varchar,
    primary key(id)
);


create table chebi.class_references
(
    class       integer not null,
    reference   varchar not null,
    primary key(class, reference)
);


create table chebi.class_related_synonyms
(
    class       integer not null,
    synonym     varchar not null,
    primary key(class, synonym)
);


create table chebi.class_exact_synonyms
(
    class       integer not null,
    synonym     varchar not null,
    primary key(class, synonym)
);


create table chebi.class_formulas
(
    class       integer not null,
    formula     varchar not null,
    primary key(class, formula)
);


create table chebi.class_masses
(
    class       integer not null,
    mass        varchar not null,
    primary key(class, mass)
);


create table chebi.class_monoisotopic_masses
(
    class       integer not null,
    mass        varchar not null,
    primary key(class, mass)
);


create table chebi.class_alternative_identifiers
(
    class       integer not null,
    identifier  varchar not null,
    primary key(class, identifier)
);


create table chebi.class_labels
(
    class       integer not null,
    label       varchar not null,
    primary key(class)
);


create table chebi.class_identifiers
(
    class       integer not null,
    identifier  varchar not null,
    primary key(class)
);


create table chebi.class_namespaces
(
    class       integer not null,
    namespace   varchar not null,
    primary key(class)
);


create table chebi.class_charges
(
    class       integer not null,
    charge      varchar not null,
    primary key(class)
);


create table chebi.class_smileses
(
    class       integer not null,
    smiles      varchar not null,
    primary key(class)
);


create table chebi.class_inchikeys
(
    class       integer not null,
    inchikey    varchar not null,
    primary key(class)
);


create table chebi.class_inchis
(
    class       integer not null,
    inchi       varchar not null,
    primary key(class)
);


create table chebi.class_wurcs_representations
(
    class       integer not null,
    wurcs       varchar not null,
    primary key(class)
);


create table chebi.class_molfiles
(
    class       integer not null,
    molfile     varchar not null,
    primary key(class)
);


create table chebi.class_definitions
(
    class       integer not null,
    definition  varchar not null,
    primary key(class)
);


create table chebi.class_deprecated_flags
(
    class       integer not null,
    flag        boolean not null,
    primary key(class)
);
