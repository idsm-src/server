create table chembl.components
(
    id           integer not null,
    chembl_id    varchar,
    type         varchar,
    description  varchar,
    organism     varchar,
    taxonomy     integer,
    sequence     varchar,
    accession    varchar,
    primary key(id)
);


create table chembl.component_alternatives
(
    component    integer not null,
    alternative  varchar not null,
    primary key(component, alternative)
);


create type chembl.component_reference_type as enum
(
    'CGD',
    'ENZYME CLASS',
    'GO COMPONENT',
    'GO FUNCTION',
    'GO PROCESS',
    'INTACT',
    'INTERPRO',
    'PDB',
    'PFAM',
    'PHARMGKB',
    'REACTOME',
    'TIMBAL',
    'UNIPROT'
);


create table chembl.component_references
(
    component  integer not null,
    type       chembl.component_reference_type not null,
    reference  varchar not null,
    primary key(component, type, reference)
);


create table chembl.component_reference_labels
(
    type       chembl.component_reference_type not null,
    reference  varchar not null,
    label      varchar not null,
    primary key(type, reference, label)
);
