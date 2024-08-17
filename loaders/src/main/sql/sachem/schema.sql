create table molecules.drugbank
(
    id        integer not null,
    molfile   text not null,
    primary key(id)
);


create table molecules.chebi
(
    id        integer not null,
    molfile   text not null,
    primary key(id)
);


create table molecules.chembl
(
    id        integer not null,
    molfile   text not null,
    primary key(id)
);


create table molecules.pubchem
(
    id        integer not null,
    molfile   text not null,
    primary key(id)
);


create table molecules.wikidata
(
    id        integer not null,
    smiles    text not null,
    primary key(id)
);
