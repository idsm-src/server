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
