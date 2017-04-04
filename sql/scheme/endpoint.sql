create table endpoint_outcomes__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


create table endpoint_bases
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    outcome         smallint not null,
    primary key(substance, bioassay, measuregroup)
);


create table endpoint_measurements
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    type            smallint not null,
    value           float not null,
    label           varchar not null,
    primary key(substance, bioassay, measuregroup)
);


create table endpoint_references
(
    substance       integer not null,
    bioassay        integer not null,
    measuregroup    integer not null,
    reference       integer not null,
    primary key(substance, bioassay, measuregroup, reference)
);

--============================================================================--

insert into endpoint_outcomes__reftable(id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#active');
insert into endpoint_outcomes__reftable(id, iri) values (1, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inactive');
insert into endpoint_outcomes__reftable(id, iri) values (2, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#inconclusive');
insert into endpoint_outcomes__reftable(id, iri) values (3, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#unspecified');
insert into endpoint_outcomes__reftable(id, iri) values (4, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#probe');
