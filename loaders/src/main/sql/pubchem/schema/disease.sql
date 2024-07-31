create table pubchem.disease_bases
(
    id            integer not null,
    label         varchar,
    primary key(id)
);


create table pubchem.disease_alternatives
(
    disease       integer not null,
    alternative   varchar not null,
    primary key(disease, alternative)
);


create table pubchem.disease_matches
(
    disease       integer not null,
    match_unit    smallint not null,
    match_id      integer not null,
    primary key(disease, match_unit, match_id)
);


create table pubchem.disease_mesh_matches
(
    disease       integer not null,
    match         varchar not null,
    primary key(disease, match)
);


create table pubchem.disease_related_matches
(
    disease       integer not null,
    match_unit    smallint not null,
    match_id      integer not null,
    primary key(disease, match_unit, match_id)
);
