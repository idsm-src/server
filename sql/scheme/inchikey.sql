create table inchikey_bases
(
    id          integer identity,
    inchikey    varchar(27) unique not null,
    primary key(id)
);


create table inchikey_compounds
(
    inchikey    integer not null,
    compound    integer not null,
    primary key(compound)
);


create table inchikey_subjects
(
    inchikey    integer not null,
    subject     integer not null,
    primary key(inchikey)
);
