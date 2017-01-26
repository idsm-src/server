create table compound_sdfiles_gz
(
    compound    integer not null,
    sdf_gz      long varbinary not null,
    primary key(compound)
);


create table compound_relations__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


create table compound_relations
(
    compound_from    integer  not null,
    relation         smallint not null,
    compound_to      integer  not null,
    primary key(compound_from, relation, compound_to)
);


create table compound_roles__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


create table compound_roles
(
    compound    integer not null,
    roleid      smallint not null,
    primary key(compound, roleid)
);


create table compound_biosystems
(
    compound     integer not null,
    biosystem    integer not null,
    primary key(compound, biosystem)
);


create table compound_type_units__reftable
(
    id     smallint not null,
    iri    varchar not null,
    primary key(id),
    unique(iri)
);


create table compound_types
(
    compound    integer not null,
    unit        smallint not null,
    type        integer not null,
    primary key(compound, unit, type)
);


create table compound_active_ingredients
(
    compound      integer not null,
    unit          smallint not null,
    ingredient    integer not null,
    primary key(compound, unit, ingredient)
);


create table compound_bases
(
    id    integer not null,
    primary key(id)
);

--============================================================================--

insert into compound_relations__reftable(id, iri) values (455, 'http://semanticscience.org/resource/CHEMINF_000455');
insert into compound_relations__reftable(id, iri) values (461, 'http://semanticscience.org/resource/CHEMINF_000461');
insert into compound_relations__reftable(id, iri) values (462, 'http://semanticscience.org/resource/CHEMINF_000462');
insert into compound_relations__reftable(id, iri) values (480, 'http://semanticscience.org/resource/CHEMINF_000480');
insert into compound_relations__reftable(id, iri) values (1024, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#has_parent');

insert into compound_roles__reftable(id, iri) values (0, 'http://rdf.ncbi.nlm.nih.gov/pubchem/vocabulary#FDAApprovedDrugs');

insert into compound_type_units__reftable(id, iri) values (0, 'http://purl.obolibrary.org/obo/CHEBI_');
insert into compound_type_units__reftable(id, iri) values (1, 'http://purl.bioontology.org/ontology/SNOMEDCT/');
insert into compound_type_units__reftable(id, iri) values (2, 'http://purl.bioontology.org/ontology/NDFRT/N');
insert into compound_type_units__reftable(id, iri) values (3, 'http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C');
insert into compound_type_units__reftable(id, iri) values (4, 'http://www.biopax.org/release/biopax-level3.owl#SmallMolecule');
