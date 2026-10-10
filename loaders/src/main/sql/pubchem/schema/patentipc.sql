create table pubchem.patentipcs
(
    id                  varchar not null,
    label               varchar,
    broader             varchar,
    epo_id              varchar,
    wipo_id             varchar,
    image               varchar,
    primary key(id)
);


create table pubchem.patentipc_titles
(
    patentipc           varchar not null,
    title               varchar not null,
    primary key(patentipc, title)
);
