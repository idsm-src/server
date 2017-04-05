create table class_bases
(
    id       integer,
    iri      varchar not null,
    label    varchar,
    primary key(id),
    unique(iri)
);


create table class_superclasses
(
    class         integer not null,
    superclass    integer not null,
    primary key(class, superclass)
);


create table property_bases
(
    id       integer,
    iri      varchar not null,
    label    varchar,
    primary key(id),
    unique(iri)
);


create table property_superproperties
(
    property         integer not null,
    superproperty    integer not null,
    primary key(property, superproperty)
);


create table property_domains
(
    property    integer not null,
    domain      integer not null,
    primary key(property, domain)
);


create table property_ranges
(
    property    integer not null,
    range      integer not null,
    primary key(property, range)
);
