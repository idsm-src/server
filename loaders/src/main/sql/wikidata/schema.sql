create table wikidata.compound_canonical_smileses
(
    compound    integer not null,
    smiles      varchar not null,
    primary key(compound, smiles)
);


create table wikidata.compound_isomeric_smileses
(
    compound    integer not null,
    smiles      varchar not null,
    primary key(compound, smiles)
);


create table wikidata.compound_inchis
(
    compound    integer not null,
    inchi       varchar not null,
    primary key(compound, inchi)
);


create table wikidata.compound_structures
(
    compound    integer not null,
    smiles      varchar not null,
    primary key(compound)
);
