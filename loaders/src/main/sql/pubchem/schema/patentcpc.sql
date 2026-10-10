create type pubchem.patentcpc_type_type as enum
(
    'SECTION',
    'SUBSECTION',
    'CLASS',
    'SUBCLASS',
    'MAINGROUP',
    'SUBGROUP'
);


create table pubchem.patentcpcs
(
    id                  varchar not null,
    type                pubchem.patentcpc_type_type,
    level               numeric,
    symbol              varchar,
    title               varchar,
    concordant_ipc      varchar,
    primary key(id)
);


create table pubchem.patentcpc_broaders
(
    patentcpc           varchar not null,
    broader             varchar not null,
    primary key(patentcpc, broader)
);


create table pubchem.patentcpc_modified_dates
(
    patentcpc           varchar not null,
    date                date not null,
    primary key(patentcpc, date)
);
