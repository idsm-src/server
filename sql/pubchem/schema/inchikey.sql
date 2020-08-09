create table pubchem.inchikey_bases
(
    id          integer,
    inchikey    char(27) unique not null,
    primary key(id)
);


create table pubchem.inchikey_compounds
(
    inchikey    integer not null,
    compound    integer not null,
    primary key(compound)
);


create table pubchem.inchikey_subjects
(
    inchikey    integer not null,
    subject     varchar not null,
    primary key(inchikey)
);
