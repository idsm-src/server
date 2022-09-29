create table pubchem.bioassay_bases
(
    id        integer not null,
    source    smallint not null,
    title     varchar not null,
    primary key(id)
);


create table pubchem.bioassay_data
(
    __          integer,
    bioassay    integer not null,
    type_id     smallint not null,
    value       varchar not null,
    primary key(__)
);


create table pubchem.bioassay_stages
(
    bioassay    integer not null,
    stage       integer not null,
    primary key(bioassay)
);


create table pubchem.bioassay_confirmatory_assays
(
    bioassay            integer not null,
    confirmatory_assay  integer not null,
    primary key(bioassay, confirmatory_assay)
);


create table pubchem.bioassay_primary_assays
(
    bioassay        integer not null,
    primary_assay   integer not null,
    primary key(bioassay, primary_assay)
);


create table pubchem.bioassay_summary_assays
(
    bioassay        integer not null,
    summary_assay   integer not null,
    primary key(bioassay, summary_assay)
);


create table pubchem.bioassay_chembl_assays
(
    bioassay            integer not null,
    chembl_assay        integer not null,
    primary key(bioassay)
);


create table pubchem.bioassay_chembl_mechanisms
(
    bioassay            integer not null,
    chembl_mechanism    integer not null,
    primary key(bioassay)
);


create table pubchem.bioassay_patents
(
    bioassay            integer not null,
    patent              varchar not null,
    primary key(bioassay, patent)
);
