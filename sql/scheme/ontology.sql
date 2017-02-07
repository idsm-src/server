create table class_bases
(
    id       integer identity,
    iri      varchar not null,
    label    nvarchar,
    primary key(id),
    unique(iri)
);


create table class_subclasses
(
    class       integer not null,
    subclass    integer not null,
    primary key(class, subclass)
);


create table property_bases
(
    id       integer identity,
    iri      varchar not null,
    label    nvarchar,
    primary key(id),
    unique(iri)
);


create table property_subproperties
(
    property       integer not null,
    subproperty    integer not null,
    primary key(property, subproperty)
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
