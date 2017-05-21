create table graphs__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);

--============================================================================--

insert into graphs__reftable(id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/bioassay');
insert into graphs__reftable(id, iri) values (1, 'http://rdf.ncbi.nlm.nih.gov/pubchem/biosystem');
insert into graphs__reftable(id, iri) values (2, 'http://rdf.ncbi.nlm.nih.gov/pubchem/compound');
insert into graphs__reftable(id, iri) values (3, 'http://rdf.ncbi.nlm.nih.gov/pubchem/concept');
insert into graphs__reftable(id, iri) values (4, 'http://rdf.ncbi.nlm.nih.gov/pubchem/conserveddomain');
insert into graphs__reftable(id, iri) values (5, 'http://rdf.ncbi.nlm.nih.gov/pubchem/endpoint');
insert into graphs__reftable(id, iri) values (6, 'http://rdf.ncbi.nlm.nih.gov/pubchem/gene');
insert into graphs__reftable(id, iri) values (7, 'http://rdf.ncbi.nlm.nih.gov/pubchem/inchikey');
insert into graphs__reftable(id, iri) values (8, 'http://rdf.ncbi.nlm.nih.gov/pubchem/measuregroup');
insert into graphs__reftable(id, iri) values (9, 'http://rdf.ncbi.nlm.nih.gov/pubchem/ontology');
insert into graphs__reftable(id, iri) values (10, 'http://rdf.ncbi.nlm.nih.gov/pubchem/protein');
insert into graphs__reftable(id, iri) values (11, 'http://rdf.ncbi.nlm.nih.gov/pubchem/reference');
insert into graphs__reftable(id, iri) values (12, 'http://rdf.ncbi.nlm.nih.gov/pubchem/source');
insert into graphs__reftable(id, iri) values (13, 'http://rdf.ncbi.nlm.nih.gov/pubchem/substance');
insert into graphs__reftable(id, iri) values (14, 'http://rdf.ncbi.nlm.nih.gov/pubchem/synonym');
insert into graphs__reftable(id, iri) values (15, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/compound');
insert into graphs__reftable(id, iri) values (16, 'http://rdf.ncbi.nlm.nih.gov/pubchem/descriptor/substance');
