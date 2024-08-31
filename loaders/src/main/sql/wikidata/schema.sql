create table wikidata.canonical_smiles
(
    compound    integer not null,
    smiles      varchar not null,
    primary key(compound, smiles)
);


create table wikidata.isomeric_smiles
(
    compound    integer not null,
    smiles      varchar not null,
    primary key(compound, smiles)
);


create table wikidata.inchies
(
    compound    integer not null,
    inchi       varchar not null,
    primary key(compound, inchi)
);
