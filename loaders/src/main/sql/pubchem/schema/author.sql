create table pubchem.author_bases
(
    id          integer not null,
    iri         varchar unique not null,
    primary key(id)
);


create table pubchem.author_given_names
(
    author      integer not null,
    name        varchar not null,
    primary key(author, name)
);


create table pubchem.author_family_names
(
    author      integer not null,
    name        varchar not null,
    primary key(author, name)
);


create table pubchem.author_formatted_names
(
    author      integer not null,
    name        varchar not null,
    primary key(author, name)
);


create table pubchem.author_organizations
(
    __              integer,
    author          integer not null,
    organization    varchar not null,
    primary key(__)
);


create table pubchem.author_orcids
(
    author      integer not null,
    orcid       varchar unique not null,
    primary key(author)
);
