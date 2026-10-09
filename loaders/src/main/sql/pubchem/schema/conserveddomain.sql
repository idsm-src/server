create table pubchem.conserveddomains
(
    id          integer not null,
    title       varchar,
    abstract    varchar,
    primary key(id)
);


create table pubchem.conserveddomain_references
(
    conserveddomain integer not null,
    reference       integer not null,
    primary key(conserveddomain, reference)
);
