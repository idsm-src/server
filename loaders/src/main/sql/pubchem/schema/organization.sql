create table pubchem.organization_bases
(
    id              integer not null,
    iri             varchar unique not null,
    primary key(id)
);


create table pubchem.organization_country_names
(
    organization    integer not null,
    name            varchar not null,
    primary key(organization)
);


create table pubchem.organization_formatted_names
(
    organization    integer not null,
    name            varchar not null,
    primary key(organization, name)
);


create table pubchem.organization_crossref_matches
(
    organization    integer not null,
    crossref        varchar not null,
    primary key(organization, crossref)
);
