create type chembl_tmp.component_reference_type as enum
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


create table chembl_tmp.component_references
(
    refcomp_id      integer not null,
    component_id    integer not null,
    reference_type  chembl_tmp.component_reference_type not null,
    reference       varchar not null,
    primary key(refcomp_id)
);
