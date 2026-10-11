create table pubchem.substance_versions
(
    substance    integer not null,
    version      numeric not null,
    primary key(substance)
);
