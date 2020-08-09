create table pubchem.conserveddomain_bases
(
    id          integer not null,
    title       varchar,
    abstract    varchar,
    primary key(id)
);


create table pubchem.conserveddomain_references
(
    domain       integer not null,
    reference    integer not null,
    primary key(domain, reference)
);
