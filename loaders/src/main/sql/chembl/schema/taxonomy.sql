create type chembl.taxonomy_reference_type as enum
(
    'IDENTIFIERS.ORG',
    'NCBI TAXONOMY'
);


create table chembl.taxonomy_labels
(
    taxonomy  integer not null,
    type      chembl.taxonomy_reference_type not null,
    label     varchar not null,
    primary key(taxonomy, type, label)
);
