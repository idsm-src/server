create table isdb.compound_bases
(
    id              serial unique not null,
    accession       varchar not null,
    exact_mass      real not null,
    formula         varchar not null,
    smiles          varchar not null,
    inchi           varchar not null,
    primary key(accession)
);


create table isdb.spectrum_bases
(
    id              integer not null,
    ionmode         char not null,
    pepmass         real not null,
    spectrum        pgms.spectrum not null,
    primary key(id, ionmode)
);
