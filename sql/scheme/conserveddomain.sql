create table conserveddomain_bases
(
    id          integer not null,
    title       varchar,
    abstract    varchar,
    primary key(id)
);


create table conserveddomain_references
(
    domain       integer not null,
    reference    integer not null,
    primary key(domain, reference)
);
